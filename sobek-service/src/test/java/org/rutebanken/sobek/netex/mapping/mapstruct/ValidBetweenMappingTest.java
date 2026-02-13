package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.ValidBetween;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class ValidBetweenMappingTest {

    @Autowired
    private ValidBetweenListConverter mapper;


    @Test
    void testMapperIsInjected() {
        assertNotNull(mapper, "Mapper should be injected by Spring");
    }

    @Test
    void testMapToSobek() {
        // Given
        List<ValidBetween> netexModel = new ArrayList<ValidBetween>();

        LocalDateTime now = LocalDateTime.now();
        ValidBetween validBetween = new ValidBetween();
        validBetween.setFromDate(now);
        netexModel.add(validBetween);

        // When
        org.rutebanken.sobek.model.ValidBetween sobekModel =
                mapper.mapToSobek(netexModel);

        // Then
        assertNotNull(sobekModel);
    }

}