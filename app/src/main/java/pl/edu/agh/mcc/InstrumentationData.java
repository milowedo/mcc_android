package pl.edu.agh.mcc;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.UUID;

import pl.edu.agh.mcc.descriptors.BatteryInformation;
import pl.edu.agh.mcc.descriptors.DeviceInformation;
import pl.edu.agh.mcc.descriptors.NetworkInformation;
import pl.edu.agh.mcc.descriptors.TaskInformation;
import pl.edu.agh.mcc.descriptors.TimeMeasurements;

public class InstrumentationData {
    private static final String ID_KEY = "DEVICE_ID";
    private static final String MCC_DEVICE_INFORMATION_FILENAME = "mcc_deviceInformationFile";
    private static String DEVICE_ID;

    public DeviceInformation deviceInformation;
    public BatteryInformation batteryInformation;
    public EXECUTION executionLocation;
    public NetworkInformation networkInformation;
    public TimeMeasurements timeMeasurements;
    public TaskInformation taskInformation;

    public InstrumentationData(Context context) {
        this.deviceInformation = new DeviceInformation();
        this.batteryInformation = new BatteryInformation();
        this.networkInformation = new NetworkInformation();
        this.timeMeasurements = new TimeMeasurements();
        this.taskInformation = new TaskInformation();

        this.networkInformation.updateInformation(context);
        this.deviceInformation.deviceId = getDeviceId(context);
        this.deviceInformation.deviceName = Build.MODEL;
    }

    private String getDeviceId(Context context) {
        if (DEVICE_ID != null) {
            return DEVICE_ID;
        }

        SharedPreferences settings = context.getSharedPreferences(MCC_DEVICE_INFORMATION_FILENAME, Activity.MODE_PRIVATE);

        String id = settings.getString(ID_KEY, "");
        if ("".equals(id)) {
            id = UUID.randomUUID().toString();
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ID_KEY, id);
        editor.apply();

        DEVICE_ID = id;
        return id;
    }

    public enum EXECUTION {
        LOCAL,
        CLOUD
    }
}
