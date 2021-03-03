package com.github.devgcoder.model;

public class ElasticSearchConfig {

  private String hostname;

  private String port;

  private String schemeName;

  private String userName;

  private String userPass;

  private Integer connectionTimeOut;

  private Integer socketTimeOut;

  private Integer connectionRequestTimeOut;

  private Integer maxConnectTotal;

  private Integer maxConnectPerRoute;

  private Integer deleteFromDayNum;

  private Integer deleteDayNum;

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getSchemeName() {
    return schemeName;
  }

  public void setSchemeName(String schemeName) {
    this.schemeName = schemeName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserPass() {
    return userPass;
  }

  public void setUserPass(String userPass) {
    this.userPass = userPass;
  }

  public Integer getConnectionTimeOut() {
    return connectionTimeOut;
  }

  public void setConnectionTimeOut(Integer connectionTimeOut) {
    this.connectionTimeOut = connectionTimeOut;
  }

  public Integer getSocketTimeOut() {
    return socketTimeOut;
  }

  public void setSocketTimeOut(Integer socketTimeOut) {
    this.socketTimeOut = socketTimeOut;
  }

  public Integer getConnectionRequestTimeOut() {
    return connectionRequestTimeOut;
  }

  public void setConnectionRequestTimeOut(Integer connectionRequestTimeOut) {
    this.connectionRequestTimeOut = connectionRequestTimeOut;
  }

  public Integer getMaxConnectTotal() {
    return maxConnectTotal;
  }

  public void setMaxConnectTotal(Integer maxConnectTotal) {
    this.maxConnectTotal = maxConnectTotal;
  }

  public Integer getMaxConnectPerRoute() {
    return maxConnectPerRoute;
  }

  public void setMaxConnectPerRoute(Integer maxConnectPerRoute) {
    this.maxConnectPerRoute = maxConnectPerRoute;
  }

  public Integer getDeleteFromDayNum() {
    return deleteFromDayNum;
  }

  public void setDeleteFromDayNum(Integer deleteFromDayNum) {
    this.deleteFromDayNum = deleteFromDayNum;
  }

  public Integer getDeleteDayNum() {
    return deleteDayNum;
  }

  public void setDeleteDayNum(Integer deleteDayNum) {
    this.deleteDayNum = deleteDayNum;
  }
}
