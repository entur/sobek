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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ENTITY_CLASSIFIER_ALL_ATTRIBUTES;
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
                .anyMatch(roleAssignment -> ROLE_EDIT_VEHICLE_DATA.equals(roleAssignment.getRole())
                                             && roleAssignment.getEntityClassifications() != null
                                             && roleAssignment.getEntityClassifications().get(AuthorizationConstants.ENTITY_TYPE) != null
                                             && roleAssignment.getEntityClassifications().get(AuthorizationConstants.ENTITY_TYPE).contains(ENTITY_CLASSIFIER_ALL_ATTRIBUTES)
                                             && StringUtils.isEmpty(roleAssignment.getAdministrativeZone())
                );
    }

    @Override
    public boolean canEditEntities(Collection<? extends EntityStructure> entities) {
        return dataScopedAuthorizationService.isAuthorized(ROLE_EDIT_VEHICLE_DATA, entities);
    }


    @Override
    public void verifyCanEditEntities(Collection<? extends EntityStructure> entities) {
        dataScopedAuthorizationService.assertAuthorized(ROLE_EDIT_VEHICLE_DATA, entities);
    }

    @Override
    public void verifyCanDeleteEntities(Collection<? extends EntityStructure> entities) {
        dataScopedAuthorizationService.assertAuthorized(ROLE_DELETE_VEHICLE_DATA, entities);
    }

    @Override
    public boolean canDeleteEntity(EntityStructure entity) {
        return canEditDeleteEntity(entity, ROLE_DELETE_VEHICLE_DATA);
    }

    @Override
    public boolean canEditEntity(EntityStructure entity) {
        return canEditDeleteEntity(entity, ROLE_EDIT_VEHICLE_DATA);
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

    @Override
    public List<String> getOrganisationRefsUserIsAuthorizedFor() {
        if(!authorizationEnabled) { return null; }

        List<RoleAssignment> roleAssignments = roleAssignmentExtractor.getRoleAssignmentsForUser();

        Set<String> organisationRefs = new HashSet<>();
        roleAssignments.forEach(roleAssignment -> {
            if(roleAssignment.getRole().equals(ROLE_EDIT_VEHICLE_DATA)
                    && roleAssignment.getEntityClassifications() != null) {  // Maybe return also "read" or "delete"? Review this later on, for now only "edit" is needed.
                List<String> dataOwnersAllowed = roleAssignment.getEntityClassifications().get(CLASSIFICATION_DATA_OWNER);
                if(dataOwnersAllowed != null) {
                    organisationRefs.addAll(dataOwnersAllowed.stream().map(dataOwner -> dataOwner.replace("/", ":")).toList());
                }
            }
        });
        return organisationRefs.stream().toList();
    }

}
