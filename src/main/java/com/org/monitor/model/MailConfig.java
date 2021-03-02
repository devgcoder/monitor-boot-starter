package com.org.monitor.model;

public class MailConfig {

  private String mailHost;
  private String mailPort;
  private String mailFrom;
  private String mailRecipient;
  private String mailUsername;
  private String mailPassword;
  private String mailSmtp;
  private Long maxCostTime;
  private Long mailLimitTime;
  private Boolean mailJvm;
  private Double mailJvmRate;
  private Boolean mailPhysical;
  private Double mailPhysicalRate;
  private Boolean mailMessageError;
  private Boolean mailMessageOvertime;

  public String getMailHost() {
    return mailHost;
  }

  public void setMailHost(String mailHost) {
    this.mailHost = mailHost;
  }

  public String getMailPort() {
    return mailPort;
  }

  public void setMailPort(String mailPort) {
    this.mailPort = mailPort;
  }

  public String getMailFrom() {
    return mailFrom;
  }

  public void setMailFrom(String mailFrom) {
    this.mailFrom = mailFrom;
  }

  public String getMailRecipient() {
    return mailRecipient;
  }

  public void setMailRecipient(String mailRecipient) {
    this.mailRecipient = mailRecipient;
  }

  public String getMailUsername() {
    return mailUsername;
  }

  public void setMailUsername(String mailUsername) {
    this.mailUsername = mailUsername;
  }

  public String getMailPassword() {
    return mailPassword;
  }

  public void setMailPassword(String mailPassword) {
    this.mailPassword = mailPassword;
  }

  public String getMailSmtp() {
    return mailSmtp;
  }

  public void setMailSmtp(String mailSmtp) {
    this.mailSmtp = mailSmtp;
  }

  public Long getMaxCostTime() {
    return maxCostTime;
  }

  public void setMaxCostTime(Long maxCostTime) {
    this.maxCostTime = maxCostTime;
  }

  public Long getMailLimitTime() {
    return mailLimitTime;
  }

  public void setMailLimitTime(Long mailLimitTime) {
    this.mailLimitTime = mailLimitTime;
  }

  public Boolean getMailJvm() {
    return mailJvm;
  }

  public void setMailJvm(Boolean mailJvm) {
    this.mailJvm = mailJvm;
  }

  public Double getMailJvmRate() {
    return mailJvmRate;
  }

  public void setMailJvmRate(Double mailJvmRate) {
    this.mailJvmRate = mailJvmRate;
  }

  public Boolean getMailPhysical() {
    return mailPhysical;
  }

  public void setMailPhysical(Boolean mailPhysical) {
    this.mailPhysical = mailPhysical;
  }

  public Double getMailPhysicalRate() {
    return mailPhysicalRate;
  }

  public void setMailPhysicalRate(Double mailPhysicalRate) {
    this.mailPhysicalRate = mailPhysicalRate;
  }

  public Boolean getMailMessageError() {
    return mailMessageError;
  }

  public void setMailMessageError(Boolean mailMessageError) {
    this.mailMessageError = mailMessageError;
  }

  public Boolean getMailMessageOvertime() {
    return mailMessageOvertime;
  }

  public void setMailMessageOvertime(Boolean mailMessageOvertime) {
    this.mailMessageOvertime = mailMessageOvertime;
  }
}
