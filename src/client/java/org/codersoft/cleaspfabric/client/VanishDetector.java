package org.codersoft.cleaspfabric.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Pure-Java vanished-player detection. Kept free of Minecraft types so it can be
 * exercised by the headless unit tests.
 */
public final class VanishDetector {

    private VanishDetector() {
    }

    /**
     * Returns the entity IDs that are present in the world but absent from the tab
     * list. When the tab list is empty the server does not use it (or has not sent it
     * yet), so nothing is flagged: otherwise every player on such a server would be a
     * false positive.
     *
     * @param entityIds  UUIDs of the player entities the client can currently see
     * @param tabListIds UUIDs the server lists in the tab list
     * @param self       the local player, never flagged
     */
    public static Set<UUID> findVanished(Collection<UUID> entityIds,
                                         Collection<UUID> tabListIds,
                                         UUID self) {
        Set<UUID> vanished = new HashSet<>();
        if (entityIds == null || tabListIds == null || tabListIds.isEmpty()) {
            return vanished;
        }

        Set<UUID> listed = new HashSet<>(tabListIds);
        for (UUID id : entityIds) {
            if (id != null && !id.equals(self) && !listed.contains(id)) {
                vanished.add(id);
            }
        }
        return vanished;
    }
}
