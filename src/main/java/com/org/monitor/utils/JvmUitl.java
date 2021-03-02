package com.org.monitor.utils;

import com.org.monitor.model.MomeryModel;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class JvmUitl {

  private static final String NEWLINE = "\n";

  public static String getMemoryInfo() {
    StringBuffer stringBuffer = new StringBuffer();
    MomeryModel momeryModel = getMomeryModel();
    stringBuffer.append("JVM内存已用的空间为：" + momeryModel.getVmUse() + " MB").append(NEWLINE);
    stringBuffer.append("JVM内存的空闲空间为：" + momeryModel.getVmFree() + " MB").append(NEWLINE);
    stringBuffer.append("JVM总内存空间为：" + momeryModel.getVmTotal() + " MB").append(NEWLINE);
    stringBuffer.append("JVM最大内存空间为：" + momeryModel.getVmMax() + " MB").append(NEWLINE);
    stringBuffer.append("操作系统的版本：" + momeryModel.getOs());
    stringBuffer.append("操作系统物理内存已用的空间为：" + momeryModel.getPhysicalUse() + " MB").append(NEWLINE);
    stringBuffer.append("操作系统物理内存的空闲空间为：" + momeryModel.getPhysicalFree() + " MB").append(NEWLINE);
    stringBuffer.append("操作系统总物理内存：" + momeryModel.getPhysicalTotal() + " MB").append(NEWLINE);
    stringBuffer.append("线程总数：" + momeryModel.getTotalThread());
    return stringBuffer.toString();
  }

  public static MomeryModel getMomeryModel() {
    MomeryModel momeryModel = new MomeryModel();
    int byteToMb = 1024 * 1024;
    Runtime rt = Runtime.getRuntime();
    long vmTotal = rt.totalMemory() / byteToMb; // 现在已经从操作系统那里挖过来的内存大小
    long vmFree = rt.freeMemory() / byteToMb;
    long vmMax = rt.maxMemory() / byteToMb; // 能够从操作系统那里挖到的最大的内存
    long vmUse = vmTotal - vmFree;
    momeryModel.setVmUse("JVM内存已用的空间为：" + vmUse + " MB");
    momeryModel.setVmFree("JVM内存的空闲空间为：" + vmFree + " MB");
    momeryModel.setVmTotal("JVM总内存空间为：" + vmTotal + " MB");
    momeryModel.setVmMax("JVM最大内存空间为：" + vmMax + " MB");
    // 操作系统级内存情况查询
    OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    String os = System.getProperty("os.name");
    long physicalFree = osmxb.getFreePhysicalMemorySize() / byteToMb;
    long physicalTotal = osmxb.getTotalPhysicalMemorySize() / byteToMb;
    long physicalUse = physicalTotal - physicalFree;
    momeryModel.setOs("操作系统的版本：" + os);
    momeryModel.setPhysicalFree("操作系统物理内存的空闲空间为：" + physicalFree + " MB");
    momeryModel.setPhysicalUse("操作系统物理内存已用的空间为：" + physicalUse + " MB");
    momeryModel.setPhysicalTotal("操作系统总物理内存：" + physicalTotal + " MB");
    // 获得线程总数
    ThreadGroup parentThread;
    int totalThread = 0;
    for (parentThread = Thread.currentThread().getThreadGroup(); parentThread
        .getParent() != null; parentThread = parentThread.getParent()) {
      totalThread = parentThread.activeCount();
    }
    momeryModel.setTotalThread("线程总数：" + totalThread);
    return momeryModel;
  }

 /* public static void printJvm() {
    // 虚拟机级内存情况查询
    long vmFree = 0;
    long vmUse = 0;
    long vmTotal = 0;
    long vmMax = 0;
    int byteToMb = 1024 * 1024;
    Runtime rt = Runtime.getRuntime();
    vmTotal = rt.totalMemory() / byteToMb; // 现在已经从操作系统那里挖过来的内存大小
    vmFree = rt.freeMemory() / byteToMb;
    vmMax = rt.maxMemory() / byteToMb; // 能够从操作系统那里挖到的最大的内存
    vmUse = vmTotal - vmFree;
    System.out.println("JVM内存已用的空间为：" + vmUse + " MB");
    System.out.println("JVM内存的空闲空间为：" + vmFree + " MB");
    System.out.println("JVM总内存空间为：" + vmTotal + " MB");
    System.out.println("JVM最大内存空间为：" + vmMax + " MB");

    System.out.println("======================================");
    // 操作系统级内存情况查询
    OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    String os = System.getProperty("os.name");
    long physicalFree = osmxb.getFreePhysicalMemorySize() / byteToMb;
    long physicalTotal = osmxb.getTotalPhysicalMemorySize() / byteToMb;
    long physicalUse = physicalTotal - physicalFree;
    System.out.println("操作系统的版本：" + os);
    System.out.println("操作系统物理内存已用的空间为：" + physicalFree + " MB");
    System.out.println("操作系统物理内存的空闲空间为：" + physicalUse + " MB");
    System.out.println("操作系统总物理内存：" + physicalTotal + " MB");
    // 获得线程总数
    ThreadGroup parentThread;
    int totalThread = 0;
    for (parentThread = Thread.currentThread().getThreadGroup(); parentThread
        .getParent() != null; parentThread = parentThread.getParent()) {
      totalThread = parentThread.activeCount();
    }
    System.out.println("获得线程总数:" + totalThread);
  }

  private static String getMemoryMb(long memory) {
    return (memory / 1024 / 1024) + "MB";
  }*/

  public static void main(String args[]) {
    System.out.println(getMemoryInfo());
   /* printJvm();
    final MonitorMemoryInfo monitorMemory = new MonitorMemoryInfo();
    final long usedMemory = monitorMemory.getUsedMemory();
    final long maxMemory = monitorMemory.getMaxMemory();
    final long usedPermGen = monitorMemory.getUsedPermGen();
    final long maxPermGen = monitorMemory.getMaxPermGen();
    final long usedNonHeapMemory = monitorMemory.getUsedNonHeapMemory();
    final long usedBufferedMemory = monitorMemory.getUsedBufferedMemory();
    final int loadedClassesCount = monitorMemory.getLoadedClassesCount();
    final long garbageCollectionTimeMillis = monitorMemory.getGarbageCollectionTimeMillis();
    final long usedPhysicalMemorySize = monitorMemory.getUsedPhysicalMemorySize();
    final long usedSwapSpaceSize = monitorMemory.getUsedSwapSpaceSize();
    final String memoryDetails = monitorMemory.getMemoryDetails();
    System.out.println("usedMemory:" + getMemoryMb(usedMemory));
    System.out.println("maxMemory:" + getMemoryMb(maxMemory));
    System.out.println("usedPermGen:" + getMemoryMb(usedPermGen));
    System.out.println("maxPermGen:" + getMemoryMb(maxPermGen));
    System.out.println("usedNonHeapMemory:" + getMemoryMb(usedNonHeapMemory));
    System.out.println("usedBufferedMemory:" + getMemoryMb(usedBufferedMemory));
    System.out.println("loadedClassesCount:" + getMemoryMb(loadedClassesCount));
    System.out.println("garbageCollectionTimeMillis:" + getMemoryMb(garbageCollectionTimeMillis));
    System.out.println("usedPhysicalMemorySize:" + getMemoryMb(usedPhysicalMemorySize));
    System.out.println("usedSwapSpaceSize:" + getMemoryMb(usedSwapSpaceSize));
    System.out.println("memoryDetails:" + memoryDetails);*/
  }
}
