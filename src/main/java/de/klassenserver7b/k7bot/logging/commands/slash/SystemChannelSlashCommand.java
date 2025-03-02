package de.klassenserver7b.k7bot.logging.commands.slash;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.manage.SystemNotificationChannelManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class SystemChannelSlashCommand implements TopLevelSlashCommand {

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        InteractionHook hook = event.deferReply().complete();

        assert event.getOption("syschannel") != null;
        GuildMessageChannel chan = event.getOption("syschannel").getAsChannel().asGuildMessageChannel();

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
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER));
    }

}
