package org.rutebanken.sobek.permission;

import java.util.List;
import org.rutebanken.helper.organisation.RoleAssignment;

/**
 * Client interface for communicating with the new permission store.
 * Implementations will use the JFrog dependency to fetch user permissions.
 */
public interface PermissionStoreClient {
    /**
     * Fetch role assignments for a given user from the permission store.
     *
     * @param username the username to fetch permissions for
     * @return list of role assignments for the user
     */
    List<RoleAssignment> getRoleAssignments(String username);

    /**
     * Check if the permission store is available.
     *
     * @return true if the permission store can be reached
     */
    boolean isAvailable();
}
