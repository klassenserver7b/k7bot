/* (C)2026 */
package de.klassenserver7b.k7bot.commands.slash.util;

import org.jetbrains.annotations.NotNull;

import de.klassenserver7b.k7bot.commands.types.GuildSlashCommand;
import de.klassenserver7b.k7bot.database.dao.ReactRolesDAO;
import de.klassenserver7b.k7bot.util.CommandUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ReactRolesSlashCommand implements GuildSlashCommand {

	@Override
	public void performGuildSlashCommand(@NotNull SlashCommandInteraction event, @NotNull Guild guild,
			@NotNull Member m) {

		InteractionHook hook = event.deferReply(true).complete();

		if (m.hasPermission(Permission.MANAGE_ROLES)) {

			OptionMapping channel = CommandUtils.getRequiredOption(event, "channel");
			OptionMapping messageid = CommandUtils.getRequiredOption(event, "messageid");
			OptionMapping emoteop = CommandUtils.getRequiredOption(event, "emoteid-oder-utfemote");
			OptionMapping roleop = CommandUtils.getRequiredOption(event, "role");

			GuildMessageChannel tc = channel.getAsChannel().asGuildMessageChannel();
			Role role = roleop.getAsRole();
			long MessageId = messageid.getAsLong();

			Emoji emote = Emoji.fromFormatted(emoteop.getAsString());

			tc.addReactionById(MessageId, emote).queue();

			new ReactRolesDAO().addRole(tc.getGuild().getIdLong(), tc.getIdLong(), MessageId, emote.getFormatted(),
					role.getIdLong()).join();

			hook.sendMessage("Reactrole was successfull set for Message: " + MessageId).queue();

		} else {
			PermissionError.onPermissionError(new GenericMessageSendHandler(event.getChannel().asGuildMessageChannel()),
					m);
		}
	}

	@Override
	public @NotNull SlashCommandData getCommandData() {
		return Commands.slash("reactrole", "Erstellt eine Reactionrole mit den übermittelten Parametern")
				.addOption(OptionType.CHANNEL, "channel", "Der Channel in dem die Message ist", true)
				.addOption(OptionType.STRING, "messageid",
						"Die MessageId der Message an die die Reaction angefügt werden soll", true)
				.addOption(OptionType.STRING, "emoteid-oder-utfemote",
						"Die EmoteId des Emotes bzw. das UTF8 Emoji auf das die Rolle registriert" + " werden soll",
						true)
				.addOption(OptionType.ROLE, "role",
						"Die Rolle die zugewiesen werden soll -  stelle sicher: Rechte und Rolle"
								+ " des Bots > Rechte der Rolle",
						true);
	}
}
