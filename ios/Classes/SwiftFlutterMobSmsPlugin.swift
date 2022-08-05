import Flutter
import UIKit

public class SwiftFlutterMobSmsPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_mob_sms", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterMobSmsPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

    func error(toUZDict error: Error?) -> [AnyHashable : Any]? {
       var dict: [AnyHashable : Any]? = nil
       if let error = error {
           dict = [:]
           let des = (error as NSError).userInfo["description"] as? String
           let code = (error as NSError).code

           if let des = des {
               dict?["msg"] = des
           } else {
               dict?["msg"] = (error as NSError).userInfo[NSLocalizedDescriptionKey]
           }
           dict?["code"] = NSNumber(value: code)
       }

       return dict
    }
    
  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    
      let arguments = call.arguments as? [String:String]

      if "getTextCode" == call.method {
          print("\(arguments ?? [:])")
          SMSSDK.getVerificationCode(by: .SMS, phoneNumber: arguments?["phoneNumber"], zone: arguments?["zone"], template: arguments?["tempCode"], result: { error in
             var dict: [AnyHashable : Any] = [:]
             if let error = error {
                 if let anError = self.error(toUZDict: error) {
                     dict["err"] = anError
                 }
             }
             result(dict)
         })
      }else if "getVoiceCode" == call.method {
          SMSSDK.getVerificationCode(by: .voice, phoneNumber: arguments?["phoneNumber"], zone: arguments?["zone"], template: arguments?["tempCode"]) { error in
              var dict: [AnyHashable : Any] = [:]
              if let error = error {
                  if let anError = self.error(toUZDict: error) {
                      dict["err"] = anError
                  }
              }
              result(dict)
          }
      }else if "commitCode" == call.method {
          SMSSDK.commitVerificationCode(arguments?["code"] , phoneNumber: arguments?["phoneNumber"], zone: arguments?["zone"]) { error in
              var dict: [AnyHashable : Any] = [:]
              if let error = error {
                  if let anError = self.error(toUZDict: error) {
                      dict["err"] = anError
                  }
              }
              result(dict)
          }
      }else if "getSupportedCountries" == call.method {
          SMSSDK.getCountryZone { error, zonesArray in
              
              var dict: [AnyHashable : Any] = [:]
              if let error = error {
                  if let anError = self.error(toUZDict: error) {
                      dict["err"] = anError
                  }
              }
              if let zonesArray  = zonesArray {
                  dict["ret"] = zonesArray
              }

            result(dict)
          }
      }else if "getVersion" == call.method {
            result(SMSSDK.sdkVersion)
      }else{
          result("no sms")
      }
      
  }
    
    func test() -> Void {
        let call = FlutterMethodCall();
        
        handle(call) { result in
            print(result ?? "nil");
        };
    }
    
}
