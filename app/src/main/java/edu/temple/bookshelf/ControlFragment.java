package edu.temple.bookshelf;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;


public class ControlFragment extends Fragment {

    ControlFragmentInterface parentActivity;

    public ControlFragment() {
        // Required empty public constructor
    }

    public static ControlFragment newInstance(String param1, String param2) {
        ControlFragment fragment = new ControlFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        ImageButton playButton = layout.findViewById(R.id.play_button);
        ImageButton pauseButton = layout.findViewById(R.id.pause_button);
        ImageButton stopButton = layout.findViewById(R.id.stop_button);
        SeekBar seekBar = layout.findViewById(R.id.seek_bar);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.playAudio();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.pauseAudio();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.stopAudio();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                parentActivity.seekAudio();
            }
        });

        return layout;
    }

    interface ControlFragmentInterface {
        void playAudio();
        void pauseAudio();
        void stopAudio();
        void seekAudio();
    }
}