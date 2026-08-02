/* (C)2026 */
package de.klassenserver7b.k7bot.audio.commands.common;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.audio.AudioLoadOption;
import de.klassenserver7b.k7bot.audio.GuildAudioManager;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.HelpCategories;
import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class AudioServerCommand implements ServerCommand {

	private boolean isEnabled = true;

	@Override
	public String getHelp() {
		String commands = Arrays.stream(AudioCommandType.values()).map(type -> type.getAliases()[0])
				.collect(Collectors.joining(", "));
		return "Audio Commands: " + commands;
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.MUSIC;
	}

	@Override
	public String[] getCommandStrings() {
		return Arrays.stream(AudioCommandType.values()).flatMap(type -> Arrays.stream(type.getAliases()))
				.toArray(String[]::new);
	}

	@Override
	public void performCommand(Member caller, GuildMessageChannel channel, Message message) {
		String[] args = message.getContentRaw().split("\\s+");
		if (args.length == 0)
			return;

		String commandName = args[0].substring(1).toLowerCase(); // Assuming 1 char prefix
		AudioCommandType commandType = AudioCommandType.fromString(commandName);

		if (commandType == null) {
			return;
		}

		long guildId = channel.getGuild().getIdLong();

		GuildVoiceState memberVoiceState = caller.getVoiceState();
		Objects.requireNonNull(memberVoiceState, "CacheFlag.VOICE_STATE should be enabled");

		if (!memberVoiceState.inAudioChannel()) {
			channel.sendMessageEmbeds(
					EmbedUtils.getErrorEmbed("You must be in a voice channel to use this command!", guildId).build())
					.queue();
			return;
		}

		// Connect if not connected
		GuildVoiceState botVoiceState = channel.getGuild().getSelfMember().getVoiceState();
		Objects.requireNonNull(botVoiceState, "CacheFlag.VOICE_STATE should be enabled");

		boolean justConnected = false;
		if (!botVoiceState.inAudioChannel()) {
			channel.getJDA().getDirectAudioController()
					.connect(Objects.requireNonNull(memberVoiceState.getChannel(), "Member left vc after check"));
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

	private void handleServerLoad(String[] args, Message message, GuildMessageChannel channel, Link link,
			GuildAudioManager gam, Member caller, boolean justConnected, AudioLoadOption option,
			String responsePrefix) {
		long guildId = channel.getGuild().getIdLong();
		if (args.length < 2) {
			channel.sendMessageEmbeds(
					EmbedUtils.getErrorEmbed("Please provide a track url or search query!", guildId).build()).queue();
			return;
		}
		String identifier = message.getContentRaw().substring(args[0].length()).trim();
		identifier = AudioCommandUtils.resolveQuery(identifier);

		AudioCommandUtils.loadItem(link, identifier, gam, caller.getIdLong(), option, justConnected,
				EmbedUtils.getLavalinkErrorHandler(channel, guildId));
		channel.sendMessageEmbeds(EmbedUtils.getInfoEmbed(responsePrefix + identifier, guildId).build()).queue();
	}

	private void handlePlay(String[] args, Message message, GuildMessageChannel channel, Link link,
			GuildAudioManager gam, Member caller, boolean justConnected) {
		handleServerLoad(args, message, channel, link, gam, caller, justConnected, AudioLoadOption.REPLACE,
				"Loading track: ");
	}

	private void handleAddQueue(String[] args, Message message, GuildMessageChannel channel, Link link,
			GuildAudioManager gam, Member caller, boolean justConnected) {
		handleServerLoad(args, message, channel, link, gam, caller, justConnected, AudioLoadOption.APPEND,
				"Searching and adding to queue: ");
	}

	private void handlePlayNext(String[] args, Message message, GuildMessageChannel channel, Link link,
			GuildAudioManager gam, Member caller, boolean justConnected) {
		handleServerLoad(args, message, channel, link, gam, caller, justConnected, AudioLoadOption.NEXT,
				"Loading next: ");
	}

	private void handleStop(GuildMessageChannel channel, GuildAudioManager gam) {
		long guildId = channel.getGuild().getIdLong();
		gam.stop();
		channel.getJDA().getDirectAudioController().disconnect(channel.getGuild());
		channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Stopped playback and left the channel.", guildId).build())
				.queue();
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
		channel.sendMessageEmbeds(AudioCommandUtils.formatQueue(gam, guildId)).queue();
	}

	private void handleClearQueue(GuildMessageChannel channel, GuildAudioManager gam) {
		long guildId = channel.getGuild().getIdLong();
		gam.getTrackScheduler().clearQueue();
		channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Queue cleared.", guildId).build()).queue();
	}

	private void handleNowPlaying(GuildMessageChannel channel, GuildAudioManager gam) {
		long guildId = channel.getGuild().getIdLong();
		var embed = AudioCommandUtils.formatNowPlaying(gam, guildId);
		channel.sendMessageEmbeds(Objects.requireNonNullElseGet(embed,
				() -> EmbedUtils.getErrorEmbed("Nothing is playing right now.", guildId).build())).queue();
	}

	private void handleVolume(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
		long guildId = channel.getGuild().getIdLong();
		if (args.length < 2)
			return;
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
		if (args.length < 2)
			return;
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
		if (args.length < 2)
			return;
		try {
			long pos = Long.parseLong(args[1]);
			gam.getTrackScheduler().forward(pos);
			channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Forwarded by " + pos + "ms", guildId).build())
					.queue();
		} catch (NumberFormatException e) {
			channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Invalid amount.", guildId).build()).queue();
		}
	}

	private void handleBack(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
		long guildId = channel.getGuild().getIdLong();
		if (args.length < 2)
			return;
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
		channel.sendMessageEmbeds(
				EmbedUtils.getSuccessEmbed("Looping is now " + (!repeating ? "enabled" : "disabled"), guildId).build())
				.queue();
	}

	private void handleShuffle(GuildMessageChannel channel, GuildAudioManager gam) {
		long guildId = channel.getGuild().getIdLong();
		gam.getTrackScheduler().shuffle();
		channel.sendMessageEmbeds(
				EmbedUtils.getBuilderOf(java.awt.Color.decode("#A537FD"), "Playlist shuffled", guildId).build())
				.queue();
	}

	private void handleSpeed(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
		long guildId = channel.getGuild().getIdLong();
		if (args.length < 2)
			return;
		try {
			double speed = Double.parseDouble(args[1]);
			gam.getTrackScheduler().setSpeed(speed);
			channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Speed set to " + speed + "x", guildId).build())
					.queue();
		} catch (NumberFormatException e) {
			channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Invalid speed.", guildId).build()).queue();
		}
	}

	private void handlePitch(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
		long guildId = channel.getGuild().getIdLong();
		if (args.length < 2)
			return;
		try {
			double pitch = Double.parseDouble(args[1]);
			gam.getTrackScheduler().setPitch(pitch);
			channel.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("Pitch set to " + pitch + "x", guildId).build())
					.queue();
		} catch (NumberFormatException e) {
			channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Invalid pitch.", guildId).build()).queue();
		}
	}

	private void handleEq(String[] args, GuildMessageChannel channel, GuildAudioManager gam) {
		long guildId = channel.getGuild().getIdLong();
		if (args.length < 2) {
			channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed(
					"Available EQ Presets: 0 (Off), 1 (Ultra Low Bass), 2 (Low Bass), 3 (Less Low Bass), 4 (Less Bass Boost), 5 (Bass Boost), 6 (Ultra Bass Boost), 7 (Lyrics)",
					guildId).build()).queue();
			return;
		}
		try {
			int id = Integer.parseInt(args[1]);
			EQPreset preset = EQPreset.byId(id);
			if (preset == null) {
				channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Unknown EQ ID", guildId).build()).queue();
				return;
			}
			gam.getTrackScheduler().setEQ(preset.getBands());
			channel.sendMessageEmbeds(
					EmbedUtils.getSuccessEmbed("EQ Preset applied: " + preset.getName(), guildId).build()).queue();
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