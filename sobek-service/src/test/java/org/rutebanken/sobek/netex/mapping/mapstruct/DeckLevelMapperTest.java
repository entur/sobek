package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.DeckLevel;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.deckplan.DeckLevelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DeckLevelMapperTest {
    @Autowired
    DeckLevelMapper mapper;

    @Autowired
    private NetexIdHelper netexIdHelper;
    @Autowired
    private ValidPrefixList validPrefixList;
    @Autowired
    DataManagedObjectStructureMapper dataManagedObjectStructureMapper;


    private MappingContext context;


    @BeforeEach
    void setUp() {
        context = new MappingContext();
        context.setNetexIdHelper(netexIdHelper);
        context.setValidPrefixList(validPrefixList);
        context.setDataManagedObjectStructureMapper(dataManagedObjectStructureMapper);
    }

    @Test
    void testMapperIsInjected() {
        assertNotNull(mapper);
    }

    @Test
    void testMapToSobek() {
        DeckLevel deckLevel = new DeckLevel();
        deckLevel.setId("NMR:DeckLevel:1");
        deckLevel.setVersion("1");
        deckLevel.setDescription(new MultilingualString().withContent("Test deckLevel"));

        var sobekDeckLevel = mapper.mapToSobek(deckLevel, context);

        assertNotNull(sobekDeckLevel);
        assertEquals("Test deckLevel", sobekDeckLevel.getDescription().getValue());
    }
}
