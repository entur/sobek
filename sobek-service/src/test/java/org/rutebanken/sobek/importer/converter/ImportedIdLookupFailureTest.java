package org.rutebanken.sobek.importer.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.sobek.graphql.converter.GenericNeTExIdConverter;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.repository.generic.GenericEntityInVersionRepository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * A failing imported-id lookup must fail the request. Treating it as "no match" would create a duplicate
 * of an object that already exists.
 */
@ExtendWith(MockitoExtension.class)
class ImportedIdLookupFailureTest {

    private static final String EXTERNAL_ID = "AUTOSYS:Vehicle:42";

    @Mock private GenericEntityInVersionRepository repository;

    private ValidPrefixList validPrefixList;
    private NetexIdHelper netexIdHelper;

    @BeforeEach
    void setUp() {
        validPrefixList = new ValidPrefixList("NMR", List.of("AKT"));
        netexIdHelper = new NetexIdHelper(validPrefixList);
    }

    private org.entur.netex2.model.Vehicle netexVehicle() {
        var vehicle = new org.entur.netex2.model.Vehicle();
        vehicle.setId(EXTERNAL_ID);
        return vehicle;
    }

    private Vehicle sobekVehicle() {
        var vehicle = new Vehicle();
        vehicle.setNetexId(EXTERNAL_ID);
        return vehicle;
    }

    @Test
    void importConverterPropagatesLookupFailure() {
        when(repository.findByKeyValue(any(), any(), any())).thenThrow(new IllegalStateException("db down"));
        var converter = new VehicleIdConverter(validPrefixList, netexIdHelper, repository);

        assertThatThrownBy(() -> converter.convertIncomingId(netexVehicle()))
                .isInstanceOf(IllegalStateException.class).hasMessage("db down");
    }

    @Test
    void graphqlConverterPropagatesLookupFailure() {
        when(repository.findByKeyValue(any(), any(), any())).thenThrow(new IllegalStateException("db down"));
        var converter = new GenericNeTExIdConverter<>(validPrefixList, netexIdHelper, repository, Vehicle.class);

        assertThatThrownBy(() -> converter.convertIncomingId(sobekVehicle()))
                .isInstanceOf(IllegalStateException.class).hasMessage("db down");
    }

    @Test
    void importConverterStillMapsKnownExternalId() {
        when(repository.findByKeyValue(any(), eq(Set.of(EXTERNAL_ID)), any())).thenReturn("NMR:Vehicle:7");
        var converter = new VehicleIdConverter(validPrefixList, netexIdHelper, repository);

        assertThat(converter.convertIncomingId(netexVehicle()).getId()).isEqualTo("NMR:Vehicle:7");
    }

    @Test
    void importConverterLeavesUnknownExternalIdAlone() {
        when(repository.findByKeyValue(any(), any(), any())).thenReturn(null);
        var converter = new VehicleIdConverter(validPrefixList, netexIdHelper, repository);

        assertThat(converter.convertIncomingId(netexVehicle()).getId()).isEqualTo(EXTERNAL_ID);
    }
}
