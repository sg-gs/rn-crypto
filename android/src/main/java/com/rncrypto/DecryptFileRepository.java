package com.rncrypto;

import com.facebook.common.util.Hex;
import com.rncrypto.util.OnlyErrorCallback;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

  public DecryptFileRepository(Executor executor) {
    this.executor = executor;
  }

  public void decryptFileInBackground(
    String sourcePath,
    String destinationPath,
    byte[] key,
    byte[] iv,
    final OnlyErrorCallback callback
  ) {
    executor.execute(() -> decryptFile(sourcePath, destinationPath, key, iv, callback));
  }

  public void decryptFile(
    String sourcePath,
    String destinationPath,
    byte[] key,
    byte[] iv,
    final OnlyErrorCallback onlyErrorCallback
  ) {
    try {
      this.decryptFile(sourcePath, destinationPath, this.getAES256CTRCipher(key, iv));

      onlyErrorCallback.onComplete(null);
    } catch (IOException | NoSuchPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeyException e) {
      e.printStackTrace();
      onlyErrorCallback.onComplete(e);
    }
  }

  /**
   * Generates an AES-256-CTR Cipher
   *
   * @param key
   * @param iv
   * @return
   */
  private Cipher getAES256CTRCipher(byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
    SecretKeySpec secretKey = new SecretKeySpec(key, 0, key.length, "AES");
    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

    if (!cipher.getAlgorithm().toUpperCase().startsWith(("AES/CTR")))
      throw new IllegalArgumentException("Invalid algorithm, only AES/CTR mode supported");

    cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

    return cipher;
  }

  /**
   * Decrypts a file at sourcePath, writes the output to destinationPath
   *
   * @param sourcePath      Path where file to decrypt is
   * @param destinationPath Path where decrypted content is going to be written
   * @param cipher          Cipher used to decrypt the content
   */
  private void decryptFile(
    String sourcePath,
    String destinationPath,
    Cipher cipher
  ) throws IOException {
    this.decrypt(
      new FileInputStream(sourcePath),
      new FileOutputStream(destinationPath),
      cipher
    );
  }

  /**
   * Decrypts content received from an input and writes it to an output
   *
   * @param inputStream  Source to decrypt
   * @param outputStream Source to write decrypted content
   * @param cipher       Cipher used to decrypt data
   */
  public void decrypt(
    InputStream inputStream,
    OutputStream outputStream,
    Cipher cipher
  ) throws IOException {
    CipherInputStream cis = new CipherInputStream(inputStream, cipher);

    int b;
    byte[] buffer = new byte[4096];

    while ((b = cis.read(buffer)) != -1) {
      outputStream.write(buffer, 0, b);
    }

    outputStream.flush();
    outputStream.close();
    cis.close();
  }
}
