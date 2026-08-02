/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import jakarta.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "musiclogs")
public class MusicLogsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "songname")
	private String songname;
	@Column(name = "songauthor")
	private String songauthor;
	@Column(name = "guildId")
	private Long guildId;
	@Column(name = "timestamp")
	private Long timestamp;

	public MusicLogsEntity() {
	}

	public MusicLogsEntity(String songname, String songauthor, Long guildId, Long timestamp) {
		this.songname = songname;
		this.songauthor = songauthor;
		this.guildId = guildId;
		this.timestamp = timestamp;
	}
}
