package com.example.soundbeam;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by FD on 10.07.2016.
 */
public class ConnectionThread extends Thread {
    private String ip="192.168.1.4";
    private int port=45000;
    Socket client;
    private String path;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;

    public ConnectionThread(String imagePath){
    this.path = imagePath;
    }
    @Override
    public void run() {
        super.run();
        String message = "testing";
        String response = "";
        client =null;
        DataOutputStream dataOutputStream= null;
        DataInputStream dataInputStream = null;
        try {
            client = new Socket(ip, port);

            ByteArrayOutputStream byteArrayOutputStream =
                    new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            bis = new BufferedInputStream(new FileInputStream(path));
            bos = new BufferedOutputStream(client.getOutputStream());
            InputStream inputStream = client.getInputStream();
            OutputStream outputStream = client.getOutputStream();
            DataInputStream in = new DataInputStream(inputStream);
            DataOutputStream out = new DataOutputStream(outputStream);

            int count;
            byte[] byteArray = new byte[8192];
            while ((count = bis.read(byteArray)) != -1){
                bos.write(byteArray,0,count);
            }
            bis.close();
            bos.close();


/* передача строки рабочая!
            while ((bytesRead = inputStream.read(buffer)) != -1){
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }
*/
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Got an IOException: " + e.getMessage());
        }
        finally{
            if(client != null){
                try {
                    client.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}



