package com.org.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.org.monitor.elasticsearch.CustomConnectionKeepAliveStrategy;
import com.org.monitor.model.*;
import com.org.monitor.model.CommonParams.MonitorModelType;
import com.org.monitor.utils.CommonEnum;
import com.org.monitor.utils.CommonEnum.WarnType;
import com.org.monitor.utils.CommonEnum.messageType;
import com.org.monitor.utils.DingdingUtil;
import com.org.monitor.utils.MonitorMomeryUtil;
import com.org.monitor.utils.MonitorUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class MonitorInit implements InitializingBean {

  private final Logger logger = LoggerFactory.getLogger(MonitorInit.class);

  private MonitorConfig monitorConfig;

  public MonitorInit(MonitorConfig monitorConfig) {
    this.monitorConfig = monitorConfig;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      initElasticSearch(monitorConfig);// 初始化elasticsearch
      String queueType = monitorConfig == null ? null : monitorConfig.getQueueType();
      if (null != queueType && queueType.equals(MonitorMomeryUtil.rabbitmq)) {
        initRabbitMqChannel(monitorConfig);
      } else if (null != queueType && queueType.equals(MonitorMomeryUtil.kafka)) {
      }
      String authorizedUser = monitorConfig.getAuthorizedUser();
      MonitorUtil.authorizedUser = authorizedUser;
    } catch (Exception ex) {
      logger.error(ex.getMessage());
      ex.printStackTrace();
    }
  }

  private void initRabbitMqChannel(MonitorConfig monitorConfig) throws Exception {
    RabbitConfig rabbitConfig = monitorConfig.getRabbitConfig();
    if (null == rabbitConfig) {
      logger.error("devg-monitor init rabbit-config error....");
      return;
    }
    // 初始化消息
    RabbitMonitor rabbitMonitorMessage = rabbitConfig.getRabbitMonitorMessage();
    initRabbitMqChannel(rabbitConfig, rabbitMonitorMessage);
  }


  private void initRabbitMqChannel(RabbitConfig rabbitConfig, RabbitMonitor rabbitMonitor) throws Exception {
    if (null == rabbitMonitor) {
      logger.error("devg-monitor init rabbit-devg-monitor error....");
      return;
    }
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(rabbitConfig.getRabbitHost());
    connectionFactory.setPort(rabbitConfig.getRabbitPort());
    connectionFactory.setUsername(rabbitConfig.getRabbitUsername());
    connectionFactory.setPassword(rabbitConfig.getRabbitPassword());
    String rabbitVirtualHost = rabbitConfig.getRabbitVirtualHost();
    if (null != rabbitVirtualHost && !rabbitVirtualHost.equals("")) {
      connectionFactory.setVirtualHost(rabbitVirtualHost);
    }
    Integer multiple = 5;
    connectionFactory.setAutomaticRecoveryEnabled(true);
    connectionFactory.setNetworkRecoveryInterval(2000 * multiple);
    connectionFactory.setRequestedHeartbeat(multiple);
    connectionFactory.setConnectionTimeout(6000 * multiple);
    connectionFactory.setHandshakeTimeout(6000 * multiple);
    Integer poolSize = rabbitConfig.getRabbitThreadPoolSize() == null ? 30 : rabbitConfig.getRabbitThreadPoolSize();
    ExecutorService es = Executors.newFixedThreadPool(poolSize);
    Connection connection = connectionFactory.newConnection(es);
    if (connection == null) {
      logger.error("devg-monitor init rabbit-connection error....");
      return;
    }
    Channel channel = connection.createChannel();
    initRabbitMqQueue(rabbitMonitor, channel);
    String queueName = rabbitMonitor.getQueueName();
    initRabbitMqConsumer(channel, queueName);
    MonitorMomeryUtil.connectionChannelMap.put(queueName, channel);
  }


  private void initRabbitMqQueue(RabbitMonitor rabbitMonitor, Channel channel) throws IOException {
    String queueName = rabbitMonitor.getQueueName();
    String exchangeName = rabbitMonitor.getExchangeName();
    String routeKey = rabbitMonitor.getRouteKey();
    channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true, false, null);  // 声明交换器
    channel.queueDeclare(queueName, true, false, false, null);  // 声明队列
    channel.queueBind(queueName, exchangeName, routeKey); // 队列，交换器，路由键绑定到一起
  }

  private void initRabbitMqConsumer(Channel channel, String queueName) throws IOException {
    if (null == channel) {
      logger.error("devg-monitor init rabbit-channel error....");
      return;
    }
    RestHighLevelClient restHighLevelClient = MonitorMomeryUtil.restHighLevelClientMap.get(MonitorMomeryUtil.restHighLevelClient);
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
          AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body, "UTF-8");
