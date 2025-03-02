package de.klassenserver7b.k7bot.commands.slash;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Shutdownslashcommand implements TopLevelSlashCommand {

	@Override
	public void performSlashCommand(SlashCommandInteraction event) {

		Member m = event.getMember();
		GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();

		if (m.getIdLong() == K7Bot.getInstance().getOwnerId()) {

			EmbedBuilder build = EmbedUtils.getBuilderOf(Color.orange,
					"# Bot is shutting down! #" + "\n \n Requested by @" + event.getMember().getEffectiveName());

			event.replyEmbeds(build.build()).complete().deleteOriginal().completeAfter(10, TimeUnit.SECONDS);

			K7Bot.getInstance().setExit(true);
			K7Bot.getInstance().getShutdownThread().onShutdown();
			return;

		}

		PermissionError.onPermissionError(m, channel);

	}

	@Override
	public @NotNull SlashCommandData getCommandData() {
		return Commands.slash("shutdown", "fährt den Bot herunter")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
	}

}
