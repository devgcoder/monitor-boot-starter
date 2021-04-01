package com.github.devgcoder.monitor.schedule;

import com.github.devgcoder.monitor.model.ElasticSearchConfig;
import com.github.devgcoder.monitor.model.MonitorConfig;
import com.github.devgcoder.monitor.utils.MonitorMomeryUtil;
import com.github.devgcoder.monitor.utils.MonitorUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author devg
 * @Date 2020/2/13 11:33
 */
public class MonitorSchedule implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(MonitorSchedule.class);

  private static final Integer deleteDayNum = 10;

  private static final Integer deleteFromDayNum = 10;

  private MonitorConfig monitorConfig;

  public MonitorSchedule(MonitorConfig monitorConfig) {
    this.monitorConfig = monitorConfig;
  }

	@Override
	public void run() {
//		for (; ; ) {
//			deleteMonitorIndex();
//			try {
//				Thread.sleep(30 * 1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
    deleteMonitorIndex();
	}

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
            if (!isAcknowledged) {
              logger.info("delete index{} error", Arrays.toString(index));
            }
          }

          @Override
          public void onFailure(Exception e) {
            e.printStackTrace();
          }
        });
  }
}
