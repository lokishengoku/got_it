package code.butchers.gotit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
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
import java.io.IOException;
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

        btnSolve = findViewById(R.id.btnSolve);
        btnRecognize = findViewById(R.id.btnRecognize);
        txtResult = findViewById(R.id.txtResult);

        initImageView();

        try {
            prepareModel();
            tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.init(getFilesDir()+"", "vie");
        } catch (Exception e){
            Log.w("ERROR: ", Objects.requireNonNull(e.getMessage()));
        }



        btnRecognize.setOnClickListener(this::doRecognize);
        

    }

    public void initImageView(){
        imgInput = findViewById(R.id.imgInput);
        Bitmap input = BitmapFactory.decodeResource(getResources(), R.drawable.ptb2);
        imgInput.setImageBitmap(input);
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
            tessBaseAPI.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ptb2));
            String result = tessBaseAPI.getUTF8Text();
            txtResult.setText(result);
        }catch (Exception e){
            Log.w("ERROR: ", Objects.requireNonNull(e.getMessage()));
        }
    }
}