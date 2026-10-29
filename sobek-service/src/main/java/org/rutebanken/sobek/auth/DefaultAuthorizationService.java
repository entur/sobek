package org.rutebanken.sobek.auth;

import org.rutebanken.helper.organisation.DataScopedAuthorizationService;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.rutebanken.sobek.model.EntityStructure;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collection;
import java.util.List;

import static org.rutebanken.sobek.auth.AuthorizationConstants.*;

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
                .anyMatch(roleAssignment -> ROLE_ADMIN_ALL_VEHICLE_DATA.equals(roleAssignment.getRole())
                );
    }

    @Override
    public void verifyCanEditEntities(Collection<? extends EntityStructure> entities) {
        // If the user is authorized to edit all entities, no need to check further.
        if( !canEditAllEntities()) {
            dataScopedAuthorizationService.assertAuthorized(ROLE_EDIT_VEHICLE_DATA, entities);
        }
    }

    @Override
    public boolean isGuest() {
        if (hasNoAuthentications()) {
            return true;
        }
        return roleAssignmentExtractor.getRoleAssignmentsForUser().isEmpty();
    }

    @Override
    public boolean isAdmin() {
        if (hasNoAuthentications()) {
            return false;
        }
        return roleAssignmentExtractor.getRoleAssignmentsForUser().stream()
                .anyMatch(roleAssignment -> ROLE_ADMIN_ALL_VEHICLE_DATA.equals(roleAssignment.getRole()));
    }

    private boolean hasNoAuthentications() {
        if(!authorizationEnabled) {
            return true;
        }
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return !(auth instanceof JwtAuthenticationToken);
    }

    @Override
    public List<String> getOrganisationRefsUserIsAuthorizedFor () {
        if (!authorizationEnabled) {
            return null;
        }

        List<RoleAssignment> roleAssignments = roleAssignmentExtractor.getRoleAssignmentsForUser();

        return roleAssignments.stream()
                .filter(ra -> ROLE_EDIT_VEHICLE_DATA.equals(ra.getRole()))
                .map(RoleAssignment::getOrganisation)
                .distinct()
                .toList();
    }

    @Override
    public void verifyCanViewDataOwner(String dataOwnerRef) {
        // Admins can access all data
        if(isAdmin()) {
            return;
        }

        List<String> authorizedRefs = getOrganisationRefsUserIsAuthorizedFor();
        // null means authorization is disabled -> all organisations allowed
        if (authorizedRefs != null && !authorizedRefs.contains(dataOwnerRef)) {
            throw new AccessDeniedException("Not authorized to access data owned by '" + dataOwnerRef + "'");
        }
    }

}
