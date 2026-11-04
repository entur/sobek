package org.rutebanken.sobek.netex.id;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rutebanken.sobek.netex.id.CodespaceType.EXTERNAL;
import static org.rutebanken.sobek.netex.id.CodespaceType.REGISTERED;
import static org.rutebanken.sobek.netex.id.CodespaceType.SOBEK;

/**
 * NMR and CVT are Sobek's own, the codespaces in the configured list are registered, and anything else is external.
 */
class ValidPrefixListCodespaceTypeTest {

    private final ValidPrefixList validPrefixList = new ValidPrefixList("NMR", List.of("AKT", "ATB"));

    @Test
    void configuredPrefixIsSobeksForEveryType() {
        assertThat(validPrefixList.getCodespaceTypeOf("NMR:VehicleType:1", "VehicleType")).isEqualTo(SOBEK);
        assertThat(validPrefixList.getCodespaceTypeOf("NMR:Vehicle:1", "Vehicle")).isEqualTo(SOBEK);
        assertThat(validPrefixList.getCodespaceTypeOf("NMR:DeckPlan:1", "DeckPlan")).isEqualTo(SOBEK);
    }

    @Test
    void contractualPrefixIsSobeksForVehicleTypesOnly() {
        assertThat(validPrefixList.getCodespaceTypeOf("CVT:VehicleType:1", "VehicleType")).isEqualTo(SOBEK);
        assertThat(validPrefixList.getCodespaceTypeOf("CVT:Vehicle:1", "Vehicle")).isEqualTo(EXTERNAL);
        assertThat(validPrefixList.getCodespaceTypeOf("CVT:DeckPlan:1", "DeckPlan")).isEqualTo(EXTERNAL);
    }

    @Test
    void codespacesInTheConfiguredListAreRegistered() {
        assertThat(validPrefixList.getCodespaceTypeOf("AKT:VehicleType:1", "VehicleType")).isEqualTo(REGISTERED);
        assertThat(validPrefixList.getCodespaceTypeOf("ATB:Vehicle:1", "Vehicle")).isEqualTo(REGISTERED);
    }

    @Test
    void anyOtherCodespaceIsExternal() {
        assertThat(validPrefixList.getCodespaceTypeOf("AUTOSYS:VehicleType:1", "VehicleType")).isEqualTo(EXTERNAL);
        assertThat(validPrefixList.getCodespaceTypeOf("TST:Vehicle:1", "Vehicle")).isEqualTo(EXTERNAL);
    }

    @Test
    void sobeksCodespaceWinsEvenIfItIsAlsoInTheList() {
        ValidPrefixList listedToo = new ValidPrefixList("NMR", List.of("NMR", "AKT"));
        assertThat(listedToo.getCodespaceTypeOf("NMR:Vehicle:1", "Vehicle")).isEqualTo(SOBEK);
    }

    @Test
    void wildcardInTheListMakesEveryOtherCodespaceRegisteredButNotSobeks() {
        ValidPrefixList anything = new ValidPrefixList("NMR", List.of("*"));
        assertThat(anything.getCodespaceTypeOf("WHATEVER:Vehicle:1", "Vehicle")).isEqualTo(REGISTERED);
        assertThat(anything.getCodespaceTypeOf("NMR:Vehicle:1", "Vehicle")).isEqualTo(SOBEK);
    }

    @Test
    void anIdThatIsNotOnTheNetexFormIsExternal() {
        assertThat(validPrefixList.getCodespaceTypeOf(null, "Vehicle")).isEqualTo(EXTERNAL);
        assertThat(validPrefixList.getCodespaceTypeOf("NMR", "Vehicle")).isEqualTo(EXTERNAL);
        assertThat(validPrefixList.getCodespaceTypeOf("NMR:Vehicle", "Vehicle")).isEqualTo(EXTERNAL);
        assertThat(validPrefixList.getCodespaceTypeOf("NMR:Vehicle:1:2", "Vehicle")).isEqualTo(EXTERNAL);
        assertThat(validPrefixList.getCodespaceTypeOf("", "Vehicle")).isEqualTo(EXTERNAL);
    }

    @Test
    void validPrefixForTypeIsTrueForSobeksAndRegisteredCodespaces() {
        assertThat(validPrefixList.isValidPrefixForType("NMR", "Vehicle")).isTrue();
        assertThat(validPrefixList.isValidPrefixForType("CVT", "VehicleType")).isTrue();
        assertThat(validPrefixList.isValidPrefixForType("AKT", "Vehicle")).isTrue();
        assertThat(validPrefixList.isValidPrefixForType("AUTOSYS", "Vehicle")).isFalse();
        assertThat(validPrefixList.isValidPrefixForType("CVT", "Vehicle")).isFalse();
    }
}
