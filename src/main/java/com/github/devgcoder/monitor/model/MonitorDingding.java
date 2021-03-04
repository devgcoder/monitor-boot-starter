package com.github.devgcoder.monitor.model;

public class MonitorDingding extends MonitorModelType {

  private String msgType;
  private String msgContent;
  private String isAtAll;

  public MonitorDingding(String msgType, String msgContent) {
    this.msgType = msgType;
    this.msgContent = msgContent;
    this.isAtAll = "false";
  }

  public MonitorDingding(String msgType, String msgContent, String isAtAll) {
    this.msgType = msgType;
    this.msgContent = msgContent;
    this.isAtAll = isAtAll;
  }

  public String getMsgType() {
    return msgType;
  }

  public void setMsgType(String msgType) {
    this.msgType = msgType;
  }

  public String getMsgContent() {
    return msgContent;
  }

  public void setMsgContent(String msgContent) {
    this.msgContent = msgContent;
  }

  public String getIsAtAll() {
    return isAtAll;
  }

  public void setIsAtAll(String isAtAll) {
    this.isAtAll = isAtAll;
  }
}
