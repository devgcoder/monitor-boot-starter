package com.org.monitor.model;

public class MonitorMemory extends MonitorModelType {

  private String localIp;
  private String localPort;
  private long vmFree;
  private long vmUse;
  private long vmTotal;
  private long vmMax;
  private long physicalFree;
  private long physicalTotal;
  private long physicalUse;
  private long totalThread;
  private String monitorTime;

  public String getLocalIp() {
    return localIp;
  }

  public void setLocalIp(String localIp) {
    this.localIp = localIp;
  }

  public String getLocalPort() {
    return localPort;
  }

  public void setLocalPort(String localPort) {
    this.localPort = localPort;
  }

  public long getVmFree() {
    return vmFree;
  }

  public void setVmFree(long vmFree) {
    this.vmFree = vmFree;
  }

  public long getVmUse() {
    return vmUse;
  }

  public void setVmUse(long vmUse) {
    this.vmUse = vmUse;
  }

  public long getVmTotal() {
    return vmTotal;
  }

  public void setVmTotal(long vmTotal) {
    this.vmTotal = vmTotal;
  }

  public long getVmMax() {
    return vmMax;
  }

  public void setVmMax(long vmMax) {
    this.vmMax = vmMax;
  }

  public long getPhysicalFree() {
    return physicalFree;
  }

  public void setPhysicalFree(long physicalFree) {
    this.physicalFree = physicalFree;
  }

  public long getPhysicalTotal() {
    return physicalTotal;
  }

  public void setPhysicalTotal(long physicalTotal) {
    this.physicalTotal = physicalTotal;
  }

  public long getPhysicalUse() {
    return physicalUse;
  }

  public void setPhysicalUse(long physicalUse) {
    this.physicalUse = physicalUse;
  }

  public long getTotalThread() {
    return totalThread;
  }

  public void setTotalThread(long totalThread) {
    this.totalThread = totalThread;
  }

  public String getMonitorTime() {
    return monitorTime;
  }

  public void setMonitorTime(String monitorTime) {
    this.monitorTime = monitorTime;
  }
}
