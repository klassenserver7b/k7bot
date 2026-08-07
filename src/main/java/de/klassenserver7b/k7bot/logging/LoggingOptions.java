/* (C)2026 */
package de.klassenserver7b.k7bot.logging;

/**
 * Enum representing every logging option which can be toggled by the logging
 * config. <br>
 * Used for guild specific logging selection.
 *
 * @author Klassenserver7b
 */
public enum LoggingOptions {
	// @formatter:off
	
	ALL(1),
	
	/**
	 * Log member events
	 */
	MEMBERS(10),
	MEMBER_JOIN(11),
	MEMBER_LEAVE(12),
	MEMBER_UPDATE_NICKNAME(13),
	MEMBER_ROLE_ADD(14),
	MEMBER_ROLE_REMOVE(15),
	
	/**
	 * Log moderation events
	 */
	MODERATION(20),
	BAN(21),
	UNBAN(22),
	TIMEOUT(23),

	AUDITLOG_ENTRY_CREATE(24),
	AUTOMOD_EXECUTED(25),
	AUTOMOD_RULE_CREATE(26),
	AUTOMOD_RULE_DELETE(27),
	AUTOMOD_RULE_UPDATE(28),
	
	/**
	 * Log channel events
	 */
	ROLES(30),
	ROLE_CREATE(31),
	ROLE_DELETE(32),
	ROLE_NAME(33),
	ROLE_PERMISSIONS(34),
	ROLE_POSITION(35),
	
	/**
	 * Log channel events
	 */
	CHANNELS(40),
	CHANNEL_CREATE(41),
	CHANNEL_DELETE(42),
	CHANNEL_UPDATE_NAME(43),
	CHANNEL_UPDATE_POSITION(44),
	CHANNEL_UPDATE_TYPE(45),
	
	/**
	 * Log message events
	 */
	MESSAGES(50),
	MESSAGE_EDITED(51),
	MESSAGE_DELETED(52),
	MESSAGE_BULK_DELETED(53),
	
	/**
	 * Log voice events
	 */
	VOICE(60),
	VOICE_JOIN(61),
	VOICE_LEAVE(62),
	VOICE_MOVE(63),
	GUILD_MUTE(64),
	GUILD_DEAF(65),
	STREAM_START(66),
	STREAM_STOP(67),
	VIDEO_START(68),
	VIDEO_STOP(69),
	
	/**
     * Log invites events.
     */
	INVITES(70),
	INVITE_CREATE(71),
	INVITE_DELETE(72),
	
	/**
     * Log emojis and stickers events.
     */
	EMOJIS(80),
	EMOJI_ADD(81),
	EMOJI_REMOVE(82),
	STICKER_ADD(83),
	STICKER_REMOVE(84),
	
	/**
     * Log sheduled-events events.
     */
	EVENTS(90),
	EVENT_CREATE(91),
	EVENT_DELETE(92),
	EVENT_UPDATE_STATUS(93),
	EVENT_UPDATE_NAME(94),
	EVENT_UPDATE_STARTTIME(95),
	EVENT_UPDATE_ENDTIME(96),
	EVENT_UPDATE_LOCATION(97),
	EVENT_MEMBER_JOIN(98),
	EVENT_MEMBER_LEAVE(99),

	/**
	 * Unknown event option.
	 */
	UNKNOWN(-1);
	
	// @formatter:on
	private final int id;

	/**
	 * Constructs a LoggingOptions with the specified ID.
	 *
	 * @param id The identifier for the logging option.
	 */
	LoggingOptions(int id) {
		this.id = id;
	}

	/**
	 * Retrieves the LoggingOptions enum constant associated with the given ID.
	 *
	 * @param id The ID to look up.
	 * @return The LoggingOptions enum constant with the specified ID, or UNKNOWN if
	 *         not found.
	 */
	public static LoggingOptions byId(int id) {
		for (LoggingOptions opt : values()) {
			if (opt.id == id) {
				return opt;
			}
		}

		return UNKNOWN;

	}

	/**
	 * Gets the ID associated with the logging option.
	 *
	 * @return id The ID of the logging option.
	 */
	public int getId() {
		return id;
	}

}
