package image.processing.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
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
import android.graphics.Matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainImageProcessActivity extends Activity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
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
                buttonPressed = 1;
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);


            }
        });
        Button passwordButton = (Button) this.findViewById(R.id.button2);
        passwordButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //starts the camera up
                buttonPressed = 1;
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);


            }
        });

        Button compare = (Button) this.findViewById(R.id.button3);
        compare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //starts the camera up
                compareImages();
            }
        });
    }

    public void compareImages(){
        String root = Environment.getExternalStorageDirectory().toString();

        File[] files = getListFiles(new File(root + "/user_images"));
        Bitmap[] bitmap = new Bitmap[files.length];
        for(int i = 0; i < files.length; i++){
            bitmap[i] = BitmapFactory.decodeFile(files[i].getAbsolutePath());
        }
        Random rnd = new Random();
        int x = rnd.nextInt(2);
        imageView.setImageBitmap(bitmap[x]);


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
                    bitmap = embossFilter(bitmap);
                    imageView.setImageBitmap(bitmap);
                    SaveImage(bitmap, "/user_images");

                } catch (FileNotFoundException ex) {

                } catch(IOException ex2){

                }

            }
            c1.close();
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

    public static Bitmap createContrast(Bitmap src, double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.red(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.red(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public Bitmap blackAndWhite(Bitmap bitmap){
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

                if(r > 150 && g > 150 && b > 150 ){
                    newMap.setPixel(j,i, Color.WHITE);
                }
                else{
                    newMap.setPixel(j, i, Color.BLACK);
                } 

            }
        }
        return newMap;
    }

    public Bitmap edgeDetection(Bitmap bitmap){
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

                int r1 = Color.red(pixels[i+1][j]);
                int g1 = Color.green(pixels[i+1][j]);
                int b1 = Color.blue(pixels[i+1][j]);

                int r2 = Color.red(pixels[i][j+1]);
                int g2 = Color.green(pixels[i][j+1]);
                int b2 = Color.blue(pixels[i][j + 1]);

                if((Math.sqrt((r-r1)*(r-r1)+(g-g1)*(g-g1)+(b-b1)*(b-b1)) >= 25)||
                        (Math.sqrt((r-r2)*(r-r2)+(g-g2)*(g-g2)+(b-b2)*(b-b2)) >= 25)){
                    newMap.setPixel(j,i, Color.BLACK);
                }
                else{
                    newMap.setPixel(j,i, Color.WHITE);
                }

            }
        }

        return newMap;
    }

    public Bitmap betterEdge(Bitmap bitmap){
        final int KERNAL_WIDTH = 3;
        final int KERNAL_HEIGHT = 3;

        int[][] knl ={
                {0, -1, 0},
                {-1, 4, -1},
                {0, -1, 0}
        };

        Bitmap.Config conf = Bitmap.Config.RGB_565; // see other conf types
        Bitmap dest = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), conf);

        int bmWidth = bitmap.getWidth();
        int bmHeight = bitmap.getHeight();
        int bmWidth_MINUS_2 = bmWidth - 2;
        int bmHeight_MINUS_2 = bmHeight - 2;

        for(int i = 1; i <= bmWidth_MINUS_2; i++){
            for(int j = 1; j <= bmHeight_MINUS_2; j++){

                //get the surround 3*3 pixel of current src[i][j] into a matrix subSrc[][]
                int[][] subSrc = new int[KERNAL_WIDTH][KERNAL_HEIGHT];
                for(int k = 0; k < KERNAL_WIDTH; k++){
                    for(int l = 0; l < KERNAL_HEIGHT; l++){
                        subSrc[k][l] = bitmap.getPixel(i-1+k, j-1+l);
                    }
                }

                //subSum = subSrc[][] * knl[][]
                int subSumA = 0;
                int subSumR = 0;
                int subSumG = 0;
                int subSumB = 0;

                for(int k = 0; k < KERNAL_WIDTH; k++){
                    for(int l = 0; l < KERNAL_HEIGHT; l++){
                        subSumR += Color.red(subSrc[k][l]) * knl[k][l];
                        subSumG += Color.green(subSrc[k][l]) * knl[k][l];
                        subSumB += Color.blue(subSrc[k][l]) * knl[k][l];
                    }
                }

                subSumA = Color.alpha(bitmap.getPixel(i, j));

                if(subSumR <0){
                    subSumR = 0;
                }else if(subSumR > 255){
                    subSumR = 255;
                }

                if(subSumG <0){
                    subSumG = 0;
                }else if(subSumG > 255){
                    subSumG = 255;
                }

                if(subSumB <0){
                    subSumB = 0;
                }else if(subSumB > 255){
                    subSumB = 255;
                }

                dest.setPixel(i, j, Color.argb(
                        subSumA,
                        subSumR,
                        subSumG,
                        subSumB));
            }
        }

        return dest;
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

    public Bitmap fastblur(Bitmap sentBitmap, int radius) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
}
