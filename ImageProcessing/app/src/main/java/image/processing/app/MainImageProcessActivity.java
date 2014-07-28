package image.processing.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainImageProcessActivity extends Activity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);

        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        //sets the really ugly ass button
        Button photoButton = (Button) this.findViewById(R.id.button1);
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //starts the camera up
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);

            final ContentResolver cr = getContentResolver();
            final String[] p1 = new String[] {
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATE_TAKEN
            };
            Cursor c1 = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p1, null, null, p1[1] + " DESC");
            if ( c1.moveToFirst() ) {
                String uristringpic = "content://media/external/images/media/" +c1.getInt(0);
                Uri newuri = Uri.parse(uristringpic);
                try {
                    //this gets the bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), newuri);

                    //code for displaying an image
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException ex) {

                } catch(IOException ex2){

                }

            }
            c1.close();
        }
    }
}
