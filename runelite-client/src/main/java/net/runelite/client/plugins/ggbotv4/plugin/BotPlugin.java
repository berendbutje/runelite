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
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ggbotv4.bot.Bot;
import net.runelite.client.plugins.ggbotv4.bot.Script;
import net.runelite.client.plugins.ggbotv4.bot.task.TaskExecutor;
import net.runelite.client.plugins.ggbotv4.util.Axe;
import net.runelite.client.plugins.ggbotv4.util.TreeType;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

@PluginDescriptor(
	name = "ggbot v4",
	description = "Bottin' around the world",
	tags = {"bot"},
	enabledByDefault = false
)
@PluginDependency(XpTrackerPlugin.class)
@Slf4j
public class BotPlugin extends Plugin
{
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
	private EventBus eventBus;

	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private SkillIconManager skillIconManager;

	private Bot bot;

	private NavigationButton navButton;

	@Getter
	@Nullable
	private Axe axe;

	private boolean isRunning = false;
	@Getter
	private LocalPoint startPosition = null;
	@Getter
	private GameObject treeTarget = null;
	@Getter
	private GameObject bankTarget = null;
	@Getter
	private BotState state = BotState.Idle;

	private Script script;
	private final TaskExecutor executor = new TaskExecutor();


	@Getter
	private final Map<Long, GameObject> treeObjects = new HashMap<>();

	public void onBankTestClick() {
//		if(bankTarget != null) {
//			interact(bankTarget, MenuAction.GAME_OBJECT_SECOND_OPTION);
//		}

		bankDepositAll(0);
	}

	@Provides
	BotConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BotConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		bot = new Bot(client, clientThread, eventBus);

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

		client.setPrintMenuActions(true);
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

		if(!isRunning)
			return;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		bot.onGameTick(gameTick);

		if(!isRunning)
			return;

