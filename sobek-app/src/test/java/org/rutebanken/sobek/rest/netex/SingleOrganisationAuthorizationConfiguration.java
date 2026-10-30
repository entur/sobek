package org.rutebanken.sobek.rest.netex;

import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.model.EntityStructure;
import org.rutebanken.sobek.model.authorization.OwnedEntity;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collection;
import java.util.List;

/**
 * Runs the application as a non-admin user who is authorized for exactly one organisation.
 */
@TestConfiguration
public class SingleOrganisationAuthorizationConfiguration {

    /** Must be the data owner of the organisation that the test fixture imports. */
    public static final String DATA_OWNER = "AKT";

    @Bean
    @Primary
    public AuthorizationService singleOrganisationAuthorizationService() {
        return new SingleOrganisationAuthorizationService(DATA_OWNER);
    }

    static class SingleOrganisationAuthorizationService implements AuthorizationService {

        private final String organisation;

        SingleOrganisationAuthorizationService(String organisation) {
            this.organisation = organisation;
        }

        @Override
        public boolean canEditAllEntities() {
            return false;
        }

        @Override
        public void verifyCanEditEntities(Collection<? extends EntityStructure> entities) {
            for (EntityStructure entity : entities) {
                if (entity instanceof OwnedEntity owned && !organisation.equals(owned.getDataOwnerRef())) {
                    throw new AccessDeniedException(
                        "Not authorized to edit data owned by '" + owned.getDataOwnerRef() + "'");
                }
            }
        }

        @Override
        public boolean isGuest() {
            return false;
        }

        @Override
        public boolean isAdmin() {
            return false;
        }

        @Override
        public List<String> getOrganisationRefsUserIsAuthorizedFor() {
            return List.of(organisation);
        }

        @Override
        public void verifyCanViewDataOwner(String dataOwnerRef) {
            if (dataOwnerRef == null) {
                throw new AccessDeniedException("Not authorized to access data across multiple organisations");
            }
            if (!organisation.equals(dataOwnerRef)) {
                throw new AccessDeniedException("Not authorized to access data owned by '" + dataOwnerRef + "'");
            }
        }
    }
}