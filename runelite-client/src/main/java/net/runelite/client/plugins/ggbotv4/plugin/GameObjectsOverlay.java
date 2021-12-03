/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2019, David <Dava96@github.com>
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

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.ggbotv4.util.Axe;
import net.runelite.client.plugins.ggbotv4.util.TreeType;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

class GameObjectsOverlay extends Overlay
{
	private final Client client;
	private final BotConfig config;
	private final ItemManager itemManager;
	private final BotPlugin plugin;

	@Inject
	private GameObjectsOverlay(final Client client, final BotConfig config, final ItemManager itemManager, final BotPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.itemManager = itemManager;
		this.plugin = plugin;
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if(plugin.getScript() != null) {
			plugin.getScript().renderDebug(graphics, plugin);
		}

		final BufferedImage axeIcon;
		Axe axe = Axe.findActive(client);
		if(axe != null) {
			axeIcon = itemManager.getImage(axe.getItemId());
		} else {
			axeIcon = null;
		}

		if(plugin.getBankTarget() != null) {
			Shape poly = plugin.getBankTarget().getConvexHull();
			if(poly != null) {
				OverlayUtil.renderPolygon(graphics, poly, Color.YELLOW);
			}

			BufferedImage stacksOfMoney = itemManager.getImage(8899);
			OverlayUtil.renderImageLocation(client, graphics, plugin.getBankTarget().getLocalLocation(), stacksOfMoney, 120);
		}

		for (GameObject treeObject : plugin.getTreeObjects().values())
		{
			if(TreeType.of(treeObject.getId()) == null)
				continue;

			if (treeObject.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) <= 12)
			{
				if(axeIcon != null) {
					OverlayUtil.renderImageLocation(client, graphics, treeObject.getLocalLocation(), axeIcon, 120);
				}

				Shape poly = treeObject.getConvexHull();
				if(poly != null) {
					OverlayUtil.renderPolygon(graphics, poly, ColorUtil.colorWithAlpha(Color.ORANGE, 255 / 4));
				}

				if(axe == null) {
					String text = "No usable axe";

					Point point = treeObject.getCanvasTextLocation(graphics, text, 75);
					if (point != null) {
						OverlayUtil.renderTextLocation(graphics, point, text, Color.RED);
					}
				}
			}
		}



//		for(Player player : client.getPlayers()) {
//			for(Tile tile : player.getPath()) {
//				OverlayUtil.renderLocalPoint(client, graphics, tile.getLocalLocation(), Color.CYAN);
//			}
//		}
//
//		for(NPC npc : client.getNpcs()) {
//			for(Tile tile : npc.getPath()) {
//				OverlayUtil.renderLocalPoint(client, graphics, tile.getLocalLocation(), Color.CYAN);
//			}
//		}

		return null;
	}
}
