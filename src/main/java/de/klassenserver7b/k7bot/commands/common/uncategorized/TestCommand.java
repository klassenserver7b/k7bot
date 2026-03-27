package de.klassenserver7b.k7bot.commands.common.uncategorized;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.audio.AudioLoadOption;
import de.klassenserver7b.k7bot.audio.AudioLoadResultHandler;
import de.klassenserver7b.k7bot.audio.GuildAudioManager;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.HelpCategories;
import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class TestCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"test"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.UNKNOWN;
    }

    @Override
    public void performCommand(Member caller, GuildMessageChannel channel, Message message) {

        String[] args = message.getContentStripped().split(" ");

        final Guild guild = channel.getGuild();

        final GuildVoiceState memberVoiceState = caller.getVoiceState();

        if (memberVoiceState.inAudioChannel()) {
            channel.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
        }

        final String identifier = args[1];
        final long guildId = guild.getIdLong();
        final Link link = K7Bot.getInstance().getLavalinkClient().getOrCreateLink(guildId);

        GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
        link.loadItem(identifier).subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.APPEND, caller.getUser().getIdLong()));


        //Test command is only used when I have something to test......
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void disableCommand() {
        isEnabled = false;
    }

    @Override
    public void enableCommand() {
        isEnabled = true;
    }
}
