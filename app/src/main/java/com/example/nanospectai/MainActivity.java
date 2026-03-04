package com.example.nanospectai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import android.net.Uri;
import android.os.Environment;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 22;
    Button btnpicture;
    ImageView imageView, micIcon;
    boolean micActive = false;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnpicture= findViewById(R.id.btncamera_id);
        imageView= findViewById(R.id.imageview1);
        AppCompatButton btnMic = findViewById(R.id.btnmic_id);
        micIcon = findViewById(R.id.mic_icon);

        btnpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File photoFile = null;
                try{
                    photoFile = createImageFile();
                } catch(IOException e){
                    e.printStackTrace();
                }

                if (photoFile != null){
                    imageUri = FileProvider.getUriForFile(
                            MainActivity.this,
                            getPackageName() + ".provider",
                            photoFile
                    );
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(cameraIntent,REQUEST_CODE);
                }
            }
        });

        btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                micActive = !micActive; // toggle state

                if (micActive) {
                    // Mic active: red background and new icon
                    btnMic.setBackgroundTintList(getColorStateList(R.color.red)); // make sure you have red in colors.xml
                    micIcon.setImageResource(R.drawable.baseline_mic_off_24); // replace with your drawable
                } else {
                    // Mic inactive: original background and original icon
                    btnMic.setBackgroundTintList(getColorStateList(R.color.green)); // replace with your original color
                    micIcon.setImageResource(R.drawable.baseline_mic_24);
                }
            }
        });
    }
    private File createImageFile() throws IOException {
        String fileName = "photo_" + System.currentTimeMillis();
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );
        File image = File.createTempFile(
                fileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            imageView.setImageURI(imageUri);
        }
        else{
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}