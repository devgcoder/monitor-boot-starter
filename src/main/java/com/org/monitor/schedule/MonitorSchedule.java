package com.org.monitor.schedule;

import com.alibaba.fastjson.JSON;
import com.org.monitor.model.CommonParams.MonitorModelType;
import com.org.monitor.model.ElasticSearchConfig;
import com.org.monitor.model.MonitorConfig;
import com.org.monitor.model.MonitorMemory;
import com.org.monitor.utils.CommonEnum.WarnType;
import com.org.monitor.utils.DingdingUtil;
import com.org.monitor.utils.IpAddressUtil;
import com.org.monitor.utils.MonitorMomeryUtil;
import com.org.monitor.utils.MonitorUtil;
import com.org.monitor.utils.SockerUtil;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author devg
 * @Date 2020/2/13 11:33
 */
public class MonitorSchedule {

  private static final Logger logger = LoggerFactory.getLogger(MonitorSchedule.class);

  private static final Integer deleteDayNum = 10;

  private static final Integer deleteFromDayNum = 10;

  private MonitorConfig monitorConfig;

  public MonitorSchedule(MonitorConfig monitorConfig) {
    this.monitorConfig = monitorConfig;
  }

  @Scheduled(cron = "0/30 * * * * ?")
  public void saveMonitorMemory() {
    try {
      if (null == monitorConfig || null == monitorConfig.getVmMemory() || !monitorConfig.getVmMemory()) {
        return;
      }
      int byteToMb = 1024 * 1024;
      Runtime rt = Runtime.getRuntime();
      long vmTotal = rt.totalMemory() / byteToMb; // 现在已经从操作系统那里挖过来的内存大小
      long vmFree = rt.freeMemory() / byteToMb;
      long vmMax = rt.maxMemory() / byteToMb; // 能够从操作系统那里挖到的最大的内存
      long vmUse = vmTotal - vmFree;
      OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      long physicalFree = osmxb.getFreePhysicalMemorySize() / byteToMb;
      long physicalTotal = osmxb.getTotalPhysicalMemorySize() / byteToMb;
      long physicalUse = physicalTotal - physicalFree;
      // 获得线程总数
      ThreadGroup parentThread;
      int totalThread = 0;
      for (parentThread = Thread.currentThread().getThreadGroup(); parentThread
          .getParent() != null; parentThread = parentThread.getParent()) {
        totalThread = parentThread.activeCount();
      }
      String localIp = IpAddressUtil.getLocalIp();
      String localPort = monitorConfig == null ? MonitorUtil.defaultPort : monitorConfig.getLocalPort();
      String monitorTime = MonitorUtil.getStartTime();
      MonitorMemory monitorMemory = getMonitorMemory(vmTotal, vmFree, vmMax, vmUse, physicalFree, physicalTotal, physicalUse, totalThread, localIp,
          localPort, monitorTime);
      String sendMqMsg = JSON.toJSONString(monitorMemory);
      MonitorUtil.sendMessage(monitorConfig, monitorConfig.getRabbitConfig().getRabbitMonitorMemory(), sendMqMsg);
    } catch (Exception ex) {
      logger.error("saveMonitorMemory error", ex);
    }
  }

