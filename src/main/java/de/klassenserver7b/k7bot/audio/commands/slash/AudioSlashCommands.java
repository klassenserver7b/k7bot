/* (C)2026 */
package de.klassenserver7b.k7bot.audio.commands.slash;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.audio.AudioLoadOption;
import de.klassenserver7b.k7bot.audio.GuildAudioManager;
import de.klassenserver7b.k7bot.audio.LyricsFetcher;
import de.klassenserver7b.k7bot.audio.commands.common.AudioCommandUtils;
import de.klassenserver7b.k7bot.audio.commands.common.EQPreset;
import de.klassenserver7b.k7bot.commands.types.GuildSlashCommand;
import de.klassenserver7b.k7bot.util.CommandUtils;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AudioSlashCommands {

	public static List<GuildSlashCommand> getAllCommands() {
		List<GuildSlashCommand> list = new ArrayList<>();
		list.add(new PlayCommand());
		list.add(new PlayNextCommand());
		list.add(new AddQueueCommand());
		list.add(new StopCommand());
		list.add(new PauseCommand());
		list.add(new ResumeCommand());
		list.add(new SkipCommand());
		list.add(new ListQueueCommand());
		list.add(new ClearQueueCommand());
		list.add(new NowPlayingCommand());
		list.add(new VolumeCommand());
		list.add(new SeekCommand());
		list.add(new ForwardCommand());
		list.add(new BackCommand());
		list.add(new LoopCommand());
		list.add(new ShuffleCommand());
		list.add(new EQCommand());
		list.add(new SpeedCommand());
		list.add(new PitchCommand());
		list.add(new LyricsCommand());
		return list;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean isMemberConnectedToSameVc(SlashCommandInteraction event, Guild guild, Member caller) {
		GuildVoiceState memberVoiceState = caller.getVoiceState();
		Objects.requireNonNull(memberVoiceState, "CacheFlag.VOICE_STATE should be enabled");

		if (!memberVoiceState.inAudioChannel()) {
			if (event.isAcknowledged()) {
				event.getHook().sendMessageEmbeds(EmbedUtils
						.getErrorEmbed("You must be in a voice channel to use this" + " command!", guild.getIdLong())
						.build()).queue();
			} else {
				event.replyEmbeds(EmbedUtils
						.getErrorEmbed("You must be in a voice channel to use this" + " command!", guild.getIdLong())
						.build()).queue();
			}
			return false;
		}

		GuildVoiceState botVoiceState = guild.getSelfMember().getVoiceState();
		Objects.requireNonNull(botVoiceState, "CacheFlag.VOICE_STATE should be enabled");

		if (!botVoiceState.inAudioChannel()) {
			guild.getJDA().getDirectAudioController()
					.connect(Objects.requireNonNull(memberVoiceState.getChannel(), "Member left vc after check"));
		}
		return true;
	}

	public static void handleLoadCommand(SlashCommandInteraction event, Guild guild, Member m, AudioLoadOption option,
			String responsePrefix) {
		event.deferReply().queue();

		GuildVoiceState botVoiceState = guild.getSelfMember().getVoiceState();
		Objects.requireNonNull(botVoiceState, "CacheFlag.VOICE_STATE should be enabled");
		boolean justConnected = !botVoiceState.inAudioChannel();
		if (!isMemberConnectedToSameVc(event, guild, m))
			return;

		String query = CommandUtils.getRequiredOption(event, "query").getAsString();
		query = AudioCommandUtils.resolveQuery(query);

		long guildId = guild.getIdLong();
		Link link = K7Bot.getInstance().getLavalinkClient().getOrCreateLink(guildId);
		GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
		gam.setChannelId(event.getChannel().getIdLong());

		AudioCommandUtils.loadItem(link, query, gam, m.getIdLong(), option, justConnected,
				EmbedUtils.getLavalinkErrorHandler(event.getHook(), guildId));

		event.getHook().sendMessageEmbeds(EmbedUtils.getInfoEmbed(responsePrefix + query, guildId).build()).queue();
	}

	public static class PlayCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("play", "Replace current track and play").addOption(OptionType.STRING, "query",
					"URL or search query", true);
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			handleLoadCommand(event, guild, m, AudioLoadOption.REPLACE, "Loading track: ");
		}
	}

	public static class PlayNextCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("playnext", "Play a track next in queue").addOption(OptionType.STRING, "query",
					"URL or search query", true);
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			handleLoadCommand(event, guild, m, AudioLoadOption.NEXT, "Loading next: ");
		}
	}

	public static class AddQueueCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("addqueue", "Add a track to the queue").addOption(OptionType.STRING, "query",
					"URL or search query", true);
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			handleLoadCommand(event, guild, m, AudioLoadOption.APPEND, "Searching and adding to queue: ");
		}
	}

	public static class StopCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("stop", "Stop playback and leave voice channel");
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			event.deferReply().queue();

			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.stop();
			guild.getJDA().getDirectAudioController().disconnect(guild);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Stopped playback and left the channel.", guildId).build())
					.queue();
		}
	}

	public static class PauseCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("pause", "Pause playback");
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setPaused(true);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Paused playback.", guildId).build()).queue();
		}
	}

	public static class ResumeCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("resume", "Resume playback");
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setPaused(false);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Resumed playback.", guildId).build()).queue();
		}
	}

	public static class SkipCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("skip", "Skip current track").addOption(OptionType.INTEGER, "amount",
					"Songs to skip (0 = next song, 1 = song after next)", false);
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);

			int amount = 0;
			OptionMapping amountOpt = event.getOption("amount");
			if (amountOpt != null) {
				amount = amountOpt.getAsInt();
			}
			if (amount < 0)
				amount = 0;

			gam.getTrackScheduler().skipTracks(amount);

			String msg = amount > 0 ? "Skipped " + amount + " tracks from the queue." : "Skipped the current track.";
			event.replyEmbeds(EmbedUtils.getSuccessEmbed(msg, guildId).build()).queue();
		}
	}

	public static class ListQueueCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("queue", "Show current queue");
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			event.replyEmbeds(AudioCommandUtils.formatQueue(gam, guildId)).queue();
		}
	}

	public static class ClearQueueCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("clearqueue", "Clear the current queue");
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().clearQueue();
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Queue cleared.", guildId).build()).queue();
		}
	}

	public static class NowPlayingCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("nowplaying", "Show currently playing track");
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			var embed = AudioCommandUtils.formatNowPlaying(gam, guildId);
			event.replyEmbeds(Objects.requireNonNullElseGet(embed,
					() -> EmbedUtils.getErrorEmbed("Nothing is playing right now.", guildId).build())).queue();
		}
	}

	public static class ForwardCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("forward", "Forward the current track").addOption(OptionType.INTEGER, "amount",
					"Amount in milliseconds to forward", true);
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long pos = CommandUtils.getRequiredOption(event, "amount").getAsInt();
			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().forward(pos);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Forwarded by " + pos + "ms", guildId).build()).queue();
		}
	}

	public static class BackCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("back", "Rewind the current track").addOption(OptionType.INTEGER, "amount",
					"Amount in milliseconds to rewind", true);
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long pos = CommandUtils.getRequiredOption(event, "amount").getAsInt();
			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().back(pos);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Rewound by " + pos + "ms", guildId).build()).queue();
		}
	}

	public static class VolumeCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("volume", "Set playback volume").addOption(OptionType.INTEGER, "level",
					"Volume level (0-100)", true);
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			int vol = CommandUtils.getRequiredOption(event, "level").getAsInt();
			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setVolume(vol);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Volume set to " + vol, guildId).build()).queue();
		}
	}

	public static class SeekCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("seek", "Seek to a specific position").addOption(OptionType.INTEGER, "position",
					"Position in milliseconds", true);
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long pos = CommandUtils.getRequiredOption(event, "position").getAsInt();
			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setPosition(pos);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Seeked to " + pos + "ms", guildId).build()).queue();
		}
	}

	public static class LoopCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("loop", "Toggle track looping");
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			boolean repeating = gam.getTrackScheduler().isRepeating();
			gam.getTrackScheduler().setRepeating(!repeating);
			event.replyEmbeds(EmbedUtils
					.getSuccessEmbed("Looping is now " + (!repeating ? "enabled" : "disabled"), guildId).build())
					.queue();
		}
	}

	public static class ShuffleCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("shuffle", "Shuffle the current queue");
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().shuffle();
			event.replyEmbeds(
					EmbedUtils.getBuilderOf(java.awt.Color.decode("#A537FD"), "Playlist shuffled", guildId).build())
					.queue();
		}
	}

	public static class EQCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("eq", "Set equalizer preset")
					.addOptions(new net.dv8tion.jda.api.interactions.commands.build.OptionData(OptionType.INTEGER,
							"preset", "The EQ preset to apply", true).addChoice("Off", 0).addChoice("Ultra Low Bass", 1)
							.addChoice("Low Bass", 2).addChoice("Less Low Bass", 3).addChoice("Less Bass Boost", 4)
							.addChoice("Bass Boost", 5).addChoice("Ultra Bass Boost", 6));
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			int id = CommandUtils.getRequiredOption(event, "preset").getAsInt();
			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);

			EQPreset preset = EQPreset.byId(id);
			if (preset == null) {
				event.replyEmbeds(EmbedUtils.getErrorEmbed("Unknown EQ ID", guildId).build()).queue();
				return;
			}
			gam.getTrackScheduler().setEQ(preset.getBands());
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("EQ Preset applied: " + preset.getName(), guildId).build())
					.queue();
		}
	}

	public static class SpeedCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("speed", "Set playback speed").addOption(OptionType.NUMBER, "factor",
					"Speed factor (0.1 - 2.0)", true);
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			double speed = CommandUtils.getRequiredOption(event, "factor").getAsDouble();
			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setSpeed(speed);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Speed set to " + speed + "x", guildId).build()).queue();
		}
	}

	public static class PitchCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("pitch", "Set playback pitch").addOption(OptionType.NUMBER, "factor",
					"Pitch factor (0.1 - 2.0)", true);
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			double pitch = CommandUtils.getRequiredOption(event, "factor").getAsDouble();
			long guildId = guild.getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setPitch(pitch);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Pitch set to " + pitch + "x", guildId).build()).queue();
		}
	}

	public static class LyricsCommand implements GuildSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("lyrics", "Get lyrics for the currently playing track");
		}

		@Override
		public void performGuildSlashCommand(@NonNull SlashCommandInteraction event, @NonNull Guild guild,
				@NonNull Member m) {
			if (!isMemberConnectedToSameVc(event, guild, m))
				return;

			long guildId = guild.getIdLong();
			Link link = K7Bot.getInstance().getLavalinkClient().getOrCreateLink(guildId);
			event.deferReply().queue(hook -> LyricsFetcher.fetchAndSendLyrics(link.getNode(), guildId,
					embed -> hook.sendMessageEmbeds(embed).queue()));
		}
	}
}
