package de.klassenserver7b.k7bot.commands.common.uncategorized;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.manage.LavaLinkManager;
import de.klassenserver7b.k7bot.util.HelpCategories;
import dev.arbjerg.lavalink.client.FunctionalLoadResultHandler;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.List;

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

        link.loadItem(identifier).subscribe(new FunctionalLoadResultHandler(
                // Track loaded
                (trackLoad) -> {
                    final Track track = trackLoad.getTrack();

                    final var userData = new LavaLinkManager.UserData(caller.getUser().getIdLong());
                    track.setUserData(userData);
                    link.createOrUpdatePlayer()
                            .setTrack(track)
                            .setVolume(35)
                            .subscribe((player) -> {
                                final Track playingTrack = player.getTrack();
                                final var trackTitle = playingTrack.getInfo().getTitle();

                                channel.sendMessage("Now playing: " + trackTitle + "\nRequested by: <@" + caller.getUser().getIdLong() + '>').queue();
                            });

                },
                null,
                // search result loaded
                (search) -> {
                    final List<Track> tracks = search.getTracks();

                    if (tracks.isEmpty()) {
                        channel.sendMessage("No tracks found!").queue();
                        return;
                    }

                    final Track firstTrack = tracks.getFirst();

                    // This is a different way of updating the player! Choose your preference!
                    // This method will also create a player if there is not one in the server yet
                    link.updatePlayer((update) -> update.setTrack(firstTrack).setVolume(35))
                            .subscribe((ignored) -> channel.sendMessage("Now playing: " + firstTrack.getInfo().getTitle()).queue());
                },
                null, // no matches
                null // load failed
        ));


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
