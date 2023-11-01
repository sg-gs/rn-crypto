import Foundation
import IDZSwiftCommonCrypto

struct AES256 {
    private var key: Data
    private var iv: Data
    
    public init(key: Data, iv: Data) throws {
        guard key.count == kCCKeySizeAES256 else {
            throw Error.badKeyLength
        }
        
        guard iv.count == kCCBlockSizeAES128 else {
            throw Error.badInputVectorLength
        }
    }
    
    enum Error: Swift.Error {
        case keyGeneration(status: Int)
        case cryptoFailed(status: CCCryptorStatus)
        case badKeyLength
        case badInputVectorLength
    }
}

@objc(RnCrypto)
class RnCrypto: NSObject {

    @objc(multiply:withB:withResolver:withRejecter:)
    func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve(a*b)
    }
    
    @objc(encryptFile:withEncryptPath:theKey:theIv:theCallback:)
    func encryptFile(plainFilePath: NSString, encryptedFilePath: NSString, hexKey: NSString, hexIv: NSString, cb: RCTResponseSenderBlock) -> Void {
        var plainFileInputStream = InputStream(fileAtPath: plainFilePath);
        var encryptedFileOutputStream = OutputStream(toFileAtPath: encryptedFilePath, append: false)
        
        let key = Array<UInt8>()
        let iv = Array<UInt8>()
        
        let bufferSize = 4096
        
        var inputBuffer = Array<UInt8>(repeating: 0, count: bufferSize)
        var outputBuffer = Array<UInt8>(repeating: 0, count: bufferSize)
        
        plainFileInputStream.open()
        encryptedFileOutputStream.open()
        
        var encryptedBytes = 0;
        
        while plainFileInputStream.hasBytesAvailable {
            let bytesRead = plainFileInputStream.read(&inputBuffer, maxLength: inputBuffer.count)
            let status = encrypter.update(bufferIn: &inputBuffer, byteCountIn: bytesRead, bufferOut: &inputBuffer, byteCountOut: &encryptedBytes);
            
            if (status == false) {
                throw
            }
            
            let bytesWritten = encryptedFileOutputStream.write(inputBuffer, maxLength: encryptedBytes)
            
            assert(bytesWritten == encryptedBytes)
        }
        
        plainFileInputStream.close()
        encryptedFileOutputStream.close()
    }
}
