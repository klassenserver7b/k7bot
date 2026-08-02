/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("unused")
public class ReactRolesId implements Serializable {
	private Long guildId;
	private Long channelId;
	private Long messageId;
	private String emote;

	public ReactRolesId() {
	}

	public ReactRolesId(Long guildId, Long channelId, Long messageId, String emote) {
		this.guildId = guildId;
		this.channelId = channelId;
		this.messageId = messageId;
		this.emote = emote;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ReactRolesId that = (ReactRolesId) o;
		return Objects.equals(guildId, that.guildId) && Objects.equals(channelId, that.channelId)
				&& Objects.equals(messageId, that.messageId) && Objects.equals(emote, that.emote);
	}

	@Override
	public int hashCode() {
		return Objects.hash(guildId, channelId, messageId, emote);
	}
}
