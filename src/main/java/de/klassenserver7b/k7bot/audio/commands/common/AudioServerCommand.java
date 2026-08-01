package de.klassenserver7b.k7bot.audio.commands.common;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.audio.AudioLoadOption;
import de.klassenserver7b.k7bot.audio.AudioLoadResultHandler;
import de.klassenserver7b.k7bot.audio.GuildAudioManager;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.HelpCategories;
import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AudioServerCommand implements ServerCommand {

    private boolean isEnabled = true;

    @Override
    public String getHelp() {
        String commands = Arrays.stream(AudioCommandType.values())
                .map(type -> type.getAliases()[0])
                .collect(Collectors.joining(", "));
        return "Audio Commands: " + commands;
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MUSIC;
    }

    @Override
    public String[] getCommandStrings() {
        return Arrays.stream(AudioCommandType.values())
                .flatMap(type -> Arrays.stream(type.getAliases()))
                .toArray(String[]::new);
    }

    @Override
    public void performCommand(Member caller, GuildMessageChannel channel, Message message) {
        String[] args = message.getContentRaw().split("\\s+");
        if (args.length == 0) return;
        
        String commandName = args[0].substring(1).toLowerCase(); // Assuming 1 char prefix
        AudioCommandType commandType = AudioCommandType.fromString(commandName);

        if (commandType == null) {
            return;
        }

        long guildId = channel.getGuild().getIdLong();

        GuildVoiceState memberVoiceState = caller.getVoiceState();
        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("You must be in a voice channel to use this command!", guildId).build()).queue();
            return;
        }
        
        // Connect if not connected
        GuildVoiceState botVoiceState = channel.getGuild().getSelfMember().getVoiceState();
        boolean justConnected = false;
        if (botVoiceState == null || !botVoiceState.inAudioChannel()) {
            channel.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
            justConnected = true;
        }

        Link link = K7Bot.getInstance().getLavalinkClient().getOrCreateLink(guildId);
        GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
        gam.setChannelId(channel.getIdLong());

        switch (commandType) {
            case PLAY -> handlePlay(args, message, channel, link, gam, caller, justConnected);
            case ADD_QUEUE -> handleAddQueue(args, message, channel, link, gam, caller, justConnected);
            case PLAY_NEXT -> handlePlayNext(args, message, channel, link, gam, caller, justConnected);
            case STOP -> handleStop(channel, gam);
            case PAUSE -> handlePause(channel, gam);
            case RESUME -> handleResume(channel, gam);
            case SKIP -> handleSkip(channel, gam);
            case QUEUE -> handleQueue(channel, gam);
            case CLEAR_QUEUE -> handleClearQueue(channel, gam);
            case NOW_PLAYING -> handleNowPlaying(channel, gam);
            case VOLUME -> handleVolume(args, channel, gam);
            case SEEK -> handleSeek(args, channel, gam);
            case FORWARD -> handleForward(args, channel, gam);
            case BACK -> handleBack(args, channel, gam);
            case LOOP -> handleLoop(channel, gam);
            case SHUFFLE -> handleShuffle(channel, gam);
            case SPEED -> handleSpeed(args, channel, gam);
            case PITCH -> handlePitch(args, channel, gam);
            case EQ -> handleEq(args, channel, gam);
            case LYRICS -> handleLyrics(channel, link);
        }
    }

    private void handlePlay(String[] args, Message message, GuildMessageChannel channel, Link link, GuildAudioManager gam, Member caller, boolean justConnected) {
        long guildId = channel.getGuild().getIdLong();
        if (args.length < 2) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Please provide a track url or search query!", guildId).build()).queue();
            return;
        }
        String identifier = message.getContentRaw().substring(args[0].length()).trim();
        if (!identifier.startsWith("http")) {
            identifier = "ytsearch:" + identifier;
        }
        
        if (justConnected) {
            reactor.core.publisher.Mono.delay(java.time.Duration.ofMillis(600))
                .then(link.loadItem(identifier))
                .subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.REPLACE, caller.getUser().getIdLong()), 
                           EmbedUtils.getLavalinkErrorHandler(channel, guildId));
        } else {
            link.loadItem(identifier).subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.REPLACE, caller.getUser().getIdLong()),
                                                EmbedUtils.getLavalinkErrorHandler(channel, guildId));
        }
        channel.sendMessageEmbeds(EmbedUtils.getInfoEmbed("Loading track: " + identifier, guildId).build()).queue();
    }

    private void handleAddQueue(String[] args, Message message, GuildMessageChannel channel, Link link, GuildAudioManager gam, Member caller, boolean justConnected) {
        long guildId = channel.getGuild().getIdLong();
        if (args.length < 2) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Please provide a track url or search query!", guildId).build()).queue();
            return;
        }
        String aqIdentifier = message.getContentRaw().substring(args[0].length()).trim();
        if (!aqIdentifier.startsWith("http")) {
            aqIdentifier = "ytsearch:" + aqIdentifier;
        }
        
        if (justConnected) {
            reactor.core.publisher.Mono.delay(java.time.Duration.ofMillis(600))
                .then(link.loadItem(aqIdentifier))
                .subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.APPEND, caller.getUser().getIdLong()),
                           EmbedUtils.getLavalinkErrorHandler(channel, guildId));
        } else {
            link.loadItem(aqIdentifier).subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.APPEND, caller.getUser().getIdLong()),
                                                  EmbedUtils.getLavalinkErrorHandler(channel, guildId));
        }
        channel.sendMessageEmbeds(EmbedUtils.getInfoEmbed("Searching and adding to queue: " + aqIdentifier, guildId).build()).queue();
    }

    private void handlePlayNext(String[] args, Message message, GuildMessageChannel channel, Link link, GuildAudioManager gam, Member caller, boolean justConnected) {
        long guildId = channel.getGuild().getIdLong();
        if (args.length < 2) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Please provide a track url or search query!", guildId).build()).queue();
            return;
        }
        String nextIdentifier = message.getContentRaw().substring(args[0].length()).trim();
        if (!nextIdentifier.startsWith("http")) {
            nextIdentifier = "ytsearch:" + nextIdentifier;
        }
        
        if (justConnected) {
            reactor.core.publisher.Mono.delay(java.time.Duration.ofMillis(600))
                .then(link.loadItem(nextIdentifier))
                .subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.NEXT, caller.getUser().getIdLong()),
                           EmbedUtils.getLavalinkErrorHandler(channel, guildId));
        } else {
            link.loadItem(nextIdentifier).subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.NEXT, caller.getUser().getIdLong()),
                                                    EmbedUtils.getLavalinkErrorHandler(channel, guildId));
        }
        channel.sendMessageEmbeds(EmbedUtils.getInfoEmbed("Loading next: " + nextIdentifier, guildId).build()).queue();
    }

    private void handleStop(GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        gam.stop();
        channel.getJDA().getDirectAudioController().disconnect(channel.getGuild());
        channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Stopped playback and left the channel.", guildId).build()).queue();
    }

    private void handlePause(GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        gam.getTrackScheduler().setPaused(true);
        channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Paused playback.", guildId).build()).queue();
    }

    private void handleResume(GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        gam.getTrackScheduler().setPaused(false);
        channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Resumed playback.", guildId).build()).queue();
    }

    private void handleSkip(GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        gam.getTrackScheduler().nextTrack();
        channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Skipped the current track.", guildId).build()).queue();
    }

    private void handleQueue(GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        StringBuilder sb = new StringBuilder("**Current Queue:**\n");
        int i = 1;
        for (var track : gam.getTrackScheduler().queue) {
            sb.append(i++).append(". ").append(track.getInfo().getTitle()).append("\n");
            if (i > 10) break;
        }
        if (i == 1) sb.append("Empty");
        
        var embed = EmbedUtils.getBuilderOf(java.awt.Color.decode("#14cdc8"), sb.toString(), guildId).setTitle("Queue List");
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    private void handleClearQueue(GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        gam.getTrackScheduler().clearQueue();
        channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Queue cleared.", guildId).build()).queue();
    }

    private void handleNowPlaying(GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        var player = gam.getPlayer().orElse(null);
        if (player != null && player.getTrack() != null) {
            var track = player.getTrack();
            var info = track.getInfo();
            long pos = player.getPosition();
            long len = info.getLength();
            
            String posStr = String.format("%02d:%02d", (pos / 1000) / 60, (pos / 1000) % 60);
            String lenStr = String.format("%02d:%02d", (len / 1000) / 60, (len / 1000) % 60);
            
            var embed = EmbedUtils.getDefault(guildId)
                    .setTitle("Now Playing")
                    .setDescription("[" + info.getTitle() + "](" + info.getUri() + ")\n\n" +
                                    "Author: " + info.getAuthor() + "\n" +
                                    "Position: `" + posStr + " / " + lenStr + "`");
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Nothing is playing right now.", guildId).build()).queue();
        }
    }

    private void handleVolume(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        if (args.length < 2) return;
        try {
            int vol = Integer.parseInt(args[1]);
            gam.getTrackScheduler().setVolume(vol);
            channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Volume set to " + vol, guildId).build()).queue();
        } catch (NumberFormatException e) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Invalid volume.", guildId).build()).queue();
        }
    }

    private void handleSeek(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        if (args.length < 2) return;
        try {
            long pos = Long.parseLong(args[1]);
            gam.getTrackScheduler().setPosition(pos);
            channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Seeked to " + pos + "ms", guildId).build()).queue();
        } catch (NumberFormatException e) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Invalid position.", guildId).build()).queue();
        }
    }

    private void handleForward(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        if (args.length < 2) return;
        try {
            long pos = Long.parseLong(args[1]);
            gam.getTrackScheduler().forward(pos);
            channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Forwarded by " + pos + "ms", guildId).build()).queue();
        } catch (NumberFormatException e) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Invalid amount.", guildId).build()).queue();
        }
    }

    private void handleBack(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        if (args.length < 2) return;
        try {
            long pos = Long.parseLong(args[1]);
            gam.getTrackScheduler().back(pos);
            channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Rewound by " + pos + "ms", guildId).build()).queue();
        } catch (NumberFormatException e) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Invalid amount.", guildId).build()).queue();
        }
    }

    private void handleLoop(GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        boolean repeating = gam.getTrackScheduler().isRepeating();
        gam.getTrackScheduler().setRepeating(!repeating);
        channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Looping is now " + (!repeating ? "enabled" : "disabled"), guildId).build()).queue();
    }

    private void handleShuffle(GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        gam.getTrackScheduler().shuffle();
        channel.sendMessageEmbeds(EmbedUtils.getBuilderOf(java.awt.Color.decode("#A537FD"), "Playlist shuffled", guildId).build()).queue();
    }

    private void handleSpeed(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        if (args.length < 2) return;
        try {
            double speed = Double.parseDouble(args[1]);
            gam.getTrackScheduler().setSpeed(speed);
            channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Speed set to " + speed + "x", guildId).build()).queue();
        } catch (NumberFormatException e) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Invalid speed.", guildId).build()).queue();
        }
    }

    private void handlePitch(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        if (args.length < 2) return;
        try {
            double pitch = Double.parseDouble(args[1]);
            gam.getTrackScheduler().setPitch(pitch);
            channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Pitch set to " + pitch + "x", guildId).build()).queue();
        } catch (NumberFormatException e) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Invalid pitch.", guildId).build()).queue();
        }
    }

    private void handleEq(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
        long guildId = channel.getGuild().getIdLong();
        if (args.length < 2) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Available EQ Presets: 0 (Off), 1 (Ultra Low Bass), 2 (Low Bass), 3 (Less Low Bass), 4 (Less Bass Boost), 5 (Bass Boost), 6 (Ultra Bass Boost), 7 (Lyrics)", guildId).build()).queue();
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            float[] bands;
            String name;
            switch (id) {
                case 0 -> { bands = new float[15]; name = "Off"; }
                case 1 -> { bands = new float[]{-0.2f, -0.15f, -0.1f, -0.05f, 0.0f, 0.05f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f}; name = "Ultra Low Bass"; }
                case 2 -> { bands = new float[]{-0.15f, -0.1f, -0.05f, -0.05f, 0.0f, 0.0f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.1f, 0.1f, 0.1f}; name = "Low Bass"; }
                case 3 -> { bands = new float[]{-0.1f, -0.075f, -0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f}; name = "Less Low Bass"; }
                case 4 -> { bands = new float[]{0.1f, 0.075f, 0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.05f, -0.05f, -0.05f, -0.05f, -0.1f, -0.05f, -0.05f}; name = "Less Bass Boost"; }
                case 5 -> { bands = new float[]{0.15f, 0.1f, 0.05f, 0.05f, 0.0f, -0.0f, -0.05f, -0.05f, -0.05f, -0.05f, -0.05f, -0.05f, -0.1f, -0.1f, -0.1f}; name = "Bass Boost"; }
                case 6 -> { bands = new float[]{0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f}; name = "Ultra Bass Boost"; }
                default -> { channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Unknown EQ ID", guildId).build()).queue(); return; }
            }
            gam.getTrackScheduler().setEQ(bands);
            channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("EQ Preset applied: " + name, guildId).build()).queue();
        } catch (NumberFormatException e) {
            channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Invalid EQ ID.", guildId).build()).queue();
        }
    }

    private void handleLyrics(GuildMessageChannel channel, Link link) {
        long guildId = channel.getGuild().getIdLong();
        LyricsFetcher.fetchAndSendLyrics(link.getNode(), guildId, embed -> channel.sendMessageEmbeds(embed).queue());
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void disableCommand() {
        this.isEnabled = false;
    }

    @Override
    public void enableCommand() {
        this.isEnabled = true;
    }
}