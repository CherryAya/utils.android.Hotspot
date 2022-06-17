package top.kagurayayoi.utils.android.hotspot;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.format.Formatter;

import java.net.Socket;

import top.kagurayayoi.utils.android.hotspot.thread.ConnectThread;
import top.kagurayayoi.utils.android.hotspot.thread.ListenerThread;

public class HotspotClient {

    private static WifiManager wifiManager;
    private static ConnectThread connector;
    private static ListenerThread listener;

    public static void initWifiManager(WifiManager wifiManager) {
        HotspotClient.wifiManager = wifiManager;
    }

    public static boolean connect(int port, Handler handler) {
        if (wifiManager == null)
            return false;
        new Thread(() -> {
            try {
                Socket socket = new Socket(getWifiRouteIpAddress(), port);
                connector = new ConnectThread(socket, handler);
                connector.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
        listener = new ListenerThread(port, handler);
        listener.start();
        return true;
    }

    public static void sendMessage(String message) {
        if (connector == null || message == null)
            return;
        new Thread(()->connector.sendData(message)).start();
    }

    public static String getWifiRouteIpAddress() {
        return Formatter.formatIpAddress(wifiManager.getDhcpInfo().gateway);
    }

    public static String getIpAddress() {
        if (!wifiManager.isWifiEnabled())
            return null;
        return intToRouterIp(wifiManager.getConnectionInfo().getIpAddress());
    }

    public static String getRouterIpAddress() {
        if (!wifiManager.isWifiEnabled())
            return null;
        return intToRouterIp(wifiManager.getConnectionInfo().getIpAddress());
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    private static String intToRouterIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                1;
    }

}