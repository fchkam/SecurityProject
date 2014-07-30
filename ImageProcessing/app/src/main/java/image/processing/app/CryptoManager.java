package image.processing.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by kentma on 2014-07-29.
 */
public class CryptoManager {

    private static CryptoManager INSTANCE;

    Cipher encryptCipher;
    Cipher decryptCipher;

    private CryptoManager() {
        initCiphers("e8ffc7e56311679f12b6fc91aa77a5eb".getBytes());
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


    public void initCiphers(byte[] keybytes) {

        PBEKeySpec pbeKeySpec;
        PBEParameterSpec pbeParamSpec;
        SecretKeyFactory keyFac;

        byte[] salt = {
                (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
                (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99,
                (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
                (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99
        };

        IvParameterSpec ivSpec = new IvParameterSpec(salt);
        SecretKeySpec newKey = new SecretKeySpec(keybytes, "AES");
        try {
            encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
            decryptCipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        } catch (Exception e) {
            Log.v("tag", e.toString());
        }
    }
}
