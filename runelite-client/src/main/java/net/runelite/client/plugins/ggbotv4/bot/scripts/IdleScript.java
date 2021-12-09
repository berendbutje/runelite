package net.runelite.client.plugins.ggbotv4.bot.scripts;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.ggbotv4.bot.Bot;
import net.runelite.client.plugins.ggbotv4.bot.Script;
import net.runelite.client.plugins.ggbotv4.bot.task.Task;
import net.runelite.client.plugins.ggbotv4.bot.task.util.TaskUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

public class IdleScript implements Script {
    private final Client client;
    private final WorldService worldService;

    private static final Duration TIME_BEFORE_HOP = Duration.ofHours(4);

    private LocalDateTime lastHop = LocalDateTime.now();
    private boolean hopping = false;

    public IdleScript(Bot bot) {
        this.client = bot.getClient();
        this.worldService = RuneLite.getInjector().getInstance(WorldService.class);

        client.setIsHidingEntities(true);
        client.setOthersHidden(true);
        client.setOthersHidden2D(true);
    }

    @Override
    public void stop() {
        client.setIsHidingEntities(false);
        client.setOthersHidden(false);
        client.setOthersHidden2D(false);
    }

    @Override
    public String getName() {
        return "Idling";
    }

    @Override
    public Task evaluate(Bot bot) {
        Duration timeLoggedIn = Duration.between(lastHop, LocalDateTime.now());Player local = client.getLocalPlayer();
        if(timeLoggedIn.compareTo(TIME_BEFORE_HOP) >= 0 && !hopping) {
            WorldResult worldResult = worldService.getWorlds();

            if(worldResult != null && worldResult.getWorlds() != null) {
                client.getLogger().info("Found {} worlds to hop to!", worldResult.getWorlds().size());

                Optional<World> world = worldResult.getWorlds().stream().filter(w ->
                        w != null && w.getId() != client.getWorld() && !w.getTypes().contains(WorldType.MEMBERS) && !w.getTypes().contains(WorldType.PVP)
                ).max(Comparator.comparingInt(World::getPlayers));

                if(world.isPresent()) {
                    World value = world.get();
                    client.getLogger().info("Hopping to world {}", value.getId());

                    hopping = true;
                    return Task.chain(
                            Task.once(client::openWorldHopper),
                            TaskUtil.awaitWidget(WidgetInfo.WORLD_SWITCHER_LIST),
                            Task.once(() -> client.hopToWorld(value.getId()))
                    );
                } else {
                    lastHop = LocalDateTime.now();
                    hopping = false;

                    client.getLogger().info("Unable to find world to hop to.");
                }
            }
        }

        return null;
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        Duration timeLoggedIn = Duration.between(lastHop, LocalDateTime.now());
        Player local = client.getLocalPlayer();
        assert (local != null);

        Duration left = Duration.from(TIME_BEFORE_HOP).minus(timeLoggedIn);
        if (local.getOverheadCycle() == 0) {
            local.setOverheadText("Hopping in " + String.format("%02d:%02d:%02d", left.toHours(), left.toMinutesPart(), left.toSecondsPart()));
            local.setOverheadCycle(1);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if(event.getGameState() == GameState.LOGGED_IN) {
            lastHop = LocalDateTime.now();
            hopping = false;
        }

    }

    @Override
    public void renderDebug(Graphics2D graphics, Bot bot) {

    }
}
