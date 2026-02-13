package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.Deck;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.deckplan.DeckMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.deckplan.DeckSpaceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DeckMapperTest {
    @Autowired
    DeckMapper mapper;

    @Autowired
    DeckSpaceMapper deckSpaceMapper;

    private MappingContext context;

    @BeforeEach
    void setUp() {
        context = new MappingContext();
        context.setDeckSpaceMapper(deckSpaceMapper);
    }

    @Test
    void testDeckMapperIsInjected() {
        assertNotNull(mapper);
    }

    @Test
    void testMapToSobek() {
        Deck deck = new Deck();
        deck.setId("D:1");
        deck.setVersion("1");
        deck.setDescription(new MultilingualString().withContent("Test Deck"));

        var sobekDeck = mapper.mapToSobek(deck, context);

        assertNotNull(sobekDeck);
        assertEquals("Test Deck", sobekDeck.getDescription().getValue());
    }
}
