package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.Deck;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.rutebanken.sobek.netex.mapping.mapstruct.SimplePointMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.ZoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DeckMapperTest extends DataManagedObjectStructureMapperTestBase<
    Deck,
org.rutebanken.sobek.model.vehicle.Deck> {
    @Autowired
    DeckMapper mapper;

    @Autowired
    DeckSpaceMapper deckSpaceMapper;
    @Autowired
    private NetexIdHelper netexIdHelper;
    @Autowired
    private ValidPrefixList validPrefixList;
    @Autowired
    DataManagedObjectStructureMapper dataManagedObjectStructureMapper;
    @Autowired
    ZoneMapper zoneMapper;
    @Autowired
    SimplePointMapper simplePointMapper;

    protected DeckMapperTest() {
        super(org.rutebanken.netex.model.Deck.class, org.rutebanken.sobek.model.vehicle.Deck.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.Deck mapToSobek(org.rutebanken.netex.model.Deck source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.rutebanken.netex.model.Deck mapToNetex(org.rutebanken.sobek.model.vehicle.Deck source) {
        return mapper.mapToNetex(source, context);
    }

    @BeforeEach
    void setUp() {
        context.setDeckSpaceMapper(deckSpaceMapper);
        context.setNetexIdHelper(netexIdHelper);
        context.setValidPrefixList(validPrefixList);
        context.setDataManagedObjectStructureMapper(dataManagedObjectStructureMapper);
        context.setZoneMapper(zoneMapper);
        context.setSimplePointMapper(simplePointMapper);
        context.setCurrentSobekDeckPlan(new DeckPlan());
    }

    @Test
    void testDeckMapperIsInjected() {
        assertNotNull(mapper);
    }

    @Test
    void testMapToSobek() {
        Deck deck = new Deck();
        deck.setId("NMR:Deck:1");
        deck.setVersion("1");
        deck.setDescription(new MultilingualString().withContent("Test Deck"));

        var sobekDeck = mapper.mapToSobek(deck, context);

        assertNotNull(sobekDeck);
        assertEquals("Test Deck", sobekDeck.getDescription().getValue());
    }
}
