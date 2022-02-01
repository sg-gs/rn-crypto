package com.rncrypto.util;

import com.facebook.common.util.Hex;
import com.rncrypto.DecryptFileRepository;
import com.rncrypto.EncryptFileRepository;
import com.rncrypto.ThreadPerTaskExecutor;

import java.security.SecureRandom;

public class CryptoService {
  private static CryptoService instance = null;
  private final EncryptFileRepository encryptFileRepository;
  private final DecryptFileRepository decryptFileRepository;

  public CryptoService(EncryptFileRepository encryptFileRepository, DecryptFileRepository decryptFileRepository) {
    this.decryptFileRepository = decryptFileRepository;
    this.encryptFileRepository = encryptFileRepository;
  }

  private synchronized static void createInstance() {
    if (instance == null) {
      instance = new CryptoService(
        new EncryptFileRepository(new ThreadPerTaskExecutor()),
        new DecryptFileRepository(new ThreadPerTaskExecutor())
      );
    }
  }

  public static CryptoService getInstance() {
    if (instance == null) {
      createInstance();
    }
    return instance;
  }

  public static byte[] generateIv(int size) {
    byte[] iv = new byte[size];
    new SecureRandom().nextBytes(iv);

    return iv;
  }

  /**
   * Encrypts a file given in a sourcePath, writing output on destinationPath
   *
   * @param sourcePath
   * @param destinationPath
   * @param hexKey
   * @param hexIv
   * @param runInBackground Determines if encryption should be run on background
   * @param onlyErrorCallback
   */
  public void encryptFile(
    String sourcePath,
    String destinationPath,
    String hexKey,
    String hexIv,
    boolean runInBackground,
    OnlyErrorCallback onlyErrorCallback
  ) {
    byte[] key = Hex.decodeHex(hexKey);
    byte[] iv = Hex.decodeHex(hexIv);

    if (runInBackground) {
      this.encryptFileRepository.encryptFileInBackground(
        sourcePath,
        destinationPath,
        key,
        iv,
        onlyErrorCallback
      );
    } else {
      this.encryptFileRepository.encryptFile(
        sourcePath,
        destinationPath,
        key,
        iv,
        onlyErrorCallback
      );
    }
  }

  public void decryptFile(
    String sourcePath,
    String destinationPath,
    String hexKey,
    String hexIv,
    boolean runInBackground,
    OnlyErrorCallback onlyErrorCallback
  ) {
    byte[] key = Hex.decodeHex(hexKey);
    byte[] iv = Hex.decodeHex(hexIv);

    if (runInBackground) {
      this.decryptFileRepository.decryptFileInBackground(
        sourcePath,
        destinationPath,
        key,
        iv,
        onlyErrorCallback
      );
    } else {
      this.decryptFileRepository.decryptFile(
        sourcePath,
        destinationPath,
        key,
        iv,
        onlyErrorCallback
      );
    }
  }
}
