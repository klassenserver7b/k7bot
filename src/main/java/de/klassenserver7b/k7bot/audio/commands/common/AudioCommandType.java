/* (C)2026 */
package de.klassenserver7b.k7bot.audio.commands.common;

import org.jspecify.annotations.Nullable;

@SuppressWarnings("SpellCheckingInspection")
public enum AudioCommandType {
	PLAY("play", "p"), ADD_QUEUE("addqueue", "aq"), PLAY_NEXT("playnext", "pn"), STOP("stop"), PAUSE("pause"),
	RESUME("resume", "res"), SKIP("skip", "s"), QUEUE("queue", "q", "ql"), CLEAR_QUEUE("clearqueue", "cq"),
	NOW_PLAYING("nowplaying", "np"), VOLUME("volume", "vol", "v"), SEEK("seek"), FORWARD("forward", "f"),
	BACK("back", "b"), LOOP("loop"), SHUFFLE("shuffle", "random"), SPEED("speed"), PITCH("pitch"), EQ("eq"),
	LYRICS("lyrics");

	private final String[] aliases;

	AudioCommandType(String... aliases) {
		this.aliases = aliases;
	}

	public static @Nullable AudioCommandType fromString(String commandStr) {
		for (AudioCommandType type : values()) {
			for (String alias : type.aliases) {
				if (alias.equalsIgnoreCase(commandStr)) {
					return type;
				}
			}
		}
		return null;
	}

	public String[] getAliases() {
		return aliases;
	}
}
