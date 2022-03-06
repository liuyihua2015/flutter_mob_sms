#import "FlutterMobSmsPlugin.h"
#if __has_include(<flutter_mob_sms/flutter_mob_sms-Swift.h>)
#import <flutter_mob_sms/flutter_mob_sms-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_mob_sms-Swift.h"
#endif

@implementation FlutterMobSmsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterMobSmsPlugin registerWithRegistrar:registrar];
}
@end
