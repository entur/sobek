package org.rutebanken.sobek.auth;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;

import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultAuthorizationServiceTest {
    @Mock
    private RoleAssignmentExtractor roleAssignmentExtractor;

    @Test
    void canEditAllEntities() {
        List<RoleAssignment> roleAssignments = RoleAssignmentListBuilder.builder().withAccessAllAreas().build();
        DefaultAuthorizationService defaultAuthorizationService = new DefaultAuthorizationService(null, true, null);
        assertTrue(defaultAuthorizationService.verifyCanEditAllEntities(roleAssignments));
    }

    @Test
    void canEditAllEntitiesMissingRoleAssignment() {
        List<RoleAssignment> roleAssignments = RoleAssignmentListBuilder.builder().build();
        DefaultAuthorizationService defaultAuthorizationService = new DefaultAuthorizationService(null, true, null);
        assertFalse(defaultAuthorizationService.verifyCanEditAllEntities(roleAssignments));
    }

    @Test
    void verifyCanViewDataOwner_nullDataOwner_admin_doesNothing() {
        SecurityContextHolder.setContext(new SecurityContextImpl(mock(JwtAuthenticationToken.class)));
        List<RoleAssignment> adminRoles = RoleAssignmentListBuilder.builder().withAccessAllAreas().build();
        when(roleAssignmentExtractor.getRoleAssignmentsForUser()).thenReturn(adminRoles);

        DefaultAuthorizationService defaultAuthorizationService = new DefaultAuthorizationService(null, true, roleAssignmentExtractor);

        assertThatCode(() -> defaultAuthorizationService.verifyCanViewDataOwner(null))
            .doesNotThrowAnyException();
        SecurityContextHolder.clearContext();
    }

    @Test
    void verifyCanViewDataOwner_nullDataOwner_notAdmin_throwsAccessDenied() {
        SecurityContextHolder.setContext(new SecurityContextImpl(mock(JwtAuthenticationToken.class)));
        List<RoleAssignment> noRoles = RoleAssignmentListBuilder.builder().build();

        when(roleAssignmentExtractor.getRoleAssignmentsForUser()).thenReturn(noRoles);

        DefaultAuthorizationService defaultAuthorizationService = new DefaultAuthorizationService(null, true, roleAssignmentExtractor);

        assertThatThrownBy(() -> defaultAuthorizationService.verifyCanViewDataOwner(null))
            .isInstanceOf(AccessDeniedException.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void verifyCanViewDataOwner_nullDataOwner_notAuthenticated_throwsAccessDenied() {
        // No JWT in the security context: the user cannot be an admin
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> new DefaultAuthorizationService(null, true, null).verifyCanViewDataOwner(null))
            .isInstanceOf(AccessDeniedException.class);
    }
}