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
}
