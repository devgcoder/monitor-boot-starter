package com.org.monitor.utils;

import com.rabbitmq.client.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.elasticsearch.client.RestHighLevelClient;

public class MonitorMomeryUtil {

  public static final Map<String, Channel> connectionChannelMap = new ConcurrentHashMap<>();

  public static final Map<String, RestHighLevelClient> restHighLevelClientMap = new ConcurrentHashMap<>();

  public static final Map<String, String> indexExistsMap = new ConcurrentHashMap<>();

  public static final String restHighLevelClient = "restHighLevelClient";

  public static final String rabbitmq = "rabbitmq";

  public static final String kafka = "kafka";
}
