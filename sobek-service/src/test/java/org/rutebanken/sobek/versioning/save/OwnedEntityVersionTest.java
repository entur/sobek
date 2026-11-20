package org.rutebanken.sobek.versioning.save;

import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.vehicle.Deck;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.Equipment;
import org.rutebanken.sobek.model.vehicle.PassengerSpace;
import org.rutebanken.sobek.model.vehicle.PassengerSpot;
import org.rutebanken.sobek.model.vehicle.SpotAffinity;
import org.rutebanken.sobek.repository.listener.NetexIdAssigner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Objects a root owns follow its version. Objects it only refers to have a version sequence of their own.
 */
class OwnedEntityVersionTest {

    private final NetexIdAssigner assigner = new NetexIdAssigner(null);

    @Test
    void ownedObjectsGetTheVersionOfTheRoot_butReferencedObjectsKeepTheirOwn() {
        Equipment referenced = new Equipment();
        referenced.setNetexId("NMR:Equipment:1");
        referenced.setVersion(7);

        PassengerSpace space = new PassengerSpace();
        space.setActualVehicleEquipments(List.of(referenced));

        Deck deck = new Deck();
        deck.setVersion(1);
        deck.setDeckSpaces(List.of(space));
        space.setVersion(1);

        DeckPlan plan = new DeckPlan();
        plan.setVersion(3);
        plan.setDecks(List.of(deck));

        assigner.assignVersionOfRootToOwnedEntities(plan);

        assertThat(deck.getVersion()).isEqualTo(3);
        assertThat(space.getVersion()).isEqualTo(3);
        assertThat(referenced.getVersion()).isEqualTo(7);
    }

    @Test
    void spotsHeldOnlyByAnAffinity_cascadePersistAndFollowTheRoot() {
        PassengerSpot member = new PassengerSpot();
        member.setVersion(1);

        SpotAffinity affinity = new SpotAffinity();
        affinity.setMembers(List.of(member));

        PassengerSpace space = new PassengerSpace();
        space.setSpotAffinities(List.of(affinity));

        Deck deck = new Deck();
        deck.setDeckSpaces(List.of(space));

        DeckPlan plan = new DeckPlan();
        plan.setVersion(4);
        plan.setDecks(List.of(deck));

        assigner.assignVersionOfRootToOwnedEntities(plan);

        assertThat(member.getVersion()).isEqualTo(4);
    }
}
