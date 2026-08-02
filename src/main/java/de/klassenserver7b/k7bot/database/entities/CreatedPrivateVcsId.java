/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("unused")
public class CreatedPrivateVcsId implements Serializable {
	private Long guildId;
	private Long channelId;

	public CreatedPrivateVcsId() {
	}

	public CreatedPrivateVcsId(Long guildId, Long channelId) {
		this.guildId = guildId;
		this.channelId = channelId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CreatedPrivateVcsId that = (CreatedPrivateVcsId) o;
		return Objects.equals(guildId, that.guildId) && Objects.equals(channelId, that.channelId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(guildId, channelId);
	}
}
