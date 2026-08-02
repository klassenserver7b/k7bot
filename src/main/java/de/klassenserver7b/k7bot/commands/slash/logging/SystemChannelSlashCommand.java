/* (C)2026 */
package de.klassenserver7b.k7bot.commands.slash.logging;

import org.jetbrains.annotations.NotNull;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.GuildSlashCommand;
import de.klassenserver7b.k7bot.manage.SystemNotificationChannelManager;
import de.klassenserver7b.k7bot.util.CommandUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class SystemChannelSlashCommand implements GuildSlashCommand {

	@Override
	public void performGuildSlashCommand(@NotNull SlashCommandInteraction event, @NotNull Guild guild,
			@NotNull Member member) {

		InteractionHook hook = event.deferReply().complete();

		GuildMessageChannel chan = CommandUtils.getRequiredOption(event, "syschannel").getAsChannel()
				.asGuildMessageChannel();

		SystemNotificationChannelManager sys = K7Bot.getInstance().getSysChannelMgr();
		sys.insertChannel(chan);

		hook.sendMessage("Systemchannel was sucsessful set to " + chan.getAsMention()).queue();
	}

	@NotNull
	@Override
	public SlashCommandData getCommandData() {
		return Commands.slash("syschannel", "change syschannel")
				.addOptions(new OptionData(OptionType.CHANNEL, "channel", "the channel to use")
						.setChannelTypes(ChannelType.TEXT).setRequired(true))
				.setContexts(InteractionContextType.GUILD)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER));
	}

}
