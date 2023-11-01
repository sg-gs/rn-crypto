#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(RnCrypto, NSObject)

RCT_EXTERN_METHOD(multiply:(float)a withB:(float)b
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(encryptFile:(NSString *) plainFilePath withEncryptPath:(NSString *)encryptedFilePath theKey:(NSString *) hexKey theIv:(NSString*)hexIv theCallback:(RCTResponseSenderBlock) callback)

@end
