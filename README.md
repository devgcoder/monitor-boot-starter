# 配置部署说明

  1.  放开过滤资源
      - /monitor-ui.html
      - /monitor-login.html
  
  2.  过滤器或者拦截器放开资源
       
          String path = req.getServletPath();
          if (null != path && (path.contains("devg/monitor/") || path.contains("devg-monitor"))) {
            chain.doFilter(request, response);
            return;
          }
      
  3.  WebMvcConfigurer增加路由转发
  
           @Override
           public void addResourceHandlers(ResourceHandlerRegistry registry) {
             registry.addResourceHandler("monitor-ui.html").addResourceLocations("classpath:/META-INF/resources/");
             registry.addResourceHandler("monitor-login.html").addResourceLocations("classpath:/META-INF/resources/");
             registry.addResourceHandler("/monitor*").addResourceLocations("classpath:/META-INF/resources/monitor/");
           }


  4.pom.xml
        
           <dependency>
                 <groupId>org.aspectj</groupId>
                 <artifactId>aspectjweaver</artifactId>
                 <optional>true</optional>
               </dependency>
               <dependency>
                 <groupId>com.rabbitmq</groupId>
                 <artifactId>amqp-client</artifactId>
                 <optional>true</optional>
               </dependency>
               <dependency>
                 <groupId>com.alibaba</groupId>
                 <artifactId>fastjson</artifactId>
                 <version>1.2.54</version>
                 <optional>true</optional>
               </dependency>
               <dependency>
                 <groupId>org.elasticsearch</groupId>
                 <artifactId>elasticsearch</artifactId>
                 <version>7.3.2</version>
                 <optional>true</optional>
               </dependency>
               <dependency>
                 <groupId>org.elasticsearch.client</groupId>
                 <artifactId>elasticsearch-rest-high-level-client</artifactId>
                 <version>7.3.2</version>
                 <optional>true</optional>
               </dependency>
        
        
  5.application.yml
        
    devg:
      monitor:
        enableMonitorController: true
        enableMonitorMapper: true
        queue-type: rabbitmq
        monitor-name: 监控系统
    #    request-params:
        model-name: consumer
        local-port: 8080
        evn-name: dev
        authorized-user: admin,123456
        messageMaxCostTime: 3000
        messageLimitTime: 12000
        messageError: true
        messageOvertime: true
        rabbit-config:
          rabbit-host: 127.0.0.1
          rabbit-port: 5672
          rabbit-username: rabbitadmin
          rabbit-password: 123456
          rabbit-virtual-host: dev
          rabbit-thread-pool-size: 30
          rabbit-monitor-message:
            exchange-name: monitor_exchange
            queue-name: monitor_message_consumer
            route-key: monitor_message_consumer
        elasticSearchConfig:
          hostname: xxx.xxx.xxx.xxx
          port: 9201
          schemeName: http
          connectionTimeOut: 30000
          socketTimeOut: 30000
          connectionRequestTimeOut: 30000
          maxConnectTotal: 60
          maxConnectPerRoute: 20
    #    dingdingConfig:
    #      serverUrl: http://www.xxxxxx.com:8090/dingDing/robotMsg
    #      isAtAll: false
        monitorNonClassMethod:
        - com.fasterxml.jackson.databind.ObjectMapper
        - com.org.monitor.web.controller.MonitorIndexController
        - com.org.monitor.web.controller.MonitorBasicController