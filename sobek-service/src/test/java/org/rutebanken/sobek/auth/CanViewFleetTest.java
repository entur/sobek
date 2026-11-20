package org.rutebanken.sobek.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.EntityStructure;
import org.rutebanken.sobek.model.vehicle.Fleet;

/**
 * Who may see a fleet that is reached from a vehicle or a vehicle type: the owner, those it is shared with, and everyone if it is
 * shared with everyone.
 */
class CanViewFleetTest {

    @Test
    @DisplayName("The organisations of the user see the fleets they own")
    void ownFleet() {
        assertTrue(userOf("AKT").canViewFleet(fleet("AKT")));
        assertFalse(userOf("RUT").canViewFleet(fleet("AKT")));
    }

    @Test
    @DisplayName("A fleet that is shared with an organisation of the user is seen, one that is shared with others is not")
    void sharedWithOrganisation() {
        Fleet shared = fleet("AKT");
        shared.setSharedWith(Set.of("RUT"));

        assertTrue(userOf("RUT").canViewFleet(shared));
        assertTrue(userOf("NSB", "RUT").canViewFleet(shared));
        assertFalse(userOf("NSB").canViewFleet(shared));
    }

    @Test
    @DisplayName("A fleet that is shared with everyone is seen by a user of any organisation, but not by a user of none")
    void sharedWithEveryone() {
        Fleet shared = fleet("AKT");
        shared.setSharedWithAll(true);

        assertTrue(userOf("NSB").canViewFleet(shared));
        assertFalse(userOf().canViewFleet(shared));
    }

    @Test
    @DisplayName("Everything is seen when authorization is disabled, and by an administrator")
    void everythingIsSeenWithoutAuthorizationAndByAdmins() {
        assertTrue(new User(null, false).canViewFleet(fleet("AKT")));
        assertTrue(new User(List.of(), true).canViewFleet(fleet("AKT")));
    }

    private static Fleet fleet(String dataOwner) {
        Fleet fleet = new Fleet();
        fleet.setDataOwnerRef(dataOwner);
        return fleet;
    }

    private static User userOf(String... organisations) {
        return new User(List.of(organisations), false);
    }

    /** A user with organisations (null when authorization is disabled), or an administrator. */
    private record User(List<String> organisations, boolean admin) implements AuthorizationService {
        @Override
        public boolean canEditAllEntities() {
            return admin;
        }

        @Override
        public void verifyCanEditEntities(Collection<? extends EntityStructure> entities) {
        }

        @Override
        public boolean isGuest() {
            return organisations != null && organisations.isEmpty();
        }

        @Override
        public boolean isAdmin() {
            return admin;
        }

        @Override
        public List<String> getOrganisationRefsUserIsAuthorizedFor() {
            return organisations;
        }

        @Override
        public void verifyCanViewDataOwner(String dataOwnerRef) {
        }
    }
}
