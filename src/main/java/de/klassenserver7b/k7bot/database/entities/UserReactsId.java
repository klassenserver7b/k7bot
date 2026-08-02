/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("unused")
public class UserReactsId implements Serializable {
	private Long userId;
	private Long guildId;
	private Long messageId;
	private String emote;

	public UserReactsId() {
	}

	public UserReactsId(Long userId, Long guildId, Long messageId, String emote) {
		this.userId = userId;
		this.guildId = guildId;
		this.messageId = messageId;
		this.emote = emote;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserReactsId that = (UserReactsId) o;
		return Objects.equals(userId, that.userId) && Objects.equals(guildId, that.guildId)
				&& Objects.equals(messageId, that.messageId) && Objects.equals(emote, that.emote);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, guildId, messageId, emote);
	}
}
