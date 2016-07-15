package com.example.soundbeam;

import android.os.Environment;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private int port=45020;
    Socket client;
    private String path;

    public ConnectionThread(String imagePath){
    this.path = imagePath;
    }
    @Override
    public void run() {
        super.run();
        client =null;
        try {
            client = new Socket(ip, port);
            InputStream inputStream = client.getInputStream();
            OutputStream outputStream = client.getOutputStream();
            File file = new File(path);
            long length = file.length();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            BufferedOutputStream bos = new BufferedOutputStream(outputStream);
            DataOutputStream leos = new DataOutputStream(outputStream);
            DataInputStream lein = new DataInputStream(inputStream);


            int count = 0;
            long res = 0;
            byte[] byteArray = new byte[8192];
            leos.writeLong(length);
            while (res<length){
                    count = bis.read(byteArray);
                    bos.write(byteArray, 0, count);
                    res += count;
                System.out.println("Count = " + count + " Res = " + res);
                bos.flush();
            }
            System.out.println("Sent: " + res);

            length = lein.readLong();
            outputStream.flush();
            System.out.println(length);
            BufferedInputStream bis2 = new BufferedInputStream(client.getInputStream());
            BufferedOutputStream bos2 = new BufferedOutputStream(new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/sound.mid")));

          // progressDialog.setMax(1000);

           res = 0;
            byteArray = new byte[8192];
            while(res < length){
                    count = bis2.read(byteArray);
                    res += count;
                    //publishProgress(len1 / 1000);
                    System.out.println("Count: " + count + ", res: " + res);
                    bos2.write(byteArray, 0, count);
            }

            bos2.flush();
            bos.close();
            bis.close();
            bos2.close();
            bis2.close();
            System.err.println("GOT MIDI!");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Got an IOException: " + e.toString());
        }
        finally{
            if(client != null){
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}



