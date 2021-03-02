package com.org.monitor.ext;

public class DingMessageExt {

  private String msgId;
  private String title;
  private String msgType;
  //  msgType为link时用
  private String msgUrl;
  // msgType为link时用
  private String picUrl;
  private String msgContent;
  private String isAtAll;

  public DingMessageExt(String msgType, String msgContent) {
    this.msgType = msgType;
    this.msgContent = msgContent;
  }

  public DingMessageExt(String msgType, String msgContent, String msgUrl) {
    this.msgType = msgType;
    this.msgContent = msgContent;
    this.msgUrl = msgUrl;
  }

  public DingMessageExt(String msgType, String msgContent, String msgUrl, String isAtAll) {
    this.msgType = msgType;
    this.msgContent = msgContent;
    this.msgUrl = msgUrl;
    this.isAtAll = isAtAll;
  }

  public DingMessageExt(String msgType, String title, String msgUrl, String picUrl, String msgContent, String isAtAll) {
    this.title = title;
    this.msgType = msgType;
    this.msgUrl = msgUrl;
    this.picUrl = picUrl;
    this.msgContent = msgContent;
    this.isAtAll = isAtAll;
  }

  public String getMsgId() {
    return msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public String getMsgType() {
    return msgType;
  }

  public void setMsgType(String msgType) {
    this.msgType = msgType;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMsgUrl() {
    return msgUrl;
  }

  public void setMsgUrl(String msgUrl) {
    this.msgUrl = msgUrl;
  }

  public String getPicUrl() {
    return picUrl;
  }

  public void setPicUrl(String picUrl) {
    this.picUrl = picUrl;
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
