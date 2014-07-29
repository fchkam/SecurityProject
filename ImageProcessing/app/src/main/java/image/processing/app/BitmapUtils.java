package image.processing.app;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by kentma on 2014-07-28.
 */
public class BitmapUtils {

    public static Bitmap extractColor(Bitmap bitmap, int color, double threshold){
        int [][] pixels = getPixels(bitmap);
        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();

        int cR = Color.red(color);
        int cG = Color.green(color);
        int cB = Color.blue(color);


        Bitmap.Config conf = Bitmap.Config.RGB_565;
        Bitmap newMap = Bitmap.createBitmap(bWidth, bHeight, conf);

        for (int i = 0;i < bWidth; i++){
            for (int j = 0; j < bHeight; j++){
                int r = Color.red(pixels[i][j]);
                int g = Color.green(pixels[i][j]);
                int b = Color.blue(pixels[i][j]);

                double rr = (r-cR)*(r-cR);
                double gg = (g-cG)*(g-cG);
                double bb = (b-cB)*(b-cB);

                if (Math.sqrt(rr+gg+bb) <= threshold){
                    newMap.setPixel(i,j,Color.WHITE);
                } else {
                    newMap.setPixel(i,j,Color.BLACK);
                }
            }
        }
    }

    public boolean checkColor (int r, int g, int b, int color, double threshold){
        int cR = Color.red(color);
        int cG = Color.green(color);
        int cB = Color.blue(color);

        double rr = (r-_r)*(r-_r);
        double gg = (g-_g)*(g-_g);
        double bb = (b-_b)*(b-_b);

        double x = Math.sqrt(rr+bb+gg);

        return (x <= threshold);
    }

    public static int[][] getPixels(Bitmap bitmap){
        int [] temp = new int [bitmap.getWidth()*bitmap.getHeight()];
        int[][] pixels = new int[bitmap.getHeight()][bitmap.getWidth()];

        bitmap.getPixels(temp, 0, bitmap.getWidth(), 0, 0,bitmap.getWidth(), bitmap.getHeight());

        for(int i = 0; i < bitmap.getHeight(); i++){
            for(int j = 0; j < bitmap.getWidth(); j++){
                pixels[i][j] = temp[i * bitmap.getWidth()+ j];
            }
        }

        return  pixels;
    }

}
