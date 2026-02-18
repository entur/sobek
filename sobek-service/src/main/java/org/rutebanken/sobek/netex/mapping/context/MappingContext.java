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
                          DataManagedObjectStructureMapper dataManagedObjectStructureMapper) {
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

    public void updateMappingContext(PublicationDeliveryStructure publicationDeliveryStructure) {
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
        logger.info("Setting default time zone for netex mapping context to {}", this.defaultTimeZone);
    }
}