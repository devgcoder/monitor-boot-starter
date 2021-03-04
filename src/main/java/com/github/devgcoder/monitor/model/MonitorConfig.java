package com.github.devgcoder.monitor.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devg.monitor")
public class MonitorConfig {

  // 是否开启controller
  private Boolean enableMonitorController;

  // 是否开启mapper监控
  private Boolean enableMonitorMapper;

  private String[] monitorNonClassMethod;

  private String requestParams;

  private String monitorName;

  private String queueType;

  private String modelName;

  private String evnName;

  private String authorizedUser;

  private String localPort;

  private Long messageMaxCostTime;

  private Long messageLimitTime;

  private Boolean messageError;

  private Boolean messageOvertime;

  private RabbitConfig rabbitConfig;

  private ElasticSearchConfig elasticSearchConfig;

  private DingdingConfig dingdingConfig;

  public String getMonitorName() {
    return monitorName;
  }

  public void setMonitorName(String monitorName) {
    this.monitorName = monitorName;
  }

  public String getRequestParams() {
    return requestParams;
  }

  public void setRequestParams(String requestParams) {
    this.requestParams = requestParams;
  }

  public Boolean getEnableMonitorController() {
    return enableMonitorController;
  }

  public void setEnableMonitorController(Boolean enableMonitorController) {
    this.enableMonitorController = enableMonitorController;
  }

  public Boolean getEnableMonitorMapper() {
    return enableMonitorMapper;
  }

  public void setEnableMonitorMapper(Boolean enableMonitorMapper) {
    this.enableMonitorMapper = enableMonitorMapper;
  }

  public String[] getMonitorNonClassMethod() {
    return monitorNonClassMethod;
  }

  public void setMonitorNonClassMethod(String[] monitorNonClassMethod) {
    this.monitorNonClassMethod = monitorNonClassMethod;
  }

  public String getQueueType() {
    return queueType;
  }

  public void setQueueType(String queueType) {
    this.queueType = queueType;
  }

  public String getEvnName() {
    return evnName;
  }

  public void setEvnName(String evnName) {
    this.evnName = evnName;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getAuthorizedUser() {
    return authorizedUser;
  }

  public void setAuthorizedUser(String authorizedUser) {
    this.authorizedUser = authorizedUser;
  }

  public String getLocalPort() {
    return localPort;
  }

  public void setLocalPort(String localPort) {
    this.localPort = localPort;
  }

  public RabbitConfig getRabbitConfig() {
    return rabbitConfig;
  }

  public void setRabbitConfig(RabbitConfig rabbitConfig) {
    this.rabbitConfig = rabbitConfig;
  }

  public ElasticSearchConfig getElasticSearchConfig() {
    return elasticSearchConfig;
  }

  public void setElasticSearchConfig(ElasticSearchConfig elasticSearchConfig) {
    this.elasticSearchConfig = elasticSearchConfig;
  }

  public DingdingConfig getDingdingConfig() {
    return dingdingConfig;
  }

  public void setDingdingConfig(DingdingConfig dingdingConfig) {
    this.dingdingConfig = dingdingConfig;
  }

  public Long getMessageMaxCostTime() {
    return messageMaxCostTime;
  }

  public void setMessageMaxCostTime(Long messageMaxCostTime) {
    this.messageMaxCostTime = messageMaxCostTime;
  }

  public Long getMessageLimitTime() {
    return messageLimitTime;
  }

  public void setMessageLimitTime(Long messageLimitTime) {
    this.messageLimitTime = messageLimitTime;
  }

  public Boolean getMessageError() {
    return messageError;
  }

  public void setMessageError(Boolean messageError) {
    this.messageError = messageError;
  }

  public Boolean getMessageOvertime() {
    return messageOvertime;
  }

  public void setMessageOvertime(Boolean messageOvertime) {
    this.messageOvertime = messageOvertime;
  }
}
