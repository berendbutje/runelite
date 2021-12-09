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
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ggbotv4.bot.Bot;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.image.BufferedImage;

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

	@Provides
	BotConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BotConfig.class);
	}

	@Inject
	@Getter
	private Bot bot;

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
		clientToolbar.removeNavigation(navButton);

		client.getLogger().info("Ggbot shutdown.");
	}

    public void onTestClick() {
		clientThread.invoke(() -> {
			ItemContainer bank = client.getItemContainer(InventoryID.BANK);
			Widget bankWidget = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			for(int i = 0; i < bank.size(); i++) {
				Item item = bank.getItem(i);
				Widget child = bankWidget.getChild(i);

				if(item != null) {
					System.out.println("Item (" + client.getItemDefinition(item.getId()).getName() + ") at " + i + " = " + Text.removeTags(child.getName()));
				}

			}
		});
    }
}
