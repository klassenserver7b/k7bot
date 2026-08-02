/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import jakarta.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "slashcommandlog")
public class SlashCommandLogEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "command")
	private String command;

	@Column(name = "guildId")
	private Long guildId;

	@Column(name = "userId")
	private Long userId;

	@Column(name = "timestamp")
	private Long timestamp;

	@Column(name = "commandstring")
	private String commandstring;

	public SlashCommandLogEntity() {
	}

	public SlashCommandLogEntity(String command, Long guildId, Long userId, Long timestamp, String commandstring) {
		this.command = command;
		this.guildId = guildId;
		this.userId = userId;
		this.timestamp = timestamp;
		this.commandstring = commandstring;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Long getGuildId() {
		return guildId;
	}

	public void setGuildId(Long guildId) {
		this.guildId = guildId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getCommandstring() {
		return commandstring;
	}

	public void setCommandstring(String commandstring) {
		this.commandstring = commandstring;
	}
}
