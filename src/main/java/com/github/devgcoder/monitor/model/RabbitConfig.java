package com.github.devgcoder.monitor.model;

public class RabbitConfig {

  private String rabbitHost;

  private Integer rabbitPort;

  private String rabbitUsername;

  private String rabbitPassword;

  private String rabbitVirtualHost;

  private Integer rabbitThreadPoolSize;

  private RabbitMonitor rabbitMonitorMessage;

  public String getRabbitHost() {
    return rabbitHost;
  }

  public void setRabbitHost(String rabbitHost) {
    this.rabbitHost = rabbitHost;
  }

  public Integer getRabbitPort() {
    return rabbitPort;
  }

  public void setRabbitPort(Integer rabbitPort) {
    this.rabbitPort = rabbitPort;
  }

  public String getRabbitUsername() {
    return rabbitUsername;
  }

  public void setRabbitUsername(String rabbitUsername) {
    this.rabbitUsername = rabbitUsername;
  }

  public String getRabbitPassword() {
    return rabbitPassword;
  }

  public void setRabbitPassword(String rabbitPassword) {
    this.rabbitPassword = rabbitPassword;
  }

  public String getRabbitVirtualHost() {
    return rabbitVirtualHost;
  }

  public void setRabbitVirtualHost(String rabbitVirtualHost) {
    this.rabbitVirtualHost = rabbitVirtualHost;
  }

  public Integer getRabbitThreadPoolSize() {
    return rabbitThreadPoolSize;
  }

  public void setRabbitThreadPoolSize(Integer rabbitThreadPoolSize) {
    this.rabbitThreadPoolSize = rabbitThreadPoolSize;
  }

  public RabbitMonitor getRabbitMonitorMessage() {
    return rabbitMonitorMessage;
  }

  public void setRabbitMonitorMessage(RabbitMonitor rabbitMonitorMessage) {
    this.rabbitMonitorMessage = rabbitMonitorMessage;
  }
}
