/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "messagelogs")
public class MessageLogsEntity {
	@Id
	@Column(name = "messageId")
	private Long messageId;
	@Column(name = "guildId")
	private Long guildId;
	@Column(name = "timestamp")
	private Long timestamp;
	@Column(name = "authorId")
	private Long authorId;
	@Column(name = "messageText", length = 4000)
	private String messageText;

	public MessageLogsEntity() {
	}

	public MessageLogsEntity(Long messageId, Long guildId, Long timestamp, Long authorId, String messageText) {
		this.messageId = messageId;
		this.guildId = guildId;
		this.timestamp = timestamp;
		this.authorId = authorId;
		this.messageText = messageText;
	}

	public Long getMessageId() {
		return messageId;
	}

	public Long getGuildId() {
		return guildId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public String getMessageText() {
		return messageText;
	}
}
