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

package org.rutebanken.sobek.organisation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Strings;
import jakarta.annotation.PostConstruct;
import org.rutebanken.sobek.error.CodedError;
import org.rutebanken.sobek.error.CodedIllegalArgumentException;
import org.rutebanken.sobek.error.ErrorCodeEnumeration;
import org.rutebanken.sobek.netex.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Abstract base class for organisation registries with caching and filtering capabilities.
 * Subclasses must implement the loadOrganisationsFromSource() method to provide the source-specific loading logic.
 */
public abstract class AbstractCachedOrganisationRegistry implements OrganisationRegistry {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCachedOrganisationRegistry.class);
    private static final double REFRESH_THRESHOLD = 0.9; // Start refresh at 90% of cache duration

    private final Duration CACHE_DURATION;
    private volatile List<Organisation> organisations = List.of();
    private volatile Instant lastLoadTime;
    private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);

    protected AbstractCachedOrganisationRegistry(
        @Value("${sobek.organisations.cache-duration-seconds:3600}") String cacheDurationSeconds
    ) {
        long cacheDuration;
        try {
            cacheDuration = Long.parseLong(cacheDurationSeconds);
        } catch (NumberFormatException e) {
            cacheDuration = 3600L;
            logger.warn(
                "Invalid value for sobek.organisations.cache-duration-seconds: '{}'. Falling back to default {} seconds.",
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

    /**
     * Ensures that the cached data is fresh and triggers reload if necessary.
     */
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

    /**
     * Loads organisations from the source and updates the cache.
     */
    private void loadOrganisations() {
        try {
            List<Organisation> loadedOrganisations = loadOrganisationsFromSource();

            if (loadedOrganisations == null || loadedOrganisations.isEmpty()) {
                logger.info("No organisations loaded from source");
                this.organisations = List.of();
                lastLoadTime = Instant.now();
                return;
            }

            // Atomic replacement of the organisations list
            this.organisations = List.copyOf(loadedOrganisations);
            lastLoadTime = Instant.now();

            logger.info("Organisations loaded (total: {})", this.organisations.size());
        } catch (Exception e) {
            logger.warn("Unable to load organisations, organisation registry will remain unchanged", e);
        }
    }

    /**
     * Subclasses must implement this method to load organisations from their specific source.
     *
     * @return List of organisations loaded from the source
     * @throws Exception if loading fails
     */
    protected abstract List<Organisation> loadOrganisationsFromSource() throws Exception;

    // Generic filtering methods

    @Override
    public List<Organisation> getOrganisations() {
        ensureFreshData();
        return organisations;
    }

    @Override
    public Optional<Organisation> getOrganisation(String id) {
        ensureFreshData();
        return organisations.stream()
            .filter(org -> org.netexId().equals(id))
            .findFirst();
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
            organisations.stream().anyMatch(org -> organisationRef.equals(org.netexId())),
            CodedError.fromErrorCode(ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY),
            "Organisation with ref %s not found in organisation registry",
            organisationRef
        );
    }
}