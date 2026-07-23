/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

/* Copied from UTTU and refined for Sobek needs */

package org.rutebanken.sobek.organisation;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.transform.Source;

import com.google.common.base.Strings;
import jakarta.annotation.PostConstruct;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.error.CodedError;
import org.rutebanken.sobek.error.CodedIllegalArgumentException;
import org.rutebanken.sobek.error.ErrorCodeEnumeration;
import org.rutebanken.sobek.netex.marshal.NetexUnmarshaller;
import org.rutebanken.sobek.netex.marshal.NetexUnmarshallerUnmarshalFromSourceException;
import org.rutebanken.sobek.netex.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public abstract class NetexPublicationDeliveryOrganisationRegistry
        implements OrganisationRegistry {

    private final Duration CACHE_DURATION;
    private static final double REFRESH_THRESHOLD = 0.9; // Start refresh at 90% of cache duration


    public NetexPublicationDeliveryOrganisationRegistry(
            @Value("${netex.organisations.cache-duration-seconds:3600}") String cacheDurationSeconds
    ) {
        long cacheDuration;
        try {
            cacheDuration = Long.parseLong(cacheDurationSeconds);
        } catch (NumberFormatException e) {
            cacheDuration = 3600L;
            logger.warn(
                    "Invalid value for netex.organisations.cache-duration-seconds: '{}'. Falling back to default {} seconds.",
                    cacheDurationSeconds,
                    cacheDuration,
                    e
            );
        }
        this.CACHE_DURATION = Duration.ofSeconds(cacheDuration);
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing organisation registry on application startup");
        try {
            loadOrganisations();
        } catch (Exception e) {
            logger.warn("Failed to initialize organisation registry on startup; will retry on demand", e);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(
            NetexPublicationDeliveryOrganisationRegistry.class
    );
    private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
            PublicationDeliveryStructure.class
    );

    private volatile List<Organisation_VersionStructure> organisations = List.of();

    private volatile Instant lastLoadTime;
    private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);

    private void ensureFreshData() {
        if (lastLoadTime == null || Instant.now().isAfter(lastLoadTime.plus(CACHE_DURATION))) {
            synchronized (this) {
                if (lastLoadTime == null || Instant.now().isAfter(lastLoadTime.plus(CACHE_DURATION))) {
                    loadOrganisations();
                }
            }
        } else if (shouldRefreshProactively()) {
            // Grace period - refresh asynchronously in background
            logger.debug("Entering grace period, triggering background refresh");
            refreshInBackground();
        }
    }

    private boolean shouldRefreshProactively() {
        long cacheAgeSeconds = Duration.between(lastLoadTime, Instant.now()).getSeconds();
        long thresholdSeconds = (long) (CACHE_DURATION.getSeconds() * REFRESH_THRESHOLD);
        return cacheAgeSeconds >= thresholdSeconds;
    }

    private void refreshInBackground() {
        // Use CompletableFuture to avoid blocking
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            synchronized (this) {
                // Only schedule one background refresh at a time
                if (!refreshInProgress.compareAndSet(false, true)) {
                    logger.debug("Background refresh already in progress, skipping");
                    return;
                }
                try {
                    if (shouldRefreshProactively() || Instant.now().isAfter(lastLoadTime.plus(CACHE_DURATION))) {
                        logger.info("Background refresh: reloading organisations");
                        loadOrganisations();
                    }
                } finally {
                    refreshInProgress.set(false);
                }
            }
        }).exceptionally(ex -> {
            logger.error("Error during background refresh of organisations", ex);
            return null;
        });
    }

    private void loadOrganisations() {
        try {
            List<Organisation_VersionStructure> loadedOrganisations = new ArrayList<>();
            Source orgSource = getPublicationDeliverySource();
            if(orgSource == null) {
                logger.info("No data received when loading organisations");
                return;
            }
            PublicationDeliveryStructure publicationDeliveryStructure =
                    netexUnmarshaller.unmarshalFromSource(orgSource);
            publicationDeliveryStructure
                    .getDataObjects()
                    .getCompositeFrameOrCommonFrame()
                    .forEach(frame -> {
                        var frameValue = frame.getValue();
                        if (frameValue instanceof ResourceFrame resourceFrame) {
                            resourceFrame
                                    .getOrganisations()
                                    .getOrganisation_Dummy()
                                    .forEach(org -> {
                                        if (Organisation_VersionStructure.class.isAssignableFrom(org.getDeclaredType())) {
                                            loadedOrganisations.add((Organisation_VersionStructure)org.getValue());
                                        } else {
                                            throw new UnsupportedOrganisationTypeException(org.getDeclaredType());
                                        }
                                    });
                        }
                    });
            
            // Atomic replacement of the organisations list
            this.organisations = List.copyOf(loadedOrganisations);
            lastLoadTime = Instant.now();
            
            logger.info("Organisations loaded from organisations xml (total: {})", this.organisations.size());
        } catch (NetexUnmarshallerUnmarshalFromSourceException e) {
            logger.warn(
                    "Unable to unmarshal organisations xml, organisation registry will be an empty list",
                    e
            );
        }
    }

    protected abstract Source getPublicationDeliverySource();

    @Override
    public List<Authority> getAuthorities() {
        ensureFreshData();
        return organisations.stream().filter(org -> org instanceof Authority).map(org -> (Authority) org).toList();
    }

    @Override
    public Optional<Authority> getAuthority(String id) {
        ensureFreshData();
        return organisations
                .stream()
                .filter(authority -> authority.getId().equals(id) && authority instanceof Authority)
                .map(org -> (Authority)org)
                .findFirst();
    }

    @Override
    public List<GeneralOrganisation> getGeneralOrganisations() {
        ensureFreshData();
        return organisations.stream().filter(org -> org instanceof GeneralOrganisation).map(org -> (GeneralOrganisation) org).toList();
    }

    @Override
    public Optional<GeneralOrganisation> getGeneralOrganisation(String id) {
        ensureFreshData();
        return organisations
                .stream()
                .filter(org -> org.getId().equals(id) && org instanceof GeneralOrganisation)
                .map(org -> (GeneralOrganisation)org)
                .findFirst();
    }

    @Override
    public List<Organisation_VersionStructure> getOrganisations() {
        ensureFreshData();
        return organisations;
    }

    @Override
    public Optional<Organisation_VersionStructure> getOrganisation(String id) {
        ensureFreshData();
        return organisations
                .stream()
                .filter(org -> org.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Operator> getOperators() {
        ensureFreshData();
        return organisations.stream().filter(org -> org instanceof Operator).map(org -> (Operator) org).toList();
    }

    @Override
    public Optional<Operator> getOperator(String id) {
        ensureFreshData();
        return organisations.stream().filter(org -> org.getId().equals(id) && org instanceof Operator).map(org -> (Operator)org).findFirst();
    }

    /**
     * By default, all organisations in the registry are valid operators
     */
    @Override
    public void validateOperatorRef(String operatorRef) {
        ensureFreshData();
        Preconditions.checkArgument(
                organisations.stream().anyMatch(org -> org.getId().equals(operatorRef) && org instanceof Operator),
                CodedError.fromErrorCode(
                        ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY
                ),
                "Organisation with ref %s not found in organisation registry",
                operatorRef
        );
    }

    /**
     * By default, all organisations in the registry are valid authorities
     */
    @Override
    public void validateAuthorityRef(String authorityRef) {
        ensureFreshData();
        Preconditions.checkArgument(
                organisations.stream().anyMatch(org -> org.getId().equals(authorityRef) && org instanceof Authority),
                CodedError.fromErrorCode(
                        ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY
                ),
                "Organisation with ref %s not found in organisation registry",
                authorityRef
        );
    }

    /**
     * By default, all organisations in the registry are valid authorities
     */
    @Override
    public void validateGeneralOrganisationRef(String generalOrganisationRef) {
        ensureFreshData();
        Preconditions.checkArgument(
                organisations.stream().anyMatch(org -> org.getId().equals(generalOrganisationRef) && org instanceof GeneralOrganisation),
                CodedError.fromErrorCode(
                        ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY
                ),
                "Organisation with ref %s not found in organisation registry",
                generalOrganisationRef
            );
    }

    @Override
    public void validateOrganisationRef(String organisationRef) {
        if (Strings.isNullOrEmpty(organisationRef) || organisationRef.isBlank()) {
            throw new CodedIllegalArgumentException(
                    "Organisation ref is null or blank",
                    CodedError.fromErrorCode(ErrorCodeEnumeration.ORGANISATION_REF_NULL)
            );
        }
        ensureFreshData();
        Preconditions.checkArgument(
                organisations.stream().anyMatch(org -> organisationRef.equals(org.getId())),
                CodedError.fromErrorCode(
                        ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY
                ),
                "Organisation with ref %s not found in organisation registry",
                organisationRef
        );
    }

}
