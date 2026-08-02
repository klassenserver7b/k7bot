/* (C)2026 */
package de.klassenserver7b.k7bot.database.entities;

import jakarta.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "modlogs")
public class ModLogEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "guildId")
	private Long guildId;

	@Column(name = "memberId", nullable = false)
	private Long memberId;

	@Column(name = "requesterId", nullable = false)
	private Long requesterId;

	@Column(name = "memberName")
	private String memberName;

	@Column(name = "requesterName")
	private String requesterName;

	@Column(name = "action")
	private String action;

	@Column(name = "reason")
	private String reason;

	@Column(name = "date")
	private String date;

	public ModLogEntity() {
	}

	public ModLogEntity(Long guildId, Long memberId, Long requesterId, String memberName, String requesterName,
			String action, String reason, String date) {
		this.guildId = guildId;
		this.memberId = memberId;
		this.requesterId = requesterId;
		this.memberName = memberName;
		this.requesterName = requesterName;
		this.action = action;
		this.reason = reason;
		this.date = date;
	}

	public Long getId() {
		return id;
	}

	public Long getGuildId() {
		return guildId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public Long getRequesterId() {
		return requesterId;
	}

	public String getMemberName() {
		return memberName;
	}

	public String getRequesterName() {
		return requesterName;
	}

	public String getAction() {
		return action;
	}

	public String getReason() {
		return reason;
	}

	public String getDate() {
		return date;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setGuildId(Long guildId) {
		this.guildId = guildId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public void setRequesterId(Long requesterId) {
		this.requesterId = requesterId;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public void setRequesterName(String requesterName) {
		this.requesterName = requesterName;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
