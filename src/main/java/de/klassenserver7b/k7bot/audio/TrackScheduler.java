/* (C)2026 */
package de.klassenserver7b.k7bot.audio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.database.dao.MusicLogDAO;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import dev.arbjerg.lavalink.client.player.FilterBuilder;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import dev.arbjerg.lavalink.protocol.v4.Timescale;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class TrackScheduler {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final GuildAudioManager guildMusicManager;
	public final Deque<Track> queue = new LinkedList<>();
	private boolean repeating = false;
	private FilterBuilder filterBuilder = new FilterBuilder();
	private double speed = 1.0;
	private double pitch = 1.0;
	private double rate = 1.0;
	private int volume = 10;

	public TrackScheduler(GuildAudioManager guildAudioManager) {
		this.guildMusicManager = guildAudioManager;
	}

	public void loadTrack(Track track, AudioLoadOption alo) {
		switch (alo) {
			case AudioLoadOption.APPEND -> enqueue(track);
			case AudioLoadOption.NEXT -> enqueueNext(track);
			case AudioLoadOption.REPLACE_QUEUE -> {
				clearQueue();
				startTrack(track);
			}
			case AudioLoadOption.REPLACE -> startTrack(track);
		}
	}

	public void loadPlaylist(List<Track> tracks, AudioLoadOption alo) {
		switch (alo) {
			case AudioLoadOption.APPEND -> enqueuePlaylist(tracks);
			case AudioLoadOption.NEXT -> enqueueNextPlaylist(tracks);
			case AudioLoadOption.REPLACE_QUEUE -> {
				clearQueue();
				this.queue.addAll(tracks);
				startTrack(this.queue.poll());
			}
			case AudioLoadOption.REPLACE -> {
				tracks.reversed().forEach(this.queue::addFirst);
				startTrack(this.queue.poll());
			}
		}
	}

	private void enqueueNext(Track track) {
		LavalinkPlayer player = this.guildMusicManager.getOrCreatePlayer();
		if (player == null || player.getTrack() == null) {
			this.startTrack(track);
		} else {
			this.queue.addFirst(track);
		}
	}

	private void enqueue(Track track) {
		LavalinkPlayer player = this.guildMusicManager.getOrCreatePlayer();
		if (player == null || player.getTrack() == null) {
			this.startTrack(track);
		} else {
			this.queue.offer(track);
		}
	}

	private void enqueueNextPlaylist(List<Track> tracks) {
		tracks.reversed().forEach(this.queue::addFirst);

		LavalinkPlayer player = this.guildMusicManager.getOrCreatePlayer();

		if (player == null || player.getTrack() == null) {
			this.startTrack(this.queue.poll());
		}
	}

	private void enqueuePlaylist(List<Track> tracks) {
		this.queue.addAll(tracks);

		LavalinkPlayer player = this.guildMusicManager.getOrCreatePlayer();

		if (player == null || player.getTrack() == null) {
			this.startTrack(this.queue.poll());
		}
	}

	public void clearQueue() {
		this.queue.clear();
	}

	public void onTrackStart(Track track) {
		System.out.println("Track started: " + track.getInfo().getTitle());

		long channelId = guildMusicManager.getChannelId();
		if (channelId != -1) {
			try {
				Guild guild = K7Bot.getInstance().getShardManager().getGuildById(guildMusicManager.getGuildId());
				if (guild != null) {
					GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, channelId);
					if (channel != null) {
						EmbedBuilder builder = EmbedUtils.getBuilderOf(java.awt.Color.decode("#4d05e8"),
								guildMusicManager.getGuildId());
						builder.setTitle("Jetzt läuft: " + track.getInfo().getTitle());
						builder.addField("Name", "[" + track.getInfo().getAuthor() + " - " + track.getInfo().getTitle()
								+ "](" + track.getInfo().getUri() + ")", false);

						long lengthMs = track.getInfo().getLength();
						long minutes = (lengthMs / 1000) / 60;
						long seconds = (lengthMs / 1000) % 60;
						builder.addField("Länge:", minutes + "min " + seconds + "s", false);

						if (track.getInfo().getArtworkUrl() != null) {
							builder.setImage(track.getInfo().getArtworkUrl());
						} else if (track.getInfo().getUri() != null
								&& track.getInfo().getUri().contains("youtube.com")) {
							// Extract video ID for YouTube thumbnail
							String uri = track.getInfo().getUri();
							String videoId = uri.substring(uri.indexOf("v=") + 2);
							if (videoId.contains("&"))
								videoId = videoId.substring(0, videoId.indexOf('&'));
							builder.setImage("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg");
						}
						channel.sendMessageEmbeds(builder.build()).queue();
					}
				}
			} catch (Exception e) {
				System.err.println("Failed to send now playing embed: " + e.getMessage());
			}
		}

		try {
			long datetime = Long.parseLong(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuuMMddHHmmss")));
			new MusicLogDAO().insertLog(track.getInfo().getTitle(), track.getInfo().getAuthor(),
					guildMusicManager.getGuildId(), datetime).join();
		} catch (Exception e) {
			System.err.println("Failed to log track to SQLite: " + e.getMessage());
		}
	}

	public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
		if (endReason.getMayStartNext()) {
			if (repeating && lastTrack != null) {
				String uri = lastTrack.getInfo().getUri();
				if (uri != null) {
					this.guildMusicManager.getLink().getNode().loadItem(uri)
							.subscribe(new AudioLoadResultHandler(guildMusicManager, AudioLoadOption.APPEND, 0));
				} else {
					log.warn("Track uri from last track was null although lastTrack wasn't null - lastTrack: {}",
							lastTrack.getInfo());
				}
			}

			final var nextTrack = this.queue.poll();

			if (nextTrack != null) {
				this.startTrack(nextTrack);
			} else {
				this.guildMusicManager.getLink().createOrUpdatePlayer().setTrack(null).subscribe(_ -> {
				}, e -> System.err.println("Lavalink operation failed: " + e.getMessage()));
				Guild guild = K7Bot.getInstance().getShardManager().getGuildById(guildMusicManager.getGuildId());
				if (guild != null) {
					guild.getJDA().getDirectAudioController().disconnect(guild);
				}
			}
		}
	}

	private void startTrack(Track track) {
		this.guildMusicManager.getLink().createOrUpdatePlayer().setTrack(track).setVolume(this.volume).subscribe(_ -> {
		}, e -> System.err.println("Lavalink operation failed: " + e.getMessage()));
	}

	public void nextTrack() {
		Track nextTrack = this.queue.poll();
		if (nextTrack != null) {
			startTrack(nextTrack);
		} else {
			this.guildMusicManager.getLink().createOrUpdatePlayer().setTrack(null).subscribe(_ -> {
			}, e -> System.err.println("Lavalink operation failed: " + e.getMessage()));
			Guild guild = K7Bot.getInstance().getShardManager().getGuildById(guildMusicManager.getGuildId());
			if (guild != null) {
				guild.getJDA().getDirectAudioController().disconnect(guild);
			}
		}
	}

	public void setPaused(boolean pause) {
		this.guildMusicManager.getLink().createOrUpdatePlayer().setPaused(pause).subscribe(_ -> {
		}, e -> System.err.println("Lavalink operation failed: " + e.getMessage()));
	}

	public void setVolume(int volume) {
		this.volume = volume;
		this.guildMusicManager.getLink().createOrUpdatePlayer().setVolume(volume).subscribe(_ -> {
		}, e -> System.err.println("Lavalink operation failed: " + e.getMessage()));
	}

	public void setPosition(long positionMs) {
		this.guildMusicManager.getLink().createOrUpdatePlayer().setPosition(positionMs).subscribe(_ -> {
		}, e -> System.err.println("Lavalink operation failed: " + e.getMessage()));
	}

	public void setSpeed(double speed) {
		if (speed <= 0)
			speed = 0.1;
		this.speed = speed;
		this.applyFilters();
	}

	public void setPitch(double pitch) {
		if (pitch <= 0)
			pitch = 0.1;
		this.pitch = pitch;
		this.applyFilters();
	}

	@SuppressWarnings("unused")
	public void setRate(double rate) {
		if (rate <= 0)
			rate = 0.1;
		this.rate = rate;
		this.applyFilters();
	}

	public void setEQ(float[] gains) {
		for (int i = 0; i < gains.length && i < 15; i++) {
			this.filterBuilder.setEqualizerBand(i, gains[i]);
		}
		this.applyFilters();
	}

	private void applyFilters() {
		this.filterBuilder.setTimescale(new Timescale(this.speed, this.pitch, this.rate));
		this.guildMusicManager.getLink().createOrUpdatePlayer().setFilters(this.filterBuilder.build()).subscribe(_ -> {
		}, e -> System.err.println("Lavalink operation failed: " + e.getMessage()));
	}

	public void shuffle() {
		List<Track> list = new ArrayList<>(this.queue);
		Collections.shuffle(list);
		this.queue.clear();
		this.queue.addAll(list);
	}

	public void setRepeating(boolean repeating) {
		this.repeating = repeating;
	}

	public boolean isRepeating() {
		return repeating;
	}

	public void forward(long positionMs) {
		var player = this.guildMusicManager.getOrCreatePlayer();
		if (player == null)
			return;
		var track = player.getTrack();
		if (track != null) {
			long newPos = player.getPosition() + positionMs;
			if (newPos > track.getInfo().getLength()) {
				newPos = track.getInfo().getLength();
			}
			player.setPosition(newPos).subscribe(_ -> {
			}, e -> System.err.println("Lavalink operation failed: " + e.getMessage()));
		}
	}

	public void back(long positionMs) {
		var player = this.guildMusicManager.getOrCreatePlayer();
		if (player == null)
			return;
		var track = player.getTrack();
		if (track != null) {
			long newPos = player.getPosition() - positionMs;
			if (newPos < 0) {
				newPos = 0;
			}
			player.setPosition(newPos).subscribe(_ -> {
			}, e -> System.err.println("Lavalink operation failed: " + e.getMessage()));
		}
	}

	@SuppressWarnings("unused")
	public void clearFilters() {
		this.speed = 1.0;
		this.pitch = 1.0;
		this.rate = 1.0;
		this.filterBuilder = new FilterBuilder();
		this.guildMusicManager.getLink().createOrUpdatePlayer().setFilters(this.filterBuilder.build()).subscribe(_ -> {
		}, e -> System.err.println("Lavalink operation failed: " + e.getMessage()));
	}
}
