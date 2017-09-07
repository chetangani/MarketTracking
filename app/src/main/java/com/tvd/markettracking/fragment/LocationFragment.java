package com.tvd.markettracking.fragment;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tvd.markettracking.MainActivity;
import com.tvd.markettracking.R;
import com.tvd.markettracking.receiver.LocationReceiver;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocationFragment extends Fragment {
    View view;
    Button start_btn, stop_btn;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_location, container, false);

        start_btn = (Button) view.findViewById(R.id.start_btn);
        stop_btn = (Button) view.findViewById(R.id.stop_btn);

        settings = ((MainActivity) getActivity()).getShared();
        editor = ((MainActivity) getActivity()).getEditor();

        if (settings.getString("Start", "").equals("")) {
            stop_btn.setVisibility(View.GONE);
            start_btn.setVisibility(View.VISIBLE);
        } else if (settings.getString("Start", "").equals("No")) {
            stop_btn.setVisibility(View.GONE);
            start_btn.setVisibility(View.VISIBLE);
        } else {
            start_btn.setVisibility(View.GONE);
            stop_btn.setVisibility(View.VISIBLE);
        }

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("Start", "Yes");
                editor.commit();
                start_btn.setVisibility(View.GONE);
                stop_btn.setVisibility(View.VISIBLE);
                startReceiver();
            }
        });

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("Start", "No");
                editor.commit();
                stop_btn.setVisibility(View.GONE);
                start_btn.setVisibility(View.VISIBLE);
                stopReceiver();
            }
        });

        return view;
    }

    private void startReceiver() {
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), LocationReceiver.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_NO_CREATE) != null);
        if (!alarmRunning) {
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 240000, pendingIntent);
        }
    }

    private void stopReceiver() {
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), LocationReceiver.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_NO_CREATE) != null);
        if (alarmRunning) {
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
            alarmManager.cancel(pendingIntent);
        }
    }

}
