package org.rutebanken.sobek.repository;

import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.Operator;
import org.rutebanken.netex.model.OrganisationTypeEnumeration;
import org.rutebanken.netex.model.Organisation_VersionStructure;
import org.rutebanken.sobek.netex.mapping.mapstruct.MultilingualStringMapper;
import org.rutebanken.sobek.organisation.Organisation;
import org.rutebanken.sobek.organisation.OrganisationRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrganisationRepositoryImpl implements OrganisationRepository {

    private final OrganisationRegistry organisationRegistry;
    private final MultilingualStringMapper multilingualStringMapper;

    public OrganisationRepositoryImpl(OrganisationRegistry organisationRegistry, MultilingualStringMapper multilingualStringMapper) {
        this.organisationRegistry = organisationRegistry;
        this.multilingualStringMapper = multilingualStringMapper;
    }

    @Override
    public Page<Organisation> findCurrentFiltered(List<String> ids, OrganisationTypeEnumeration organisationType, Pageable pageable) {
        List<? extends Organisation_VersionStructure> organisations;

        if(organisationType == null) {
            organisations = organisationRegistry.getOrganisations();
        } else if(organisationType == OrganisationTypeEnumeration.AUTHORITY) {
            organisations = organisationRegistry.getAuthorities();
        } else if (organisationType == OrganisationTypeEnumeration.OPERATOR) {
            organisations = organisationRegistry.getOperators();
        } else {
            organisations = organisationRegistry.getOrganisations();
        }

        if(ids != null && !ids.isEmpty()) {
            organisations = organisations
                    .stream()
                    .filter(org -> ids.stream().anyMatch(id -> org.getId().equals(id)))
                    .toList();
        }

        // Convert to Organisation records
        List<Organisation> organisationList = organisations.stream()
                .map(org -> new Organisation(
                        org.getId(),
                        multilingualStringMapper.mapToSobek(org.getName()),
                        getOrganisationType(org)
                ))
                .toList();

        // Handle pagination
        if (pageable.isUnpaged()) {
            return new PageImpl<>(organisationList);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), organisationList.size());
        
        if (start >= organisationList.size()) {
            return new PageImpl<>(List.of(), pageable, organisationList.size());
        }

        List<Organisation> pagedList = organisationList.subList(start, end);
        return new PageImpl<>(pagedList, pageable, organisationList.size());
    }

    private OrganisationTypeEnumeration getOrganisationType(Organisation_VersionStructure org) {
        if (org instanceof Authority) {
            return OrganisationTypeEnumeration.AUTHORITY;
        } else if (org instanceof Operator) {
            return OrganisationTypeEnumeration.OPERATOR;
        } else {
            return OrganisationTypeEnumeration.OTHER;
        }
    }
}
