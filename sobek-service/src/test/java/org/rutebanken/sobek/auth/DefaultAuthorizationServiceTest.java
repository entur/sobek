package org.rutebanken.sobek.auth;

import org.junit.jupiter.api.Test;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class DefaultAuthorizationServiceTest {

    @Test
    void canEditAllEntities() {
        List<RoleAssignment> roleAssignments = RoleAssignmentListBuilder.builder().withAccessAllAreas().build();
        DefaultAuthorizationService defaultAuthorizationService = new DefaultAuthorizationService(null,false, null);
        assertTrue(defaultAuthorizationService.verifyCanEditAllEntities(roleAssignments));
    }

    @Test
    void canEditAllEntitiesMissingRoleAssignment() {
        List<RoleAssignment> roleAssignments = RoleAssignmentListBuilder.builder().build();
        DefaultAuthorizationService defaultAuthorizationService = new DefaultAuthorizationService(null,false, null);
        assertFalse(defaultAuthorizationService.verifyCanEditAllEntities(roleAssignments));

    }
}