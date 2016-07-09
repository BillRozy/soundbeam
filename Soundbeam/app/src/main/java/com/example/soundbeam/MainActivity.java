package com.example.soundbeam;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final int GALLERY_REQUEST = 1;
    private Section[] sections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Bitmap bitmap = null;
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        switch(requestCode) {
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageView.setImageBitmap(bitmap);
                    sections= sectionMaker(bitmap, 8);
                    TextView textView = (TextView)findViewById(R.id.textView);
                   // textView.setText(av[0]+"  "+av[1]+" "+av[2]+" "+av[3]);
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
}
