package com.org.monitor.utils;

import com.alibaba.fastjson.JSON;
import com.org.monitor.model.CommonParams.MonitorModelType;
import com.org.monitor.model.MonitorAppWarn;
import com.org.monitor.model.MonitorConfig;

public class AppUtil {

  /**
   * @param appType APP类型: android/ios
   * @param username 用户名
   * @param appVersion APP版本号
   * @param phoneModel 手机型号
   * @param warnReason 预警原因: app crash
   * @param warnTime APP崩溃时间  yyyy-MM-dd HH:mm:ss
   */
  public static void sendMessage(String appType, String username, String appVersion, String phoneModel, String warnReason, String warnTime) {
    MonitorConfig monitorConfig = MonitorUtil.monitorConfig;
    MonitorAppWarn monitorAppWarn = getMonitorAppWarn(appType, username, appVersion, phoneModel, warnReason, warnTime);
    String sendMqMsg = JSON.toJSONString(monitorAppWarn);
    MonitorUtil.sendMessage(monitorConfig, monitorConfig.getRabbitConfig().getRabbitMonitorMessage(), sendMqMsg);
  }


  public static MonitorAppWarn getMonitorAppWarn(String appType, String username, String appVersion, String phoneModel, String warnReason,
      String warnTime) {
    if (warnTime == null || warnTime.equals("")) {
      warnTime = MonitorUtil.getNowDate(MonitorUtil.FORMAT_PATTERN3);
    }
    MonitorAppWarn monitorAppWarn = new MonitorAppWarn();
    monitorAppWarn.setAppType(appType);
    monitorAppWarn.setUsername(username);
    monitorAppWarn.setAppVersion(appVersion);
    monitorAppWarn.setPhoneModel(phoneModel);
    monitorAppWarn.setWarnReason(warnReason);
    monitorAppWarn.setWarnTime(warnTime);
    monitorAppWarn.setMessageModelType(MonitorModelType.APP.getKey());
    return monitorAppWarn;
  }
}
