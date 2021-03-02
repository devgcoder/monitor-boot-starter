package com.org.monitor.ext;

import com.org.monitor.utils.MonitorUtil;
import java.util.concurrent.atomic.AtomicLong;

public class BaseMessageExt {

  private String businessId;

  private String indexName;

  public BaseMessageExt() {
    this.businessId = getBusId();
  }

  public BaseMessageExt(String indexName) {
    this.businessId = getBusId();
    this.indexName = indexName;
  }

  public String getBusinessId() {
    return businessId;
  }

  public void setBusinessId(String businessId) {
    this.businessId = businessId;
  }

  public String getIndexName() {
    return indexName;
  }

  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  public static AtomicLong getMsgIdNum() {
    return msgIdNum;
  }

  private static final AtomicLong msgIdNum = new AtomicLong(0);

  private static String getBusId() {
    String msgNumId = MonitorUtil.getNowDate(MonitorUtil.FORMAT_PATTERN1) + msgIdNum.incrementAndGet();
    return msgNumId;
  }
}
