/**
 *
 */
package de.klassenserver7b.k7bot.music.utilities;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import de.klassenserver7b.k7bot.music.utilities.BotAudioEffectsManager.FilterTypes;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public abstract class BotEqualizer {

    /**
     * Applies the provided {@link EqualizerPreset}
     *
     * @param preset the {@link EqualizerPreset} to use
     * @param effman the {@link BotAudioEffectsManager} to apply the filters to
     */
    public static void setEQMode(EqualizerPreset preset, BotAudioEffectsManager effman) {

        if (preset == EqualizerPreset.OFF) {
            effman.removeAudioFilterFunction(FilterTypes.EQ);
            return;
        }

        effman.addAudioFilterFunction(FilterTypes.EQ, ((track, format, output) -> {

            float[] bands = preset.getBands();

            Equalizer eq = new Equalizer(format.channelCount, output);

            for (int i = 0; i < bands.length; i++) {
                eq.setGain(i, bands[i]);
            }

            return eq;
        }));

    }

    public enum EqualizerPreset {

        /**
         *
         */
        UNKNOWN(-1, new float[]{}),

        /**
         *
         */
        OFF(0, new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f}),

        /**
         *
         */
        ULTRA_LOW_BASS(1,
                new float[]{-0.2f, -0.15f, -0.1f, -0.05f, 0.0f, 0.05f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f,
                        0.1f}),

        /**
         *
         */
        LOW_BASS(2,
                new float[]{-0.15f, -0.1f, -0.05f, -0.05f, 0.0f, 0.0f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.1f,
                        0.1f, 0.1f}),

        /**
         *
         */
        LESS_LOW_BASS(3,
                new float[]{-0.1f, -0.75f, -0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f,
                        0.05f, 0.05f}),

        /**
         *
         */
        LESS_BASS_BOOST(4,
                new float[]{0.1f, 0.75f, 0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.05f, -0.05f, -0.05f, -0.05f, -0.1f,
                        -0.05f, -0.05f}),

        /**
         *
         */
        BASS_BOOST(5,
                new float[]{0.15f, 0.1f, 0.05f, 0.05f, 0.0f, -0.0f, -0.05f, -0.05f, -0.05f, -0.05f, -0.05f, -0.05f,
                        -0.1f, -0.1f, -0.1f}),

        /**
         *
         */
        ULTRA_BASS_BOOST(6, new float[]{0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,
                -0.1f, -0.1f, -0.1f, -0.1f});

        private final int id;
        private final float[] bands;

        /**
         * @param id    The K7Bot id key used to represent the {@link EqualizerPreset}.
         * @param bands The bands of the EQ-Preset
         */
        EqualizerPreset(int id, float[] bands) {
            this.id = id;
            this.bands = bands;
        }

        /**
         * The K7Bot id key used to represent the {@link EqualizerPreset}.
         *
         * @return The id key used by K7Bot for this EQ-Preset.
         */
        public int getId() {
            return id;
        }

        /**
         * Used to obtain the bands of the EQ-Preset
         *
         * @return The Bands as a float-array
         */
        @NotNull
        public float[] getBands() {
            return bands;
        }

        /**
         * Static accessor for retrieving a {@link EqualizerPreset} based on its K7Bot
         * id key.
         *
         * @param id The id key of the requested target type.
         * @return The {@link EqualizerPreset} that is referred to by the provided key.
         * If the id key is unknown, {@link #UNKNOWN} is returned.
         */
        @NotNull
        public static EqualizerPreset fromId(int id) {
            for (EqualizerPreset type : values()) {
                if (type.id == id)
                    return type;
            }
            return UNKNOWN;
        }
    }

}
