/*
 * Copyright (c) 2017, honeyhoney <https://github.com/honeyhoney>
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
import net.runelite.api.ggbot.BotProfile;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;

class BotOverlay extends OverlayPanel
{
	private final Client client;
	private final BotPlugin plugin;

	@Inject
	private BotOverlay(Client client, BotPlugin plugin)
	{
		super(plugin);

		this.client = client;

		setPosition(OverlayPosition.TOP_CENTER);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{

		panelComponent.getChildren().add(TitleComponent.builder()
				.text("Moeders")
				.color(Color.WHITE)
				.build());

		panelComponent.getChildren().add(LineComponent.builder()
				.left("State: ")
						.right(plugin.getBot().getState().name())
				.build());

		BotProfile profile = plugin.getBot().getActiveProfile();
		if(profile != null) {
			Duration timePlayed = Duration.between(profile.getLastLogin(), LocalDateTime.now());

			panelComponent.getChildren().add(LineComponent.builder()
					.left("Session: ")
					.right(String.format("%02d:%02d:%02d", timePlayed.toHours(), timePlayed.toMinutesPart(), timePlayed.toSecondsPart()))
					.build());

			Duration last24hours = Duration.ofSeconds(0);
			Duration total = Duration.ofSeconds(0);
			for(LocalDateTime[] session : profile.getSessions()) {
				assert(session.length == 2);

				LocalDateTime start = session[0];
				LocalDateTime end = session[1];

				Duration timeBetween = Duration.between(start, LocalDateTime.now());
				total = total.plus(Duration.between(start, end));
				if(timeBetween.toHours() > 24) {
					// Only check last 24 hours.
					continue;
				}

				last24hours = last24hours.plus(Duration.between(start, end));
			}

			last24hours = last24hours.plus(timePlayed);
			total = total.plus(timePlayed);

			panelComponent.getChildren().add(LineComponent.builder()
					.left("Last 24 hours: ")
					.right(String.format("%02d:%02d:%02d", last24hours.toHours(), last24hours.toMinutesPart(), last24hours.toSecondsPart()))
					.build());

			panelComponent.getChildren().add(LineComponent.builder()
					.left("Total: ")
					.right(String.format("%02d:%02d:%02d", total.toHours(), total.toMinutesPart(), total.toSecondsPart()))
					.build());

		}


		return super.render(graphics);
	}
}
