package net.runelite.client.plugins.ggbotv4.bot;

import lombok.Getter;
import net.runelite.api.AnimationID;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.ggbotv4.bot.task.TaskExecutor;
import net.runelite.client.plugins.ggbotv4.plugin.BotState;
import net.runelite.client.plugins.ggbotv4.util.Axe;

public class Bot {
    private final Client client;
    private final ClientThread clientThread;
    private final EventBus eventBus;

    private Script script;

    @Getter
    private BotState state = BotState.Idle;

    private final TaskExecutor executor = new TaskExecutor();

    public Bot(
            Client client,
            ClientThread clientThread,
            EventBus eventBus
    ) {
        this.client = client;
        this.clientThread = clientThread;
        this.eventBus = eventBus;

        this.eventBus.register(this);
    }

    @Subscribe
    public void onGameTick(final GameTick gameTick) {
        System.out.println("Game Tick");

        Player local = client.getLocalPlayer();
        if(local != null) {
            if(local.isMoving() && state == BotState.Idle) {
                state = BotState.Moving;
            } else if(!local.isMoving() && state == BotState.Moving) {
                state = BotState.Idle;
            }
        }

        if(executor.getTask() != null) {
            executor.execute();
        } else {
            if (script != null) {
                executor.setTask(script.evaluate(this));
            }
        }
    }

    @Subscribe
    public void onClientTick(final ClientTick clientTick) {
        System.out.println("Client Tick");

    }

    public void start(Script script) {
        executor.setTask(null);
        this.script = script;
    }

    public void stop() {
        executor.setTask(null);
        this.script = null;
    }

    @Subscribe
    public void onAnimationChanged(final AnimationChanged event)
    {
        Player local = client.getLocalPlayer();

        if (local == null || event.getActor() != local)
            return;

        int animId = local.getAnimation();
        if (animId == AnimationID.IDLE) {
            state = BotState.Idle;
        } else {
            Axe axe = Axe.byAnimId(animId);
            if (axe != null) {
                state = BotState.Woodcutting;
            } else {
                System.out.println("No idea what he's doing..");
            }
        }
    }
}
