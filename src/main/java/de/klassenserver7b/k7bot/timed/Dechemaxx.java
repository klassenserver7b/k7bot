package de.klassenserver7b.k7bot.timed;

import de.klassenserver7b.k7bot.K7Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class Dechemaxx {

	public static void notifymessage() {

		Guild guild = K7Bot.getInstance().getShardManager().getGuildById(779024287733776454L);
		GuildMessageChannel channel = guild.getTextChannelById(908780877104959508L);
		String mess = "LEUTE DER DECHEMAXX STEHT AN";

		channel.sendMessage(mess).queue();

	}

}
