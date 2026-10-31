package org.rutebanken.sobek.auth;

import org.rutebanken.sobek.model.EntityStructure;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collection;
import java.util.List;

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

    /*
     * Verify that the current user has edit rights on all the given entities.
     *
     * @param entities The collection of entities to verify.
     * @throws AccessDeniedException if the user lacks the necessary permissions.
     */
    void verifyCanEditEntities(Collection<? extends EntityStructure> entities);

     /**
      * Checks if the current user is accessing the system as a guest.
      *
      * @return {@code true} if the user is a guest, otherwise {@code false}.
      */
    boolean isGuest();

    /**
     * Checks if the current user is accessing the system as administrator.
     *
     * @return {@code true} if the user is administrator, otherwise {@code false}.
     */
    boolean isAdmin();

    /**
     * Get the organisation refs the user is authorized for. If authorization is disabled, return null, which means "all organisations".
     * @return A list of organisation refs, e.g. "AKT"
     */
    List<String> getOrganisationRefsUserIsAuthorizedFor();

    /**
     * Verify that the current user may access data owned by the given organisation.
     *
     * @param dataOwnerRef organisation ref, e.g. "AKT"
     * @throws AccessDeniedException if the user is not authorized for the organisation.
     */
    void verifyCanViewDataOwner(String dataOwnerRef);}
