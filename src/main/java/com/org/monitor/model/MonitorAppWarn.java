package com.org.monitor.model;

public class MonitorAppWarn extends MonitorModelType {

  /**
   * APP类型: android/ios
   */
  private String appType;

  /**
   * 用户名
   */
  private String username;

  /**
   * app型号
   */
  private String  appVersion;

  /**
   * 手机版本
   */
  private String phoneModel;

  /**
   * 报警类型,Crash
   */
  private String warnReason;

  /**
   * APP崩溃时间
   */
  private String warnTime;

  public String getAppType() {
    return appType;
  }

  public void setAppType(String appType) {
    this.appType = appType;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public void setAppVersion(String appVersion) {
    this.appVersion = appVersion;
  }

  public String getPhoneModel() {
    return phoneModel;
  }

  public void setPhoneModel(String phoneModel) {
    this.phoneModel = phoneModel;
  }

  public String getWarnReason() {
    return warnReason;
  }

  public void setWarnReason(String warnReason) {
    this.warnReason = warnReason;
  }

  public String getWarnTime() {
    return warnTime;
  }

  public void setWarnTime(String warnTime) {
    this.warnTime = warnTime;
  }
}
