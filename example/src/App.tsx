import * as React from 'react';

import { StyleSheet, View, Text, PermissionsAndroid } from 'react-native';
import {
  decryptFile,
  encryptFile,
  getDocumentsPath,
  getDownloadsPath,
  listDir,
  multiply,
} from 'rn-crypto';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    multiply(3, 7).then(setResult);

    askForReadPermissions()
      .then(encrypt)
      .catch((err) => {
        console.log('err', err.message);
      });
  }, []);

  async function askForReadPermissions() {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE
    );

    if (granted === PermissionsAndroid.RESULTS.GRANTED) {
      console.log('granted!!!');
    } else {
      console.log('not granted');
    }
  }

  async function encrypt() {
    const path = await getDocumentsPath();
    console.log('path', path);

    const dirList = await listDir(path);
    console.log('dirList', dirList);

    if (dirList.length > 0) {
      console.log('There are files here!');
      encryptFile(
        '/data/user/0/com.example.rncrypto/files/ReactNativeDevBundle.js',
        '/data/user/0/com.example.rncrypto/files/ReactNativeDevBundle.js.z',
        'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
        'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
        (encryptErr: Error) => {
          if (encryptErr) {
            console.log(encryptErr);
          } else {
            decryptFile(
              '/data/user/0/com.example.rncrypto/files/ReactNativeDevBundle.js.z',
              '/data/user/0/com.example.rncrypto/files/out.js',
              'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
              'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
              (decryptErr: Error) => {
                if (decryptErr) {
                  console.log(decryptErr);
                } else {
                  console.log('decrypted!!!');
                }
              }
            );
          }
        }
      );
    }
  }

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
