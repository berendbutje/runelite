package net.runelite.client.plugins.ggbotv4.bot;

import com.google.inject.Inject;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.ggbot.BotProfile;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.FishingSpot;
import net.runelite.client.plugins.ggbotv4.bot.scripts.*;
import net.runelite.client.plugins.ggbotv4.bot.task.Task;
import net.runelite.client.plugins.ggbotv4.bot.task.TaskExecutor;
import net.runelite.client.plugins.ggbotv4.plugin.BotState;
import net.runelite.client.plugins.ggbotv4.util.Axe;
import net.runelite.client.plugins.ggbotv4.util.InteractionUtil;
import net.runelite.client.plugins.ggbotv4.util.TreeType;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

import static net.runelite.api.Constants.CLIENT_TICK_LENGTH;

public class Bot {
    @Getter private final Client client;
    @Getter private final GameObjectManager gameObjects;
    @Getter private final InventoryManager inventory;
    @Getter private final NPCManager npcs;
    @Getter private final TileItemManager tileItems;

    private final EventBus eventBus;
    private final TaskExecutor executor = new TaskExecutor();

    @Getter private Script script;
    @Getter private long bankTarget = -1;
    @Getter private BotState state = BotState.Idle;

    @Inject
    Bot(
            Client client,
            EventBus eventBus,
            GameObjectManager gameObjects,
            InventoryManager inventory,
            NPCManager npcs,
            TileItemManager tileItems
    ) {
        this.client = client;
        this.eventBus = eventBus;

        this.gameObjects = gameObjects;
        this.inventory = inventory;
        this.npcs = npcs;
        this.tileItems = tileItems;

        this.eventBus.register(this);
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        clickedRunThisGameTick = false;

        Player local = client.getLocalPlayer();
        if(local != null) {
            int animId = local.getAnimation();

            if (animId == AnimationID.IDLE) {
                if(local.isMoving()) {
                    final int ACTIVATE_THRESHOLD = 90;
                    final int DEACTIVATE_THRESHOLD = 70;

                    if(script != null) {
                        boolean isRunning = client.getVar(VarPlayer.IS_RUNNING) > 0;
                        // Only change when a script is running.
                        if(!isRunning && client.getEnergy() >= ACTIVATE_THRESHOLD) {
                            InteractionUtil.executeMenuAction(1, MenuAction.CC_OP.getId(), -1, WidgetInfo.MINIMAP_TOGGLE_RUN_ORB.getId(), 0, 0);
                        } else if(isRunning && client.getEnergy() <= DEACTIVATE_THRESHOLD) {
                            InteractionUtil.executeMenuAction(1, MenuAction.CC_OP.getId(), -1, WidgetInfo.MINIMAP_TOGGLE_RUN_ORB.getId(), 0, 0);
                        }
                    }

                    state = BotState.Moving;
                } else if(client.getWidget(WidgetInfo.BANK_CONTAINER) != null) {
                    state = BotState.Banking;
                } else {
                    state = BotState.Idle;
                }
            } else {
                Axe axe = Axe.byAnimId(animId);
                if (axe != null) {
                    state = BotState.Woodcutting;
                } else if(animId == AnimationID.SMITHING_SMELTING) {
                    state = BotState.Smelting;
                }
                else {
                    state = BotState.Unknown;
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

    private boolean clickedRunThisGameTick = false;

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        Player local = client.getLocalPlayer();
        if(local == null)
            return;

        if(client.getMouseIdleTicks() > 14000) {
            client.setMouseIdleTicks(0);

            local.setOverheadText("AFK prevented.");
            local.setOverheadCycle(1000 / CLIENT_TICK_LENGTH);
        }

        if(local.getOverheadCycle() == 0 && script == null) {
            local.setOverheadText("Under player control: " + getState());
            local.setOverheadCycle(1);
        }
    }

    private GameState previousState = GameState.UNKNOWN;
    private String ACTIVE_USERNAME = "";

    public BotProfile getActiveProfile() {
        if(ACTIVE_USERNAME.isEmpty())
            return null;

        return BotProfile.get(ACTIVE_USERNAME);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        client.getLogger().info("Current gamestate: {}", event.getGameState());

        if (event.getGameState() == GameState.LOGIN_SCREEN)
        {
            client.getLogger().info("Current login state: {}", client.getLoginState());
            // If you want to relog, do it here before resetting ACTIVE_USERNAME.

            if(getActiveProfile() != null) {
                Duration playTime = getActiveProfile().endSession();
                client.getLogger().info("Saving session for {} playing {} hours, {} minutes, and {} seconds.",
                        ACTIVE_USERNAME, playTime.toHours(), playTime.toMinutesPart(), playTime.toSecondsPart());

                ACTIVE_USERNAME = "";
            }

            if(client.getLoginState() == LoginState.WELCOME) {
                client.setLoginState(LoginState.INPUT_CREDENTIALS);
                client.setGameState(GameState.LOGIN_SCREEN);
            } else if(client.getLoginState() == LoginState.INPUT_CREDENTIALS) {
                switch(client.getLoggedInResponse()) {
                    case 0: {
                        // Auto login
                        String rememberedUsername = client.getPreferences().getRememberedUsername();
                        if(rememberedUsername != null && !rememberedUsername.isEmpty()) {
                            BotProfile profile = BotProfile.get(rememberedUsername);

                            if(profile.getPassword() != null && !profile.getPassword().isEmpty()) {
                                login(rememberedUsername, profile.getPassword());
                            }
                        }
                    } break;

                    case 1: {
                        // Invalid credentials entered.
                    } break;

                    case 2: {
                        // Already logged in.
                    } break;

                }
                client.setLoginResponseString("ggbot v4.0", "Logging in saves your account", "for ease of use later.");

            }

            if(client.getLoginState() == LoginState.ACCOUNT_BLOCKED) {
                if(client.getAccountBlockedReason() == 0) {
                    // Account disabled.
                    client.getLogger().error("Account {} is disabled!", client.getUsername());

                    BotProfile.get(client.getUsername()).setBlocked(true).save();

                    client.setLoginResponseString("ggbot v4.0", "Uh oh, " + client.getUsername(), " is disabled.");
                    client.setLoginState(LoginState.INPUT_CREDENTIALS);
                } else if(client.getAccountBlockedReason() == 1) {
                    // Account locked, suspected stolen.
                    client.getLogger().error("Account {} is locked!", client.getUsername());

                    BotProfile.get(client.getUsername()).setLocked(true).save();

                    client.setLoginResponseString("ggbot v4.0", "Uh oh, " + client.getUsername(), " is locked.");
                    client.setLoginState(LoginState.INPUT_CREDENTIALS);
                }
            }
        } else if(event.getGameState() == GameState.LOGGED_IN) {
            client.getLogger().info("LOGGED_IN: Logging in with {} and {}", client.getUsername(), client.getPassword());

            BotProfile last = getActiveProfile();
            // Check if there was already an active profile. GameState.LOGGED_IN gets
            // triggered by region changes and world hops.
            if (last == null) {
                // New user logged in.
                ACTIVE_USERNAME = client.getUsername();
                getActiveProfile().setPassword(client.getPassword());

                Duration total = Duration.ofSeconds(0);
                for(LocalDateTime[] session : getActiveProfile().getSessions()) {
                    assert(session.length == 2);

                    LocalDateTime start = session[0];
                    LocalDateTime end = session[1];

                    Duration timeBetween = Duration.between(start, LocalDateTime.now());
                    if(timeBetween.toHours() > 24) {
                        // Only check last 24 hours.
                        continue;
                    }

                    total = total.plus(Duration.between(start, end));
                }

                client.getLogger().info(
                        "{} has played {} hours, {} minutes, and {} seconds the last 24 hours.",
                        client.getUsername(), total.toHours(), total.toMinutesPart(), total.toSecondsPart()
                );

                getActiveProfile().startSession(client.getWorld());
            }
        }

        previousState = event.getGameState();
    }

    @Subscribe
    public void onForceDisconnect(ForceDisconnect event) {
        String reason = "Unknown";
        switch(event.getReason()) {
            case 1:
                reason = "Most probably 6 hour limit";
                break;

            case 2:
                reason = "Game was updated!";
                break;

            case 3:
                reason = "Ggbot disconnect";
                break;
        }

        if(getActiveProfile() != null) {
            // If the session was not ended yet, end it.
            Duration playTime = getActiveProfile().endSession();
            client.getLogger().info("Saving session for {} playing {} hours, {} minutes, and {} seconds.",
                    ACTIVE_USERNAME, playTime.toHours(), playTime.toMinutesPart(), playTime.toSecondsPart());

            ACTIVE_USERNAME = "";
        }

        client.getLogger().warn(
                "Client was forcefully disconnected from the server because {}!",
                reason
        );
    }

    private static final int MENU_BOT_CANCEL = 1;
    private static final int MENU_BOT_WOODCUTTING = 2;
    private static final int MENU_SET_BANK_BOOTH = 3;
    private static final int MENU_BOT_ATTACKING = 4;
    private static final int MENU_BOT_FISHING = 5;
    private static final int MENU_BOT_IDLING = 6;
    private static final int MENU_BOT_SMELTING = 100;
    private static final int MENU_BOT_SMELTING_END = MENU_BOT_SMELTING + SmeltingRecipe.values().length;

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if(script != null) {
            if(event.getMenuAction().getId() == MenuAction.WALK.getId()) {
                MenuEntry[] menuEntries = client.getMenuEntries();
                menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

                MenuEntry menuEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();

                menuEntry.setId(MENU_BOT_CANCEL);
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

            final TileObject tileObject = TileObjectUtil.findTileObject(tile, event.getIdentifier());
            if (tileObject == null) {
                return;
            }

            TreeType tree = TreeType.of(event.getId());
            if (tree != null) {
                MenuEntry[] menuEntries = client.getMenuEntries();
                menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

                MenuEntry menuEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();

                menuEntry.setId(MENU_BOT_WOODCUTTING);
                menuEntry.setOption("Bot");
                menuEntry.setTarget(ColorUtil.wrapWithColorTag(Text.removeTags(event.getTarget()), Color.ORANGE));
                menuEntry.setType(MenuAction.PRIO_GGBOT.getId());

                menuEntry.setParam0((int) (tileObject.getHash() >> 32));
                menuEntry.setParam1((int) tileObject.getHash());

                client.setMenuEntries(menuEntries);
            } else if(Text.removeTags(event.getTarget()).equalsIgnoreCase("bank booth")) {
                MenuEntry[] menuEntries = client.getMenuEntries();
                menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

                MenuEntry menuEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();

                menuEntry.setId(MENU_SET_BANK_BOOTH);
                menuEntry.setOption("Set bank spot");
                menuEntry.setTarget(ColorUtil.wrapWithColorTag(Text.removeTags(event.getTarget()), Color.ORANGE));
                menuEntry.setType(MenuAction.PRIO_GGBOT.getId());

                menuEntry.setParam0((int) (tileObject.getHash() >> 32));
                menuEntry.setParam1((int) tileObject.getHash());

                client.setMenuEntries(menuEntries);
            } else if(ArrayUtils.contains(tileObject.getActions(), "Smelt")) {
                for(int i = 0; i < SmeltingRecipe.values().length; i++) {
                    SmeltingRecipe recipe = SmeltingRecipe.values()[i];

                    MenuEntry[] menuEntries = client.getMenuEntries();
                    menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

                    MenuEntry menuEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();

                    menuEntry.setId(MENU_BOT_SMELTING + i);
                    menuEntry.setOption("Bot " + client.getItemDefinition(recipe.getResult()).getName() + " at");
                    menuEntry.setTarget(ColorUtil.wrapWithColorTag(Text.removeTags(event.getTarget()), Color.ORANGE));
                    menuEntry.setType(MenuAction.PRIO_GGBOT.getId());

                    menuEntry.setParam0((int) (tileObject.getHash() >> 32));
                    menuEntry.setParam1((int) tileObject.getHash());

                    client.setMenuEntries(menuEntries);
                }
            }

        } else if(event.getMenuAction().getId() == MenuAction.EXAMINE_NPC.getId()) {
            NPC npc = client.getCachedNPCs()[event.getId()];
            FishingSpot spot = FishingSpot.findSpot(npc.getId());

            MenuEntry[] menuEntries = client.getMenuEntries();
            menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

            MenuEntry menuEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();
            menuEntry.setOption("Bot");
            menuEntry.setTarget(ColorUtil.wrapWithColorTag(Text.removeTags(event.getTarget()), Color.ORANGE));
            menuEntry.setType(MenuAction.PRIO_GGBOT.getId());
            menuEntry.setParam0(event.getId());

            if(spot != null) {
                menuEntry.setId(MENU_BOT_FISHING);

                client.setMenuEntries(menuEntries);
            } else if(ArrayUtils.contains(npc.getTransformedComposition().getActions(), "Attack")) {
                menuEntry.setId(MENU_BOT_ATTACKING);

                client.setMenuEntries(menuEntries);
            }
        } else if(event.getMenuAction().getId() == MenuAction.WALK.getId()) {
            MenuEntry[] menuEntries = client.getMenuEntries();
            menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

            MenuEntry menuEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();

            menuEntry.setId(MENU_BOT_IDLING);
            menuEntry.setOption("Start");
            menuEntry.setTarget(ColorUtil.wrapWithColorTag("Idling", Color.ORANGE));
            menuEntry.setType(MenuAction.PRIO_GGBOT.getId() + 2000);

            client.setMenuEntries(menuEntries);
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        final Player local = client.getLocalPlayer();
        assert(local != null);

        if(event.getMenuAction().getId() == MenuAction.PRIO_GGBOT.getId()) {
            if(event.getId() == MENU_BOT_CANCEL) {
                local.setOverheadText("Bot stopped because of interaction from the user.");
                local.setOverheadCycle(2000 / CLIENT_TICK_LENGTH);

                this.stopScript();
            } else if(event.getId() == MENU_BOT_WOODCUTTING) {
                long hash = (((long)event.getParam0()) << 32) | (event.getParam1() & 0xffffffffL);
                GameObject gameObject = gameObjects.get(hash);

                TreeType tree = TreeType.of(gameObject.getId());
                if(tree != null) {
                    this.startScript(new WoodcuttingScript(this, tree));

                    local.setOverheadText("Started woodcutting script");
                    local.setOverheadCycle(2000 / CLIENT_TICK_LENGTH);
                }
            } else if(event.getId() == MENU_BOT_FISHING) {
                NPC npc = npcs.get(event.getParam0());
                FishingSpot spot = FishingSpot.findSpot(npc.getId());
                if(spot != null) {
                    this.startScript(new FishingScript(this));

                    local.setOverheadText("Started fishing script");
                    local.setOverheadCycle(2000 / CLIENT_TICK_LENGTH);
                }
            } else if(event.getId() == MENU_BOT_ATTACKING) {
                NPC npc = npcs.get(event.getParam0());
                this.startScript(new FightingScript(this, npc.getId()));

                local.setOverheadText("Started fighting script");
                local.setOverheadCycle(2000 / CLIENT_TICK_LENGTH);
            } else if(event.getId() == MENU_SET_BANK_BOOTH) {
                long hash = (((long)event.getParam0()) << 32) | (event.getParam1() & 0xffffffffL);
                GameObject gameObject = gameObjects.get(hash);
                bankTarget = gameObject.getHash();

                local.setOverheadText("Bank target set!");
                local.setOverheadCycle(1000 / CLIENT_TICK_LENGTH);
            }  else if(event.getId() == MENU_BOT_IDLING) {
                this.startScript(new IdleScript(this));
                local.setOverheadText("Started idling!");
                local.setOverheadCycle(1000 / CLIENT_TICK_LENGTH);
            } else if(event.getId() >= MENU_BOT_SMELTING && event.getId() < MENU_BOT_SMELTING_END) {
                SmeltingRecipe recipe = SmeltingRecipe.values()[event.getId() - MENU_BOT_SMELTING];
                long hash = (((long)event.getParam0()) << 32) | (event.getParam1() & 0xffffffffL);

                System.out.printf("%X%n", hash);

                this.startScript(new SmeltingScript(this, hash, recipe));
                local.setOverheadText("Started smelting " + recipe.name());
                local.setOverheadCycle(2000 / CLIENT_TICK_LENGTH);
            }
        }
    }

    public void startScript(Script script) {
        this.executor.clear();
        this.script = script;
        this.eventBus.register(script);
    }

    public void stopScript() {
        this.eventBus.unregister(script);
        this.executor.clear();
        this.script = null;
    }

    public void logout() {
        client.forceDisconnect(3);
        client.setLoginResponseString("ggbot v4.0", "Disconnected by the bot.", "");
    }

    public void login(String username, String password) {
        if(client.getGameState() == GameState.LOGGED_IN) {
            this.logout();
        }

        client.setUsername(username);
        client.setPassword(password);
        client.setLoginResponseString("ggbot v4.0", "Automatically logging in with", client.getUsername());
        client.setGameState(GameState.LOGGING_IN);
    }
}
