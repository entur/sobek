package org.rutebanken.sobek.netex.mapping.context;


import jakarta.xml.bind.JAXBElement;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.model.vehicle.Deck;
import org.rutebanken.sobek.model.vehicle.DeckSpace;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.KeyListStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.OwnedEntityMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.deckplan.DeckSpaceMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.deckplan.SpotAffinityMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.equipment.*;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;


/**
 * Context object to pass state between mappers during mapping operations.
 * Replaces Orika's MappingContext.
 */
@Getter
@Setter
@Service()
@RequestScope
public class MappingContext {

    Logger logger = LoggerFactory.getLogger(MappingContext.class);

    private ReferenceResolver referenceResolver;
    private SeatEquipmentMapper seatEquipmentMapper;
    private BedEquipmentMapper bedEquipmentMapper;
    private AccessVehicleEquipmentMapper accessVehicleEquipmentMapper;
    private SpotEquipmentMapper spotEquipmentMapper;
    private LuggageSpotEquipmentMapper luggageSpotEquipmentMapper;
    private StaircaseEquipmentMapper staircaseEquipmentMapper;
    private EntranceEquipmentMapper entranceEquipmentMapper;
    private KeyListStructureMapper keyListStructureMapper;
    private Deck currentSobekDeck;
    private DeckSpace currentSobekDeckSpace;
    private DeckSpaceMapper deckSpaceMapper;
    private SpotAffinityMapper spotAffinityMapper;
    private ZoneId defaultTimeZone;
    private ValidPrefixList validPrefixList;
    private NetexIdHelper netexIdHelper;
    private DataManagedObjectStructureMapper dataManagedObjectStructureMapper;
    private OwnedEntityMapper ownedEntityMapper;

    public MappingContext() {
    }

    @Autowired
    public MappingContext(ReferenceResolver resolver,
                          SeatEquipmentMapper seatEquipmentMapper,
                          BedEquipmentMapper bedEquipmentMapper,
                          AccessVehicleEquipmentMapper accessVehicleEquipmentMapper,
                          SpotEquipmentMapper spotEquipmentMapper,
                          LuggageSpotEquipmentMapper luggageSpotEquipmentMapper,
                          StaircaseEquipmentMapper staircaseEquipmentMapper,
                          EntranceEquipmentMapper entranceEquipmentMapper,
                          KeyListStructureMapper keyListStructureMapper,
                          DeckSpaceMapper deckSpaceMapper,
                          SpotAffinityMapper spotAffinityMapper,
                          ValidPrefixList validPrefixList,
                          NetexIdHelper netexIdHelper,
                          DataManagedObjectStructureMapper dataManagedObjectStructureMapper,
                          OwnedEntityMapper ownedEntityMapper) {
        this.referenceResolver = resolver;
        this.seatEquipmentMapper = seatEquipmentMapper;
        this.bedEquipmentMapper = bedEquipmentMapper;
        this.accessVehicleEquipmentMapper = accessVehicleEquipmentMapper;
        this.spotEquipmentMapper = spotEquipmentMapper;
        this.luggageSpotEquipmentMapper = luggageSpotEquipmentMapper;
        this.staircaseEquipmentMapper = staircaseEquipmentMapper;
        this.entranceEquipmentMapper = entranceEquipmentMapper;
        this.keyListStructureMapper = keyListStructureMapper;
        this.deckSpaceMapper = deckSpaceMapper;
        this.spotAffinityMapper = spotAffinityMapper;
        this.validPrefixList = validPrefixList;
        this.netexIdHelper = netexIdHelper;
        this.dataManagedObjectStructureMapper = dataManagedObjectStructureMapper;
        this.ownedEntityMapper = ownedEntityMapper;
    }

