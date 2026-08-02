/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import jakarta.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "userreacts")
@IdClass(UserReactsId.class)
public class UserReactsEntity {
	@Id
	@Column(name = "userId")
	private Long userId;
	@Id
	@Column(name = "guildId")
	private Long guildId;
	@Id
	@Column(name = "messageId")
	private Long messageId;
	@Id
	@Column(name = "emote")
	private String emote;

	public UserReactsEntity() {
	}

	public UserReactsEntity(Long userId, Long guildId, Long messageId, String emote) {
		this.userId = userId;
		this.guildId = guildId;
		this.messageId = messageId;
		this.emote = emote;
	}
}
