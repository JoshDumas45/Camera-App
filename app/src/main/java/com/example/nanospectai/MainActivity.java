package com.example.nanospectai;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import java.io.FileOutputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.media.ExifInterface;
import android.graphics.Matrix;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 22;
    Button btnpicture;
    ImageView imageView, micIcon;
    boolean micActive = false;
    File currentPhotoFile;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
                    currentPhotoFile = photoFile;
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

                    cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivityForResult(cameraIntent, REQUEST_CODE);
                }
            }
        });

        btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                micActive = !micActive; // toggle state

                if (micActive) {
                    // Mic active: red background and new icon
                    btnMic.setBackgroundTintList(getColorStateList(R.color.red));
                    micIcon.setImageResource(R.drawable.baseline_mic_off_24);
                } else {
                    // Mic inactive: original background and original icon
                    btnMic.setBackgroundTintList(getColorStateList(R.color.green));
                    micIcon.setImageResource(R.drawable.baseline_mic_24);
                }
            }
        });
    }
    private File createImageFile() throws IOException {

        File storageDir = new File(getExternalFilesDir(null), "NanoSpectAI_Images");

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        String fileName = "image_" + System.currentTimeMillis() + ".jpg";

        return new File(storageDir, fileName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            if (currentPhotoFile != null) {
                Toast.makeText(this, "Saved: " + currentPhotoFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(),
                        imageUri
                );

                bitmap = fixRotation(bitmap, currentPhotoFile);

                int width = bitmap.getWidth() / 4;
                int height = bitmap.getHeight() / 4;

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

                // Display
                imageView.setImageBitmap(resizedBitmap);

                // Save
                FileOutputStream out = new FileOutputStream(currentPhotoFile);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                // Send
                sendImageBluetooth(imageUri);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap fixRotation(Bitmap bitmap, File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );

            Matrix matrix = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
                matrix.postRotate(90);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                matrix.postRotate(180);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                matrix.postRotate(270);

            return Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private void sendImageBluetooth(Uri uri) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Send Image"));
    }
}