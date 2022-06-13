package top.kagurayayoi.utils.android.hotspot.thread;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import top.kagurayayoi.utils.android.hotspot.HotspotStatusCode;

public class ListenerThread extends Thread{

    private ServerSocket    serverSocket = null;
    private final Handler   handler;
    private Socket          socket;

    public ListenerThread(int port, Handler handler) {
        setName("listenerThread");
        this.handler = handler;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (serverSocket != null)
                    socket = serverSocket.accept();
                Message message = Message.obtain();
                message.what = HotspotStatusCode.DEVICE_CONNECTING;
                handler.sendMessage(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void close() {
        try {
            if (serverSocket != null)
                serverSocket.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
