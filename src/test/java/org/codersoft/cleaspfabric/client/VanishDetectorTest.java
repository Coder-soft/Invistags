package org.codersoft.cleaspfabric.client;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanishDetectorTest {

    private static final UUID SELF = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID STAFF = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID FRIEND = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void entityMissingFromTabIsFlagged() {
        Set<UUID> vanished = VanishDetector.findVanished(
            List.of(SELF, FRIEND, STAFF),
            List.of(SELF, FRIEND),
            SELF);

        assertEquals(Set.of(STAFF), vanished);
    }

    @Test
    void fullyListedServerFlagsNobody() {
        Set<UUID> vanished = VanishDetector.findVanished(
            List.of(SELF, FRIEND),
            List.of(SELF, FRIEND),
            SELF);

        assertTrue(vanished.isEmpty());
    }

    @Test
    void emptyTabListIsSkipped() {
        Set<UUID> vanished = VanishDetector.findVanished(
            List.of(SELF, FRIEND, STAFF),
            List.of(),
            SELF);

        assertTrue(vanished.isEmpty());
    }

    @Test
    void selfIsNeverFlagged() {
        Set<UUID> vanished = VanishDetector.findVanished(
            List.of(SELF),
            List.of(FRIEND),
            SELF);

        assertTrue(vanished.isEmpty());
    }

    @Test
    void multipleVanishedPlayersAreAllFlagged() {
        UUID otherStaff = UUID.fromString("00000000-0000-0000-0000-000000000004");

        Set<UUID> vanished = VanishDetector.findVanished(
            List.of(SELF, STAFF, FRIEND, otherStaff),
            List.of(SELF, FRIEND),
            SELF);

        assertEquals(Set.of(STAFF, otherStaff), vanished);
    }

    @Test
    void nullInputsAreSafe() {
        assertTrue(VanishDetector.findVanished(null, List.of(SELF), SELF).isEmpty());
        assertTrue(VanishDetector.findVanished(List.of(STAFF), null, SELF).isEmpty());
    }
}
