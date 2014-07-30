package image.processing.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainImageProcessActivity extends Activity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private LinearLayout linearLayout;
    public static int buttonPressed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buttonPressed = 0;
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
        Button passwordButton = (Button) this.findViewById(R.id.button2);
        passwordButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //starts the camera up
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        Button compare = (Button) this.findViewById(R.id.button3);
        compare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //starts the camera up
                int tally = 0;
                float embossValue = sectionEmbossFilterCompare();
                float colorCompare = standardCompareImages();


                TextView t = (TextView)findViewById(R.id.textView2);
                t.setText("Edge Detection %: " + Float.toString(embossValue * 100.0f)  + "%");

                TextView t2 = (TextView)findViewById(R.id.textView1);
                t2.setText("Color %: " + Float.toString(colorCompare * 100.0f)  + "%");

                /*if(!sectionEmbossFilterCompare() || !standardCompareImages()){
                    imageView.setImageResource(R.drawable.dontmatch);
                }
                else{
                    imageView.setImageResource(R.drawable.theymatch);
                }*/
            }
        });

        Button encrypt = (Button) this.findViewById(R.id.buttonEncrypt);
        encrypt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 133);
            }
        });

        Button decrypt = (Button) this.findViewById(R.id.buttonDecrypt);
        decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fp = Environment.getExternalStorageDirectory().getAbsolutePath() + "/image_processing/key.jpg";
                Bitmap decrypted = CryptoManager.getInstance().decryptImage(new File(fp));
                imageView.setImageBitmap(decrypted);
            }
        });

        Button delete = (Button) this.findViewById(R.id.deletePhotos);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String root = Environment.getExternalStorageDirectory().toString();
                File[] files = getListFiles(new File(root + "/user_images"));
                for(int i = 0; i < files.length; i++){
                    files[i].delete();
                }
            }
        });
    }

    public float humanDetection(Bitmap bitmap){
        float tally = 0;
        int[][] pixels = new int[bitmap.getHeight()][bitmap.getWidth()];

        int[] temp = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(temp, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        Bitmap.Config conf = Bitmap.Config.RGB_565; // see other conf types
        Bitmap newMap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), conf);

        for(int i = 0; i < bitmap.getHeight(); i++){
            for(int j = 0; j < bitmap.getWidth(); j++){
                pixels[i][j] = temp[i * bitmap.getWidth()+ j];
            }
        }

        for(int i = 0; i < bitmap.getHeight(); i++){
            for(int j = 0; j < bitmap.getWidth(); j++){
                int r = Color.red(pixels[i][j]);
                int g = Color.green(pixels[i][j]);
                int b = Color.blue(pixels[i][j]);
                if(checkHumanRange(r, g, b)){
                    tally += 1.0f;
                }
            }
        }
        return tally/(float)(bitmap.getHeight() * bitmap.getHeight());
    }

    public boolean checkHumanRange(int r, int g, int b){
        if((r < 255 && r > 110) && (g  < 244 && g > 90) && (b < 241 && b > 80)){
            return true;
        }
        return false;
    }

    public float sectionEmbossFilterCompare(){
        String root = Environment.getExternalStorageDirectory().toString();

        File[] files = getListFiles(new File(root + "/user_images"));
        Bitmap[] bitmap = new Bitmap[files.length];
        for(int i = 0; i < files.length; i++){
            bitmap[i] = embossFilter(BitmapFactory.decodeFile(files[i].getAbsolutePath()));
            //SaveImage(bitmap[i], "/emboss_filter");
        }
        //compare hisograms based on sections
        int[][][] histo = new int[2][20][64];


        Bitmap[][] sectionBitmap = new Bitmap[2][20];
        for(int i = 0; i < 2; i++){
            sectionBitmap[i][0] = Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0),    (int)(bitmap[i].getHeight() * 0),    (int)(bitmap[i].getWidth() * 0.5  ),(int)(bitmap[i].getHeight() * 0.25));
            sectionBitmap[i][1] = Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.5),  (int)(bitmap[i].getHeight() * 0),    (int)(bitmap[i].getWidth() * 0.5  ),(int)(bitmap[i].getHeight() * 0.25));

            sectionBitmap[i][2] = Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0   ), (int)(bitmap[i].getHeight() * 0.25),(int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][3] = Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0   ), (int)(bitmap[i].getHeight() * 0.375), (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][4] = Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.25),(int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][5] = Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.375), (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][6] = Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.5 ), (int)(bitmap[i].getHeight() * 0.25),(int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][7] = Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.5 ), (int)(bitmap[i].getHeight() * 0.375), (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][8] = Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.75), (int)(bitmap[i].getHeight() * 0.25),(int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][9] = Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.75), (int)(bitmap[i].getHeight() * 0.375), (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));

            sectionBitmap[i][10]= Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0   ), (int)(bitmap[i].getHeight() * 0.5),  (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][11]= Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0   ), (int)(bitmap[i].getHeight() * 0.625),(int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][12]= Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.5),  (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][13]= Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.625),(int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][14]= Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.5 ), (int)(bitmap[i].getHeight() * 0.5),  (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.1125));
            sectionBitmap[i][15]= Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.5 ), (int)(bitmap[i].getHeight() * 0.625),(int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][16]= Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.75), (int)(bitmap[i].getHeight() * 0.5),  (int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));
            sectionBitmap[i][17]= Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.75), (int)(bitmap[i].getHeight() * 0.625),(int)(bitmap[i].getWidth() * 0.25), (int)(bitmap[i].getHeight() * 0.125));

            sectionBitmap[i][18]= Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0   ), (int)(bitmap[i].getHeight() * 0.75), (int)(bitmap[i].getWidth() * 0.5),  (int)(bitmap[i].getHeight() * 0.25));
            sectionBitmap[i][19]= Bitmap.createBitmap (bitmap[i], (int)(bitmap[i].getWidth() * 0.5 ), (int)(bitmap[i].getHeight() * 0.75), (int)(bitmap[i].getWidth() * 0.5),  (int)(bitmap[i].getHeight() * 0.25));
        }


        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 20; j++){
                histo[i][j] = genHistogram(sectionBitmap[i][j]);
            }
        }

        float tallyCount = 0;
        int threshold = 5500;

        for(int i = 0; i < 20; i++){
            tallyCount +=  0.05 * compareHistos(histo[0][i], histo[1][i], threshold);
        }

        return tallyCount;
    }

    public float standardCompareImages(){
        String root = Environment.getExternalStorageDirectory().toString();

        File[] files = getListFiles(new File(root + "/user_images"));
        Bitmap[] bitmap = new Bitmap[files.length];
        for(int i = 0; i < files.length; i++){
            bitmap[i] = BitmapFactory.decodeFile(files[i].getAbsolutePath());
        }

        int[][] histo = new int[bitmap.length][64];
        for(int i = 0; i < bitmap.length; i++){
            histo[i] = genHistogram(bitmap[i]);
        }
        return compareHistos(histo[0], histo[1], 360000);
    }

    public float compareHistos(int[] histo1, int[] histo2, int threshold){
        int[] diff = new int[histo1.length];
        int diffTally = 0;
        for(int i = 0; i < histo1.length; i++){
            diff[i]= (int)Math.sqrt(Math.abs(Math.pow(histo1[i],2) - Math.pow(histo2[i], 2)));

            diffTally += diff[i];
        }
        double test = Math.sqrt(diffTally);
        if(diffTally <= threshold){
            return 1;
        }

        return (float)threshold/(float)diffTally;
    }

    public int[] genHistogram(Bitmap bitmap){
        int[] histogram = new int[4 * 4 * 4];

        int[] temp = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(temp, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int[][] pixels = new int[bitmap.getHeight()][bitmap.getWidth()];

        Bitmap.Config conf = Bitmap.Config.RGB_565; // see other conf types
        Bitmap newMap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), conf);

        for(int i = 0; i < bitmap.getHeight(); i++){
            for(int j = 0; j < bitmap.getWidth(); j++){
                pixels[i][j] = temp[i * bitmap.getWidth()+ j];
            }
        }

        for(int i = 0; i < bitmap.getHeight()-1; i++){
            for(int j = 0; j < bitmap.getWidth()-1; j++){
                int r = Color.red(pixels[i][j]);
                int g = Color.green(pixels[i][j]);
                int b = Color.blue(pixels[i][j]);
                histogram[checkRange(r) + checkRange(g)*4 + checkRange(b) * 16] += 1;
            }
        }

        return histogram;
    }

    public int checkRange(int x){
        if(x > 0 && x <= 63){
            return 0;
        }
        else if(x > 63 && x <= 127){
            return 1;
        }
        else if(x > 127 && x <= 191){
            return 2;
        }
        else{
            return 3;
        }
    }

    private File[] getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();

        return files;
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
                    bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/4, bitmap.getHeight()/4, false);
                    //fastblur(bitmap, 4);
                    //edgeDetection(bitmap);
                    //code for displaying an image

                    //imageView.setImageBitmap(bitmap);
                    //
                    float humanDetect = humanDetection(bitmap);
                    TextView t = (TextView)findViewById(R.id.textView3);
                    t.setText("Human %: " + Float.toString(humanDetect * 100.0f)  + "%");

                    imageView.setImageBitmap(bitmap);
                    SaveImage(bitmap, "/user_images");

                } catch (FileNotFoundException ex) {

                } catch(IOException ex2){

                }

            }
            c1.close();
        } else if(requestCode == 133 && resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

            String fp = Environment.getExternalStorageDirectory().getAbsolutePath() + "/image_processing";

            File dir = new File(fp);
            if (!dir.exists()){
                dir.mkdirs();
            }

            try {
                File outFile = new File(dir, "key.jpg");
                FileOutputStream outStream = new FileOutputStream(outFile);
                CryptoManager.getInstance().encryptFile(outStream, bitmap);
            } catch (IOException exception){

            }
        }
    }

    private void SaveImage(Bitmap finalBitmap, String path) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + path);
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap embossFilter(Bitmap bitmap){
        int emboss_w = 3;
        int emboss_h = 3;

        int sumr = 0;
        int sumg = 0;
        int sumb = 0;

        int[][] emboss_filter =
                {
                        {2,0,0},
                        {0,-1,0},
                        {0,0,-1}
                };
        int emboss_sum=1;

        int[] temp = new int[bitmap.getWidth() * bitmap.getHeight()];


        bitmap.getPixels(temp, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int[][] pixels = new int[bitmap.getHeight()][bitmap.getWidth()];

        Bitmap.Config conf = Bitmap.Config.RGB_565; // see other conf types
        Bitmap newMap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), conf);

        for(int i = 0; i < bitmap.getHeight(); i++){
            for(int j = 0; j < bitmap.getWidth(); j++){
                pixels[i][j] = temp[i * bitmap.getWidth()+ j];
            }
        }

        for(int i=1;i<bitmap.getHeight()-1;i++){
            for(int j=1;j<bitmap.getWidth()-1;j++){

                int r = Color.red(pixels[i][j]);
                int g = Color.green(pixels[i][j]);
                int b = Color.blue(pixels[i][j]);
                int h=(r+g+b)/3;
                if(h>255)
                    h=255;
                if(h<0)
                    h=0;

                newMap.setPixel(j, i, Color.rgb(h, h, h));
            }
        }

        for(int i=1;i<bitmap.getHeight()-1;i++){
            for(int j=1;j<bitmap.getWidth()-1;j++){
                sumr=0;
                for(int k=0;k<emboss_w;k++){
                    for(int l=0;l<emboss_h;l++){

                        int r = Color.red(pixels[i-((emboss_w-1)>>1)+k][j-((
                                emboss_h-1)>>1)+l]);
                        sumr+=r*emboss_filter[k][l];
                    }
                }
                sumr/=emboss_sum;
                sumr+=128;
                if(sumr>255)
                    sumr=255;
                if(sumr<0)
                    sumr=0;
                newMap.setPixel(j, i, Color.rgb(sumr, sumr, sumr));
            }
        }
        return newMap;
    }
}
