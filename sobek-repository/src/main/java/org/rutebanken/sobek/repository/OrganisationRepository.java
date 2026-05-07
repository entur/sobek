package org.rutebanken.sobek.repository;

import org.rutebanken.netex.model.OrganisationTypeEnumeration;
import org.rutebanken.sobek.organisation.Organisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrganisationRepository {
    Page<Organisation> findCurrentFiltered(List<String> ids, OrganisationTypeEnumeration organisationType, List<String> authorizedIds, Pageable pageable);
}

