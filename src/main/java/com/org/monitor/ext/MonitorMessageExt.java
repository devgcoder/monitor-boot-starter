package com.org.monitor.ext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.org.monitor.utils.DingdingUtil;
import com.org.monitor.utils.MonitorMomeryUtil;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorMessageExt {

  private static final Logger logger = LoggerFactory.getLogger(MonitorMessageExt.class);

  private BaseMessageExt data;

  private DingMessageExt dingMessageExt;

  public MonitorMessageExt(BaseMessageExt data, DingMessageExt dingMessageExt) {
    this.data = data;
    this.dingMessageExt = dingMessageExt;
  }

  public void sendMonitorMessage() {
    dingMessageExt.setMsgId(data.getBusinessId());
    invokeElasticSearch();
    invokeDingding();
  }

  private void invokeElasticSearch() {
    String indexName = data.getIndexName();
    if (null == indexName || indexName.equals("")) {
      return;
    }
    RestHighLevelClient restHighLevelClient = MonitorMomeryUtil.restHighLevelClientMap.get(MonitorMomeryUtil.restHighLevelClient);
    if (null == restHighLevelClient) {
      return;
    }
    try {
      IndexRequest indexRequest = new IndexRequest();
      indexRequest.index(indexName);
      indexRequest.id(dingMessageExt.getMsgId());
      String dataJson = JSON.toJSONString(this.data, SerializerFeature.WriteMapNullValue);
      indexRequest.source(dataJson, XContentType.JSON);
      IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
      Result result = indexResponse.getResult();
    } catch (Exception ex) {
      logger.error("send devg-monitor message error", ex);
    }
  }

  private void invokeDingding() {
    String msgUrl = dingMessageExt.getMsgUrl();
    if (null != msgUrl && !msgUrl.equals("")) {
      String msgId = dingMessageExt.getMsgId();
      msgUrl = msgUrl + "?businessId=" + msgId;
      String msgContent = dingMessageExt.getMsgContent();
      if (msgContent != null && msgContent.endsWith(",")) {
        msgContent = msgContent + "查看详情:" + msgUrl;
      } else {
        msgContent = msgContent + ",查看详情:" + msgUrl;
      }
      dingMessageExt.setMsgContent(msgContent);
    }
    DingdingUtil.sendMsg(this.dingMessageExt);
  }
}
