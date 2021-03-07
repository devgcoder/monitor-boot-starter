package com.github.devgcoder.monitor;

import com.github.devgcoder.monitor.aspect.MonitorWebAspect;
import com.github.devgcoder.monitor.model.MonitorConfig;
import com.github.devgcoder.monitor.schedule.MonitorSchedule;
import com.github.devgcoder.monitor.utils.MonitorUtil;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = {"com.github.devgcoder.monitor"})
@EnableConfigurationProperties(MonitorConfig.class)
@ConditionalOnWebApplication //web应用才生效
public class MonitorAutoConfiguration {

  private final MonitorConfig monitorConfig;

  public MonitorAutoConfiguration(MonitorConfig monitorConfig) {
    MonitorUtil.monitorConfig = monitorConfig;
    this.monitorConfig = monitorConfig;
  }

  @Bean
  public MonitorWebAspect webAspect() {
    MonitorWebAspect monitorWebAspect = new MonitorWebAspect(monitorConfig);
    return monitorWebAspect;
  }

  @Bean
  public MonitorInit monitorInit() {
    RestTemplate dingRestTempalte = dingRestTempalte();
    MonitorUtil.dingRestTempalte = dingRestTempalte;
    MonitorInit monitorInit = new MonitorInit(monitorConfig);
    return monitorInit;
  }

  public RestTemplate dingRestTempalte() {
    Integer timeOut = 6000;
    // 长链接保持时间长度20秒
    PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
        new PoolingHttpClientConnectionManager(20, TimeUnit.SECONDS);
    int cpuCore = Runtime.getRuntime().availableProcessors();
    // 设置最大链接数
    poolingHttpClientConnectionManager.setMaxTotal(2 * cpuCore + 3);
    // 单路由的并发数
    poolingHttpClientConnectionManager.setDefaultMaxPerRoute(2 * cpuCore);
    HttpClientBuilder httpClientBuilder = HttpClients.custom();
    httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);
    // 重试次数3次，并开启
    httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, true));
    HttpClient httpClient = httpClientBuilder.build();
    // 保持长链接配置，keep-alive
    httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
    HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
    // 链接超时配置 5秒
    httpComponentsClientHttpRequestFactory.setConnectTimeout(timeOut);
    // 连接读取超时配置
    httpComponentsClientHttpRequestFactory.setReadTimeout(timeOut);
    // 连接池不够用时候等待时间长度设置，分词那边 500毫秒 ，我们这边设置成1秒
    httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(timeOut);
    // 缓冲请求数据，POST大量数据，可以设定为true 我们这边机器比较内存较大
    httpComponentsClientHttpRequestFactory.setBufferRequestBody(true);
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(httpComponentsClientHttpRequestFactory);
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
    return restTemplate;
  }
}
