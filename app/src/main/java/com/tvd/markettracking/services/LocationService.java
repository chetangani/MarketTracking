package com.tvd.markettracking.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import com.tvd.markettracking.posting.SendingData;
import com.tvd.markettracking.values.FunctionsCall;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.tvd.markettracking.values.ConstantValues.SHARED_PREFS_NAME;

public class LocationService extends Service {

    SharedPreferences settings;
    SharedPreferences.Editor editor;
    Date lastdatetime = null;
    String lasttime="";
    long minute = 0, interval = 1;
    FunctionsCall functionsCall;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(locationReceiver, new IntentFilter(LocationTrace.BROADCAST_ACTION));
        settings = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        editor = settings.edit();
        editor.apply();
        functionsCall = new FunctionsCall();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String time = intent.getStringExtra("time");
            lasttime = settings.getString("time", "");
            if (!lasttime.equals("")) {
                try {
                    lastdatetime = new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(lasttime);
                    Date currenttime = new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(time);
                    long difftime = currenttime.getTime() - lastdatetime.getTime();
                    long diffSec = difftime / 1000;
                    minute = diffSec / 60;
                    editor.putString("time", time);
                    editor.commit();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                editor.putString("time", time);
                editor.commit();
                minute = interval;
            }
            if (minute >= interval) {
                if (isInternetOn(context)) {
                    SendingData sendingData = new SendingData();
                    SendingData.SendingLocationData sendingLocationData = sendingData.new SendingLocationData();
                    sendingLocationData.execute(settings.getString("MTP_ID", ""), ""+LocationTrace.mLastLocation.getLongitude(),
                            ""+LocationTrace.mLastLocation.getLatitude(), getLocationAddress(context,
                                    LocationTrace.mLastLocation.getLatitude(), LocationTrace.mLastLocation.getLongitude()));
                }
            }
        }
    };

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

    public String getLocationAddress(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        // Get the current location from the input parameter list
        // Create a list to contain the result address
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e2) {
            // Error message to post in the log
            String errorString = "Illegal arguments "
                    + Double.toString(latitude) + " , "
                    + Double.toString(longitude)
                    + " passed to address service";
            e2.printStackTrace();
            return errorString;
        }
        // If the reverse geocode returned an address
        if (addresses != null && addresses.size() > 0) {
            // Get the first address
            Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available), city, and
                 * country name.
                 */
            String addressText = String.format(
                    "%s, %s, %s, %s",
                    // If there's a street address, add it
                    address.getMaxAddressLineIndex() > 0 ?
                            address.getAddressLine(0) : "",
                    address.getAddressLine(1),
                    address.getAddressLine(2),
                    /*address.getAddressLine(3),
                    address.getAddressLine(4),
                    address.getLocality(),*/
                    address.getCountryName());
            // Return the text
            return addressText;
        } else {
            return "";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopSelf();
        unregisterReceiver(locationReceiver);
    }
}
