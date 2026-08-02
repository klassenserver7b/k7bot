/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import jakarta.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "createdprivatevcs")
@IdClass(CreatedPrivateVcsId.class)
public class CreatedPrivateVcsEntity {
	@Id
	@Column(name = "guildId")
	private Long guildId;
	@Id
	@Column(name = "channelId")
	private Long channelId;

	public CreatedPrivateVcsEntity() {
	}

	public CreatedPrivateVcsEntity(Long guildId, Long channelId) {
		this.guildId = guildId;
		this.channelId = channelId;
	}

	public Long getGuildId() {
		return guildId;
	}

	public void setGuildId(Long guildId) {
		this.guildId = guildId;
	}

	public Long getChannelId() {
		return channelId;
	}

	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}
}
