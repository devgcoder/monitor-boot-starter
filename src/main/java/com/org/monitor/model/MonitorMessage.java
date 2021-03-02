package com.org.monitor.model;

import java.util.Map;

public class MonitorMessage extends MonitorModelType {

  private int messageType; //类型: 1-cont

  private String classMethod; // 类名

  private String requestUrl; // 请求url

  private String requestIp; // 请求IP

  private long costTime; //花费时长

  private String resultType;// 返回类型: info,error

  private String modelName; // 模块名称

  private String startTime; // 开始时间

  private String messageKey; // 唯一键,用于排查问题

  private Map<String,Object> requestParams; // 请求参数

  public int getMessageType() {
    return messageType;
  }

  public void setMessageType(int messageType) {
    this.messageType = messageType;
  }

  public String getClassMethod() {
    return classMethod;
  }

  public void setClassMethod(String classMethod) {
    this.classMethod = classMethod;
  }

  public String getRequestUrl() {
    return requestUrl;
  }

  public void setRequestUrl(String requestUrl) {
    this.requestUrl = requestUrl;
  }

  public String getRequestIp() {
    return requestIp;
  }

  public void setRequestIp(String requestIp) {
    this.requestIp = requestIp;
  }

  public long getCostTime() {
    return costTime;
  }

  public void setCostTime(long costTime) {
    this.costTime = costTime;
  }

  public String getResultType() {
    return resultType;
  }

  public void setResultType(String resultType) {
    this.resultType = resultType;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public void setMessageKey(String messageKey) {
    this.messageKey = messageKey;
  }

  public Map<String, Object> getRequestParams() {
    return requestParams;
  }

  public void setRequestParams(Map<String, Object> requestParams) {
    this.requestParams = requestParams;
  }
}
