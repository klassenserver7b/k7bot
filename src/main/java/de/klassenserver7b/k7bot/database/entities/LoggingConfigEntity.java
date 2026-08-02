/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "loggingConfig")
public class LoggingConfigEntity {

	@Id
	@Column(name = "guildId")
	private Long guildId;

	@Column(name = "optionJson", nullable = false)
	private String optionJson = "[]";

	public LoggingConfigEntity() {
	}

	public Long getGuildId() {
		return guildId;
	}

	public void setGuildId(Long guildId) {
		this.guildId = guildId;
	}

	public String getOptionJson() {
		return optionJson;
	}

	public void setOptionJson(String optionJson) {
		this.optionJson = optionJson;
	}
}
