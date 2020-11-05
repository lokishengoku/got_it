package code.butchers.gotit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private ImageView imgInput;
    private TextView txtResult;
    private MaterialButton btnSolve;
    private TessBaseAPI tessBaseAPI;
    private Bitmap bitmap;
    private FloatingActionButton btnCapture;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSolve = findViewById(R.id.btnSolve);
        MaterialButton btnRecognize = findViewById(R.id.btnRecognize);
        txtResult = findViewById(R.id.txtResult);
        btnCapture = findViewById(R.id.btnCapture);

        initImageView();

        try {
            prepareModel();
            tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.init(getFilesDir()+"", "vie");
        } catch (Exception e){
            Log.w("ERROR: ", Objects.requireNonNull(e.getMessage()));
        }

        btnRecognize.setOnClickListener(this::doRecognize);

        btnCapture.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_CAMERA_PERMISSION_CODE);
            else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    public void initImageView(){
        imgInput = findViewById(R.id.imgInput);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ptb2);
        imgInput.setImageBitmap(bitmap);
    }

    public void prepareModel() throws IOException {
        //check if folder tessdata exists?
        File dir = new File(getFilesDir()+"/tessdata");
        //if not create folder tessdata
        if(!dir.exists()) dir.mkdirs();
        //check if traineddata exists in folder
        File trainedData = new File(getFilesDir()+"/tessdata/vie.traineddata");
        //if not, copy from asset
        if(!trainedData.exists()){
            copyFile();
        }
    }

    private void copyFile() throws IOException{
        AssetManager asset = getAssets();
        InputStream is = asset.open("tessdata/vie.traineddata");
        OutputStream os = new FileOutputStream(getFilesDir()+"/tessdata/vie.traineddata");

        int read;
        byte[] buffer = new byte[1024];

        while((read = is.read(buffer)) != 1){
            os.write(buffer, 0, read);
        }
        is.close();
        os.flush();
        os.close();
    }

    public void doRecognize(View view){
        if(tessBaseAPI == null) return;

        try {
            tessBaseAPI.setImage(bitmap);
            String result = tessBaseAPI.getUTF8Text();
            txtResult.setText(result);
        }catch (Exception e){
            Log.w("ERROR: ", Objects.requireNonNull(e.getMessage()));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_SHORT).show();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CAMERA_REQUEST && resultCode== Activity.RESULT_OK){
            assert data != null;
            bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            imgInput.setImageBitmap(bitmap);
        }
    }
}