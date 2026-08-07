/* (C)2026 */
package de.klassenserver7b.k7bot.audio.commands.common;

@SuppressWarnings("unused")
public enum EQPreset {
	OFF(0, "Off", new float[15]),
	ULTRA_LOW_BASS(1, "Ultra Low Bass",
			new float[] { -0.2f, -0.15f, -0.1f, -0.05f, 0.0f, 0.05f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f,
					0.1f }),
	LOW_BASS(2, "Low Bass",
			new float[] { -0.15f, -0.1f, -0.05f, -0.05f, 0.0f, 0.0f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.1f,
					0.1f, 0.1f }),
	LESS_LOW_BASS(3, "Less Low Bass",
			new float[] { -0.1f, -0.075f, -0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f,
					0.05f, 0.05f }),
	LESS_BASS_BOOST(4, "Less Bass Boost",
			new float[] { 0.1f, 0.075f, 0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.05f, -0.05f, -0.05f, -0.05f, -0.1f,
					-0.05f, -0.05f }),
	BASS_BOOST(5, "Bass Boost",
			new float[] { 0.15f, 0.1f, 0.05f, 0.05f, 0.0f, -0.0f, -0.05f, -0.05f, -0.05f, -0.05f, -0.05f, -0.05f, -0.1f,
					-0.1f, -0.1f }),
	ULTRA_BASS_BOOST(6, "Ultra Bass Boost", new float[] { 0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f,
			-0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f });

	private final int id;
	private final String name;
	private final float[] bands;

	EQPreset(int id, String name, float[] bands) {
		this.id = id;
		this.name = name;
		this.bands = bands;
	}

	public static EQPreset byId(int id) {
		for (EQPreset preset : values()) {
			if (preset.id == id) {
				return preset;
			}
		}
		return OFF;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public float[] getBands() {
		return bands;
	}
}