//        logger.info("devg-monitor init received '" + message + "'");
        if (null != restHighLevelClient) {
          JSONObject messageMap = JSON.parseObject(message);
          String messageModelType = messageMap.getString("messageModelType");
          String indexName = null;
          if (null != messageModelType && messageModelType.equals(MonitorModelType.MESSAGE.getKey())) {
            indexName = MonitorUtil.getMessageIndexName(); // 获取索引名称
            DingdingConfig dingdingConfig = monitorConfig.getDingdingConfig();
            String keyword = dingdingConfig == null ? null : dingdingConfig.getKeyword();
            // next check send mail
            if (null != monitorConfig) {
              String evnName = monitorConfig.getEvnName();
              String classMethod = messageMap.getString("classMethod");
              String resultType = messageMap.getString("resultType");
              String startTime = messageMap.getString("startTime");
              String messageKey = messageMap.getString("messageKey");
              Integer msgType = messageMap.getInteger("messageType");
              String messageParams = "";
              if (null != msgType && msgType == messageType.MsgController.getKey()) {
                JSONObject requestParams = messageMap.getJSONObject("requestParams");
                if (!MonitorUtil.isNullOrEmpty(requestParams)) {
                  messageParams = ",请求参数:" + requestParams.toJSONString();
                }
              }
              if (null != resultType && resultType.equals(CommonEnum.resultType.ERROR.getKey())) {
                Boolean mailMessageError = monitorConfig.getMessageError();
                if (null != mailMessageError && mailMessageError) {
                  String mailContent = (MonitorUtil.isNullOrEmpty(keyword) ? WarnType.BUSSINESS.getName() : keyword) + "类名方法:" + classMethod + "请求错误,"
                      + "消息KEY:" + messageKey + ",请求时间:" + startTime + messageParams + ",环境:" + evnName;
                  DingdingUtil.sendMsg(MonitorUtil.TEXT, mailContent, null);
                }
              } else {
                Long costTime = messageMap.getLong("costTime");
                Long maxCostTime = monitorConfig.getMessageMaxCostTime();
                if (null != maxCostTime && null != costTime && costTime > maxCostTime) {
                  Boolean mailMessageOvertime = monitorConfig.getMessageOvertime();
                  if (null != mailMessageOvertime && mailMessageOvertime) {
                    String mailContent = (MonitorUtil.isNullOrEmpty(keyword) ? WarnType.BUSSINESS.getName() : keyword) + "类名方法:" + classMethod + "请求超时,"
                        + "消息KEY:" + messageKey + ",花费时长:" + costTime + ",请求时间:" + startTime + messageParams + ",环境:" + evnName;
                    DingdingUtil.sendMsg(MonitorUtil.TEXT, mailContent, null);
                  }
                }
              }
            }
          }
          if (null != indexName && !indexName.equals("")) {
            MonitorUtil.checkAndCreateIndex(indexName, restHighLevelClient);
            String indexExists = MonitorMomeryUtil.indexExistsMap.get(indexName);
            if (null != indexExists && indexExists.equals(MonitorUtil.indExists)) {
              try {
                IndexRequest indexRequest = new IndexRequest();
                indexRequest.index(indexName);
                String dataJson = JSON.toJSONString(messageMap, SerializerFeature.WriteMapNullValue);
                indexRequest.source(dataJson, XContentType.JSON);
                IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
              } catch (Exception ex) {
                logger.error("devg-monitor message create error", ex);
              }
            }
          }
        }
        String routingKey = envelope.getRoutingKey();
        String contentType = properties.getContentType();
        long deliveryTag = envelope.getDeliveryTag();
        channel.basicAck(deliveryTag, false);
      }
    };
    channel.basicQos(5);
    channel.basicConsume(queueName, false, consumer);//这里改为手动确认
  }

  private void initElasticSearch(MonitorConfig monitorConfig) {
    ElasticSearchConfig elasticSearchConfig = monitorConfig.getElasticSearchConfig();
    if (null == elasticSearchConfig) {
      logger.error("devg-monitor init elasticsearch-config error....");
      return;
    }
    String hostName = elasticSearchConfig.getHostname();
    String port = elasticSearchConfig.getPort();
    String schemeName = elasticSearchConfig.getSchemeName();
    String[] hostArray = hostName.split(",");
    String[] portArray = port.split(",");
    String[] schemeArray = schemeName.split(",");
    if (hostArray.length != portArray.length || hostArray.length != schemeArray.length) {
      logger.error("datasync init elasticsearch-httphost error....");
      return;
    }
    HttpHost[] httpHosts = new HttpHost[hostArray.length];
    for (int i = 0; i < hostArray.length; i++) {
      httpHosts[i] = new HttpHost(hostArray[i], Integer.valueOf(portArray[i]), schemeArray[i]);
    }
    RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
        RestClient.builder(httpHosts)
            .setRequestConfigCallback(new RequestConfigCallback() {
              @Override
              public Builder customizeRequestConfig(Builder requestConfigBuilder) {
                requestConfigBuilder.setSocketTimeout(elasticSearchConfig.getSocketTimeOut());
                requestConfigBuilder.setConnectTimeout(elasticSearchConfig.getConnectionTimeOut());
                requestConfigBuilder.setConnectionRequestTimeout(elasticSearchConfig.getConnectionRequestTimeOut());
                return requestConfigBuilder;
              }
            }).setHttpClientConfigCallback(new HttpClientConfigCallback() {
          @Override
          public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
            httpAsyncClientBuilder.setMaxConnTotal(elasticSearchConfig.getMaxConnectTotal());
            httpAsyncClientBuilder.setMaxConnPerRoute(elasticSearchConfig.getMaxConnectPerRoute());
            httpAsyncClientBuilder.setKeepAliveStrategy(CustomConnectionKeepAliveStrategy.INSTANCE);
            if (!MonitorUtil.isNullOrEmpty(elasticSearchConfig.getUserName()) && !MonitorUtil.isNullOrEmpty(elasticSearchConfig.getUserPass())) {
              final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
              credentialsProvider.setCredentials(AuthScope.ANY,
                  new UsernamePasswordCredentials(elasticSearchConfig.getUserName(), elasticSearchConfig.getUserPass()));
              httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            return httpAsyncClientBuilder;
          }
        }));
    MonitorMomeryUtil.restHighLevelClientMap.put(MonitorMomeryUtil.restHighLevelClient, restHighLevelClient);
  }
}
