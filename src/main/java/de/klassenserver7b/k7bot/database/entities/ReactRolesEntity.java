/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import jakarta.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "reactroles")
@IdClass(ReactRolesId.class)
public class ReactRolesEntity {
	@Id
	@Column(name = "guildId")
	private Long guildId;
	@Id
	@Column(name = "channelId")
	private Long channelId;
	@Id
	@Column(name = "messageId")
	private Long messageId;
	@Id
	@Column(name = "emote")
	private String emote;
	@Column(name = "roleId", nullable = false)
	private Long roleId;

	public ReactRolesEntity() {
	}

	public ReactRolesEntity(Long guildId, Long channelId, Long messageId, String emote, Long roleId) {
		this.guildId = guildId;
		this.channelId = channelId;
		this.messageId = messageId;
		this.emote = emote;
		this.roleId = roleId;
	}

	public Long getGuildId() {
		return guildId;
	}

	public Long getChannelId() {
		return channelId;
	}

	public Long getMessageId() {
		return messageId;
	}

	public String getEmote() {
		return emote;
	}

	public Long getRoleId() {
		return roleId;
	}
}
