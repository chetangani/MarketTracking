package com.tvd.markettracking.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tvd.markettracking.posting.SendingData;
import com.tvd.markettracking.posting.SendingData.SendingLocationData;
import com.tvd.markettracking.services.LocationTrace;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.tvd.markettracking.values.ConstantValues.SHARED_PREFS_NAME;

public class LocationReceiver extends BroadcastReceiver {
    SharedPreferences settings;

    @Override
    public void onReceive(Context context, Intent intent) {
        settings = context.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        if (isInternetOn(context)) {
            SendingData sendingData = new SendingData();
            SendingLocationData sendingLocationData = sendingData.new SendingLocationData();
            sendingLocationData.execute(settings.getString("MTP_ID", ""), ""+LocationTrace.mLastLocation.getLongitude(),
                    ""+LocationTrace.mLastLocation.getLatitude());
        }
    }

    public final boolean isInternetOn(Context context) {
        ConnectivityManager connect = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        if (connect.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
                connect.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
                connect.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
                connect.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else if (connect.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||
                connect.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }
}
