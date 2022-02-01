package com.rncrypto;

import com.facebook.common.util.Hex;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

interface RepositoryCallback {
  void onComplete(Exception ex);
}

public class EncryptFileRepository {
  private final Executor executor;

  public EncryptFileRepository(Executor executor) {
    this.executor = executor;
  }

  public void encryptFileInBackground(
    String sourcePath,
    String destinationPath,
    String hexKey,
    String hexIv,
    final RepositoryCallback callback
  ) {
    executor.execute(() -> {
      try {
        encryptFile(sourcePath, destinationPath, hexKey, hexIv);
        callback.onComplete(null);
      } catch (IOException | NoSuchPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeyException e) {
        e.printStackTrace();
        callback.onComplete(e);
      }
    });
  }

  public void encryptFile(
    String sourcePath,
    String destinationPath,
    String hexKey,
    String hexIv
  ) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
    FileInputStream fis = new FileInputStream(sourcePath);
    FileOutputStream fos = new FileOutputStream(destinationPath);

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
}
