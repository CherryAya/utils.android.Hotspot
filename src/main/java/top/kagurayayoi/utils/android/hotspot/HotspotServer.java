package top.kagurayayoi.utils.android.hotspot;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import top.kagurayayoi.utils.android.hotspot.thread.ConnectThread;
import top.kagurayayoi.utils.android.hotspot.thread.ListenerThread;

public class HotspotServer {

    private static WifiManager wifiManager;
    private static ListenerThread listener;
    private static ConnectThread connector;

    public static void initWifiManager(WifiManager wifiManager) {
        HotspotServer.wifiManager = wifiManager;
    }

    public static boolean run(String SSID, String PASSWD) {
        if (wifiManager == null)
            return false;
        final WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = SSID;
        configuration.preSharedKey = PASSWD;
        configuration.hiddenSSID = false;
        configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        configuration.status = WifiConfiguration.Status.ENABLED;
        try {
            return (Boolean) wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE).invoke(wifiManager, configuration, true);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void listen(int port, Handler handler) {
        if (wifiManager == null)
            return;
        listener = new ListenerThread(port, handler);
        listener.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        HotspotServer.connect(port, handler);
    }

    private static void connect(int port, Handler handler) {
        new Thread(()->{
            try {
                String ipaddr = HotspotServer.getHotspotIPAddress();
                if (ipaddr == null)
                    ipaddr = "192.168.4.1";
                connector = new ConnectThread(new Socket(ipaddr, port), handler);
                connector.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static String getHotspotIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress())
                            return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ListenerThread getListener() {
        return HotspotServer.listener;
    }

    public static void close() {
        try {
            Method ref = wifiManager.getClass().getMethod("getWifiApConfiguration");
            ref.setAccessible(true);
            final WifiConfiguration configuration = (WifiConfiguration) ref.invoke(wifiManager);
            wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class)
                    .invoke(wifiManager, configuration, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
