# rn-crypto

A library for using crypto in native modules

## Installation

```sh
npm install rn-crypto
```

## Usage

```js
import { encryptFile } from "rn-crypto";

// ...
const sourcePath = '/path/where/content/to/encrypt/is';
const destinationPath = '/path/where/writing/encrypted/output';
const key = 'hexadecimal-encryption-file-key';
const iv = 'hexadecimal-initialization-vector'; 

encryptFile(
  sourcePath,
  destinationPath,
  key,
  iv,
  (err) => {
    if (err) {
      // handle error...
    } else {
      // file is encrypted succesfully, it can be found at destinationPath
    }
  }
)
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
