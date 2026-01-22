package org.rutebanken.sobek.auth;

import org.rutebanken.sobek.model.EntityStructure;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collection;

/**
 * Authorization service interface for managing authorization of users to perform operations
 * on entities.
 */
public interface AuthorizationService {

    /**
     * Checks if the current user has permission to edit any entity.
     *
     * @return {@code true} if the user can edit all entities, otherwise {@code false}.
     */
    boolean canEditAllEntities();

    /**
     * Checks if the current user has permission to edit all the given entities.
     *
     * @param entities The collection of entities to check.
     * @return {@code true} if the user can edit all entities, otherwise {@code false}.
     */
    boolean canEditEntities(Collection<? extends EntityStructure> entities);

    /*
     * Verify that the current user has edit rights on all the given entities.
     *
     * @param entities The collection of entities to verify.
     * @throws AccessDeniedException if the user lacks the necessary permissions.
     */
    void verifyCanEditEntities(Collection<? extends EntityStructure> entities);

    /**
     * Ensures that the current user has delete rights on all the given entities.
     *
     * @param entities The collection of entities to verify.
     * @throws AccessDeniedException if the user lacks the necessary permissions.
     */
    void verifyCanDeleteEntities(Collection<? extends EntityStructure> entities);

    /**
     * Checks if the current user has the right to delete the given entity.
     *
     * @param entity The entity to check.
     * @return {@code true} if the user can delete the entity, otherwise {@code false}.
     */
    boolean canDeleteEntity(EntityStructure entity);

    /**
     * Checks if the current user has the right to edit the given entity.
     *
     * @param entity The entity to check.
     * @return {@code true} if the user can edit the entity, otherwise {@code false}.
     */
    boolean canEditEntity(EntityStructure entity);

     /**
      * Checks if the current user is accessing the system as a guest.
      *
      * @return {@code true} if the user is a guest, otherwise {@code false}.
      */
    boolean isGuest();
}
