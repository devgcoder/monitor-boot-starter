package com.org.monitor.utils;

import com.org.monitor.model.MonitorConfig;
import com.org.monitor.model.RabbitConfig;
import com.org.monitor.model.RabbitMonitor;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class MonitorUtil {

  private final static Logger logger = LoggerFactory.getLogger(MonitorUtil.class);

  public final static String verticalSplit = "|";

  public final static String horizontalSplit = "-";

  public final static String FORMAT_PATTERN1 = "yyyyMMddHHmmss";

  public final static String FORMAT_PATTERN2 = "yyyyMMdd";

  public final static String FORMAT_PATTERN3 = "yyyy-MM-dd HH:mm:ss";

  public final static String FORMAT_PATTERN4 = "yyyy-MM-dd";

  public final static String indexMessagePrefix = "index_monitor_message_";

  public final static String indexMemoryPrefix = "index_monitor_memory_";

  public final static String indexAppPrefix = "index_monitor_app_";

  public final static String indExists = "1";  //exists

  public static final String EARLY_MIN = "00:00";

  public static final String LATE_MIN = "23:50";

  public static final String EARLY_SECOND = "00:00:00";

  public static final String LATE_SECOND = "23:59:59";

  public static final String PAGE = "page";

  public static final String LIMIT = "limit";

  public static final String EMPTY = " ";

  public static final String USERINFO = "userInfo";

  public static final String TEXT = "text";

  public static String authorizedUser = null;

  public static MonitorConfig monitorConfig = null;

  public static RestTemplate dingRestTempalte = null;

  public static final String defaultPort = "8080";

  public static final String monitorName = "智慧工匠科技有限公司监管平台";

  public static final String TOKEN = "token";

  public static final int maxAge = 30;  // token 超时时间

  public static final Pattern numberPattern = Pattern.compile("[0-9]*");

  public static AtomicBoolean lock = new AtomicBoolean(false);

  public static String localDateTimeFormat(LocalDateTime localDateTime, String pattern) {
    return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
  }

  public static String getStartTime() {
    return getNowDate(FORMAT_PATTERN3);
  }

  public static String getNowDate(String format) {
    return localDateTimeFormat(LocalDateTime.now(), format);
  }

  public static String getPreDate(String format) {
    return localDateTimeFormat(LocalDateTime.now().minusDays(1), format);
  }

  public static String getNextDate(String format) {
    return localDateTimeFormat(LocalDateTime.now().plusDays(1), format);
  }

  /**
   * 得到某个日期在这一天中时间最早的日期对象
   */
  public static String getEarlyInTheDay(LocalDate localDate) {
    return localDateFormat(localDate, FORMAT_PATTERN4) + " " + EARLY_SECOND;
  }

  /**
   * 得到某个日期在这一天中时间最晚的日期对象
   */
  public static String getLastInTheDay(LocalDate localDate) {
    return localDateFormat(localDate, FORMAT_PATTERN4) + " " + LATE_SECOND;
  }

  public static String getMessageIndexName() {
    String nowDay = localDateTimeFormat(LocalDateTime.now(), FORMAT_PATTERN2);
    return getMessageIndexName(nowDay);
  }

  public static String getMessageIndexName(String theDay) {
    String evnName = MonitorUtil.monitorConfig == null ? "" : MonitorUtil.monitorConfig.getEvnName();
    return indexMessagePrefix + evnName + theDay;
  }

  public static String getMemoryIndexName() {
    String nowDay = localDateTimeFormat(LocalDateTime.now(), FORMAT_PATTERN2);
    return getMemoryIndexName(nowDay);
  }

  public static String getMemoryIndexName(String theDay) {
    String evnName = MonitorUtil.monitorConfig == null ? "" : MonitorUtil.monitorConfig.getEvnName();
    return indexMemoryPrefix + evnName + theDay;
  }

  public static String getAppIndexName() {
    String nowDay = localDateTimeFormat(LocalDateTime.now(), FORMAT_PATTERN2);
    return getAppIndexName(nowDay);
  }

  public static String getAppIndexName(String theDay) {
    String evnName = MonitorUtil.monitorConfig == null ? "" : MonitorUtil.monitorConfig.getEvnName();
    return indexAppPrefix + evnName + theDay;
  }

  /**
   * 时间转化成 String 格式的时间
   *
   * @param dateFormat
   * @param millSec
   * @return
   */
  public static String transferLongToDate(String dateFormat, Long millSec) {
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    String millSecStr = millSec + "000";
    Date date = new Date(Long.valueOf(millSecStr));
    String resDate = sdf.format(date);
    return resDate;
  }

  /**
   * 判断是否为null或空字符串。如果不为null，在判断是否为空字符串之前会调用trim()。
   *
   * @param str
   * @return
   */
  public static boolean isNullOrEmpty(String str) {
    return str == null || str.trim().equals("");
  }

  /**
   * 判断是否为null或空字符串。如果不为null，在判断是否为空字符串之前会调用trim()。
   *
   * @param object
   * @return
   */
  public static boolean isNullOrEmpty(Object object) {

    if (object == null) {
      return true;
    }
    return isNullOrEmpty(object.toString());
  }

  public static String getMapString(Map<String, Object> map, String string) {
    return map == null ? null : map.get(string) == null ? null : String.valueOf(map.get(string));
  }

  public static Long getMapLong(Map<String, Object> map, String string) {
    String object = getMapString(map, string);
    return isNullOrEmpty(object) ? null : Long.parseLong(object);
  }

  public static boolean isNumeric(String str) {
    Matcher isNum = numberPattern.matcher(str);
    if (!isNum.matches()) {
      return false;
    }
    return true;
  }

  public static boolean equals(String str1, String str2) {
    return str1 == null ? str2 == null : str1.equals(str2);
  }

  public static void checkAndCreateIndex(String indexName, RestHighLevelClient restHighLevelClient) {
    boolean exists = false;
    try {
      String indexExists = MonitorMomeryUtil.indexExistsMap.get(indexName);
      if (null != indexExists && indexExists.equals(indExists)) {
        return;
      }
      GetIndexRequest request = new GetIndexRequest(indexName);
      exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    } catch (Exception e) {
      logger.error("unknow error:{}", e);
    }
    if (exists) {
      MonitorMomeryUtil.indexExistsMap.put(indexName, indExists);
      return;
    }
    // 如果不存在
    if (lock.compareAndSet(false, true)) {
      try {
        clearIndexMap();
        if (indexName.indexOf(indexMemoryPrefix) >= 0) {
          createMemoryIndex(indexName, restHighLevelClient);
        } else if (indexName.indexOf(indexMessagePrefix) >= 0) {
          createMessageIndex(indexName, restHighLevelClient);
        } else if (indexName.indexOf(indexAppPrefix) >= 0) {
          createAppIndex(indexName, restHighLevelClient);
        }
      } catch (Exception ex) {
        logger.error("create indexName error", ex);
      }
      lock.set(false);
    }
  }

  private static void createMessageIndex(String indexName, RestHighLevelClient restHighLevelClient) throws IOException {
    XContentBuilder builder = XContentFactory.jsonBuilder()
        .startObject()
        .field("properties")
        .startObject()
        .field("messageType").startObject().field("index", "true").field("type", "integer").endObject()
        .field("classMethod").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("requestUrl").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("requestIp").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("costTime").startObject().field("index", "true").field("type", "long").endObject()
        .field("startTime").startObject().field("index", "true").field("type", "date").field("format", "yyyy-MM-dd HH:mm:ss").endObject()
        .field("resultType").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("modelName").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("messageKey").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("requestParams").startObject().field("type", "object").endObject()
        .endObject()
        .endObject();
    logger.info("builderString:" + builder.toString());
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
    createIndexRequest.settings(Settings.builder()
        .put("index.number_of_shards", 1)
        .put("index.number_of_replicas", 0)
        .put("index.blocks.read_only_allow_delete", "false")
        .put("index.max_result_window", 1000000)  // 设置最大返回结果数, 1百万
    );
    createIndexRequest.mapping(builder);
    CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    boolean acknowledged = createIndexResponse.isAcknowledged();
    if (acknowledged) {
      MonitorMomeryUtil.indexExistsMap.put(indexName, indExists);
      logger.info("create indexName:" + indexName + " success");
    } else {
      logger.info("create indexName:" + indexName + " failture");
    }
  }


  private static void createMemoryIndex(String indexName, RestHighLevelClient restHighLevelClient) throws IOException {
    XContentBuilder builder = XContentFactory.jsonBuilder()
        .startObject()
        .field("properties")
        .startObject()
        .field("localIp").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("localPort").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("vmUse").startObject().field("index", "true").field("type", "long").endObject()
        .field("vmFree").startObject().field("index", "true").field("type", "long").endObject()
        .field("vmTotal").startObject().field("index", "true").field("type", "long").endObject()
        .field("vmMax").startObject().field("index", "true").field("type", "long").endObject()
        .field("physicalUse").startObject().field("index", "true").field("type", "long").endObject()
        .field("physicalFree").startObject().field("index", "true").field("type", "long").endObject()
        .field("physicalTotal").startObject().field("index", "true").field("type", "long").endObject()
        .field("totalThread").startObject().field("index", "true").field("type", "integer").endObject()
        .field("monitorTime").startObject().field("index", "true").field("type", "date").field("format", "yyyy-MM-dd HH:mm:ss").endObject()
        .endObject()
        .endObject();
    logger.info("builderString:" + builder.toString());
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
    createIndexRequest.settings(Settings.builder()
        .put("index.number_of_shards", 1)
        .put("index.number_of_replicas", 0)
        .put("index.blocks.read_only_allow_delete", "false")
        .put("index.max_result_window", 1000000)  // 设置最大返回结果数, 1百万
    );
    createIndexRequest.mapping(builder);
    CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    boolean acknowledged = createIndexResponse.isAcknowledged();
    if (acknowledged) {
      MonitorMomeryUtil.indexExistsMap.put(indexName, indExists);
      logger.info("create indexName:" + indexName + " success");
    } else {
      logger.info("create indexName:" + indexName + " failture");
    }
  }

  private static void createAppIndex(String indexName, RestHighLevelClient restHighLevelClient) throws IOException {
    XContentBuilder builder = XContentFactory.jsonBuilder()
        .startObject()
        .field("properties")
        .startObject()
        .field("appType").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("username").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("appVersion").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("phoneModel").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("warnReason").startObject().field("index", "true").field("type", "keyword").endObject()
        .field("warnTime").startObject().field("index", "true").field("type", "date").field("format", "yyyy-MM-dd HH:mm:ss").endObject()
        .endObject()
        .endObject();
    logger.info("builderString:" + builder.toString());
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
    createIndexRequest.settings(Settings.builder()
        .put("index.number_of_shards", 1)
        .put("index.number_of_replicas", 0)
        .put("index.blocks.read_only_allow_delete", "false")
        .put("index.max_result_window", 1000000)  // 设置最大返回结果数, 1百万
    );
    createIndexRequest.mapping(builder);
    CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    boolean acknowledged = createIndexResponse.isAcknowledged();
    if (acknowledged) {
      MonitorMomeryUtil.indexExistsMap.put(indexName, indExists);
      logger.info("create indexName:" + indexName + " success");
    } else {
      logger.info("create indexName:" + indexName + " failture");
    }
  }

  public static void sendMessage(MonitorConfig monitorConfig, RabbitMonitor rabbitMonitor, String sendMqMsg) {
    try {
      String queueType = monitorConfig == null ? null : monitorConfig.getQueueType();
      if (null != queueType && queueType.equals(MonitorMomeryUtil.rabbitmq)) {
        Channel channel = MonitorMomeryUtil.connectionChannelMap.get(rabbitMonitor.getQueueName());
        if (null != channel) {
          RabbitConfig rabbitConfig = monitorConfig.getRabbitConfig();
          if (null == rabbitConfig) {
            logger.error("devg-monitor init rabbit-config error....");
            return;
          }
          channel.basicPublish(rabbitMonitor.getExchangeName(), rabbitMonitor.getQueueName(), null, sendMqMsg.getBytes("UTF-8"));
        } else {
          logger.error("devg-monitor init rabbit-channel is null");
        }
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage());
    }
  }

  private static String[] clearIndexName() {
    String monitorMessageNowIndex = indexMessagePrefix + getNowDate(FORMAT_PATTERN2);
    String monitorMessagePreIndex = indexMessagePrefix + getPreDate(FORMAT_PATTERN2);
    String monitorMessageNextIndex = indexMessagePrefix + getNextDate(FORMAT_PATTERN2);
    return new String[]{monitorMessageNowIndex, monitorMessagePreIndex, monitorMessageNextIndex};
  }

  private static void clearIndexMap() {
    //清理Map里面的数据
    List<String> indexList = Arrays.asList(clearIndexName());
    Map<String, String> indexMap = MonitorMomeryUtil.indexExistsMap;
    Iterator iter = indexMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry entry = (Map.Entry) iter.next();
      Object key = entry.getKey();
      if (key != null) {
        String curKey = key.toString();
        if (!indexList.contains(curKey)) {
          iter.remove();
        }
      }
    }
  }

  public static String[] getMonitorTime(String selectHour) {
    String startTime = null;
    String endTime = null;
    switch (selectHour) {
      case "1":
        startTime = "00:00:00";
        endTime = "02:00:00";
        break;
      case "2":
        startTime = "02:00:00";
        endTime = "04:00:00";
        break;
      case "3":
        startTime = "04:00:00";
        endTime = "06:00:00";
        break;
      case "4":
        startTime = "06:00:00";
        endTime = "08:00:00";
        break;
      case "5":
        startTime = "08:00:00";
        endTime = "10:00:00";
        break;
      case "6":
        startTime = "10:00:00";
        endTime = "12:00:00";
        break;
      case "7":
        startTime = "12:00:00";
        endTime = "14:00:00";
        break;
      case "8":
        startTime = "14:00:00";
        endTime = "16:00:00";
        break;
      case "9":
        startTime = "16:00:00";
        endTime = "18:00:00";
        break;
      case "10":
        startTime = "18:00:00";
        endTime = "20:00:00";
        break;
      case "11":
        startTime = "20:00:00";
        endTime = "22:00:00";
        break;
      case "12":
        startTime = "22:00:00";
        endTime = "24:00:00";
        break;
      default:
        startTime = "00:00:00";
        endTime = "02:00:00";
        break;
    }
    return new String[]{startTime, endTime};
  }

  public static String getSelectHour() {
    String selectHour = "1";
    String nowTime = getNowDate("HH:mm:ss");
    if (nowTime.compareTo("00:00:00") >= 0 && nowTime.compareTo("02:00:00") < 0) {
      selectHour = "1";
    } else if (nowTime.compareTo("02:00:00") >= 0 && nowTime.compareTo("04:00:00") < 0) {
      selectHour = "2";
    } else if (nowTime.compareTo("04:00:00") >= 0 && nowTime.compareTo("06:00:00") < 0) {
      selectHour = "3";
    } else if (nowTime.compareTo("06:00:00") >= 0 && nowTime.compareTo("08:00:00") < 0) {
      selectHour = "4";
    } else if (nowTime.compareTo("08:00:00") >= 0 && nowTime.compareTo("10:00:00") < 0) {
      selectHour = "5";
    } else if (nowTime.compareTo("10:00:00") >= 0 && nowTime.compareTo("12:00:00") < 0) {
      selectHour = "6";
    } else if (nowTime.compareTo("12:00:00") >= 0 && nowTime.compareTo("14:00:00") < 0) {
      selectHour = "7";
    } else if (nowTime.compareTo("14:00:00") >= 0 && nowTime.compareTo("16:00:00") < 0) {
      selectHour = "8";
    } else if (nowTime.compareTo("16:00:00") >= 0 && nowTime.compareTo("18:00:00") < 0) {
      selectHour = "9";
    } else if (nowTime.compareTo("18:00:00") >= 0 && nowTime.compareTo("20:00:00") < 0) {
      selectHour = "10";
    } else if (nowTime.compareTo("20:00:00") >= 0 && nowTime.compareTo("22:00:00") < 0) {
      selectHour = "11";
    } else if (nowTime.compareTo("22:00:00") >= 0 && nowTime.compareTo("24:00:00") < 0) {
      selectHour = "12";
    }
    return selectHour;
  }

  public static long getTimestamp(String time, String format) {
    long timestamp = 0L;
    try {
      DateFormat df = new SimpleDateFormat(format);
      java.util.Date date = df.parse(time);
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      timestamp = cal.getTimeInMillis();
    } catch (Exception ex) {
      logger.error("获取时间戳失败", ex);
    }
    return timestamp;
  }

  public static String localDateFormat(LocalDate localDate, String pattern) {
    return localDate.format(DateTimeFormatter.ofPattern(pattern));
  }

  public static String getCookieValue(HttpServletRequest request, String cookieName) {
    // TODO Auto-generated method stub
    String cookieValue = null;
    Cookie[] cookies = request.getCookies(); // 这样便可以获取一个cookie数组
    if (null != cookies && cookies.length > 0) {
      for (Cookie cookie : cookies) {
        String coolieName = cookie.getName();
        if (null != coolieName && coolieName.equals(cookieName)) { // cookie存在登录成功
          cookieValue = cookie.getValue();
          break;
        }
      }
    }
    return cookieValue;
  }

  public static void setCookieValue(
      HttpServletResponse response, String name, String value, int maxAge) {
    // TODO Auto-generated method stub
    setCookieValue(response, name, value, maxAge, true);
  }

  public static void setCookieValue(
      HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly) {
    // TODO Auto-generated method stub
    Cookie cookie = new Cookie(name, value); // 设置cookie和redis超时时间
    cookie.setMaxAge(maxAge); //单位秒
    cookie.setPath("/");
    if (httpOnly) {
      cookie.setHttpOnly(true);
    }
    response.addCookie(cookie);
  }
}
