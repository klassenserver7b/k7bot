/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "botutil")
public class BotUtilEntity {

	@Id
	@Column(name = "guildId")
	private Long guildId;

	@Column(name = "syschannelId")
	private Long syschannelId;

	@Column(name = "prefix", nullable = false)
	private String prefix = "-";

	public BotUtilEntity() {
	}

	public BotUtilEntity(Long guildId, Long syschannelId, String prefix) {
		this.guildId = guildId;
		this.syschannelId = syschannelId;
		this.prefix = prefix;
	}

	public Long getGuildId() {
		return guildId;
	}

	public void setGuildId(Long guildId) {
		this.guildId = guildId;
	}

	public Long getSyschannelId() {
		return syschannelId;
	}

	public void setSyschannelId(Long syschannelId) {
		this.syschannelId = syschannelId;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
