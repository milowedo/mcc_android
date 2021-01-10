package pl.edu.agh.mcc.descriptors;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkInformation {

    String connectionType;
    String connectionSubType;

    public void updateInformation(Context context) {
        NetworkInfo info = NetworkInformation.getNetworkInfo(context);
        if (info != null && info.isConnected()) {
            this.connectionType = info.getTypeName();
            this.connectionSubType = info.getSubtypeName();
        }else {
            this.connectionType = "NOT_CONNECTED";
        }
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }


    public static boolean isMobileConnectionFastEnough(int type, int subType) {
        if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // ~25 kbps // API level 8
                case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
                    return false;
                case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:// ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EHRPD: // ~ 1-2 Mbps // API level 11
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // ~ 5 Mbps // API level 9
                case TelephonyManager.NETWORK_TYPE_HSPAP: // ~ 10-20 Mbps // API level 13
                case TelephonyManager.NETWORK_TYPE_LTE: // ~ 10+ Mbps // API level 11
                    return true;
                case TelephonyManager.NETWORK_TYPE_UNKNOWN: // Unknown
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

}