package org.schabi.newpipe.radio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.schabi.newpipe.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Lists the available radio stations and lets the user start or stop them.
 */
public class RadioFragment extends Fragment {
    private final Map<String, TextView> rows = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_radio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view,
                              @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LinearLayout list = view.findViewById(R.id.radioStationList);
        final LayoutInflater inflater = LayoutInflater.from(requireContext());
        rows.clear();
        for (final RadioStation station : RadioStation.STATIONS) {
            final TextView row = (TextView) inflater.inflate(R.layout.item_radio_station,
                    list, false);
            row.setText(station.name);
            row.setOnClickListener(v -> {
                if (station.id.equals(RadioService.getPlayingStationId().getValue())) {
                    RadioService.stop(requireContext());
                } else {
                    RadioService.play(requireContext(), station);
                }
            });
            list.addView(row);
            rows.put(station.id, row);
        }

        RadioService.getPlayingStationId().observe(getViewLifecycleOwner(), playingId -> {
            for (final Map.Entry<String, TextView> entry : rows.entrySet()) {
                final boolean playing = entry.getKey().equals(playingId);
                entry.getValue().setCompoundDrawablesRelativeWithIntrinsicBounds(
                        playing ? R.drawable.ic_pause : R.drawable.ic_play_arrow, 0, 0, 0);
                entry.getValue().setSelected(playing);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.radio);
        }
    }
}