    public void updateMappingContext(SiteFrame netexSiteFrame) {
        String timeZoneString = Optional.of(netexSiteFrame)
                .map(SiteFrame::getFrameDefaults)
                .map(VersionFrameDefaultsStructure::getDefaultLocale)
                .map(LocaleStructure::getTimeZone)
                .orElseThrow(() -> new NetexMappingException("Cannot resolve time zone from FrameDefaults in site frame " + netexSiteFrame.getId()));

        this.defaultTimeZone = ZoneId.of(timeZoneString);
        logger.info("Setting default time zone for netex mapping context to {}", this.defaultTimeZone);
    }

    public void updateMappingContext(PublicationDeliveryStructure publicationDeliveryStructure, ResourceFrame resourceFrame) {
        // Check what kind of frame we find
        List<JAXBElement<? extends Common_VersionFrameStructure>> compositeFrameOrCommonFrame = publicationDeliveryStructure.getDataObjects().getCompositeFrameOrCommonFrame();

        VersionFrameDefaultsStructure defaults = compositeFrameOrCommonFrame.getFirst().getValue().getFrameDefaults();

        try {
            if (defaults == null) {
                defaults = ((CompositeFrame) compositeFrameOrCommonFrame.getFirst().getValue()).getFrames().getCommonFrame().getFirst().getValue().getFrameDefaults();
            }
        } catch (Exception e) {
            throw new NetexMappingException("Cannot resolve time zone from FrameDefaults in frame " + compositeFrameOrCommonFrame.getFirst().getValue().getId());
        }

        if(defaults == null) {
            throw new NetexMappingException("Cannot resolve time zone from FrameDefaults in frame " + compositeFrameOrCommonFrame.getFirst().getValue().getId());
        }

        String timeZoneString = defaults.getDefaultLocale().getTimeZone();

        this.defaultTimeZone = ZoneId.of(timeZoneString);

        ResponsibilitySetsInFrame_RelStructure responsibilitySets = resourceFrame.getResponsibilitySets();

        if (responsibilitySets == null || responsibilitySets.getResponsibilitySet() == null) {
            throw new NetexMappingException("Cannot resolve responsibilitysets from resourceframe " + resourceFrame.getId());
        }

        ResponsibilitySet responsibilitySet = responsibilitySets.getResponsibilitySet().getFirst();
        if (responsibilitySet == null) {
            throw new NetexMappingException("Cannot resolve responsibilityset from resourceframe " + resourceFrame.getId());
        }

        if(responsibilitySet.getRoles() == null ||
                responsibilitySet.getRoles().getResponsibilityRoleAssignment() == null ||
                responsibilitySet.getRoles().getResponsibilityRoleAssignment().isEmpty()) {
            throw new NetexMappingException("Cannot resolve role from responsibilityset " + responsibilitySet.getId());
        }

        ResponsibilityRoleAssignment roleAssignment = responsibilitySet.getRoles()
                                .getResponsibilityRoleAssignment()
                                .stream()
                                .filter(ra -> ra.getDataRoleType().contains(DataRoleTypeEnumeration.OWNS))
                                .findFirst()
                                .orElseThrow(() -> new NetexMappingException("Cannot resolve dataowner role from responsibilityset " + responsibilitySet.getId()));

        if(roleAssignment.getResponsibleOrganisationRef() == null || roleAssignment.getResponsibleOrganisationRef().getRef() == null) {
            throw new NetexMappingException("Cannot resolve responsible organisation from responsibilityset " + responsibilitySet.getId());
        }

        if(defaults.getDefaultResponsibilitySetRef() == null || defaults.getDefaultResponsibilitySetRef().getRef() == null) {
            throw new NetexMappingException("Cannot resolve default responsibilityset from FrameDefaults");
        }

        if(!responsibilitySet.getId().equals(defaults.getDefaultResponsibilitySetRef().getRef())) {
            throw new NetexMappingException("Default responsibilitysetref " + defaults.getDefaultResponsibilitySetRef().getRef() + " doesn't match to a valid responsibilityset");
        }

        this.ownedEntityMapper.setDataOwnerRef(roleAssignment.getResponsibleOrganisationRef().getRef());



        logger.info("Setting default time zone for netex mapping context to {}", this.defaultTimeZone);
    }
}