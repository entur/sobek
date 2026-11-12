package org.rutebanken.sobek.netex.mapping.mapstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.entur.netex2.model.AuthorityRef;
import org.entur.netex2.model.KeyListStructure;
import org.entur.netex2.model.KeyValueStructure;
import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.OperatorRefStructure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.CustomKeyValueTypes;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FleetMapperTest extends DataManagedObjectStructureMapperTestBase<
        org.entur.netex2.model.Fleet,
        org.rutebanken.sobek.model.vehicle.Fleet> {

    @Autowired
    private FleetMapper mapper;
    @Autowired
    private KeyListStructureMapper keyListStructureMapper;
    @Autowired
    private NetexIdHelper netexIdHelper;
    @Autowired
    private ValidPrefixList validPrefixList;
    @Autowired
    private DataManagedObjectStructureMapper dataManagedObjectStructureMapper;
    @Autowired
    private OwnedEntityMapper ownedEntityMapper;

    private final ObjectFactory objectFactory = new ObjectFactory();

    protected FleetMapperTest() {
        super(org.entur.netex2.model.Fleet.class, org.rutebanken.sobek.model.vehicle.Fleet.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.Fleet mapToSobek(org.entur.netex2.model.Fleet source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.entur.netex2.model.Fleet mapToNetex(org.rutebanken.sobek.model.vehicle.Fleet source) {
        return mapper.mapToNetex(source, context);
    }

    // The mapping context is shared by the tests of the module: what this test changes in it is put back afterwards, or
    // the tests that run after it (TrainMapperTest uses the real reference resolver) would find a mock
    private ReferenceResolver originalReferenceResolver;
    private String originalDataOwnerRef;

    @BeforeEach
    void setUp() {
        originalReferenceResolver = context.getReferenceResolver();
        originalDataOwnerRef = context.getDataOwnerRef();
        context.setReferenceResolver(mock(ReferenceResolver.class));
        context.setKeyListStructureMapper(keyListStructureMapper);
        context.setNetexIdHelper(netexIdHelper);
        context.setValidPrefixList(validPrefixList);
        context.setDataManagedObjectStructureMapper(dataManagedObjectStructureMapper);
        context.setOwnedEntityMapper(ownedEntityMapper);
        context.setDataOwnerRef("NOG:Authority:1");
    }

    @AfterEach
    void restoreContext() {
        context.setReferenceResolver(originalReferenceResolver);
        context.setDataOwnerRef(originalDataOwnerRef);
    }

    // --- NeTEx to Sobek

    @Test
    @DisplayName("An OperatorRef becomes the id of the organisation of the fleet")
    void operatorRefIsTheOrganisation() {
        org.entur.netex2.model.Fleet netex = netexFleet();
        netex.withTransportOrganisationRef(objectFactory.createOperatorRef(new OperatorRefStructure().withRef("NOG:Operator:1")));

        assertThat(mapper.mapToSobek(netex, context).getTransportOrganisationRef()).isEqualTo("NOG:Operator:1");
    }

    @Test
    @DisplayName("An AuthorityRef becomes the id of the organisation of the fleet too: Sobek keeps the id and not the type")
    void authorityRefIsTheOrganisationToo() {
        org.entur.netex2.model.Fleet netex = netexFleet();
        netex.withTransportOrganisationRef(objectFactory.createAuthorityRef(new AuthorityRef().withRef("NOG:Authority:1")));

        assertThat(mapper.mapToSobek(netex, context).getTransportOrganisationRef()).isEqualTo("NOG:Authority:1");
    }

    @Test
    @DisplayName("Without an organisation reference the fleet has no organisation")
    void noOrganisationRef() {
        assertThat(mapper.mapToSobek(netexFleet(), context).getTransportOrganisationRef()).isNull();
    }

    @Test
    @DisplayName("The contract number is taken from its key value, which is not kept as a key value as well")
    void contractNumberIsMovedOutOfTheKeyValues() {
        org.entur.netex2.model.Fleet netex = netexFleet();
        netex.withKeyList(new KeyListStructure().withKeyValue(
                keyValue("depot", "North"),
                keyValue(CustomKeyValueTypes.CONTRACT_NUMBER_KEY, "C-2026-17"),
                keyValue("garage", "Central")));

        org.rutebanken.sobek.model.vehicle.Fleet sobek = mapper.mapToSobek(netex, context);

        assertThat(sobek.getContractNumber()).isEqualTo("C-2026-17");
        assertThat(sobek.getKeyValues()).extracting("key", "value")
                .containsExactlyInAnyOrder(org.assertj.core.groups.Tuple.tuple("depot", "North"), org.assertj.core.groups.Tuple.tuple("garage", "Central"));
    }

    @Test
    @DisplayName("Without the key value the fleet has no contract number, and the other key values are kept")
    void noContractNumberKeyValue() {
        org.entur.netex2.model.Fleet netex = netexFleet();
        netex.withKeyList(new KeyListStructure().withKeyValue(keyValue("depot", "North")));

        org.rutebanken.sobek.model.vehicle.Fleet sobek = mapper.mapToSobek(netex, context);

        assertThat(sobek.getContractNumber()).isNull();
        assertThat(sobek.getKeyValues()).extracting("key").containsExactly("depot");
    }

    // --- Sobek to NeTEx

    @Test
    @DisplayName("The organisation is always written as an OperatorRef, without a version")
    void organisationIsAnOperatorRefWithoutVersion() {
        org.rutebanken.sobek.model.vehicle.Fleet sobek = sobekFleet();
        sobek.setTransportOrganisationRef("NOG:GeneralOrganisation:1");

        org.entur.netex2.model.Fleet netex = mapper.mapToNetex(sobek, context);

        assertThat(netex.getTransportOrganisationRef().getName().getLocalPart()).isEqualTo("OperatorRef");
        assertThat(netex.getTransportOrganisationRef().getValue()).isInstanceOf(OperatorRefStructure.class);
        assertThat(netex.getTransportOrganisationRef().getValue().getRef()).isEqualTo("NOG:GeneralOrganisation:1");
        // A version would make the schema look for an Operator with that id and version in the same delivery
        assertThat(netex.getTransportOrganisationRef().getValue().getVersion()).isNull();
    }

    @Test
    @DisplayName("A fleet without an organisation has no organisation reference")
    void noOrganisationNoRef() {
        assertThat(mapper.mapToNetex(sobekFleet(), context).getTransportOrganisationRef()).isNull();
    }

    @Test
    @DisplayName("The contract number is written as a key value, next to the other key values")
    void contractNumberIsAKeyValue() {
        org.rutebanken.sobek.model.vehicle.Fleet sobek = sobekFleet();
        sobek.setContractNumber("C-2026-17");
        sobek.addKeyValue("depot", "North");

        org.entur.netex2.model.Fleet netex = mapper.mapToNetex(sobek, context);

        assertThat(netex.getKeyList().getKeyValue()).extracting("key", "value")
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(CustomKeyValueTypes.CONTRACT_NUMBER_KEY, "C-2026-17"),
                        org.assertj.core.groups.Tuple.tuple("depot", "North"));
    }

    @Test
    @DisplayName("The contract number alone makes a key list, and no contract number makes no key value")
    void contractNumberAloneAndNone() {
        org.rutebanken.sobek.model.vehicle.Fleet withNumber = sobekFleet();
        withNumber.setContractNumber("C-1");
        assertThat(mapper.mapToNetex(withNumber, context).getKeyList().getKeyValue()).extracting("key")
                .containsExactly(CustomKeyValueTypes.CONTRACT_NUMBER_KEY);

        org.entur.netex2.model.Fleet without = mapper.mapToNetex(sobekFleet(), context);
        assertThat(without.getKeyList() == null || without.getKeyList().getKeyValue().stream()
                .noneMatch(kv -> CustomKeyValueTypes.CONTRACT_NUMBER_KEY.equals(kv.getKey()))).isTrue();
    }

    @Test
    @DisplayName("Organisation and contract number survive a round trip")
    void roundTrip() {
        org.rutebanken.sobek.model.vehicle.Fleet sobek = sobekFleet();
        sobek.setTransportOrganisationRef("NOG:Operator:7");
        sobek.setContractNumber("C-7");
        sobek.addKeyValue("depot", "North");

        org.rutebanken.sobek.model.vehicle.Fleet back = mapper.mapToSobek(mapper.mapToNetex(sobek, context), context);

        assertThat(back.getTransportOrganisationRef()).isEqualTo("NOG:Operator:7");
        assertThat(back.getContractNumber()).isEqualTo("C-7");
        assertThat(back.getKeyValues()).extracting("key").containsExactly("depot");
    }

    private org.entur.netex2.model.Fleet netexFleet() {
        return new org.entur.netex2.model.Fleet().withId("NMR:Fleet:1").withVersion("1");
    }

    private org.rutebanken.sobek.model.vehicle.Fleet sobekFleet() {
        org.rutebanken.sobek.model.vehicle.Fleet fleet = new org.rutebanken.sobek.model.vehicle.Fleet();
        fleet.setNetexId("NMR:Fleet:1");
        fleet.setVersion(1L);
        fleet.setDataOwnerRef("NOG:Authority:1");
        return fleet;
    }

    private static KeyValueStructure keyValue(String key, String value) {
        return new KeyValueStructure().withKey(key).withValue(value);
    }
}
