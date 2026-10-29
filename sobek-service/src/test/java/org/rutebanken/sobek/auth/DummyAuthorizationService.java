package org.rutebanken.sobek.auth;

import org.rutebanken.sobek.model.EntityStructure;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class DummyAuthorizationService implements AuthorizationService {
    @Override
    public boolean canEditAllEntities() {
        return true;
    }

    @Override
    public void verifyCanEditEntities(Collection<? extends EntityStructure> entities) {

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
    public List<String> getOrganisationRefsUserIsAuthorizedFor() { return null; }

    @Override
    public void verifyCanViewDataOwner(String dataOwnerRef) {

    }

}
