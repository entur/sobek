package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.DeckLevel;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DeckLevelMapperTest extends DataManagedObjectStructureMapperTestBase<
    DeckLevel,
    org.rutebanken.sobek.model.vehicle.DeckLevel> {
    @Autowired
    DeckLevelMapper mapper;

    protected DeckLevelMapperTest() {
        super(org.rutebanken.netex.model.DeckLevel.class, org.rutebanken.sobek.model.vehicle.DeckLevel.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.DeckLevel mapToSobek(org.rutebanken.netex.model.DeckLevel source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.rutebanken.netex.model.DeckLevel mapToNetex(org.rutebanken.sobek.model.vehicle.DeckLevel source) {
        return mapper.mapToNetex(source, context);
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
