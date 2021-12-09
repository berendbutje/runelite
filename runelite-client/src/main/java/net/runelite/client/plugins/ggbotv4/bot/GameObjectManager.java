package net.runelite.client.plugins.ggbotv4.bot;

import lombok.Getter;
import net.runelite.api.GameObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class GameObjectManager {
    @Getter
    private final Map<Long, GameObject> gameObjects = new HashMap<>();

    @Inject
    GameObjectManager(
        EventBus eventBus
    ) {
        eventBus.register(this);
    }

    public GameObject get(long hash) {
        return gameObjects.get(hash);
    }

    public GameObject find(LocalPoint position, Predicate<GameObject> found) {
        return gameObjects.values().stream()
                .filter(found)
                .min(Comparator.comparing((GameObject a) -> a.getLocalLocation().distanceTo(position))).orElse(null);
    }

    @Subscribe
    public void onGameObjectSpawned(final GameObjectSpawned event)
    {
        GameObject gameObject = event.getGameObject();
        gameObjects.put(gameObject.getHash(), gameObject);
    }

    @Subscribe
    public void onGameObjectDespawned(final GameObjectDespawned event)
    {
        gameObjects.remove(event.getGameObject().getHash());
    }

    @Subscribe
    public void onGameObjectChanged(final GameObjectChanged event)
    {
        gameObjects.remove(event.getGameObject().getHash());
    }

    @Subscribe
    public void onGameStateChanged(final GameStateChanged event)
    {
        switch (event.getGameState())
        {
            case HOPPING:
            case LOADING:
                gameObjects.clear();
                break;
            case LOGGED_IN:
                break;
        }
    }

    public Collection<GameObject> values() {
        return gameObjects.values();
    }
}
