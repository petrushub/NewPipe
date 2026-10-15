package org.schabi.newpipe.radio;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A live radio station that can be played from the drawer's "Radio" section.
 */
public final class RadioStation {
    public static final List<RadioStation> STATIONS = Collections.unmodifiableList(Arrays.asList(
            new RadioStation("ser_barcelona", "Radio Cadena SER Barcelona",
                    "https://playerservices.streamtheworld.com/api/livestream-redirect/"
                            + "SER_BARCELONA.mp3"),
            new RadioStation("ser_salamanca", "Radio Cadena SER Salamanca",
                    "https://playerservices.streamtheworld.com/api/livestream-redirect/"
                            + "SER_SALAMANCA.mp3")));

    @NonNull
    public final String id;
    @NonNull
    public final String name;
    @NonNull
    public final String streamUrl;

    private RadioStation(@NonNull final String id, @NonNull final String name,
                         @NonNull final String streamUrl) {
        this.id = id;
        this.name = name;
        this.streamUrl = streamUrl;
    }

    public static RadioStation findById(final String id) {
        for (final RadioStation station : STATIONS) {
            if (station.id.equals(id)) {
                return station;
            }
        }
        return null;
    }
}
