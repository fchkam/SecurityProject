package image.processing.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Created by kentma on 2014-07-29.
 */
public class CryptoManager {

    private static CryptoManager INSTANCE;

    Cipher encryptCipher;
    Cipher decryptCipher;

    private CryptoManager() {
        initCiphers("password".toCharArray());
    }

    public static CryptoManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CryptoManager();
        }
        return INSTANCE;
    }

    public void encryptFile(FileOutputStream fos, Bitmap image) {
        try{
        CipherOutputStream cos = new CipherOutputStream(fos, encryptCipher);
        image.compress(Bitmap.CompressFormat.JPEG, 100, cos);
        cos.flush();
        cos.close();}
        catch (Exception e){

        }
    }


    public Bitmap decryptImage(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            CipherInputStream cis = new CipherInputStream(fis, decryptCipher);
            Bitmap b = BitmapFactory.decodeStream(cis);
            return b;
        } catch (IOException e) {
            return null;
        }
    }


    public void initCiphers(char password[]) {

        PBEKeySpec pbeKeySpec;
        PBEParameterSpec pbeParamSpec;
        SecretKeyFactory keyFac;

        byte[] salt = {
                (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
                (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99
        };
        int count = 20;
        pbeParamSpec = new PBEParameterSpec(salt, count);
        pbeKeySpec = new PBEKeySpec(password);
        try {
            keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
            encryptCipher = Cipher.getInstance("PBEWithMD5AndDES");
            decryptCipher = Cipher.getInstance("PBEWithMD5AndDES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
            decryptCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
        } catch (Exception e) {
            Log.v("tag", e.toString());
        }
    }
}