		Player player = client.getLocalPlayer();
		switch(state) {
			case Idle: {
				if(player.isMoving()) {
					state = BotState.Moving;
					System.out.println("Player started moving...");
				} else {
					if (isInventoryFull()) {
						treeTarget = null;
						state = BotState.Banking;
						System.out.println("Inventory full, going to the bank...");
						break;
					}

					if (treeTarget == null) {
						if (treeObjects.size() > 0) {
							List<GameObject> trees = new ArrayList<>(treeObjects.values());
							trees.removeIf(object -> TreeType.of(object.getId()) == null);

							System.out.println(treeObjects.size() + " trees around");

							GameObject tree = trees.stream().min(Comparator.comparing((GameObject a) -> a.getLocalLocation().distanceTo(startPosition))).orElseThrow();
							treeTarget = tree;

							System.out.println("Found new target at " + tree.getLocalLocation().getSceneX() + ", " + tree.getLocalLocation().getSceneY());

							interact(tree, MenuAction.GAME_OBJECT_FIRST_OPTION);
						} else {
							System.out.println("No trees around.");
						}
					} else {
						// Probably levelup or something...
						System.out.println("Did I go levelup? Target is not null yet, retrying old target...");
						interact(treeTarget, MenuAction.GAME_OBJECT_FIRST_OPTION);
						break;
					}

					System.out.println("Player started idling...");
				}
			} break;

			case Moving: {
				if (!player.isMoving()) {
					state = BotState.Idle;
				}
			} break;

			case Woodcutting: {

			} break;

			case Banking: {
				if(bankTarget == null || !isInventoryFull()) {
					System.out.println("Bank unavailable or inventory not full.");
					state = BotState.Idle;
					break;
				}
				if(player.isMoving()) {
					// Probably moving to bank...
					System.out.println("Player is moving to bank");
				} else if(client.getItemContainer(InventoryID.BANK) != null){
					// Banking
					Item[] items = client.getItemContainer(InventoryID.INVENTORY).getItems();
					for(int slot = 0; slot < items.length; slot++) {
						Item item = items[slot];

						if(Axe.byItemId(item.getId()) == null) {
							bankDepositAll(slot);
						}
//						interact();
					}
				} else {
					interact(bankTarget, MenuAction.GAME_OBJECT_SECOND_OPTION);
					System.out.println("Going to bank");
				}

			} break;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event) {
		if(event.getMenuAction().getId() == MenuAction.EXAMINE_OBJECT.getId()) {
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
				System.out.println("Go hug a " + event.getMenuTarget() + "");

				System.out.println("Tree Object at " + gameObject.getSceneMinLocation().getX() + ", " + gameObject.getMinimapLocation().getY());
			} else if(Text.removeTags(event.getMenuTarget()).equalsIgnoreCase("bank booth")) {
				bankTarget = gameObject;

				System.out.println("Setting " + gameObject.getName() + " (" + gameObject.getHash() + ") at " + gameObject.getSceneMinLocation().getX() + ", " + gameObject.getMinimapLocation().getY() + " as bank spot");
			}
		}
	}

	@Subscribe
	private void onItemContainerChanged(final ItemContainerChanged event)
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
		for(Item item : container.getItems()) {
			if(item != null && item.getId() != -1) {
				itemCount ++;
			}
		}
		return itemCount;
	}

	boolean isInventoryFull() {
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
		if(treeTarget != null && event.getGameObject().getHash() == treeTarget.getHash()) {
			treeTarget = null;
			state = BotState.Idle;
			System.out.println("Target disappeared, idling...");
		}

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
				treeTarget = null;
				break;
			case LOGGED_IN:
				break;
		}
	}

	@Subscribe
	public void onAnimationChanged(final AnimationChanged event)
	{
		Player local = client.getLocalPlayer();

		if (local == null || event.getActor() != local)
		{
			return;
		}

		int animId = local.getAnimation();
		if (animId == AnimationID.IDLE) {
			state = BotState.Idle;
			System.out.println("Player idles...");
		} else {
			// Bot started woodcutting

			Axe axe = Axe.byAnimId(animId);
			if (axe != null)
			{
				this.axe = axe;
				state = BotState.Woodcutting;
				System.out.println("Player went woodcutting...");
			} else {
				System.out.println("No idea what he's doing..");
			}
		}

	}

	public void onActionClick() {
		if(!isRunning) {
			startPosition = new LocalPoint(
					client.getLocalPlayer().getLocalLocation().getX(),
					client.getLocalPlayer().getLocalLocation().getY()
			);

			System.out.println("Started botting at " + startPosition.getSceneX() + ", " + startPosition.getSceneY());

			state = BotState.Idle;

			isRunning = true;
		} else {
			isRunning = false;
		}
	}

	// |MenuAction|: MenuOption=Deposit-All MenuTarget=<col=ff9040>Logs</col> Id=8 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=0 Param1=983043 CanvasX=599 CanvasY=332
	// |MenuAction|: MenuOption=Deposit-All MenuTarget=<col=ff9040>Logs</col> Id=8 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=1 Param1=983043 CanvasX=654 CanvasY=335
	// |MenuAction|: MenuOption=Deposit-All MenuTarget=<col=ff9040>Logs</col> Id=8 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=15 Param1=983043 CanvasX=723 CanvasY=443
	public void bankDepositAll(int slot) {
		int param1 = WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId();
		int param0 = slot;
		int objectId = 8;
		int action = MenuAction.CC_OP_LOW_PRIORITY.getId();

		Widget widget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
		Point screenPosition;
		if(widget != null) {
			Widget item = widget.getChild(slot);
			if(item != null) {
				screenPosition = new Point(
						(int)item.getBounds().getCenterX(),
						(int)item.getBounds().getCenterY()
				);
			} else {
				screenPosition = new Point(
						widget.getCanvasLocation().getX() + (int)(Math.floor(Math.random() * widget.getWidth())),
						widget.getCanvasLocation().getY() + (int)(Math.floor(Math.random() * widget.getHeight()))
				);
			}
		} else {
			screenPosition = new Point(
					(int)Math.floor(Math.random() * client.getViewportWidth()),
					(int)Math.floor(Math.random() * client.getViewportHeight())
			);
		}

		interact(objectId, action, param0, param1, screenPosition.getX(), screenPosition.getY());
	}

	public void interact(GameObject object, MenuAction action) {
		Shape hull = object.getConvexHull();
		Point screenPosition;
		if(hull != null) {
			screenPosition = new Point((int)hull.getBounds().getCenterX(), (int)hull.getBounds().getCenterY());
		} else {
			screenPosition = new Point(
					(int)Math.floor(Math.random() * client.getViewportWidth()),
					(int)Math.floor(Math.random() * client.getViewportHeight())
			);
		}

		interact(object.getId(), action.getId(), object.getSceneMinLocation().getX(), object.getSceneMinLocation().getY(), screenPosition.getX(), screenPosition.getY());
	}

	public void interact(int objectId, int actionId, int param0, int param1, int mouseX, int mouseY) {
	clientThread.invoke(() -> {
		client.setMouseIdleTicks(0);
		client.getPacketWriter().sendClickPacket(mouseX, mouseY, (long)(Math.floor(Math.random() * 400)), 1);
		client.sendMenuAction(
				param0,
				param1,
				actionId,
				objectId,
				"",
				"",
				mouseX,
				mouseY
		);
	});
}
}
