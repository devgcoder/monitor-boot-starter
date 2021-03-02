package com.org.monitor.utils;

import com.alibaba.fastjson.JSON;
import com.org.monitor.ext.DingMessageExt;
import com.org.monitor.model.DingdingConfig;
import com.org.monitor.model.JsonModel;
import com.org.monitor.model.MonitorDingding;
import com.org.monitor.utils.CommonEnum.WarnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class DingdingUtil {

  private static final Logger logger = LoggerFactory.getLogger(DingdingUtil.class);

  public static void sendMsg(String msgContent) {
    sendMsg(msgContent, null);
  }

  public static void sendMsg(String msgContent, Boolean atAll) {
    String message = WarnType.BUSSINESS.getName() + msgContent;
    sendMsg(MonitorUtil.TEXT, message, atAll);
  }

  public static void sendMsg(String msgType, String msgContent, Boolean atAll) {
    try {
      if (MonitorUtil.isNullOrEmpty(msgType) || MonitorUtil.isNullOrEmpty(msgContent)) {
        logger.error("msgType and msgContent can not be null");
        return;
      }

      if (MonitorUtil.monitorConfig == null) {
        logger.error("monitorConfig can not be null");
        return;
      }
      DingdingConfig dingdingConfig = MonitorUtil.monitorConfig.getDingdingConfig();
      if (dingdingConfig == null) {
        logger.error("dingdingConfig can not be null");
        return;
      }
      String serverUrl = dingdingConfig.getServerUrl();
      if (MonitorUtil.isNullOrEmpty(serverUrl)) {
        logger.error("dingdingConfig serverUrl can not be null");
        return;
      }
      if (MonitorUtil.dingRestTempalte == null) {
        logger.error("dingRestTempalte can not be null");
        return;
      }
      String isAtALl = dingdingConfig.getIsAtAll() == null ? "false" : dingdingConfig.getIsAtAll();
      if (null != atAll && atAll) {
        isAtALl = "true";
      } else if (null != atAll && !atAll) {
        isAtALl = "false";
      }
      MonitorDingding monitorDingding = new MonitorDingding(msgType, msgContent, isAtALl);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<MonitorDingding> entity = new HttpEntity<MonitorDingding>(monitorDingding, headers);
      ResponseEntity<JsonModel> responseEntity = MonitorUtil.dingRestTempalte.postForEntity(serverUrl, entity, JsonModel.class);
      JsonModel<String> jsonModel = responseEntity.getBody();
      logger.info("send dingding message:" + JSON.toJSONString(jsonModel));
    } catch (Exception ex) {
      logger.error("DingdingUtil sendMsg error", ex);
    }
  }

  public static void sendMsg(DingMessageExt dingMessageExt) {
    try {
      if (null == dingMessageExt || MonitorUtil.isNullOrEmpty(dingMessageExt.getMsgType()) || MonitorUtil
          .isNullOrEmpty(dingMessageExt.getMsgContent())) {
        logger.error("msgType and msgContent can not be null");
        return;
      }
      if (MonitorUtil.monitorConfig == null) {
        logger.error("monitorConfig can not be null");
        return;
      }
      DingdingConfig dingdingConfig = MonitorUtil.monitorConfig.getDingdingConfig();
      if (dingdingConfig == null) {
        logger.error("dingdingConfig can not be null");
        return;
      }
      String serverUrl = dingdingConfig.getServerUrl();
      if (MonitorUtil.isNullOrEmpty(serverUrl)) {
        logger.error("dingdingConfig serverUrl can not be null");
        return;
      }
      if (MonitorUtil.dingRestTempalte == null) {
        logger.error("dingRestTempalte can not be null");
        return;
      }
      String isAtALl = dingdingConfig.getIsAtAll() == null ? "false" : dingdingConfig.getIsAtAll();
      if (null != dingMessageExt.getIsAtAll()) {
        isAtALl = dingMessageExt.getIsAtAll();
      }
      dingMessageExt.setIsAtAll(isAtALl);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<DingMessageExt> entity = new HttpEntity<DingMessageExt>(dingMessageExt, headers);
      ResponseEntity<JsonModel> responseEntity = MonitorUtil.dingRestTempalte.postForEntity(serverUrl, entity, JsonModel.class);
      JsonModel<String> jsonModel = responseEntity.getBody();
      logger.info("send dingding message:" + JSON.toJSONString(jsonModel));
    } catch (Exception ex) {
      logger.error("DingdingUtil sendMsg error", ex);
    }

  }

}
