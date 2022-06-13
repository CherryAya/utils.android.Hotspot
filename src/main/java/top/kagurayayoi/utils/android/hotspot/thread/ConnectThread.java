package top.kagurayayoi.utils.android.hotspot.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import top.kagurayayoi.utils.android.hotspot.HotspotStatusCode;

public class ConnectThread extends Thread{

    private final Socket    socket;
    private final Handler   handler;
    private InputStream     input;
    private OutputStream    output;

    public ConnectThread(Socket socket, Handler handler) {
        setName("connectThread");
        this.socket  = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        if (socket == null) return;
        this.handler.sendEmptyMessage(HotspotStatusCode.DEVICE_CONNECTED);
        try {
            // stream
            input = socket.getInputStream();
            output = socket.getOutputStream();
            // read
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                bytes = input.read(buffer);
                if (bytes > 0) {
                    final byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);
                    Message message = Message.obtain();
                    message.what = HotspotStatusCode.MSG_RECEIVED;
                    Bundle bundle = new Bundle();
                    bundle.putString("data", new String(data));
                    message.setData(bundle);
                    handler.sendMessage(message);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendData(String msg) {
        if (this.output != null) {
            try {
                output.write(msg.getBytes());
                Message message = Message.obtain();
                message.what = HotspotStatusCode.SEND_MSG_SUCCEED;
                Bundle bundle = new Bundle();
                bundle.putString("MSG", msg);
                message.setData(bundle);
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = HotspotStatusCode.SEND_MSG_ERROR;
                Bundle bundle = new Bundle();
                bundle.putString("MSG", msg);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }
    }

    public void close() {
        this.close();
        try {
            if (socket != null)
                socket.close();
            if (input != null)
                input.close();
            if (output != null)
                output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
