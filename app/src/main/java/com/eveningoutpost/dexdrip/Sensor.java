package com.eveningoutpost.dexdrip;

import android.provider.BaseColumns;
import com.eveningoutpost.dexdrip.Models.UserError.Log;
import android.preference.ListPreference;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.eveningoutpost.dexdrip.UtilityModels.SensorSendQueue;

import java.util.UUID;

/**
 * Created by stephenblack on 10/29/14.
 */

@Table(name = "Sensors", id = BaseColumns._ID)
public class Sensor extends Model {

//    @Expose
    @Column(name = "started_at", index = true)
    public long started_at;

//    @Expose
    @Column(name = "stopped_at")
    public long stopped_at;

//    @Expose
    //latest minimal battery level
    @Column(name = "latest_battery_level")
    public int latest_battery_level;

//    @Expose
    @Column(name = "uuid", index = true)
    public String uuid;
    
//  @Expose
  @Column(name = "sensor_location")
  public String sensor_location;

    public static Sensor create(long started_at) {
        Sensor sensor = new Sensor();
        sensor.started_at = started_at;
        sensor.uuid = UUID.randomUUID().toString();

        sensor.save();
        SensorSendQueue.addToQueue(sensor);
        Log.d("SENSOR MODEL:", sensor.toString());
        return sensor;
    }

    public static Sensor currentSensor() {
        Sensor sensor = new Select()
                .from(Sensor.class)
                .where("started_at != 0")
                .where("stopped_at = 0")
                .orderBy("_ID desc")
                .limit(1)
                .executeSingle();
        return sensor;
    }

    public static boolean isActive() {
        Sensor sensor = new Select()
                .from(Sensor.class)
                .where("started_at != 0")
                .where("stopped_at = 0")
                .orderBy("_ID desc")
                .limit(1)
                .executeSingle();
        if(sensor == null) {
            return false;
        } else {
            return true;
        }
    }
    
    public static void updateSensorLocation(String sensor_location) {
        Sensor sensor = currentSensor();
        if (sensor == null) {
            Log.e("SENSOR MODEL:", "updateSensorLocation called but sensor is null");
            return;
        }
        sensor.sensor_location = sensor_location;
        sensor.save();
    }
}

