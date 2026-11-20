package org.rutebanken.sobek.service.fleet;

import jakarta.validation.ValidationException;
import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.organisation.OrganisationRegistry;
import org.rutebanken.sobek.versioning.save.FleetVersionedSaverService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Shares a fleet with everyone or with organisations, and stops sharing it. The users of the organisations the fleet is
 * shared with see the fleet, and its vehicles and vehicle types, but cannot change them. Only those who can edit the fleet
 * (checked when the new version is saved) can change who it is shared with.
 * <p>
 * Both are about the end result, as {@link FleetMembershipService}: what is shared with is shared with afterwards whether it
 * was before, and what is unshared is not shared afterwards whether it was before. Each call makes one new version of
 * the fleet, which is otherwise unchanged.
 */
@Service
@Transactional
public class FleetSharingService {

    private final FleetMembershipService fleetMembershipService;
    private final FleetVersionedSaverService fleetVersionedSaverService;
    private final OrganisationRegistry organisationRegistry;

    public FleetSharingService(FleetMembershipService fleetMembershipService, FleetVersionedSaverService fleetVersionedSaverService,
                               OrganisationRegistry organisationRegistry) {
        this.fleetMembershipService = fleetMembershipService;
        this.fleetVersionedSaverService = fleetVersionedSaverService;
        this.organisationRegistry = organisationRegistry;
    }

    /**
     * @param withEveryone share with everyone, until it is unshared from everyone
     * @param organisationRefs organisations to share with, as in the organisation registry. They must exist, and cannot be the owner.
     */
    public Fleet share(String fleetNetexId, boolean withEveryone, Collection<String> organisationRefs) {
        Set<String> organisations = organisationsOf(organisationRefs);
        requireSomethingToChange(withEveryone, organisations);
        Fleet existing = fleetMembershipService.findFleet(fleetNetexId);
        for (String organisation : organisations) {
            if (organisation.equals(existing.getDataOwnerRef())) {
                throw new ValidationException("The fleet " + fleetNetexId + " is owned by " + organisation + ", and cannot be shared with its owner.");
            }
            organisationRegistry.validateOrganisationRef(organisation);
        }

        Fleet newVersion = fleetMembershipService.newVersionOf(existing);
        newVersion.setSharingChanged(true);
        if (withEveryone) {
            newVersion.setSharedWithAll(true);
        }
        newVersion.getSharedWith().addAll(organisations);
        return fleetVersionedSaverService.saveNewVersion(existing, newVersion, Instant.now());
    }

    /**
     * @param fromEveryone stop sharing with everyone. A fleet that is shared with organisations by name still is.
     * @param organisationRefs organisations to stop sharing with. Ones the fleet is not shared with are left out.
     */
    public Fleet unshare(String fleetNetexId, boolean fromEveryone, Collection<String> organisationRefs) {
        Set<String> organisations = organisationsOf(organisationRefs);
        requireSomethingToChange(fromEveryone, organisations);
        Fleet existing = fleetMembershipService.findFleet(fleetNetexId);

        Fleet newVersion = fleetMembershipService.newVersionOf(existing);
        newVersion.setSharingChanged(true);
        if (fromEveryone) {
            newVersion.setSharedWithAll(false);
        }
        newVersion.getSharedWith().removeAll(organisations);
        return fleetVersionedSaverService.saveNewVersion(existing, newVersion, Instant.now());
    }

    private static Set<String> organisationsOf(Collection<String> organisationRefs) {
        Set<String> organisations = new LinkedHashSet<>();
        if (organisationRefs != null) {
            for (String organisationRef : organisationRefs) {
                if (organisationRef == null || organisationRef.isBlank()) {
                    throw new ValidationException("An organisation to share with cannot be empty.");
                }
                organisations.add(organisationRef.trim());
            }
        }
        return organisations;
    }

    private static void requireSomethingToChange(boolean everyone, Set<String> organisations) {
        if (!everyone && organisations.isEmpty()) {
            throw new ValidationException("Give at least one organisation, or everyone.");
        }
    }
}
