/* (C)2026 */
package de.klassenserver7b.k7bot.audio.commands.common;

public enum AudioCommandType {
	PLAY("play", "p"), ADD_QUEUE("addqueue", "aq"), PLAY_NEXT("playnext", "pn"), STOP("stop"), PAUSE("pause"),
	RESUME("resume"), SKIP("skip", "s"), QUEUE("queue", "q"), CLEAR_QUEUE("clearqueue", "cq"),
	NOW_PLAYING("nowplaying", "np"), VOLUME("volume", "vol", "v"), SEEK("seek"), FORWARD("forward", "f"),
	BACK("back", "b"), LOOP("loop"), SHUFFLE("shuffle"), SPEED("speed"), PITCH("pitch"), EQ("eq"), LYRICS("lyrics");

	private final String[] aliases;

	AudioCommandType(String... aliases) {
		this.aliases = aliases;
	}

	public String[] getAliases() {
		return aliases;
	}

	public static AudioCommandType fromString(String commandStr) {
		for (AudioCommandType type : values()) {
			for (String alias : type.aliases) {
				if (alias.equalsIgnoreCase(commandStr)) {
					return type;
				}
			}
		}
		return null;
	}
}
