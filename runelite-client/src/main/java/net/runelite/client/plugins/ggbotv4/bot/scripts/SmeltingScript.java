package net.runelite.client.plugins.ggbotv4.bot.scripts;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.events.ClientTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.ggbotv4.bot.Bot;
import net.runelite.client.plugins.ggbotv4.bot.GameObjectManager;
import net.runelite.client.plugins.ggbotv4.bot.InventoryManager;
import net.runelite.client.plugins.ggbotv4.bot.Script;
import net.runelite.client.plugins.ggbotv4.bot.task.Task;
import net.runelite.client.plugins.ggbotv4.bot.task.util.TaskUtil;

import java.awt.*;

import static net.runelite.api.Constants.CLIENT_TICK_LENGTH;

@Slf4j
public class SmeltingScript implements Script {
    private final SmeltingRecipe recipe;
    private final long furnaceTarget;
    private final Client client;

    private String overheadText = "";

    public SmeltingScript(Bot bot, long hash, SmeltingRecipe recipe) {
        this.client = bot.getClient();
        this.furnaceTarget = hash;
        this.recipe = recipe;
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        final Player local = client.getLocalPlayer();
        assert(local != null);

        if(local.getOverheadCycle() == 0 && !overheadText.isEmpty()) {
            local.setOverheadText(overheadText);
            local.setOverheadCycle(2000 / CLIENT_TICK_LENGTH);

            overheadText = "";
        }
    }

    @Override
    public String getName() {
        return "Smelting";
    }

    @Override
    public Task evaluate(Bot bot) {
        final InventoryManager inventory = bot.getInventory();
        final GameObjectManager gameObjects = bot.getGameObjects();
        final Player local = client.getLocalPlayer();
        assert(local != null);

        switch(bot.getState()) {
            case Idle: {
                if ((recipe.getMould() == -1 || inventory.contains(recipe.getMould())) &&
                        inventory.contains(recipe.getIngredients())) {
                    log.info("Got all the ingredients, going to smelt.");

                    // Got the mould and ingredients.
                    return startSmelting(bot);
                } else {
                    log.info("Don't have all the ingredients, going to bank.");

                    if(gameObjects.get(bot.getBankTarget()) != null) {
                        return TaskUtil.interact(gameObjects.get(bot.getBankTarget()), MenuAction.GAME_OBJECT_SECOND_OPTION);
                    } else {
                        overheadText = "Bank is not in current scene!";
                    }
                }
                //overheadText = "Ladadi ladada~";
            } break;

            case Banking: {
                int inventorySize = 28 - (recipe.getMould() != -1 ? 1 : 0);
                int quantityPerIngredient = inventorySize / recipe.getIngredients().length;

                return Task.chain(
                        // Deposit all except the mould.
                        TaskUtil.Bank.depositItems(id -> id == recipe.getMould()),
                        // If there's no mould, grab it from the bank.
                        Task.when(bot_ -> recipe.hasMould() && !bot_.getInventory().contains(recipe.getMould()))
                                        .then(TaskUtil.Bank.withdrawItems(1, recipe.getMould())),
                        // Withdraw all ingredients.
                        TaskUtil.Bank.withdrawItems(quantityPerIngredient, recipe.getIngredients()),
                        // ...
                        startSmelting(bot)
                );
            }
        }

        return null;
    }

    @Override
    public void renderDebug(Graphics2D graphics, Bot bot) {

    }

    private Task startSmelting(Bot bot) {
        final GameObjectManager gameObjects = bot.getGameObjects();

        return Task.chain(
                // |MenuAction|: MenuOption=Smelt MenuTarget=<col=ffff>Furnace Id=16469 Opcode=GAME_OBJECT_SECOND_OPTION/4 Param0=62 Param1=59 CanvasX=71 CanvasY=135
                TaskUtil.interact(gameObjects.get(furnaceTarget), MenuAction.GAME_OBJECT_SECOND_OPTION),
                TaskUtil.awaitWidget(WidgetInfo.MULTI_SKILL_MENU_OPTION_1.getId()),
                // |MenuAction|: MenuOption=Smelt MenuTarget=<col=ff9040>Bronze bar</col> Id=1 Opcode=CC_OP/57 Param0=-1 Param1=MULTI_SKILL_MENU_OPTION_1 CanvasX=60 CanvasY=643
                // |MenuAction|: MenuOption=Smelt MenuTarget=<col=ff9040>Bronze bar</col> Id=1 Opcode=CC_OP/57 Param0=-1 Param1=MULTI_SKILL_MENU_OPTION_1 CanvasX=43 CanvasY=653
                // |MenuAction|: MenuOption=Smelt MenuTarget=<col=ff9040>Iron bar</col> Id=1 Opcode=CC_OP/57 Param0=-1 Param1=MULTI_SKILL_MENU_OPTION_2 CanvasX=108 CanvasY=649
                TaskUtil.clickWidget(recipe.getButtonId(), MenuAction.CC_OP, 1, -1)
        );
    }
}
