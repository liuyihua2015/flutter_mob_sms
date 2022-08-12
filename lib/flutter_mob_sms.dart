
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterMobSms {
  static const MethodChannel _channel = MethodChannel('flutter_mob_sms');

  static Future<void> submitPolicyGrantResult() async{
    await _channel.invokeMethod("submitPolicyGrantResult");
  }

  static Future<Map> getTextCode(String phone,String tempCode,
      {String zone = "86"}) async {
    Map result = await _channel.invokeMethod('getTextCode',
        {"phoneNumber": phone, "zone": zone, "tempCode": tempCode});
    return result;
  }

  static Future<Map> getVoiceCode(String phone,String tempCode,
      {String zone = "86"}) async {
    Map result = await _channel.invokeMethod('getVoiceCode',
        {"phoneNumber": phone, "zone": zone, "tempCode": tempCode});
    return result;
  }


  static Future<Map> commitCode(String phone, String code,
      {String zone = "86"}) async {
    Map result = await _channel.invokeMethod(
        'commitCode', {"phoneNumber": phone, "zone": zone, "code": code});
    return result;
  }

  static Future getSupportedCountries(Function(dynamic ret, Map? err) result) {
    Future<dynamic> callback = _channel.invokeMethod('getSupportedCountries');
    callback.then((dynamic response) {
      if (response is Map) {
        result(response["ret"], response["err"]);
      } else {
        result(null, null);
      }
    });

    return callback;
  }

  static Future getVersion(Function(dynamic ret) result) {
    Future<dynamic> callback = _channel.invokeMethod('getVersion');

    callback.then((dynamic response) {
      print(response);
    });

    return callback;
  }


}
