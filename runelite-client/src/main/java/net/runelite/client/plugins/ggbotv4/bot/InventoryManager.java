package net.runelite.client.plugins.ggbotv4.bot;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;

public class InventoryManager {
    private final Client client;

    @Inject
    InventoryManager(
            Client client,
            EventBus eventBus
    ) {
        this.client = client;

        eventBus.register(this);
    }

    @Subscribe
    public void onItemContainerChanged(final ItemContainerChanged event)
    {
        ItemContainer container = event.getItemContainer();
        if(container != null && container.getId() == InventoryID.INVENTORY.getId()) {
            if(isFull()) {
                System.out.println("Player has 28 items..., FULL");
            } else {
                System.out.println("Player has " + (28 - getItemCount()) + " slots left");
            }
        }
    }

    int getItemCount() {
        ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
        int itemCount = 0;
        if(container != null) {
            container.getItems();
            for (Item item : container.getItems()) {
                if (item != null && item.getId() != -1) {
                    itemCount++;
                }
            }
        }
        return itemCount;
    }

    public boolean isFull() {
        return getItemCount() >= 28;
    }

    public boolean contains(int... itemIds) {
        ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);

        if(container == null)
            return false;

        for(int id : itemIds) {
            if(!container.contains(id)) {
                return false;
            }
        }

        return true;
    }

}
