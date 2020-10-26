package code.butchers.gotit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ImageView imgInput;
    private TextView txtResult;
    private MaterialButton btnSolve, btnRecognize;
    private TessBaseAPI tessBaseAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgInput = findViewById(R.id.imgInput);
        btnSolve = findViewById(R.id.btnSolve);
        btnRecognize = findViewById(R.id.btnRecognize);
        txtResult = findViewById(R.id.txtResult);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                prepareModel();
            }
        });

        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(getFilesDir()+"", "vie");


        btnRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tessBaseAPI.setImage(((BitmapDrawable) imgInput.getDrawable()).getBitmap());
                String result = tessBaseAPI.getUTF8Text();
                txtResult.setText(result);
            }
        });

    }

    public void prepareModel(){
        //check if folder tessdata exists?
        File dir = new File(getFilesDir()+"/tessdata");
        //if not create folder tessdata
        if(!dir.exists()) dir.mkdir();

        //check if traineddata exists in folder
        File trainedData = new File(getFilesDir()+"/tessdata/vie.traineddata");
        //if not, copy from asset
        if(!trainedData.exists()){
            try {
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
            } catch (Exception e){
                Log.d("DEBUG", Objects.requireNonNull(e.getMessage()));
            }
        }
    }
}