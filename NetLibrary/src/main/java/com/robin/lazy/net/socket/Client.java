/*
 * 文 件 名:  Client.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  LiCong
 * 修改时间:  2010-9-8
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.robin.lazy.net.socket;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 套接字客户端连接器
 *
 * @author 江钰锋
 * @version [版本号, 2014年6月19日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Client implements Runnable {

    private Socket socket;

    private DataInputStream dataIn = null;

    private DataOutputStream dataOut = null;

    private boolean isRun = false;

    private SocketHandler socketHandler;

    public Client(String ip, int port, SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
        try {
            socket = new Socket(ip, port);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
            Log.e("client:", "created client socket");

            isRun = true;
            new Thread(this).start();
        } catch (UnknownHostException e) {
            Log.e("Exception", e.toString());
        } catch (IOException e) {
            Log.e("Exception", e.toString());
        }
    }

    public void sendData(String string) {
        try {
            dataOut.writeUTF(string);
            dataOut.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 服务器响应捕捉线程
    @Override
    public void run() {
        while (isRun) {
            try {
                String responseData = dataIn.readUTF();
                socketHandler.response(responseData);
            } catch (IOException e) {
                Log.e("exception:", e.toString());
                isRun = false;
                Log.e("link error", "server link will shut down...");
            }
        }
    }

    /**
     * 关闭连接
     *
     * @see [类、类#方法、类#成员]
     */
    public void close() {
        try {
            isRun = false;
            dataIn.close();
            dataOut.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
