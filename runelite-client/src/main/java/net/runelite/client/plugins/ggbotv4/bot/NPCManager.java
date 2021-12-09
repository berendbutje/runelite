package net.runelite.client.plugins.ggbotv4.bot;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.eventbus.EventBus;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NPCManager {
    private final Client client;

    @Inject
    NPCManager(
            EventBus eventBus,
            Client client
    ) {
        this.client = client;

        eventBus.register(this);
    }

    public NPC get(int index) {
        try {
            return client.getCachedNPCs()[index];
        } catch(ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public NPC find(int id) {
        return client.getNpcs().stream().filter((NPC npc) -> npc.getId() == id).findFirst().orElse(null);
    }

    public NPC find(LocalPoint position, Predicate<NPC> found) {
        return client.getNpcs().stream()
                .filter(found)
                .min(Comparator.comparing((NPC a) -> a.getLocalLocation().distanceTo(position))).orElse(null);
    }

    public List<NPC> findList(LocalPoint position, Predicate<NPC> found) {
        return client.getNpcs().stream()
                .filter(found)
                .sorted(Comparator.comparing((NPC a) -> a.getLocalLocation().distanceTo(position))).collect(Collectors.toList());
    }
}
