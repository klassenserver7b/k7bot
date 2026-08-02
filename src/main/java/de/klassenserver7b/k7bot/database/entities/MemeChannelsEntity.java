/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "memechannels")
public class MemeChannelsEntity {
	@Id
	@Column(name = "channelId")
	private Long channelId;

	public MemeChannelsEntity() {
	}

	public MemeChannelsEntity(Long channelId) {
		this.channelId = channelId;
	}

	public Long getChannelId() {
		return channelId;
	}

	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}
}
