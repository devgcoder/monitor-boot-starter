package com.github.devgcoder.monitor.model;

public class CommonParams {

  /**
   * 设备租赁 是否已失效
   */
  public static enum MonitorModelType {
    MESSAGE("message", "消息类型");
    private String key;
    private String value;

    private MonitorModelType(String key, String value) {
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

  public static enum MonitorMailType {
    SEND_ERROR("E0001", "send error", "请求错误"),
    SEND_OVERTIME("E0002", "send overtime", "请求超时");

    private String key;
    private String value;
    private String name;

    private MonitorMailType(String key, String value, String name) {
      this.key = key;
      this.value = value;
      this.name = name;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    public String getName() {
      return name;
    }
  }
}