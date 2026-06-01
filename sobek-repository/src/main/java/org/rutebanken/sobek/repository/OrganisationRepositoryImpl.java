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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class OrganisationRepositoryImpl implements OrganisationRepository {

    private final OrganisationRegistry organisationRegistry;
    private final MultilingualStringMapper multilingualStringMapper;

    public OrganisationRepositoryImpl(OrganisationRegistry organisationRegistry, MultilingualStringMapper multilingualStringMapper) {
        this.organisationRegistry = organisationRegistry;
        this.multilingualStringMapper = multilingualStringMapper;
    }

    @Override
    public Page<Organisation> findCurrentFiltered(List<String> netexIds, OrganisationTypeEnumeration organisationType, String name, Pageable pageable) {
        List<? extends Organisation_VersionStructure> organisations;

        if(organisationType == null) {
            organisations = organisationRegistry.getOrganisations();
        } else if(organisationType == OrganisationTypeEnumeration.AUTHORITY) {
            organisations = organisationRegistry.getAuthorities();
        } else if (organisationType == OrganisationTypeEnumeration.OPERATOR) {
            organisations = organisationRegistry.getOperators();
        } else {
            throw new IllegalArgumentException("Unsupported organisation type filter: " + organisationType);
        }

        if(netexIds != null && !netexIds.isEmpty()) {
            Set<String> idSet = new HashSet<>(netexIds);
            organisations = organisations
                    .stream()
                    .filter(org -> idSet.contains(org.getId()))
                    .toList();
        }

        var mappedOrganisations = mapOrganisations(organisations);

        if(name != null && !name.isEmpty()) {
            var lowerName = name.toLowerCase();
            mappedOrganisations = mappedOrganisations
                    .stream()
                    .filter(org -> org.name() != null
                            && org.name().getValue() != null
                            && org.name().getValue().toLowerCase().contains(lowerName))
                    .toList();
        }

        // Handle pagination
        if (pageable.isUnpaged()) {
            return new PageImpl<>(mappedOrganisations);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), mappedOrganisations.size());
        
        if (start >= mappedOrganisations.size()) {
            return new PageImpl<>(List.of(), pageable, mappedOrganisations.size());
        }

        List<Organisation> pagedList = mappedOrganisations.subList(start, end);
        return new PageImpl<>(pagedList, pageable, mappedOrganisations.size());
    }

    private List<Organisation> mapOrganisations(List<? extends Organisation_VersionStructure> organisations) {
        // Convert to Organisation records
        List<Organisation> organisationList = organisations.stream()
                .map(org -> new Organisation(
                        org.getId(),
                        multilingualStringMapper.mapToSobek(org.getName()),
                        getOrganisationType(org)
                ))
                .toList();
        return organisationList;
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
