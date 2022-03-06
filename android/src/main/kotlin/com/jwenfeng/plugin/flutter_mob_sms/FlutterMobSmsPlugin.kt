package com.jwenfeng.plugin.flutter_mob_sms

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.annotation.NonNull
import cn.smssdk.EventHandler
import cn.smssdk.SMSSDK
import com.mob.MobSDK
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.json.JSONException
import org.json.JSONObject


/** FlutterMobSmsPlugin */
class FlutterMobSmsPlugin : FlutterPlugin, MethodCallHandler {

  private val KEY_CODE = "code"
  private val KEY_MSG = "msg"
  private val BRIDGE_ERR = 700
  private val ERROR_INTERNAL = "Flutter bridge internal error: "

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel: MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_mob_sms")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    Log.d("Flutter","method:" + call.method)
    when (call.method) {
      "submitPolicyGrantResult" -> {
        MobSDK.submitPolicyGrantResult(true, null);
        result.success("success")
      }
      "unregisterEventHandler" -> {
        SMSSDK.unregisterAllEventHandler();
        result.success("success");
      }
      "getTextCode" -> {
        Log.d("Flutter","method:getTextCode")
        getTextCode(call, result)
      }
      "commitCode" -> {
        commitCode(call, result)
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  private fun commitCode(call: MethodCall, result: Result) {
    val callback = object : EventHandler() {
      override fun afterEvent(event: Int, r: Int, data: Any?) {
        if (r == SMSSDK.RESULT_COMPLETE) {
          if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
            // callback onSuccess
            // data示例：{country=86, phone=13362206853}
            val map: Map<String, Any> = HashMap()
            onSuccess(result, map)
          }
        } else {
          if (event === SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
            // callback onError
            if (data is Throwable) {
              val msg = data.message ?: ""
              onSdkError(result, msg)
            } else {
              val msg = "Sdk returned 'RESULT_ERROR', but the data is NOT an instance of Throwable"
              onInternalError(result, msg)
            }
          }
        }
      }
    }
    // Flutter的Result对象只能返回一次数据，同一个Result对象如果再次提交数据会crash（错误信息：数据已被提交过），所以要把前一次的EventHandler注销掉
    // 否则重复调用统一个接口时，smssdk会针对所有EventHandler发送回调，旧的Result对象就会被触发，导致Flutter层crash
    SMSSDK.unregisterAllEventHandler()
    SMSSDK.registerEventHandler(callback)

    val phoneNumber: String = call.argument("phoneNumber") ?: ""
    val zone: String = call.argument("zone") ?: ""
    val code: String = call.argument("code") ?: ""
    SMSSDK.submitVerificationCode(zone, phoneNumber, code)
  }

  private fun getTextCode(call: MethodCall, result: Result) {
    val callback = object : EventHandler() {
      override fun afterEvent(event: Int, r: Int, data: Any?) {
        Log.e("Flutter","event:$event,rst:$r,data:$data")
        if (r == SMSSDK.RESULT_COMPLETE) {
          if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
            val smart = data as Boolean
            // callback onSuccess
            val map: MutableMap<String, Any> = HashMap()
            map["smart"] = smart
            onSuccess(result, map)
          }
        } else {
          if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
            // callback onError
            if (data is Throwable) {
              val msg = data.message ?: ""
              onSdkError(result, msg)
            } else {
              val msg = "Sdk returned 'RESULT_ERROR', but the data is NOT an instance of Throwable"
              onInternalError(result, msg)
            }
          }
        }
      }
    }
    // Flutter的Result对象只能返回一次数据，同一个Result对象如果再次提交数据会crash（错误信息：数据已被提交过），所以要把前一次的EventHandler注销掉
    // 否则重复调用统一个接口时，smssdk会针对所有EventHandler发送回调，旧的Result对象就会被触发，导致Flutter层crash
    SMSSDK.unregisterAllEventHandler()
    SMSSDK.registerEventHandler(callback)
    val phoneNumber: String = call.argument("phoneNumber") ?: ""
    val zone: String = call.argument("zone") ?: ""
    val tempCode: String = call.argument("tempCode") ?: ""
    Log.e("Flutter","phone:$phoneNumber,tempCode:$tempCode")
    SMSSDK.getVerificationCode(tempCode, zone, phoneNumber)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    recycle()
  }

  fun recycle() {
    SMSSDK.unregisterAllEventHandler()
  }


  private fun onSuccess(result: Result, ret: Map<String, Any>) {
    val map: MutableMap<String, Any> = HashMap()
    map["success"] = ret
    Log.e("Flutter","success:$map")
    Handler(Looper.getMainLooper()).post { result.success(map) }
  }

  private fun onInternalError(result: Result, errMsg: String) {
    val errMap: MutableMap<String, Any> = HashMap()
    errMap[KEY_CODE] = BRIDGE_ERR
    errMap[KEY_MSG] = ERROR_INTERNAL + errMsg

    val map: MutableMap<String, Any> = HashMap()
    map["error"] = errMap
    Log.e("Flutter","onInternalError:$map")
    Handler(Looper.getMainLooper()).post { result.success(map) }
  }

  private fun onSdkError(result: Result, errMsg: String) {
    try {
      val errorJson = JSONObject(errMsg)
      val code: Int = errorJson.optInt("status")
      var msg: String = errorJson.optString("detail")
      if (TextUtils.isEmpty(msg)) {
        msg = errorJson.optString("error")
      }
      val errMap: MutableMap<String, Any?> = HashMap()
      errMap[KEY_CODE] = code
      errMap[KEY_MSG] = msg
      val map: MutableMap<String, Any> = HashMap()
      map["error"] = errMap
      Log.e("Flutter","onInternalError:$map")
      Handler(Looper.getMainLooper()).post { result.success(map) }
    } catch (e: JSONException) {
      onInternalError(result, "Generate JSONObject error")
    }
  }
}