  @Scheduled(cron = "0/30 * * * * ?")
  public void serverAccessMonitor() {
    logger.info("check server can be accessed start ...");
    if (null == monitorConfig || monitorConfig.getServerAccessConfig() == null
        || monitorConfig.getServerAccessConfig().size() <= 0) {
      logger.info("no devg-monitor server need check");
      return;
    }
    String evnName = monitorConfig.getEvnName();
    Map<String, Future<Boolean>> futureTaskMap = new HashMap<>();
    List<String> serverAccesslist = monitorConfig.getServerAccessConfig();
    ExecutorService executorService = new ThreadPoolExecutor(10, 10, 0, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(50), new ThreadPoolExecutor.DiscardPolicy());
    for (String server : serverAccesslist) {
      FutureTask<Boolean> futureTask = new FutureTask<>(new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          String[] serverConfig = server.split(":");
          if (serverConfig.length != 2) {
            logger.error("devg-monitor server config error!");
            return true;
          } else {
            String host = serverConfig[0];
            Integer port = Integer.valueOf(serverConfig[1]);
            SockerUtil sockerUtil = new SockerUtil(host, port);
            boolean bool = sockerUtil.checkHostLogin();
            return bool;
          }
        }
      });
      futureTaskMap.put(server, futureTask);
      executorService.execute(futureTask);
    }
    List<String> errorServerList = new ArrayList<>();
    for (String server : serverAccesslist) {
      try {
        Future<Boolean> future = futureTaskMap.get(server);
        Boolean result = future.get();
        logger.info(server + " get futureTaks result:" + result);
        if (null == result || !result) {
          errorServerList.add(server);
        }
      } catch (Exception e) {
        logger.error(server + "get futureTaks error:", e);
      }
    }
    executorService.shutdown();
    if (null != errorServerList && errorServerList.size() > 0) {
      StringBuffer bufferServer = new StringBuffer("服务地址:");
      int i = 1;
      for (String errorServer : errorServerList) {
        bufferServer.append(errorServer);
        if (i < errorServerList.size()) {
          bufferServer.append(",");
        }
        i++;
      }
      bufferServer.append(" 无法访问,环境:" + evnName);
      String errorServerString = bufferServer.toString();
      errorServerString = WarnType.SERVER.getName() + errorServerString;
      DingdingUtil.sendMsg(MonitorUtil.TEXT, errorServerString, null);
    }
    logger.info("check server can be accessed end ...");
  }

  @Scheduled(cron = "0 30 01 * * ?")
  public void deleteMonitorIndex() {
    try {
      RestHighLevelClient restHighLevelClient = MonitorMomeryUtil.restHighLevelClientMap.get(MonitorMomeryUtil.restHighLevelClient);
      if (null == restHighLevelClient) {
        return;
      }
      Integer deleteFromDay = deleteFromDayNum;
      ElasticSearchConfig elasticSearchConfig = monitorConfig.getElasticSearchConfig();
      if (null != elasticSearchConfig && null != elasticSearchConfig.getDeleteFromDayNum()) {
        deleteFromDay = elasticSearchConfig.getDeleteFromDayNum();
      }
      Integer deleteDay = deleteDayNum;
      if (null != elasticSearchConfig && null != elasticSearchConfig.getDeleteDayNum()) {
        deleteDay = elasticSearchConfig.getDeleteDayNum();
      }
      deleteIndex(restHighLevelClient, getDeleteIndex(MonitorUtil.indexMessagePrefix, deleteFromDay, deleteDay));
      deleteIndex(restHighLevelClient, getDeleteIndex(MonitorUtil.indexMemoryPrefix, deleteFromDay, deleteDay));
      deleteIndex(restHighLevelClient, getDeleteIndex(MonitorUtil.indexAppPrefix, deleteFromDay, deleteDay));
      // 创建明天的索引
      String theDay = MonitorUtil.localDateTimeFormat(LocalDateTime.now().plusDays(1), MonitorUtil.FORMAT_PATTERN2);
      MonitorUtil.checkAndCreateIndex(MonitorUtil.getMessageIndexName(theDay), restHighLevelClient);
      MonitorUtil.checkAndCreateIndex(MonitorUtil.getMemoryIndexName(theDay), restHighLevelClient);
      MonitorUtil.checkAndCreateIndex(MonitorUtil.getAppIndexName(theDay), restHighLevelClient);
    } catch (Exception ex) {
      logger.error("deleteMonitorIndex error", ex);
    }
  }

  /**
   * 获取删除的索引
   *
   * @param prefixIndex 索引名称前缀
   * @param deleteDays 删除几天前
   * @param deleteDayNum 删除多少天
   * @return
   */
  private String[] getDeleteIndex(String prefixIndex, int deleteDays, int deleteDayNum) {
    String evnName = MonitorUtil.monitorConfig == null ? "" : MonitorUtil.monitorConfig.getEvnName();
    String[] deleteIndexs = new String[deleteDayNum];
    for (int i = 0; i < deleteDayNum; i++) {
      String indexDay = MonitorUtil.localDateFormat(LocalDate.now().minusDays(deleteDays + i), "yyyyMMdd");
      String indexName = prefixIndex + evnName + indexDay;
      deleteIndexs[i] = indexName;
    }
    return deleteIndexs;
  }

  /**
   * 删除索引
   *
   * @param restHighLevelClient
   * @param index
   */
  private void deleteIndex(RestHighLevelClient restHighLevelClient, String... index) {
    DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
    deleteIndexRequest.indicesOptions(IndicesOptions.fromOptions(true, true, true, true));
    restHighLevelClient.indices().deleteAsync(deleteIndexRequest,
        RequestOptions.DEFAULT, new ActionListener<AcknowledgedResponse>() {
          @Override
          public void onResponse(AcknowledgedResponse acknowledgedResponse) {
            Boolean isAcknowledged = acknowledgedResponse.isAcknowledged();
            System.out.println(isAcknowledged);
          }

          @Override
          public void onFailure(Exception e) {
            e.printStackTrace();
          }
        });
  }

  /**
   * @param vmTotal JVM总内存空间
   * @param vmFree JVM内存的空闲空间
   * @param vmMax JVM最大内存空间
   * @param vmUse JVM内存已用的空间
   * @param physicalFree 操作系统物理内存的空闲空间
   * @param physicalTotal 操作系统总物理内
   * @param physicalUse 操作系统物理内存已用的空间
   * @param totalThread 线程总数
   * @param localIp 本机IP
   * @param localPort 当前tomcat端口
   * @return
   */
  private MonitorMemory getMonitorMemory(long vmTotal, long vmFree, long vmMax, long vmUse, long physicalFree, long physicalTotal,
      long physicalUse, int totalThread, String localIp, String localPort, String monitorTime) {
    MonitorMemory monitorMemory = new MonitorMemory();
    monitorMemory.setVmTotal(vmTotal);
    monitorMemory.setVmFree(vmFree);
    monitorMemory.setVmMax(vmMax);
    monitorMemory.setVmUse(vmUse);
    monitorMemory.setPhysicalFree(physicalFree);
    monitorMemory.setPhysicalTotal(physicalTotal);
    monitorMemory.setPhysicalUse(physicalUse);
    monitorMemory.setTotalThread(totalThread);
    monitorMemory.setLocalIp(localIp);
    monitorMemory.setLocalPort(localPort);
    monitorMemory.setMessageModelType(MonitorModelType.MEMORY.getKey());
    monitorMemory.setMonitorTime(monitorTime);
    return monitorMemory;
  }

}
