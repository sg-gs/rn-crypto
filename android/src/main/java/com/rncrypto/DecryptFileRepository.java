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
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DecryptFileRepository {
  private final Executor executor;

  public DecryptFileRepository(Executor executor) { this.executor = executor; }

  public void decryptFileInBackground(
    String sourcePath,
    String destinationPath,
    String hexKey,
    String hexIv,
    final RepositoryCallback callback
  ) {
    executor.execute(() -> {
      try {
        decryptFile(sourcePath, destinationPath, hexKey, hexIv);
        callback.onComplete(null);
      } catch (IOException | NoSuchPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeyException e) {
        e.printStackTrace();
        callback.onComplete(e);
      }
    });
  }

  public void decryptFile(
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
}
