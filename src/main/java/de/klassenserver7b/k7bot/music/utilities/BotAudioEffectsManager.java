/**
 *
 */
package de.klassenserver7b.k7bot.music.utilities;

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.klassenserver7b.k7bot.util.TriFunction;
import net.dv8tion.jda.api.entities.Guild;

import java.util.*;

/**
 * @author Klassenserver7b
 */
public class BotAudioEffectsManager {

    private final AudioPlayer player;

    private final LinkedHashMap<FilterTypes, TriFunction<AudioTrack, AudioDataFormat, FloatPcmAudioFilter, FloatPcmAudioFilter>> filterfuncs;
    private static final HashMap<AudioPlayer, BotAudioEffectsManager> effmans = new HashMap<>();

    /**
     * @param player The {@link AudioPlayer} used for the {@link Guild}
     */
    private BotAudioEffectsManager(AudioPlayer player) {
        this.filterfuncs = new LinkedHashMap<>();
        this.player = player;
    }

    /**
     * @param p The {@link AudioPlayer} used for the {@link Guild}
     * @return The newly created {@link BotAudioEffectsManager} object for this
     * guild/player
     */
    public static BotAudioEffectsManager getAudioEffectsManager(AudioPlayer p) {

        return effmans.computeIfAbsent(p, BotAudioEffectsManager::new);
    }

    /**
     * Adds the provided {@link TriFunction AudioFilterFunction} to the currently
     * used filters and applys them
     *
     * @param type   the {@link FilterTypes} of the {@link TriFunction
     *               AudioFilterFunction}
     * @param filter The {@link TriFunction AudioFilterFunction} to add
     */
    public void addAudioFilterFunction(FilterTypes type,
                                       TriFunction<AudioTrack, AudioDataFormat, FloatPcmAudioFilter, FloatPcmAudioFilter> filter) {
        filterfuncs.put(type, filter);

        applyFilters();
    }

    /**
     * Clears all previously used filters and applys the provided {@link TriFunction
     * AudioFilterFunction}
     *
     * @param type   the {@link FilterTypes} of the {@link TriFunction
     *               AudioFilterFunction}
     * @param filter The {@link TriFunction AudioFilterFunction} to use
     */
    @SuppressWarnings("unused")
    public void setAudioFilterFunction(FilterTypes type,
                                       TriFunction<AudioTrack, AudioDataFormat, FloatPcmAudioFilter, FloatPcmAudioFilter> filter) {
        filterfuncs.clear();
        filterfuncs.put(type, filter);
        applyFilters();
    }

    /**
     * Removes every {@link TriFunction AudioFilterFunction} matching the given
     * {@link FilterTypes}
     *
     * @param type the {@link FilterTypes} to remove
     */
    public void removeAudioFilterFunction(FilterTypes type) {
        TriFunction<AudioTrack, AudioDataFormat, FloatPcmAudioFilter, FloatPcmAudioFilter> oldfilter = filterfuncs.remove(type);

        if (oldfilter == null) {
            return;
        }

        applyFilters();
    }

    /**
     * Adds all provided {@link TriFunction AudioFilterFunction} to the currently
     * used filters and applies them
     *
     * @param audiofilters A Hashmap respresenting the {@link FilterTypes
     *                     FilterTypes} and coresponding {@link TriFunction
     *                     AudioFilterFunctions}
     */
    @SuppressWarnings("unused")
    public void addAudioFilterFunctions(
            HashMap<FilterTypes, TriFunction<AudioTrack, AudioDataFormat, FloatPcmAudioFilter, FloatPcmAudioFilter>> audiofilters) {

        filterfuncs.putAll(audiofilters);
        applyFilters();
    }

    /**
     * Clears all previously used filters and applys all provided {@link TriFunction
     * AudioFilterFunction}
     *
     * @param audiofilters A Hashmap respresenting the {@link FilterTypes
     *                     FilterTypes} and coresponding {@link TriFunction
     *                     AudioFilterFunctions}
     */
    @SuppressWarnings("unused")
    public void setAudioFilterFunctions(
            HashMap<FilterTypes, TriFunction<AudioTrack, AudioDataFormat, FloatPcmAudioFilter, FloatPcmAudioFilter>> audiofilters) {
        filterfuncs.clear();
        filterfuncs.putAll(audiofilters);
        applyFilters();
    }

    /**
     * Clears all previously used filters
     */
    public void clearFilters() {
        filterfuncs.clear();
        applyFilters();
    }

    /**
     *
     */
    protected void applyFilters() {
        List<TriFunction<AudioTrack, AudioDataFormat, FloatPcmAudioFilter, FloatPcmAudioFilter>> filters = filterfuncs.values().stream().toList();

        player.setFilterFactory((track, format, output) -> Collections.singletonList(recurseFilters(track, format, output, filters, 0)));
    }

    protected FloatPcmAudioFilter recurseFilters(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter ufilter, List<TriFunction<AudioTrack, AudioDataFormat, FloatPcmAudioFilter, FloatPcmAudioFilter>> filters, int index) {

        if (index == filters.size()) {
            return ufilter;
        }

        return filters.get(index).apply(track, format, recurseFilters(track, format, ufilter, filters, index + 1));
    }

    public enum FilterTypes {

        EQ, SPEED;

        public static FilterTypes fromId(int id) {
            for (FilterTypes type : values()) {
                if (type.ordinal() == id)
                    return type;
            }
            throw new IllegalArgumentException("Invalid FilterType Id");
        }
    }

}
