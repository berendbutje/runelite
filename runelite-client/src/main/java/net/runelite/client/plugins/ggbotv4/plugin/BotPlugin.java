/*
 * Copyright (c) 2017, Seth <Sethtroll3@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.ggbotv4.plugin;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ggbotv4.bot.Script;
import net.runelite.client.plugins.ggbotv4.bot.scripts.WoodcuttingScript;
import net.runelite.client.plugins.ggbotv4.bot.task.Task;
import net.runelite.client.plugins.ggbotv4.bot.task.TaskExecutor;
import net.runelite.client.plugins.ggbotv4.util.Axe;
import net.runelite.client.plugins.ggbotv4.util.TreeType;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@PluginDescriptor(
	name = "ggbot v4",
	description = "Bottin' around the world",
	tags = {"bot"},
	enabledByDefault = false
)
@Slf4j
public class BotPlugin extends Plugin
{
	@Getter
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private GameObjectsOverlay treesOverlay;
	@Inject
	private BotOverlay botOverlay;
	@Inject
	private BotConfig config;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private SkillIconManager skillIconManager;

	private NavigationButton navButton;

	@Getter
	@Nullable
	private Axe axe;

	@Getter
	private GameObject bankTarget = null;
	@Getter
	private BotState state = BotState.Idle;
	@Getter
	private final Map<Long, GameObject> treeObjects = new HashMap<>();

	@Getter
	private Script script;
	private final TaskExecutor executor = new TaskExecutor();

	@Provides
	BotConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BotConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(botOverlay);
		overlayManager.add(treesOverlay);

		final BotPanel panel = injector.getInstance(BotPanel.class);

		final BufferedImage icon = skillIconManager.getSkillImage(Skill.WOODCUTTING);

		navButton = NavigationButton.builder()
				.tooltip("GGB")
				.icon(icon)
				.priority(1)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(botOverlay);
		overlayManager.remove(treesOverlay);
		treeObjects.clear();
		axe = null;
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick) {
		if(client.getMouseIdleTicks() > 14000) {
			client.setMouseIdleTicks(0);

			System.out.println("AFK prevented.");
		}

	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		Player local = client.getLocalPlayer();
		if(local != null) {
			int animId = local.getAnimation();

			if (animId == AnimationID.IDLE) {
				if(local.isMoving()) {
					state = BotState.Moving;
				} else if(client.getItemContainer(InventoryID.BANK) != null) {
					state = BotState.Banking;
				} else {
					state = BotState.Idle;
				}
			} else {
				Axe axe = Axe.byAnimId(animId);
				if (axe != null) {
					state = BotState.Woodcutting;
				} else {
					System.out.println("No idea what he's doing..");
				}
			}
		}

		if(executor.size() == 0 && script != null) {
			Task task = script.evaluate(this);
			if(task != null) {
				executor.add(task);
			}
		}

		executor.execute(this);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event) {
		if(script != null) {
			if(event.getMenuAction().getId() == MenuAction.CANCEL.getId()) {
				MenuEntry[] menuEntries = client.getMenuEntries();
				menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

				MenuEntry menuEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();

				menuEntry.setId(event.getId());
				menuEntry.setOption("Stop");
				menuEntry.setTarget(ColorUtil.wrapWithColorTag("Botting", Color.ORANGE));
				menuEntry.setType(MenuAction.PRIO_GGBOT.getId());

				client.setMenuEntries(menuEntries);
			}
		} else if(event.getMenuAction().getId() == MenuAction.EXAMINE_OBJECT.getId()) {
			final Tile tile = client.getScene().getTiles()[client.getPlane()][event.getActionParam0()][event.getParam1()];
			if (tile == null) {
				return;
			}

			final TileObject tileObject = findTileObject(tile, event.getIdentifier());
			if (tileObject == null) {
				return;
			}

			TreeType tree = TreeType.of(event.getId());
			if (tree != null) {
				MenuEntry[] menuEntries = client.getMenuEntries();
				menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

				MenuEntry menuEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();

				menuEntry.setId(event.getId());
				menuEntry.setOption("Bot");
				menuEntry.setTarget(ColorUtil.wrapWithColorTag(Text.removeTags(event.getTarget()), Color.ORANGE));
				menuEntry.setType(MenuAction.PRIO_GGBOT.getId());

				menuEntry.setParam0((int) (tileObject.getHash() >> 32));
				menuEntry.setParam1((int) tileObject.getHash());

				client.setMenuEntries(menuEntries);
			}

			if(Text.removeTags(event.getTarget()).equalsIgnoreCase("bank booth")) {
				MenuEntry[] menuEntries = client.getMenuEntries();
				menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

				MenuEntry menuEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();

				menuEntry.setId(event.getId());
				menuEntry.setOption("Set bank spot");
				menuEntry.setTarget(ColorUtil.wrapWithColorTag(Text.removeTags(event.getTarget()), Color.ORANGE));
				menuEntry.setType(MenuAction.PRIO_GGBOT.getId());

				menuEntry.setParam0((int) (tileObject.getHash() >> 32));
				menuEntry.setParam1((int) tileObject.getHash());

				client.setMenuEntries(menuEntries);
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction().getId() == MenuAction.PRIO_GGBOT.getId()) {
			long hash = (long)event.getParam0() << 32 | (long)event.getParam1();
			GameObject gameObject = treeObjects.get(hash);

			TreeType tree = TreeType.of(event.getId());
			if(tree != null) {
				executor.clear();
				script = new WoodcuttingScript(this);

				System.out.println("Started woodcutting script.");
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Ggbot", "Started woodcutting script.", "Ggbot");
			} else if(Text.removeTags(event.getMenuTarget()).equalsIgnoreCase("bank booth")) {
				bankTarget = gameObject;

				System.out.println("Bank target set!");
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Ggbot", "Bank target set!", "Ggbot");
			} else if(Text.removeTags(event.getMenuOption()).equalsIgnoreCase("stop")) {
				System.out.println("Bot stopped because of interaction from the user.");
				client.addChatMessage(ChatMessageType.PUBLICCHAT, "Ggbot", "Bot stopped because of interaction from the user.", "Ggbot");

				executor.clear();
				script = null;
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event)
	{
		ItemContainer container = event.getItemContainer();
		if(container != null && container.getId() == InventoryID.INVENTORY.getId()) {
			if(isInventoryFull()) {
				System.out.println("Player has 28 items..., FULL");
			} else {
				System.out.println("Player has " + (28 - getInventoryItemCount()) + " slots left");
			}
		}

	}

	int getInventoryItemCount() {
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

	public boolean isInventoryFull() {
		return getInventoryItemCount() == 28;
	}

	private TileObject findTileObject(Tile tile, int id)
	{
		if (tile == null)
		{
			return null;
		}

		final GameObject[] tileGameObjects = tile.getGameObjects();
		final DecorativeObject tileDecorativeObject = tile.getDecorativeObject();
		final WallObject tileWallObject = tile.getWallObject();
		final GroundObject groundObject = tile.getGroundObject();

		if (objectIdEquals(tileWallObject, id))
		{
			return tileWallObject;
		}

		if (objectIdEquals(tileDecorativeObject, id))
		{
			return tileDecorativeObject;
		}

		if (objectIdEquals(groundObject, id))
		{
			return groundObject;
		}

		for (GameObject object : tileGameObjects)
		{
			if (objectIdEquals(object, id))
			{
				return object;
			}
		}

		return null;
	}

	private boolean objectIdEquals(TileObject tileObject, int id)
	{
		if (tileObject == null)
		{
			return false;
		}

		if (tileObject.getId() == id)
		{
			return true;
		}

		// Menu action EXAMINE_OBJECT sends the transformed object id, not the base id, unlike
		// all of the GAME_OBJECT_OPTION actions, so check the id against the impostor ids
		final ObjectComposition comp = client.getObjectDefinition(tileObject.getId());

		if (comp.getImpostorIds() != null)
		{
			for (int impostorId : comp.getImpostorIds())
			{
				if (impostorId == id)
				{
					return true;
				}
			}
		}

		return false;
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{

	}

	@Subscribe
	public void onGameObjectSpawned(final GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		treeObjects.put(gameObject.getHash(), gameObject);
	}

	@Subscribe
	public void onGameObjectDespawned(final GameObjectDespawned event)
	{
		treeObjects.remove(event.getGameObject().getHash());
	}

	@Subscribe
	public void onGameObjectChanged(final GameObjectChanged event)
	{
		treeObjects.remove(event.getGameObject().getHash());
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case HOPPING:
			case LOADING:
				treeObjects.clear();
				break;
			case LOGGED_IN:
				break;
		}
	}

	@Subscribe
	public void onAnimationChanged(final AnimationChanged event)
	{
//		Player local = client.getLocalPlayer();
//		if (local == null || event.getActor() != local)
//			return;
//
//		int animId = local.getAnimation();
//		if (animId == AnimationID.IDLE) {
//			state = BotState.Idle;
//		} else {
//			Axe axe = Axe.byAnimId(animId);
//			if (axe != null) {
//				state = BotState.Woodcutting;
//			} else {
//				System.out.println("No idea what he's doing..");
//			}
//		}
	}
}
