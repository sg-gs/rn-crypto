package com.rncrypto.test;

import static org.junit.Assert.*;

import com.facebook.common.util.Hex;
import com.rncrypto.EncryptFileRepository;
import com.rncrypto.ThreadPerTaskExecutor;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.NoSuchPaddingException;

public class EncryptFileRepositoryTest {
  private EncryptFileRepository encryptFileRepository;

  public EncryptFileRepositoryTest() {
    this.encryptFileRepository = new EncryptFileRepository(new ThreadPerTaskExecutor());
  }

  @Test
  public void encryptFileRepository_encrypt_EncryptsProperly() {
    String inputStringUtf8 = "test test test";

    byte[] input = inputStringUtf8.getBytes();
    byte[] expectedOutput = Hex.decodeHex("7495615064043330f50e9858b503");

    byte[] key = Hex.decodeHex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    byte[] iv = Hex.decodeHex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    InputStream inputStream = new ByteArrayInputStream(input);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try {
      encryptFileRepository.encrypt(inputStream, outputStream, encryptFileRepository.getAES256CTRCipher(key, iv));

      byte[] output = outputStream.toByteArray();

      assertTrue(Arrays.equals(output, expectedOutput));
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | IOException ex) {
      fail("Cipher should not throw");
    }
  }

  @Test
  public void encryptFileRepository_getAES256CTRCipher_DoesNotAllowInvalidKey() {
    byte[] key = Hex.decodeHex("aa");
    byte[] iv = Hex.decodeHex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    try {
      encryptFileRepository.getAES256CTRCipher(key, iv);

      fail("Cipher should throw if key is invalid");
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalArgumentException ex) {
      assertTrue(ex instanceof IllegalArgumentException);
      assertTrue(((IllegalArgumentException) ex).getMessage().contains("Invalid key length"));
    }
  }

  @Test
  public void encryptFileRepository_getAES256CTRCipher_DoesNotAllowInvalidIV() {
    byte[] key = Hex.decodeHex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    byte[] iv = Hex.decodeHex("aa");

    try {
      encryptFileRepository.getAES256CTRCipher(key, iv);

      fail("Cipher should throw if IV is invalid");
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalArgumentException ex) {
      assertTrue(ex instanceof IllegalArgumentException);
      assertTrue(((IllegalArgumentException) ex).getMessage().contains("Invalid iv length"));
    }
  }
}
