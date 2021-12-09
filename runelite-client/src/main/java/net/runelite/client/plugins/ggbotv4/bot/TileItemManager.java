package net.runelite.client.plugins.ggbotv4.bot;

import lombok.Getter;
import net.runelite.api.TileItem;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

public class TileItemManager {
    @Getter
    private final Map<Long, TileItem> items = new HashMap<>();


    @Subscribe
    public void onItemSpawned(ItemSpawned event)
    {
        TileItem item = event.getItem();
        items.put(item.getHash(), item);
    }

    @Subscribe
    public void onItemDespawned(ItemDespawned event)
    {
        items.remove(event.getItem().getHash());
    }

    @Subscribe
    public void onGameStateChanged(final GameStateChanged event)
    {
        switch (event.getGameState())
        {
            case HOPPING:
            case LOADING:
                items.clear();
                break;
            case LOGGED_IN:
                break;
        }
    }
}
