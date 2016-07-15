package com.example.soundbeam;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    static final int GALLERY_REQUEST = 1;
    private Section[] sections;
    private ImageView imageView;
    private ProgressBar mProgressBar;
    private Button connectBtn;
    private Button getMidiBtn;
    private Context mContext;
    private Bitmap mBitmap;
    private String message;
    private ConnectionThread connector;
    private Uri selectedImage;
    private static final int DOWNLOAD_ONPROGRESS = 1;
    private ProgressDialog progressDialog;
    private static boolean FILE_DOWNLOADED = false;
    private Handler h;
    private long cnt;
    private long length;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        connectBtn = (Button) findViewById(R.id.connectButton);
        getMidiBtn = (Button) findViewById(R.id.getSoundBtn);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        h = new Handler();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                   // new SendImageTask().execute(getRealPathFromURI(selectedImage));
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setProgress(0);
                showDialog(DOWNLOAD_ONPROGRESS);
                    connector = new ConnectionThread(getRealPathFromURI(selectedImage));
                    connector.start();
            }
        });

        getMidiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetImageTask().execute("sound.mid");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Bitmap bitmap = null;


        switch(requestCode) {
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    selectedImage = imageReturnedIntent.getData();
                    new LoadImageTask().execute(selectedImage);
                    TextView textView = (TextView)findViewById(R.id.textView);
                }
        }
    }



    private class LoadImageTask extends AsyncTask<Uri, Integer, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageView.setImageBitmap(null);
            mProgressBar.setVisibility(View.VISIBLE);

        }

        protected Bitmap doInBackground(Uri... uris) {
            int count = uris.length;
            long totalSize = 0;
            Bitmap bitmap = null;
            for (int i = 0; i < count; i++) {
                try {
                     bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uris[i]);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
            mProgressBar.setProgress(progress[0]);

        }

        protected void onPostExecute(Bitmap bitmap) {
            mProgressBar.setProgress(100);
            mProgressBar.setVisibility(View.INVISIBLE);
            imageView.setImageBitmap(bitmap);
            //new MakeSectionsTask().execute(bitmap);
            mProgressBar.setProgress(0);

        }
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }


    private class GetImageTask extends AsyncTask<String, Integer, Integer> {
        private String ip="192.168.1.4";
        private int port=45000;
        Socket client;
        private BufferedInputStream bis;
        private BufferedOutputStream bos;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            client =null;
            showDialog(DOWNLOAD_ONPROGRESS);
        }

        protected Integer doInBackground(String... dests) {
            int count = dests.length;
            for (int i = 0; i < count; i++) {
                try {
                    client = new Socket(ip, port);
                    bis = new BufferedInputStream(client.getInputStream());
                    bos = new BufferedOutputStream(new FileOutputStream(dests[0]));
                    progressDialog.setMax(1000);
                    InputStream inputStream = client.getInputStream();
                    OutputStream outputStream = client.getOutputStream();

                    int pointer;
                    byte[] byteArray = new byte[8192];
                    int len1 = 0;
                    while ((pointer = bis.read(byteArray)) != -1){
                        len1 += pointer;
                        publishProgress(len1/1000);
                        bos.write(byteArray,0,pointer);
                    }
                    bis.close();
                    bos.close();

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
            return 1;
        }

        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);

        }

        protected void onPostExecute(Integer answer) {
            progressDialog.dismiss();
            removeDialog(DOWNLOAD_ONPROGRESS);
            FILE_DOWNLOADED = true;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DOWNLOAD_ONPROGRESS:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Downloading latest ...");
                progressDialog.setCancelable(true);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                try {
                    progressDialog.show();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return progressDialog;
            default:
                return null;
        }
    }

    Runnable updateProgress = new Runnable() {
        public void run() {
            progressDialog.setProgress((int) cnt);
        }
    };

    Runnable makeProgressSettings = new Runnable() {
        public void run() {
            progressDialog.setProgress(0);
            progressDialog.setMax((int) length);
        }
    };

    Runnable hideProgressDialog = new Runnable() {
        public void run() {
            progressDialog.hide();
        }
    };

    private class ConnectionThread extends Thread {
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
                length = file.length();
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                DataOutputStream leos = new DataOutputStream(outputStream);
                DataInputStream lein = new DataInputStream(inputStream);


                int count = 0;
                long res = 0;
                h.post(makeProgressSettings);
                byte[] byteArray = new byte[8192];
                leos.writeLong(length);
                while (res<length){
                    count = bis.read(byteArray);
                    bos.write(byteArray, 0, count);
                    res += count;
                    cnt = res;
                    h.post(updateProgress);
                    System.out.println("Count = " + count + " Res = " + res);
                    bos.flush();
                }
                System.out.println("Sent: " + res);

                length = lein.readLong();
                outputStream.flush();
                System.out.println(length);
                BufferedInputStream bis2 = new BufferedInputStream(client.getInputStream());
                BufferedOutputStream bos2 = new BufferedOutputStream(new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/sound.mid")));

                res = 0;
                byteArray = new byte[8192];
                h.post(makeProgressSettings);
                while(res < length){
                    count = bis2.read(byteArray);
                    res += count;
                    h.post(updateProgress);
                    System.out.println("Count: " + count + ", res: " + res);
                    bos2.write(byteArray, 0, count);
                }
                bos2.flush();
                bos.close();
                bis.close();
                bos2.close();
                bis2.close();
                System.err.println("GOT MIDI!");
                h.post(hideProgressDialog);

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
}



