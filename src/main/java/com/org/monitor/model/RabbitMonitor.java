package com.org.monitor.model;

public class RabbitMonitor {
  private String exchangeName;
  private String queueName;
  private String routeKey;

  public String getExchangeName() {
    return exchangeName;
  }

  public void setExchangeName(String exchangeName) {
    this.exchangeName = exchangeName;
  }

  public String getQueueName() {
    return queueName;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public String getRouteKey() {
    return routeKey;
  }

  public void setRouteKey(String routeKey) {
    this.routeKey = routeKey;
  }
}
