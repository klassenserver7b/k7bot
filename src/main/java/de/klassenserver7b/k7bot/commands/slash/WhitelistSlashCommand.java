package de.klassenserver7b.k7bot.commands.slash;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WhitelistSlashCommand implements TopLevelSlashCommand {
	private final Logger log = LoggerFactory.getLogger("Whitelist");

	@Override
	public void performSlashCommand(SlashCommandInteraction event) {
		String gamename = event.getOption("ingamename").getAsString();
		String realname = event.getOption("realname").getAsString();
		String dcname = event.getUser().getName();
		Long dcid = event.getUser().getIdLong();

		InteractionHook hook = event.deferReply(true).complete();

		try (ResultSet set = LiteSQL.onQuery("SELECT * FROM ha3users WHERE dcId = ?;", dcid)) {

			if (set.next()) {
				MessageEmbed emb = EmbedUtils.getErrorEmbed("You have already requested Whitelisting").build();
				hook.sendMessageEmbeds(emb).queue();
				return;

			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}

		LiteSQL.onUpdate("INSERT INTO ha3users(ingamename,realname,dcname,dcId,approved) VALUES(?, ?, ?, ?, ?);",
				gamename, realname, dcname, dcid, 3);

		Guild g = K7Bot.getInstance().getShardManager().getGuildById(701341683325075477L);

		if (g != null) {
			GuildMessageChannel requestChannel = g.getTextChannelById(1016819796190445758L);
			EmbedBuilder build = EmbedUtils.getDefault(g.getIdLong());
			build.setTitle(gamename + " requested whitelisting.");
			build.setDescription("**In-Game-Name: **" + gamename + "\n**Real-Name: **" + realname
					+ "\n**Discord-Name: **" + dcname + "\n**Discord-ID: **" + dcid);

			requestChannel.sendMessageEmbeds(build.build()).addActionRow(Button.success("ApproveHA3Request", "Approve"),
					Button.danger("DenyHA3Request", "Deny")).queue();
			hook.sendMessage("Your Request has been received and will now be handled by the mods.").queue();
			return;

		}

		hook.sendMessage(
				"Something went wrong while receiving your data. Please try again in a few minutes. \n Your Data: **In-Game-Name: **"
						+ gamename + "\n**Real-Name: **" + realname + "\n**Discord-Name: **" + dcname
						+ "\n**Discord-ID: **" + dcid)
				.queue();

	}

	@Override
	public @NotNull SlashCommandData getCommandData() {
		return Commands.slash("whitelistadd", "Fragt die Hinzufügung zur Whitelist an.")
				.addOption(OptionType.STRING, "ingamename", "Dein Spielername in Minecraft", true)
				.addOption(OptionType.STRING, "realname",
						"Der Name mit dem du im Talk etc. angesprochen werden willst.", true);
	}

}
