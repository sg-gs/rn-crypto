package com.rncrypto;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.security.keystore.KeyGenParameterSpec;

import androidx.annotation.NonNull;
import androidx.security.crypto.*;

import com.facebook.common.util.Hex;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@ReactModule(name = RnCryptoModule.NAME)
public class RnCryptoModule extends ReactContextBaseJavaModule {
    public static final String NAME = "RnCrypto";

    public RnCryptoModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    /*
     * Convert hex string to byte[]
     *
       * @param hexString
     *            the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
      if (hexString == null || hexString.equals("")) {
        return null;
      }
      hexString = hexString.toUpperCase();
      int length = hexString.length() / 2;
      char[] hexChars = hexString.toCharArray();
      byte[] d = new byte[length];
      for (int i = 0; i < length; i++) {
        int pos = i * 2;
        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
      }
      return d;
    }

    /**
     * Convert char to byte
     *
     * @param c
     *            char
     * @return byte
     */
    private static byte charToByte(char c) {
      return (byte) "0123456789ABCDEF".indexOf(c);
    }

    @ReactMethod
    public void getDocumentsPath(Promise promise) {
      promise.resolve(this.getReactApplicationContext().getFilesDir().getAbsolutePath());
    }

    @ReactMethod
    public void getDownloadsPath(Promise promise) {
      promise.resolve(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    }

    @ReactMethod
    public void listDir(String dirPath, Promise promise) {
      try {
        File file = new File(dirPath);

        if (!file.exists()) {
          throw new Exception("Folder does not exist");
        }

        File[] files = file.listFiles();

        if (files == null) {
          promise.resolve(null);
          return;
        }

        WritableArray fileMaps = Arguments.createArray();

        for (File childFile : files) {
          WritableMap fileMap = Arguments.createMap();

          fileMap.putDouble("mtime", (double) childFile.lastModified() / 1000);
          fileMap.putString("name", childFile.getName());
          fileMap.putString("path", childFile.getAbsolutePath());
          fileMap.putDouble("size", (double) childFile.length());
          fileMap.putInt("type", childFile.isDirectory() ? 1 : 0);

          fileMaps.pushMap(fileMap);
        }

        promise.resolve(fileMaps);
      } catch (Exception ex) {
        ex.printStackTrace();
        promise.reject(ex);
      }
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    public static String bytesToHex(byte[] bytes) {
      byte[] hexChars = new byte[bytes.length * 2];
      for (int j = 0; j < bytes.length; j++) {
        int v = bytes[j] & 0xFF;
        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
      }
      return new String(hexChars, StandardCharsets.UTF_8);
    }

    @ReactMethod
    public void encryptFile(
      String sourcePath,
      String destinationPath,
      String hexKey,
      String hexIv,
      Callback cb
    ) {
      Exception ex = null;
      try {
        _encryptFile(sourcePath, destinationPath, hexKey, hexIv);
      } catch (IOException e) {
        e.printStackTrace();
        ex = e;
      } catch (NoSuchPaddingException e) {
        e.printStackTrace();
        ex = e;
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        ex = e;
      } catch (InvalidAlgorithmParameterException e) {
        e.printStackTrace();
        ex = e;
      } catch (InvalidKeyException e) {
        e.printStackTrace();
        ex = e;
      }
      if (ex != null) {
        cb.invoke(ex);
      } else {
        cb.invoke((Object) null);
      }
    }

    public void _encryptFile(
      String sourcePath,
      String destinationPath,
      String hexKey,
      String hexIv
    ) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
      FileInputStream fis = new FileInputStream(sourcePath);
      FileOutputStream fos = new FileOutputStream(destinationPath);

      // byte[] key = new BigInteger("7F" + hexKey, 16).toByteArray();
      // SecretKeySpec secretKey = new SecretKeySpec(key, 1, key.length-1, "AES");

      byte[] key = Hex.decodeHex(hexKey);
      byte[] iv = Hex.decodeHex(hexIv);

      SecretKeySpec secretKey = new SecretKeySpec(key, 0, key.length, "AES");
      // new SecureRandom().nextBytes(iv);

      Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

      if (!cipher.getAlgorithm().toUpperCase().startsWith(("AES/CTR")))
        throw new IllegalArgumentException("Invalid algorithm, only AES/CTR mode supported");

      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

      CipherOutputStream cos = new CipherOutputStream(fos, cipher);

      int b;
      byte[] buffer = new byte[4096];

      while ((b = fis.read(buffer)) != -1) {
        cos.write(buffer, 0, b);
      }

      cos.flush();
      cos.close();
      fis.close();
    }

    @ReactMethod
    public void decryptFile(
      String sourcePath,
      String destinationPath,
      String hexKey,
      String hexIv,
      Callback cb
    ) {
      Exception ex = null;
      try {
        _decryptFile(sourcePath, destinationPath, hexKey, hexIv);
      } catch (InvalidAlgorithmParameterException e) {
        e.printStackTrace();
      } catch (NoSuchPaddingException e) {
        e.printStackTrace();
        ex = e;
      } catch (IOException e) {
        e.printStackTrace();
        ex = e;
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        ex = e;
      } catch (InvalidKeyException e) {
        e.printStackTrace();
        ex = e;
      }

      if (ex != null) {
        cb.invoke(ex);
      } else {
        cb.invoke((Object) null);
      }
    }

    public void _decryptFile(
      String sourcePath,
      String destinationPath,
      String hexKey,
      String hexIv
    ) throws IOException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
      FileInputStream fis = new FileInputStream(sourcePath);
      FileOutputStream fos = new FileOutputStream(destinationPath);

      byte[] key = Hex.decodeHex(hexKey);
      byte[] iv = Hex.decodeHex(hexIv);

      SecretKeySpec secretKey = new SecretKeySpec(key, 0, key.length, "AES");

      Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

      if (!cipher.getAlgorithm().toUpperCase().startsWith(("AES/CTR"))) {
        throw new IllegalArgumentException("Invalid algorithm, only AES/CTR mode supported");
      }

      cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
      CipherInputStream cis = new CipherInputStream(fis, cipher);

      int b;
      byte[] buffer = new byte[4096];
      while ((b = cis.read(buffer)) != -1) {
        fos.write(buffer, 0, b);
      }
      fos.flush();
      fos.close();
      cis.close();
    }


    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod
    public void multiply(int a, int b, Promise promise) {
        promise.resolve(a * b);
    }

    public static native int nativeMultiply(int a, int b);
}
