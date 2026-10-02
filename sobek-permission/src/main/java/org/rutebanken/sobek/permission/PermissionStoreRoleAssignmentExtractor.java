package org.rutebanken.sobek.permission;

import java.util.List;
import java.util.Set;

import org.entur.auth.permission.client.AuthorizeTenant;
import org.entur.auth.permission.client.model.Access;
import org.entur.auth.permission.client.model.Permission;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Implementation of RoleAssignmentExtractor that fetches user permissions
 * from the new permission store.
 */
public class PermissionStoreRoleAssignmentExtractor
    implements RoleAssignmentExtractor {

    private static final Logger logger = LoggerFactory.getLogger(
        PermissionStoreRoleAssignmentExtractor.class
    );

    private final AuthorizeTenant authorizeTenant;
    private final PermissionStoreUserInfoExtractor permissionStoreUserInfoExtractor;

    public PermissionStoreRoleAssignmentExtractor(
        AuthorizeTenant authorizeTenant, PermissionStoreUserInfoExtractor permissionStoreUserInfoExtractor
    ) {
        this.authorizeTenant = authorizeTenant;
        this.permissionStoreUserInfoExtractor = permissionStoreUserInfoExtractor;
    }

    @Override
    public List<RoleAssignment> getRoleAssignmentsForUser() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        return getRoleAssignmentsForUser(authentication);
    }

    @Override
    public List<RoleAssignment> getRoleAssignmentsForUser(
        Authentication authentication
    ) {
        if (!(authentication instanceof JwtAuthenticationToken)) {
            logger.debug(
                "No JWT authentication found, returning empty role assignments"
            );
            return List.of();
        }
        String username = permissionStoreUserInfoExtractor.getPreferredUsername(authentication);

        logger.debug("Fetching role assignments for user: {}", username);

        try {
            Set<Permission> permissions = authorizeTenant.getPermissions(authentication);
            var rs = authorizeTenant.getResponsibilitySet(authentication, "vehicle-management.onBehalfOf", Access.LES);
            if(permissions == null) {
                return List.of();
            }
            return permissions.stream().map(permission -> RoleAssignment.builder().withOrganisation("dummy").withRole(permission.getOperation()).build()).toList();
        } catch (Exception e) {
            logger.error(
                "Failed to fetch role assignments for user: {}",
                username,
                e
            );
            return List.of();
        }
    }
}
