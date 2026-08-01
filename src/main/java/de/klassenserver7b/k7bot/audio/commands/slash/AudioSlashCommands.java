/* (C)2026 */
package de.klassenserver7b.k7bot.audio.commands.slash;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.audio.AudioLoadOption;
import de.klassenserver7b.k7bot.audio.AudioLoadResultHandler;
import de.klassenserver7b.k7bot.audio.GuildAudioManager;
import de.klassenserver7b.k7bot.audio.commands.common.LyricsFetcher;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AudioSlashCommands {

	public static List<TopLevelSlashCommand> getAllCommands() {
		List<TopLevelSlashCommand> list = new ArrayList<>();
		list.add(new PlayCommand());
		list.add(new PlayNextCommand());
		list.add(new AddQueueCommand());
		list.add(new StopCommand());
		list.add(new PauseCommand());
		list.add(new ResumeCommand());
		list.add(new SkipCommand());
		list.add(new QueueCommand());
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

	private static boolean ensureConnected(SlashCommandInteraction event, Member caller) {
		GuildVoiceState memberVoiceState = caller.getVoiceState();
		if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
			if (event.isAcknowledged()) {
				event.getHook()
						.sendMessageEmbeds(
								EmbedUtils.getErrorEmbed("You must be in a voice channel to use this" + " command!",
										event.getGuild().getIdLong()).build())
						.queue();
			} else {
				event.replyEmbeds(EmbedUtils.getErrorEmbed("You must be in a voice channel to use this" + " command!",
						event.getGuild().getIdLong()).build()).queue();
			}
			return false;
		}

		GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
		if (botVoiceState == null || !botVoiceState.inAudioChannel()) {
			event.getGuild().getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
		}
		return true;
	}

	public static class PlayCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("play", "Replace current track and play").addOption(OptionType.STRING, "query",
					"URL or search query", true);
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			event.deferReply().queue();
			Member m = event.getMember();
			GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
			boolean justConnected = botVoiceState == null || !botVoiceState.inAudioChannel();
			if (m == null || !ensureConnected(event, m))
				return;

			String query = event.getOption("query").getAsString();
			if (!query.startsWith("http"))
				query = "ytsearch:" + query;

			long guildId = event.getGuild().getIdLong();
			Link link = K7Bot.getInstance().getLavalinkClient().getOrCreateLink(guildId);
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.setChannelId(event.getChannel().getIdLong());

			if (justConnected) {
				reactor.core.publisher.Mono.delay(java.time.Duration.ofMillis(600)).then(link.loadItem(query))
						.subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.REPLACE, m.getIdLong()),
								EmbedUtils.getLavalinkErrorHandler(event.getHook(), gam.getGuildId()));
			} else {
				link.loadItem(query).subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.REPLACE, m.getIdLong()),
						EmbedUtils.getLavalinkErrorHandler(event.getHook(), gam.getGuildId()));
			}
			event.getHook().sendMessageEmbeds(EmbedUtils.getInfoEmbed("Loading track: " + query, guildId).build())
					.queue();
		}
	}

	public static class StopCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("stop", "Stop playback and leave voice channel");
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.stop();
			event.getGuild().getJDA().getDirectAudioController().disconnect(event.getGuild());
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Stopped playback and left the channel.", guildId).build())
					.queue();
		}
	}

	public static class PauseCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("pause", "Pause playback");
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setPaused(true);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Paused playback.", guildId).build()).queue();
		}
	}

	public static class ResumeCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("resume", "Resume playback");
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setPaused(false);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Resumed playback.", guildId).build()).queue();
		}
	}

	public static class SkipCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("skip", "Skip current track");
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().nextTrack();
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Skipped the current track.", guildId).build()).queue();
		}
	}

	public static class QueueCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("queue", "Show current queue");
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);

			StringBuilder sb = new StringBuilder("**Current Queue:**\n");
			int i = 1;
			for (var track : gam.getTrackScheduler().queue) {
				sb.append(i++).append(". ").append(track.getInfo().getTitle()).append("\n");
				if (i > 10)
					break;
			}
			if (i == 1)
				sb.append("Empty");

			var embed = EmbedUtils.getBuilderOf(java.awt.Color.decode("#14cdc8"), sb.toString(), guildId)
					.setTitle("Queue List");
			event.replyEmbeds(embed.build()).queue();
		}
	}

	public static class ClearQueueCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("clearqueue", "Clear the current queue");
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().clearQueue();
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Queue cleared.", guildId).build()).queue();
		}
	}

	public static class NowPlayingCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("nowplaying", "Show currently playing track");
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);

			var player = gam.getPlayer().orElse(null);
			if (player != null && player.getTrack() != null) {
				var track = player.getTrack();
				var info = track.getInfo();
				long pos = player.getPosition();
				long len = info.getLength();

				String posStr = String.format("%02d:%02d", (pos / 1000) / 60, (pos / 1000) % 60);
				String lenStr = String.format("%02d:%02d", (len / 1000) / 60, (len / 1000) % 60);

				var embed = de.klassenserver7b.k7bot.util.EmbedUtils.getDefault(event.getGuild())
						.setTitle("Now Playing").setDescription("[" + info.getTitle() + "](" + info.getUri() + ")\n\n"
								+ "Author: " + info.getAuthor() + "\n" + "Position: `" + posStr + " / " + lenStr + "`");
				event.replyEmbeds(embed.build()).queue();
			} else {
				event.replyEmbeds(EmbedUtils.getErrorEmbed("Nothing is playing right now.", guildId).build()).queue();
			}
		}
	}

	public static class ForwardCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("forward", "Forward the current track").addOption(OptionType.INTEGER, "amount",
					"Amount in milliseconds to forward", true);
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long pos = event.getOption("amount").getAsInt();
			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().forward(pos);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Forwarded by " + pos + "ms", guildId).build()).queue();
		}
	}

	public static class BackCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("back", "Rewind the current track").addOption(OptionType.INTEGER, "amount",
					"Amount in milliseconds to rewind", true);
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long pos = event.getOption("amount").getAsInt();
			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().back(pos);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Rewound by " + pos + "ms", guildId).build()).queue();
		}
	}

	public static class VolumeCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("volume", "Set playback volume").addOption(OptionType.INTEGER, "level",
					"Volume level (0-100)", true);
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			int vol = event.getOption("level").getAsInt();
			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setVolume(vol);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Volume set to " + vol, guildId).build()).queue();
		}
	}

	public static class SeekCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("seek", "Seek to a specific position").addOption(OptionType.INTEGER, "position",
					"Position in milliseconds", true);
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long pos = event.getOption("position").getAsInt();
			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setPosition(pos);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Seeked to " + pos + "ms", guildId).build()).queue();
		}
	}

	public static class LoopCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("loop", "Toggle track looping");
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			boolean repeating = gam.getTrackScheduler().isRepeating();
			gam.getTrackScheduler().setRepeating(!repeating);
			event.replyEmbeds(EmbedUtils
					.getSuccessEmbed("Looping is now " + (!repeating ? "enabled" : "disabled"), guildId).build())
					.queue();
		}
	}

	public static class ShuffleCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("shuffle", "Shuffle the current queue");
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().shuffle();
			event.replyEmbeds(
					EmbedUtils.getBuilderOf(java.awt.Color.decode("#A537FD"), "Playlist shuffled", guildId).build())
					.queue();
		}
	}

	public static class EQCommand implements TopLevelSlashCommand {
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
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			int id = event.getOption("preset").getAsInt();
			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);

			float[] bands;
			String name;
			switch (id) {
				case 0 -> {
					bands = new float[15];
					name = "Off";
				}
				case 1 -> {
					bands = new float[] { -0.2f, -0.15f, -0.1f, -0.05f, 0.0f, 0.05f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f,
							0.1f, 0.1f, 0.1f };
					name = "Ultra Low Bass";
				}
				case 2 -> {
					bands = new float[] { -0.15f, -0.1f, -0.05f, -0.05f, 0.0f, 0.0f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f,
							0.05f, 0.1f, 0.1f, 0.1f };
					name = "Low Bass";
				}
				case 3 -> {
					bands = new float[] { -0.1f, -0.075f, -0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.05f, 0.05f, 0.05f,
							0.05f, 0.05f, 0.05f, 0.05f };
					name = "Less Low Bass";
				}
				case 4 -> {
					bands = new float[] { 0.1f, 0.075f, 0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.05f, -0.05f, -0.05f,
							-0.05f, -0.1f, -0.05f, -0.05f };
					name = "Less Bass Boost";
				}
				case 5 -> {
					bands = new float[] { 0.15f, 0.1f, 0.05f, 0.05f, 0.0f, -0.0f, -0.05f, -0.05f, -0.05f, -0.05f,
							-0.05f, -0.05f, -0.1f, -0.1f, -0.1f };
					name = "Bass Boost";
				}
				case 6 -> {
					bands = new float[] { 0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,
							-0.1f, -0.1f, -0.1f, -0.1f };
					name = "Ultra Bass Boost";
				}
				default -> {
					event.replyEmbeds(EmbedUtils.getErrorEmbed("Unknown EQ ID", guildId).build()).queue();
					return;
				}
			}
			gam.getTrackScheduler().setEQ(bands);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("EQ Preset applied: " + name, guildId).build()).queue();
		}
	}

	public static class SpeedCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("speed", "Set playback speed").addOption(OptionType.NUMBER, "factor",
					"Speed factor (0.1 - 2.0)", true);
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			double speed = event.getOption("factor").getAsDouble();
			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setSpeed(speed);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Speed set to " + speed + "x", guildId).build()).queue();
		}
	}

	public static class PitchCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("pitch", "Set playback pitch").addOption(OptionType.NUMBER, "factor",
					"Pitch factor (0.1 - 2.0)", true);
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			double pitch = event.getOption("factor").getAsDouble();
			long guildId = event.getGuild().getIdLong();
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.getTrackScheduler().setPitch(pitch);
			event.replyEmbeds(EmbedUtils.getSuccessEmbed("Pitch set to " + pitch + "x", guildId).build()).queue();
		}
	}

	public static class PlayNextCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("playnext", "Play a track next in queue").addOption(OptionType.STRING, "query",
					"URL or search query", true);
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			event.deferReply().queue();
			Member m = event.getMember();
			GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
			boolean justConnected = botVoiceState == null || !botVoiceState.inAudioChannel();
			if (m == null || !ensureConnected(event, m))
				return;

			String query = event.getOption("query").getAsString();
			if (!query.startsWith("http")) {
				query = "ytsearch:" + query;
			}

			long guildId = event.getGuild().getIdLong();
			Link link = K7Bot.getInstance().getLavalinkClient().getOrCreateLink(guildId);
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.setChannelId(event.getChannel().getIdLong());

			if (justConnected) {
				reactor.core.publisher.Mono.delay(java.time.Duration.ofMillis(600)).then(link.loadItem(query))
						.subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.NEXT, m.getIdLong()),
								EmbedUtils.getLavalinkErrorHandler(event.getHook(), gam.getGuildId()));
			} else {
				link.loadItem(query).subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.NEXT, m.getIdLong()),
						EmbedUtils.getLavalinkErrorHandler(event.getHook(), gam.getGuildId()));
			}
			event.getHook().sendMessageEmbeds(EmbedUtils.getInfoEmbed("Loading next: " + query, guildId).build())
					.queue();
		}
	}

	public static class AddQueueCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("addqueue", "Add a track to the queue").addOption(OptionType.STRING, "query",
					"URL or search query", true);
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			event.deferReply().queue();
			Member m = event.getMember();
			GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
			boolean justConnected = botVoiceState == null || !botVoiceState.inAudioChannel();
			if (m == null || !ensureConnected(event, m))
				return;

			String query = event.getOption("query").getAsString();
			if (!query.startsWith("http")) {
				query = "ytsearch:" + query;
			}

			long guildId = event.getGuild().getIdLong();
			Link link = K7Bot.getInstance().getLavalinkClient().getOrCreateLink(guildId);
			GuildAudioManager gam = K7Bot.getInstance().getAudioManager().getGuildAudioManager(guildId);
			gam.setChannelId(event.getChannel().getIdLong());

			if (justConnected) {
				reactor.core.publisher.Mono.delay(java.time.Duration.ofMillis(600)).then(link.loadItem(query))
						.subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.APPEND, m.getIdLong()),
								EmbedUtils.getLavalinkErrorHandler(event.getHook(), gam.getGuildId()));
			} else {
				link.loadItem(query).subscribe(new AudioLoadResultHandler(gam, AudioLoadOption.APPEND, m.getIdLong()),
						EmbedUtils.getLavalinkErrorHandler(event.getHook(), gam.getGuildId()));
			}
			event.getHook()
					.sendMessageEmbeds(
							EmbedUtils.getInfoEmbed("Searching and adding to queue: " + query, guildId).build())
					.queue();
		}
	}

	public static class LyricsCommand implements TopLevelSlashCommand {
		@NotNull
		@Override
		public SlashCommandData getCommandData() {
			return Commands.slash("lyrics", "Get lyrics for the currently playing track");
		}

		@Override
		public void performSlashCommand(SlashCommandInteraction event) {
			Member m = event.getMember();
			if (m == null || !ensureConnected(event, m))
				return;

			long guildId = event.getGuild().getIdLong();
			Link link = K7Bot.getInstance().getLavalinkClient().getOrCreateLink(guildId);
			event.deferReply().queue(hook -> LyricsFetcher.fetchAndSendLyrics(link.getNode(), guildId,
					embed -> hook.sendMessageEmbeds(embed).queue()));
		}
	}
}
