import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'rn-crypto' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const RnCrypto = NativeModules.RnCrypto
  ? NativeModules.RnCrypto
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return RnCrypto.multiply(a, b);
}

interface FileEntry {
  mtime: number;
  name: string;
  path: string;
  size: number;
  type: number;
}

export function getDocumentsPath(): Promise<string> {
  return RnCrypto.getDocumentsPath();
}

export function getDownloadsPath(): Promise<string> {
  return RnCrypto.getDownloadsPath();
}

export function listDir(dir: string): Promise<FileEntry[]> {
  return RnCrypto.listDir(dir);
}

/**
 * Encrypts a given file in AES256-CTR writing it encrypted on the encryptedFilePath
 * @param plainFilePath Path where file is located
 * @param encryptedFilePath Path where file encrypted is going to be written
 * @param hexKey Encryption key in hex format
 * @param hexIv IV in hex format
 * @param cb Only error callback
 */
export function encryptFile(
  plainFilePath: string,
  encryptedFilePath: string,
  hexKey: string,
  hexIv: string,
  cb: (err: Error) => void
): void {
  RnCrypto.encryptFile(plainFilePath, encryptedFilePath, hexKey, hexIv, cb);
}

/**
 * Decrypts a given encrypted file, writing it decrypted on the plainFilePath
 * @param encryptedFilePath Path where encrypted file is located
 * @param plainFilePath Path where file decrypted is going to be written
 * @param hexKey Encryption key in hex format
 * @param hexIv IV in hex format
 * @param cb Only error callback
 */
export function decryptFile(
  encryptedFilePath: string,
  plainFilePath: string,
  hexKey: string,
  hexIv: string,
  cb: (err: Error) => void
): void {
  RnCrypto.decryptFile(encryptedFilePath, plainFilePath, hexKey, hexIv, cb);
}
