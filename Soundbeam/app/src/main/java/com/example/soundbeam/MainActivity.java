package com.example.soundbeam;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private Socket client;
    private PrintWriter printwriter;
    String ip="192.168.1.4";
    int port=45000;
    static final int GALLERY_REQUEST = 1;
    private Section[] sections;
    private ImageView imageView;
    private ProgressBar mProgressBar;
    private Button connectBtn;
    private Context mContext;
    private Bitmap mBitmap;
    private String message;
    private ConnectionThread connector;
    private Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        connectBtn = (Button) findViewById(R.id.connectButton);
        mContext = this;
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                    int[][] info = Section.sectionsToInfo(sections);
                    System.out.println(info.toString());
                    connector = new ConnectionThread(getRealPathFromURI(selectedImage));
                    connector.start();
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

    protected static int[] averageARGB(Bitmap pic) {
        int A, R, G, B;
        A = R = G = B = 0;
        int pixelColor;
        int width = pic.getWidth();
        int height = pic.getHeight();
        int size = width * height;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                pixelColor = pic.getPixel(x, y);
                A += Color.alpha(pixelColor);
                R += Color.red(pixelColor);
                G += Color.green(pixelColor);
                B += Color.blue(pixelColor);
            }
        }

        A /= size;
        R /= size;
        G /= size;
        B /= size;

        int[] average = {A, R, G, B};
        return average;
    }

    private static Section[] sectionMaker(Bitmap pic, int num)
    {
        Section[] sections = new Section[num];
        int A, R, G, B;

        int pixelColor;
        int width = pic.getWidth();
        int height = pic.getHeight();
        int size = width * height;
        int widthOfSection = width/num;
        int sizeOfSection = widthOfSection * height;

        for (int s = 0; s < num; s++) {
            Section current = new Section();
            A = R = G = B = 0;
            for (int x = widthOfSection*s; x < widthOfSection*s + widthOfSection; ++x) {
                for (int y = 0; y < height; ++y) {
                    pixelColor = pic.getPixel(x, y);
                    A += Color.alpha(pixelColor);
                    R += Color.red(pixelColor);
                    G += Color.green(pixelColor);
                    B += Color.blue(pixelColor);
                }
            }
           // current.setStart(widthOfSection*s);
            current.setA(A/sizeOfSection);
            current.setR(R/sizeOfSection);
            current.setG(G/sizeOfSection);
            current.setB(B/sizeOfSection);
            sections[s] = current;
        }
        Section.WIDTH=widthOfSection;
        Section.HEIGHT=height;
        return sections;
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
            new MakeSectionsTask().execute(bitmap);
            mProgressBar.setProgress(0);

        }
    }

    private class MakeSectionsTask extends AsyncTask<Bitmap, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            mProgressBar.setProgress(0);
            mProgressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        protected Integer doInBackground(Bitmap... map) {
            int count = map.length;
            int num = 8;
            long totalSize = 0;
            for (int i = 0; i < count; i++) {
                //sections= sectionMaker(map[i], 8);
                sections = new Section[num];
                int A, R, G, B;

                int pixelColor;
                int width = map[i].getWidth();
                int height = map[i].getHeight();
                int size = width * height;
                int widthOfSection = width/num;
                int sizeOfSection = widthOfSection * height;

                for (int s = 0; s < num; s++) {
                    Section current = new Section();
                    A = R = G = B = 0;
                    for (int x = widthOfSection*s; x < widthOfSection*s + widthOfSection; ++x) {
                        for (int y = 0; y < height; ++y) {
                            pixelColor = map[i].getPixel(x, y);
                            A += Color.alpha(pixelColor);
                            R += Color.red(pixelColor);
                            G += Color.green(pixelColor);
                            B += Color.blue(pixelColor);
                        }
                    }
                    // current.setStart(widthOfSection*s);
                    current.setA(A/sizeOfSection);
                    current.setR(R/sizeOfSection);
                    current.setG(G/sizeOfSection);
                    current.setB(B/sizeOfSection);
                    sections[s] = current;
                    publishProgress(s);
                }
                Section.WIDTH=widthOfSection;
                Section.HEIGHT=height;
            }
            return 1;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
            mProgressBar.setProgress(progress[0]);

        }

        protected void onPostExecute(Integer num) {
            mProgressBar.setProgress(100);
            mProgressBar.setVisibility(View.INVISIBLE);
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
}



