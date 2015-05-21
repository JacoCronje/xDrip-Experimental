package com.eveningoutpost.dexdrip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.eveningoutpost.dexdrip.Models.AlertType;
import com.eveningoutpost.dexdrip.UtilityModels.AlertPlayer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class AlertList extends Activity {
    private ListView listViewLow;
    private ListView listViewHigh;
    private Button createLowAlert;
    private Button createHighAlert;
    private boolean doMgdl;
    private Context mContext;
    private final int ADD_ALERT = 1;
    private final int EDIT_ALERT = 2;
    private SharedPreferences prefs;

    private final static String TAG = AlertPlayer.class.getSimpleName();

    private String stringTimeFromAlert(AlertType alert) {
        if(alert.all_day) { return "all day"; }
        String start = timeFormatString(AlertType.time2Hours(alert.start_time_minutes), AlertType.time2Minutes(alert.start_time_minutes));
        String end = timeFormatString(AlertType.time2Hours(alert.end_time_minutes), AlertType.time2Minutes(alert.end_time_minutes));
        return start + " - " + end;
    }

    private HashMap<String, String> createAlertMap(AlertType alert) {
        HashMap<String, String> map = new HashMap<String, String>();
        String overrideSilentMode = "Override Silent Mode";
        if(!alert.override_silent_mode) {
            overrideSilentMode = "No Alert in Silent Mode";
        }

        map.put("alertName", alert.name);
        map.put("alertThreshold", EditAlertActivity.UnitsConvert2Disp(doMgdl, alert.threshold));
        map.put("alertTime", stringTimeFromAlert(alert));
        map.put("alertMp3File", shortPath(alert.mp3_file));
        map.put("alertOverrideSilenceMode", overrideSilentMode);
        map.put("uuid", alert.uuid);

        return map;
    }

    private ArrayList<HashMap<String, String>> createAlertsMap(boolean above) {
        ArrayList<HashMap<String, String>> feedList= new ArrayList<HashMap<String, String>>();

        List<AlertType> alerts = AlertType.getAll(above);
        for (AlertType alert : alerts) {
            Log.e(TAG, alert.toString());
            feedList.add(createAlertMap(alert));
        }
        return feedList;
    }


    private class AlertsOnItemLongClickListener implements AdapterView.OnItemLongClickListener {
        //      @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ListView lv = (ListView)parent;
            @SuppressWarnings("unchecked")
            HashMap<String, String> item = (HashMap<String, String>)lv.getItemAtPosition(position);
            Log.e(TAG, "Item clicked " + lv.getItemAtPosition(position) + item.get("uuid"));

            //The XML for each item in the list (should you use a custom XML) must have android:longClickable="true"
            // as well (or you can use the convenience method lv.setLongClickable(true);). This way you can have a list
            // with only some items responding to longclick. (might be used for non removable alerts)

            Intent myIntent = new Intent(AlertList.this, EditAlertActivity.class);
            myIntent.putExtra("uuid", item.get("uuid")); //Optional parameters
            AlertList.this.startActivityForResult(myIntent, EDIT_ALERT);
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_list);
        mContext = getApplicationContext();
        listViewLow = (ListView) findViewById(R.id.listView_low);
        listViewHigh = (ListView) findViewById(R.id.listView_high);
        prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        doMgdl = (prefs.getString("units", "mgdl").compareTo("mgdl") == 0);

        addListenerOnButton();
        FillLists();

        listViewLow.setOnItemLongClickListener(new AlertsOnItemLongClickListener());
        listViewHigh.setOnItemLongClickListener(new AlertsOnItemLongClickListener());
    }


    private void addListenerOnButton() {
        createLowAlert = (Button)findViewById(R.id.button_create_low);
        createHighAlert = (Button)findViewById(R.id.button_create_high);

        createLowAlert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(AlertList.this, EditAlertActivity.class);
                myIntent.putExtra("above", "false");
                AlertList.this.startActivityForResult(myIntent, ADD_ALERT);
            }

        });

        createHighAlert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(AlertList.this, EditAlertActivity.class);
                myIntent.putExtra("above", "true");
                AlertList.this.startActivityForResult(myIntent, ADD_ALERT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult called ");
        if (requestCode == ADD_ALERT || requestCode == EDIT_ALERT) {
            if(resultCode == RESULT_OK) {
                Log.e(TAG, "onActivityResult called invalidating...");
                FillLists();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void FillLists() {
        ArrayList<HashMap<String, String>> feedList;
        feedList = createAlertsMap(false);
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, feedList, R.layout.row_alerts, new String[]{"alertName", "alertThreshold", "alertTime", "alertMp3File", "alertOverrideSilenceMode"}, new int[]{R.id.alertName, R.id.alertThreshold, R.id.alertTime, R.id.alertMp3File, R.id.alertOverrideSilent});
        listViewLow.setAdapter(simpleAdapter);

        feedList = createAlertsMap(true);
        SimpleAdapter simpleAdapterHigh = new SimpleAdapter(this, feedList, R.layout.row_alerts, new String[]{"alertName", "alertThreshold", "alertTime", "alertMp3File", "alertOverrideSilenceMode"}, new int[]{R.id.alertName, R.id.alertThreshold, R.id.alertTime, R.id.alertMp3File, R.id.alertOverrideSilent});
        listViewHigh.setAdapter(simpleAdapterHigh);
    }

    private String shortPath(String path) {

        if(path != null) {
            if(path.length() == 0) {
                return "xDrip Default";
            }
            Ringtone ringtone = RingtoneManager.getRingtone(mContext, Uri.parse(path));
            if (ringtone != null) {
                return ringtone.getTitle(mContext);
            } else {
                String[] segments = path.split("/");
                if (segments.length > 1) {
                    return segments[segments.length - 1];
                }
            }
        }
        return "";
    }

    private String timeFormatString(int Hour, int Minute) {
        SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm");
        String selected = Hour+":"+Minute;
        if (!android.text.format.DateFormat.is24HourFormat(mContext)) {
            try {
                Date date = timeFormat24.parse(selected);
                SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm aa");
                return timeFormat12.format(date);
            } catch (final ParseException e) {
                e.printStackTrace();
            }
        }
        return selected;
    }
}
