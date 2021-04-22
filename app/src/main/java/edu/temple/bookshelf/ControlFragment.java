package edu.temple.bookshelf;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


public class ControlFragment extends Fragment {

    ControlFragmentInterface parentActivity;

    public static final String ARG_DURATION = "duration";
    public static final String ARG_CURRENT = "current";

    private static final String ARG_NOW_PLAYING = "now playing";
    private static final String ARG_SEEK_MAX= "seek duration";

    private SeekBar seekBar;
    private TextView nowPlaying;

    public ControlFragment() {
        // Required empty public constructor
    }

    public static ControlFragment newInstance() {
        ControlFragment fragment = new ControlFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FRAG", "Control Fragment created.");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity parentActivityTemp = getActivity();
        if (parentActivityTemp instanceof ListFragment.ListFragmentInterface) {
            parentActivity = (ControlFragment.ControlFragmentInterface) parentActivityTemp;
        }
        else {
            throw new RuntimeException("ControlFragmentInterface must be implemented in attached activity.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_control, container, false);

        nowPlaying = layout.findViewById(R.id.now_playing);
        seekBar = layout.findViewById(R.id.seek_bar);

        if(savedInstanceState != null){
            nowPlaying.setText(savedInstanceState.getString(ARG_NOW_PLAYING));
            setDuration(savedInstanceState.getInt(ARG_SEEK_MAX));
        } else {
            nowPlaying.setText(getString(R.string.now_playing));
        }

        parentActivity.restoreControllerUI();

        ImageButton playButton = layout.findViewById(R.id.play_button);
        ImageButton pauseButton = layout.findViewById(R.id.pause_button);
        ImageButton stopButton = layout.findViewById(R.id.stop_button);

        playButton.setOnClickListener(v -> parentActivity.playAudio());
        pauseButton.setOnClickListener(v -> parentActivity.pauseAudio());
        stopButton.setOnClickListener(v -> parentActivity.stopAudio());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // unneeded
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // unneeded
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                parentActivity.seekAudio(seekBar.getProgress());
            }
        });

        return layout;
    }

    public void setText(String nowPlayingTitle) {
        String npString = getString(R.string.now_playing) + nowPlayingTitle;
        nowPlaying.setText(npString);
    }

    public void setDuration(int duration) {
        seekBar.setMax(duration);
        Log.d("SEEK", "Duration is " + seekBar.getMax());
    }
    public void setProgress(int progress) {
        seekBar.setProgress(progress);
        Log.d("SEEK", "Position: " + seekBar.getProgress());
    }

    interface ControlFragmentInterface {
        void restoreControllerUI();
        void playAudio();
        void pauseAudio();
        void stopAudio();
        void seekAudio(int progress);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_NOW_PLAYING, nowPlaying.getText().toString());
        outState.putInt(ARG_SEEK_MAX, seekBar.getMax());
    }
}