package com.github.devgcoder.monitor.utils;

public class CommonEnum {

  public static enum messageType {
    MsgController(1, "Controller"),
    MsgService(2, "Service"),
    MsgMapper(3, "Mapper");
    private int key;
    private String value;

    private messageType(int key, String value) {
      this.key = key;
      this.value = value;
    }

    public int getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }
  }

  public static enum resultType {
    INFO("info", "info"),
    ERROR("error", "error");
    private String key;
    private String value;

    private resultType(String key, String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }
  }

  public static enum WarnType {
    BUSSINESS("0", "【业务报警】-"),
    SERVER("1", "【服务器报警】-"),
    APP("2", "【APP报警】-"),
    MONITOR("3", "【监控报警】-");
    private String key;
    private String name;

    private WarnType(String key, String name) {
      this.key = key;
      this.name = name;
    }

    public String getKey() {
      return key;
    }

    public String getName() {
      return name;
    }
  }

  public static enum WarnReason {
    APPCRASH("E0001", "app crash");
    private String key;
    private String name;

    private WarnReason(String key, String name) {
      this.key = key;
      this.name = name;
    }

    public String getKey() {
      return key;
    }

    public String getName() {
      return name;
    }
  }

}
