package pl.edu.agh.mcc.descriptors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;

public class BatteryInformation {
    public double batteryPercentStart;
    public double batteryPercentEnd;
    public double batteryPercentDelta;
    public long batteryCapacityStart;
    public long batteryCapacityEnd;
    public long batteryCapacityDelta;

    public void startValueUpdate(Intent batteryStatus, Context context) {
        this.batteryPercentStart = getBatteryPercent(batteryStatus);
        this.batteryCapacityStart = getCurrentBatteryCapacity(context);
    }

    public void endValueUpdate(Intent batteryStatus, Context context) {
        this.batteryPercentEnd = getBatteryPercent(batteryStatus);
        this.batteryCapacityEnd = getCurrentBatteryCapacity(context);
        this.batteryCapacityDelta = batteryCapacityStart - batteryCapacityEnd;
        this.batteryPercentDelta = batteryPercentStart - batteryPercentEnd;
    }

    private int getBatteryPercent(Intent batteryStatus) {
        return batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    }

    private long getCurrentBatteryCapacity(Context context) {
        long capacity = getBatteryCapacityWithBatteryManager(context);
        if (capacity == 0) {
            capacity = (long) getBatteryCapacityWithReflection(context);
        }
        return capacity;
    }

    private long getBatteryCapacityWithBatteryManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            int chargeCounter = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
            int capacity = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

            if (chargeCounter == Integer.MIN_VALUE || capacity == Integer.MIN_VALUE)
                return 0;

            return (chargeCounter / capacity) * 100;
        }
        return 0;
    }

    @SuppressLint("PrivateApi")
    private double getBatteryCapacityWithReflection(Context context) {
        Object mPowerProfile;
        double batteryCapacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class)
                    .newInstance(context);

            batteryCapacity = (double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return batteryCapacity;

    }

}
