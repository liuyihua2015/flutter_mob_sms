
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

  static Future<Map> commitCode(String phone, String code,
      {String zone = "86"}) async {
    Map result = await _channel.invokeMethod(
        'commitCode', {"phoneNumber": phone, "zone": zone, "code": code});
    return result;
  }
}
