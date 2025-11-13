package org.rutebanken.sobek.auth;

import org.apache.commons.lang3.StringUtils;
import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.DataScopedAuthorizationService;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.rutebanken.sobek.model.EntityStructure;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ENTITY_CLASSIFIER_ALL_ATTRIBUTES;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_DELETE_STOPS;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_EDIT_STOPS;

public class DefaultAuthorizationService implements AuthorizationService {
    private final DataScopedAuthorizationService dataScopedAuthorizationService;
    private final boolean authorizationEnabled;
    private final RoleAssignmentExtractor roleAssignmentExtractor;

    public DefaultAuthorizationService(DataScopedAuthorizationService dataScopedAuthorizationService,
                                       boolean authorizationEnabled,
                                       RoleAssignmentExtractor roleAssignmentExtractor) {
        this.dataScopedAuthorizationService = dataScopedAuthorizationService;
        this.authorizationEnabled = authorizationEnabled;
        this.roleAssignmentExtractor = roleAssignmentExtractor;
    }

    @Override
    public boolean canEditAllEntities() {
        if(hasNoAuthentications()) {
            return false;
        }
        return verifyCanEditAllEntities(roleAssignmentExtractor.getRoleAssignmentsForUser());
    }

    boolean verifyCanEditAllEntities(List<RoleAssignment> roleAssignments) {
        return roleAssignments
                .stream()
                .anyMatch(roleAssignment -> ROLE_EDIT_STOPS.equals(roleAssignment.getRole())
                                             && roleAssignment.getEntityClassifications() != null
                                             && roleAssignment.getEntityClassifications().get(AuthorizationConstants.ENTITY_TYPE) != null
                                             && roleAssignment.getEntityClassifications().get(AuthorizationConstants.ENTITY_TYPE).contains(ENTITY_CLASSIFIER_ALL_ATTRIBUTES)
                                             && StringUtils.isEmpty(roleAssignment.getAdministrativeZone())
                );
    }

    @Override
    public boolean canEditEntities(Collection<? extends EntityStructure> entities) {
        return dataScopedAuthorizationService.isAuthorized(ROLE_EDIT_STOPS, entities);
    }


    @Override
    public void verifyCanEditEntities(Collection<? extends EntityStructure> entities) {
        dataScopedAuthorizationService.assertAuthorized(ROLE_EDIT_STOPS, entities);
    }

    @Override
    public void verifyCanDeleteEntities(Collection<? extends EntityStructure> entities) {
        dataScopedAuthorizationService.assertAuthorized(ROLE_DELETE_STOPS, entities);

    }

    @Override
    public boolean canDeleteEntity(EntityStructure entity) {
        return canEditDeleteEntity(entity, ROLE_DELETE_STOPS);
    }

    @Override
    public boolean canEditEntity(EntityStructure entity) {
        return canEditDeleteEntity(entity, ROLE_EDIT_STOPS);
    }

    @Override
    public boolean isGuest() {
        if (hasNoAuthentications()) {
            return true;
        }
        return roleAssignmentExtractor.getRoleAssignmentsForUser().isEmpty();
    }

    private boolean hasNoAuthentications() {
        if(!authorizationEnabled) {
            return true;
        }
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return !(auth instanceof JwtAuthenticationToken);
    }

    private boolean canEditDeleteEntity(EntityStructure entity, String role) {
        if (hasNoAuthentications()) {
            return false;
        }

        return dataScopedAuthorizationService.isAuthorized(role, List.of(entity));
    }
}
