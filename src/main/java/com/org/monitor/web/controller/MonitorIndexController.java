package com.org.monitor.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.org.monitor.MonitorInit;
import com.org.monitor.elasticsearch.EsResult;
import com.org.monitor.elasticsearch.EsSearch;
import com.org.monitor.model.JsonModel;
import com.org.monitor.utils.IpAddressUtil;
import com.org.monitor.utils.MonitorUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.ParsedComposite;
import org.elasticsearch.search.aggregations.bucket.composite.ParsedComposite.ParsedBucket;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/devg/monitor")
public class MonitorIndexController extends MonitorBasicController {

  /*
    WebMvcConfigurer
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("devg-monitor-ui.html").addResourceLocations("classpath:/META-INF/resources/");
      registry.addResourceHandler("devg-monitor-login.html").addResourceLocations("classpath:/META-INF/resources/");
      registry.addResourceHandler("/devg-monitor*").addResourceLocations("classpath:/META-INF/resources/devg-monitor/");
    }*/
  private final Logger logger = LoggerFactory.getLogger(MonitorInit.class);

  @RequestMapping("/initDate")
  public JsonModel initDate(HttpServletRequest request) {
    if (!hasLogin()) {
      return JsonModel.newFail("未登录");
    }
    String monitorName = MonitorUtil.monitorName;
    if (null != MonitorUtil.monitorConfig && null != MonitorUtil.monitorConfig.getMonitorName() && !""
        .equals(MonitorUtil.monitorConfig.getMonitorName())) {
      monitorName = MonitorUtil.monitorConfig.getMonitorName();
    }
    String theDate = MonitorUtil.getNowDate("yyyy-MM-dd");
    Map<String, Object> resultDate = new HashMap<>();
    resultDate.put("theDate", theDate);
    resultDate.put("selectHour", MonitorUtil.getSelectHour());
    resultDate.put("monitorName", monitorName);
    return JsonModel.newSuccess(resultDate);
  }

  @RequestMapping("/pageView")
  public JsonModel pageView(HttpServletRequest request) {
    if (!hasLogin()) {
      return JsonModel.newFail("未登录");
    }
    String evnName = MonitorUtil.monitorConfig == null ? "" : MonitorUtil.monitorConfig.getEvnName();
    List<String> keyList = new ArrayList<>();
    List<Long> valueList = new ArrayList<>();
    Map<String, Object> resultMap = new HashMap<>();
    double startScollBar = 20;
    try {
      final String resultData = "result_data";
      DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(resultData);
      dateHistogramAggregationBuilder.field("startTime");
      dateHistogramAggregationBuilder.fixedInterval(DateHistogramInterval.minutes(10));
      dateHistogramAggregationBuilder.format("yyyy-MM-dd HH:mm");
      dateHistogramAggregationBuilder.minDocCount(0L);
      String now = MonitorUtil.getNowDate("yyyy-MM-dd");
      String indexName = MonitorUtil.getNowDate("yyyyMMdd");
      String theDate = request.getParameter("theDate");//获取传入时间
      if (!MonitorUtil.isNullOrEmpty(theDate)) {
        now = theDate;
        indexName = theDate.replace("-", "");
      }
      String min = now + MonitorUtil.EMPTY + MonitorUtil.EARLY_MIN;
      String max = now + MonitorUtil.EMPTY + MonitorUtil.LATE_MIN;
      ExtendedBounds extendedBounds = new ExtendedBounds(min, max);
      dateHistogramAggregationBuilder.extendedBounds(extendedBounds);
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(0);
      searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);
      BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
      QueryBuilder messageTypeBuilder = QueryBuilders.termQuery("messageType", "1"); //  只统计请求访问量
      queryBuilder.must(messageTypeBuilder);
      QueryBuilder timeBuilder = QueryBuilders.rangeQuery("startTime").lte(now + MonitorUtil.EMPTY + MonitorUtil.LATE_SECOND)
          .gte(now + MonitorUtil.EMPTY + MonitorUtil.EARLY_SECOND);
      queryBuilder.must(timeBuilder);
      searchSourceBuilder.query(queryBuilder);
      EsResult<Map<String, Object>> esResult = EsSearch.getEsResult(searchSourceBuilder, MonitorUtil.indexMessagePrefix + evnName + indexName);
      Aggregation aggregation = esResult.getAggregations().get(resultData);
      List<? extends Bucket> buckets = ((Histogram) aggregation).getBuckets();
      // 遍历返回的桶
      for (Histogram.Bucket bucket : buckets) {
        String keyString = bucket.getKeyAsString();
        long docCount = bucket.getDocCount();
        keyList.add(keyString);
        valueList.add(docCount);
      }
      startScollBar = getScollBar(min + ":00", max + ":00");
    } catch (Exception ex) {
      logger.error("query error:", ex);
    }

    startScollBar -= 3;
    startScollBar = startScollBar > 95 ? 95 : startScollBar;
    resultMap.put("startScollBar", startScollBar);
    resultMap.put("endScollBar", startScollBar + 5);
    resultMap.put("keyList", keyList);
    resultMap.put("valueList", valueList);
    return JsonModel.newSuccess(resultMap);
  }


  @RequestMapping("/pageMemoryView")
  public JsonModel pageMemoryView(HttpServletRequest request) {
    if (!hasLogin()) {
      return JsonModel.newFail("未登录");
    }
    String evnName = MonitorUtil.monitorConfig == null ? "" : MonitorUtil.monitorConfig.getEvnName();
    List<String> keyList = new ArrayList<>();
    List<Long> valueList = new ArrayList<>();

//    JVM内存已用的空间
    List<Double> vmUseValueList = new ArrayList<>();

//    JVM内存的空闲空间
    List<Double> vmFreeValueList = new ArrayList<>();

    //    JVM总内存空间
    List<Double> vmTotalValueList = new ArrayList<>();

    //    JVM最大内存空间
    List<Double> vmMaxValueList = new ArrayList<>();

    //    JVM内存已用的空间比率
    List<Double> vmUseRateValueList = new ArrayList<>();

    //    操作系统物理内存已用的空间
    List<Double> physicalUseValueList = new ArrayList<>();

    //    操作系统物理内存的空闲空间
    List<Double> physicalFreeValueList = new ArrayList<>();

    //    操作系统总物理内存
    List<Double> physicalTotalValueList = new ArrayList<>();

    //    操作系统物理内存已用的空间比率
    List<Double> physicalUseRateValueList = new ArrayList<>();

    //    线程总数
    List<Double> totalThreadValueList = new ArrayList<>();

    String startTime = null;
    String endTime = null;
    Map<String, Object> resultMap = new HashMap<>();
    try {
      final String resultData = "resultData";
      DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(resultData);
      dateHistogramAggregationBuilder.field("monitorTime");
      dateHistogramAggregationBuilder.fixedInterval(DateHistogramInterval.seconds(60));
      dateHistogramAggregationBuilder.format("yyyy-MM-dd HH:mm:ss");
      dateHistogramAggregationBuilder.minDocCount(0L);
      String now = MonitorUtil.getNowDate("yyyy-MM-dd");
      String indexName = MonitorUtil.getNowDate("yyyyMMdd");
      String theDate = request.getParameter("theDate");//获取传入时间
      if (!MonitorUtil.isNullOrEmpty(theDate)) {
        now = theDate;
        indexName = theDate.replace("-", "");
      }
      String selectHour = request.getParameter("selectHour"); // 查询时间
      String[] monitorTimeArr = MonitorUtil.getMonitorTime(selectHour);
      startTime = now + MonitorUtil.EMPTY + monitorTimeArr[0];
      endTime = now + MonitorUtil.EMPTY + monitorTimeArr[1];
      ExtendedBounds extendedBounds = new ExtendedBounds(startTime, endTime);
      dateHistogramAggregationBuilder.extendedBounds(extendedBounds);
      dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("vmUseNum").field("vmUse"));
      dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("vmMaxNum").field("vmMax"));
      dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("vmFreeNum").field("vmFree"));
      dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("vmTotalNum").field("vmTotal"));
      dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("physicalUseNum").field("physicalUse"));
      dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("physicalFreeNum").field("physicalFree"));
      dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("physicalTotalNum").field("physicalTotal"));
      dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("totalThreadNum").field("totalThread"));
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(0);
      searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);
      String localIp = IpAddressUtil.getLocalIp();
      String localPort = MonitorUtil.monitorConfig == null ? MonitorUtil.defaultPort : MonitorUtil.monitorConfig.getLocalPort();
      BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
      QueryBuilder localIpBuilder = QueryBuilders.termQuery("localIp", localIp); //  只统计当前IP
      queryBuilder.must(localIpBuilder);

      QueryBuilder localPortBuilder = QueryBuilders.termQuery("localPort", localPort); //  只统计当前端口
      queryBuilder.must(localPortBuilder);

      QueryBuilder monitorTimeRangeBuilder = QueryBuilders.rangeQuery("monitorTime").gte(startTime).lt(endTime);
      queryBuilder.must(monitorTimeRangeBuilder);

      searchSourceBuilder.query(queryBuilder);
      EsResult<Map<String, Object>> esResult = EsSearch.getEsResult(searchSourceBuilder, MonitorUtil.indexMemoryPrefix + evnName + indexName);
//      SearchResponse searchResponse = EsSearch.getSearchResponse(searchSourceBuilder, MonitorUtil.indexMemoryPrefix + indexName);
      Aggregation aggregation = esResult.getAggregations().get(resultData);
      List<? extends Bucket> buckets = ((Histogram) aggregation).getBuckets();
      // 遍历返回的桶
      for (Histogram.Bucket bucket : buckets) {
        String keyString = bucket.getKeyAsString();
        long docCount = bucket.getDocCount();
        Double vmUseNum = 0d;
        Double vmMaxNum = 0d;
        Double vmFreeNum = 0d;
        Double vmTotalNum = 0d;
        Double vmUseRateNum = 0d;
        Double physicalUseNum = 0d;
        Double physicalFreeNum = 0d;
        Double physicalTotalNum = 0d;
        Double physicalUseRateNum = 0d;
        Double totalThreadNum = 0d;
        Aggregations aggregations = bucket.getAggregations();
        if (null != aggregations && docCount > 0) {
          Avg vmUseNumAgg = (Avg) aggregations.getAsMap().get("vmUseNum");
          vmUseNum = getAvgNum(vmUseNumAgg);

          Avg vmMaxNumAgg = (Avg) aggregations.getAsMap().get("vmMaxNum");
          vmMaxNum = getAvgNum(vmMaxNumAgg);

          Avg vmFreeNumAgg = (Avg) aggregations.getAsMap().get("vmFreeNum");
          vmFreeNum = getAvgNum(vmFreeNumAgg);

          Avg vmTotalNumAgg = (Avg) aggregations.getAsMap().get("vmTotalNum");
          vmTotalNum = getAvgNum(vmTotalNumAgg);

          vmUseRateNum = (double) Math.round((vmUseNum / vmTotalNum) * 100);

          Avg physicalUseNumAgg = (Avg) aggregations.getAsMap().get("physicalUseNum");
          physicalUseNum = getAvgNum(physicalUseNumAgg);

          Avg physicalFreeNumAgg = (Avg) aggregations.getAsMap().get("physicalFreeNum");
          physicalFreeNum = getAvgNum(physicalFreeNumAgg);

          Avg physicalTotalNumAgg = (Avg) aggregations.getAsMap().get("physicalTotalNum");
          physicalTotalNum = getAvgNum(physicalTotalNumAgg);

          physicalUseRateNum = (double) Math.round((physicalUseNum / physicalTotalNum) * 100);

          Avg totalThreadNumAgg = (Avg) aggregations.getAsMap().get("totalThreadNum");
          totalThreadNum = getAvgNum(totalThreadNumAgg);
        }
        keyList.add(keyString);
        valueList.add(docCount);
        vmUseValueList.add(vmUseNum);
        vmMaxValueList.add(vmMaxNum);
        vmFreeValueList.add(vmFreeNum);
        vmTotalValueList.add(vmTotalNum);
        vmUseRateValueList.add(vmUseRateNum);
        physicalUseValueList.add(physicalUseNum);
        physicalFreeValueList.add(physicalFreeNum);
        physicalTotalValueList.add(physicalTotalNum);
        physicalUseRateValueList.add(physicalUseRateNum);
        totalThreadValueList.add(totalThreadNum);
      }
    } catch (Exception ex) {
      logger.error("query error:", ex);
    }
    resultMap.put("keyList", keyList);
    resultMap.put("valueList", valueList);
    resultMap.put("vmUseValueList", vmUseValueList);
    resultMap.put("vmMaxValueList", vmMaxValueList);
    resultMap.put("vmFreeValueList", vmFreeValueList);
    resultMap.put("vmTotalValueList", vmTotalValueList);
    resultMap.put("vmUseRateValueList", vmUseRateValueList);
    resultMap.put("physicalUseValueList", physicalUseValueList);
    resultMap.put("physicalFreeValueList", physicalFreeValueList);
    resultMap.put("physicalTotalValueList", physicalTotalValueList);
    resultMap.put("physicalUseRateValueList", physicalUseRateValueList);
    resultMap.put("totalThreadValueList", totalThreadValueList);
    double startScollBar = getScollBar(startTime, endTime);
    startScollBar -= 3;
    startScollBar = startScollBar > 95 ? 95 : startScollBar;
    resultMap.put("startScollBar", startScollBar);
    resultMap.put("endScollBar", startScollBar + 5);
    return JsonModel.newSuccess(resultMap);
  }

  @RequestMapping("/pageSearch")
  public JsonModel pageSearch(@RequestBody Map<String, Object> params) {
    if (!hasLogin()) {
      return JsonModel.newFail("未登录");
    }
    String evnName = MonitorUtil.monitorConfig == null ? "" : MonitorUtil.monitorConfig.getEvnName();
    List<Map<String, Object>> resultList = new ArrayList<>();
    long total = 0;
    try {
      Integer pageNum = MonitorUtil.isNullOrEmpty(params.get(MonitorUtil.PAGE)) ? 1 : Integer.parseInt(params.get(MonitorUtil.PAGE).toString());
      Integer pageSize = MonitorUtil.isNullOrEmpty(params.get(MonitorUtil.LIMIT)) ? 10 : Integer.parseInt(params.get(MonitorUtil.LIMIT).toString());
      String indexName = MonitorUtil.getNowDate("yyyyMMdd");
      String now = MonitorUtil.getNowDate("yyyy-MM-dd");
      String theDate = MonitorUtil.getMapString(params, "theDate");//获取传入时间
      if (!MonitorUtil.isNullOrEmpty(theDate)) {
        now = theDate;
        indexName = theDate.replace("-", "");
      }
      BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
      // 按时间查询
      String start = MonitorUtil.getMapString(params, "start");//开始时间
      String end = MonitorUtil.getMapString(params, "end");//结束时间
      if (!MonitorUtil.isNullOrEmpty(start) && !MonitorUtil.isNullOrEmpty(end)) {
        start = now + MonitorUtil.EMPTY + start;
        end = now + MonitorUtil.EMPTY + end;
        QueryBuilder timeRangeBuilder = QueryBuilders.rangeQuery("startTime").gte(start).lte(end);
        queryBuilder.must(timeRangeBuilder);
      } else if (!MonitorUtil.isNullOrEmpty(start)) {
        start = now + MonitorUtil.EMPTY + start;
        QueryBuilder timeRangeBuilder = QueryBuilders.rangeQuery("startTime").gte(start);
        queryBuilder.must(timeRangeBuilder);
      } else if (!MonitorUtil.isNullOrEmpty(end)) {
        end = now + MonitorUtil.EMPTY + end;
        QueryBuilder timeRangeBuilder = QueryBuilders.rangeQuery("startTime").lte(end);
        queryBuilder.must(timeRangeBuilder);
      }

//      按监控类型查询
      String messageType = MonitorUtil.getMapString(params, "messageType");//监控类型
      if (!MonitorUtil.isNullOrEmpty(messageType)) {
        QueryBuilder messageTypeBuilder = QueryBuilders.termQuery("messageType", messageType);
        queryBuilder.must(messageTypeBuilder);
      }

      String modelName = MonitorUtil.getMapString(params, "modelName"); // 模块名称
      if (!MonitorUtil.isNullOrEmpty(modelName)) {
        QueryBuilder modelNameBuilder = QueryBuilders.termQuery("modelName", modelName);
        queryBuilder.must(modelNameBuilder);
      }
//      按结果类型查询
      String resultType = MonitorUtil.getMapString(params, "resultType");//结果类型
      if (!MonitorUtil.isNullOrEmpty(resultType)) {
        QueryBuilder resultTypeBuilder = QueryBuilders.termQuery("resultType", resultType);
        queryBuilder.must(resultTypeBuilder);
      }
//     按关键字查询
      String keyword = MonitorUtil.getMapString(params, "keyword");
      if (!MonitorUtil.isNullOrEmpty(keyword)) {
        BoolQueryBuilder searchBuilder = QueryBuilders.boolQuery();
//      方法名
        QueryBuilder classMethodBuilder = QueryBuilders.wildcardQuery("classMethod", "*" + keyword + "*");
        searchBuilder.should(classMethodBuilder);
//      请求url
        QueryBuilder requestUrlBuilder = QueryBuilders.wildcardQuery("requestUrl", "*" + keyword + "*");
        searchBuilder.should(requestUrlBuilder);
//      消息KEY
        QueryBuilder messageKeyBuilder = QueryBuilders.wildcardQuery("messageKey", "*" + keyword + "*");
        searchBuilder.should(messageKeyBuilder);
        if (null != MonitorUtil.monitorConfig && !MonitorUtil.isNullOrEmpty(MonitorUtil.monitorConfig.getRequestParams())) {
          String[] requestParamsNames = MonitorUtil.monitorConfig.getRequestParams().split(",");
          for (String requestParamsName : requestParamsNames) {
            if (!MonitorUtil.isNumeric(keyword)) {
              continue;
            }
            QueryBuilder requestParamBuilder = QueryBuilders.termQuery("requestParams." + requestParamsName, keyword);
            searchBuilder.should(requestParamBuilder);
          }
        }
        queryBuilder.must(searchBuilder);
      }
//    花费时长查询
      String costTimeType = MonitorUtil.getMapString(params, "costTimeType");
      if (!MonitorUtil.isNullOrEmpty(costTimeType)) {
        QueryBuilder costTimeBuilder = null;
        switch (costTimeType) {
          case "0":   // <=1000ms
            costTimeBuilder = QueryBuilders.rangeQuery("costTime").lte(1000);
            queryBuilder.must(costTimeBuilder);
            break;
          case "1":  // >1000ms <=3000ms
            costTimeBuilder = QueryBuilders.rangeQuery("costTime").gt(1000).lte(3000);
            queryBuilder.must(costTimeBuilder);
            break;
          case "2":  // >3000ms
            costTimeBuilder = QueryBuilders.rangeQuery("costTime").gt(3000);
            queryBuilder.must(costTimeBuilder);
            break;
          default:  // <=1000ms
            costTimeBuilder = QueryBuilders.rangeQuery("costTime").lte(1000);
            queryBuilder.must(costTimeBuilder);
            break;
        }
      }

      SortBuilder sortBuilder = null;
      String field = MonitorUtil.getMapString(params, "field");
      String order = MonitorUtil.getMapString(params, "order");
      if (!MonitorUtil.isNullOrEmpty(field) && !MonitorUtil.isNullOrEmpty(order)) {
        sortBuilder = SortBuilders.fieldSort(field).order(order.equals("desc") ? SortOrder.DESC : SortOrder.ASC);
      } else {
        sortBuilder = SortBuilders.fieldSort("startTime").order(SortOrder.DESC); // 默认按发布时间倒序排
      }
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
      searchSourceBuilder.query(queryBuilder);
      searchSourceBuilder.sort(sortBuilder);
      EsResult<Map<String, Object>> esResult = EsSearch
          .getEsResult(searchSourceBuilder, pageNum, pageSize, MonitorUtil.indexMessagePrefix + evnName + indexName);
      resultList = esResult.getEsResultList();
      if (null != resultList && resultList.size() > 0) {
        for (Map<String, Object> map : resultList) {
          map.put("requestParams", map.get("requestParams") == null ? null : JSONObject.toJSONString(map.get("requestParams")));
        }
      }
      total = esResult.getTotal();
    } catch (Exception ex) {
      logger.error("query error:", ex);
    }
    return JsonModel.newSuccess(resultList, total);
  }

  @RequestMapping("/appWarnSearch")
  public JsonModel appWarnSearch(@RequestBody Map<String, Object> params) {
    if (!hasLogin()) {
      return JsonModel.newFail("未登录");
    }
    String evnName = MonitorUtil.monitorConfig == null ? "" : MonitorUtil.monitorConfig.getEvnName();
    List<Map<String, Object>> resultList = new ArrayList<>();
    long total = 0;
    try {
      Integer pageNum = MonitorUtil.isNullOrEmpty(params.get(MonitorUtil.PAGE)) ? 1 : Integer.parseInt(params.get(MonitorUtil.PAGE).toString());
      Integer pageSize = MonitorUtil.isNullOrEmpty(params.get(MonitorUtil.LIMIT)) ? 10 : Integer.parseInt(params.get(MonitorUtil.LIMIT).toString());
      String indexName = MonitorUtil.getNowDate("yyyyMMdd");
      String now = MonitorUtil.getNowDate("yyyy-MM-dd");
      String theDate = MonitorUtil.getMapString(params, "theDate");//获取传入时间
      if (!MonitorUtil.isNullOrEmpty(theDate)) {
        now = theDate;
        indexName = theDate.replace("-", "");
      }
      BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
      // 按时间查询
      String start = MonitorUtil.getMapString(params, "appStart");
      String end = MonitorUtil.getMapString(params, "appEnd");
      if (!MonitorUtil.isNullOrEmpty(start) && !MonitorUtil.isNullOrEmpty(end)) {
        start = now + MonitorUtil.EMPTY + start;
        end = now + MonitorUtil.EMPTY + end;
        QueryBuilder timeRangeBuilder = QueryBuilders.rangeQuery("warnTime").gte(start).lte(end);
        queryBuilder.must(timeRangeBuilder);
      } else if (!MonitorUtil.isNullOrEmpty(start)) {
        start = now + MonitorUtil.EMPTY + start;
        QueryBuilder timeRangeBuilder = QueryBuilders.rangeQuery("warnTime").gte(start);
        queryBuilder.must(timeRangeBuilder);
      } else if (!MonitorUtil.isNullOrEmpty(end)) {
        end = now + MonitorUtil.EMPTY + end;
        QueryBuilder timeRangeBuilder = QueryBuilders.rangeQuery("warnTime").lte(end);
        queryBuilder.must(timeRangeBuilder);
      }

//      按类型查询android/IOS
      String appType = MonitorUtil.getMapString(params, "appType");
      if (!MonitorUtil.isNullOrEmpty(appType)) {
        QueryBuilder typeBuilder = QueryBuilders.termQuery("appType", appType);
        queryBuilder.must(typeBuilder);
      }

//     按关键字查询
      String keyword = MonitorUtil.getMapString(params, "appKeyword");
      if (!MonitorUtil.isNullOrEmpty(keyword)) {
        BoolQueryBuilder searchBuilder = QueryBuilders.boolQuery();
//      用户名
        QueryBuilder usernameBuilder = QueryBuilders.wildcardQuery("username", "*" + keyword + "*");
        searchBuilder.should(usernameBuilder);
//      app型号
        QueryBuilder appVersionBuilder = QueryBuilders.wildcardQuery("appVersion", "*" + keyword + "*");
        searchBuilder.should(appVersionBuilder);
//      手机版本
        QueryBuilder phoneModelBuilder = QueryBuilders.wildcardQuery("phoneModel", "*" + keyword + "*");
        searchBuilder.should(phoneModelBuilder);
        queryBuilder.must(searchBuilder);
      }

      SortBuilder sortBuilder = null;
      String field = MonitorUtil.getMapString(params, "field");
      String order = MonitorUtil.getMapString(params, "order");
      if (!MonitorUtil.isNullOrEmpty(field) && !MonitorUtil.isNullOrEmpty(order)) {
        sortBuilder = SortBuilders.fieldSort(field).order(order.equals("desc") ? SortOrder.DESC : SortOrder.ASC);
      } else {
        // 默认按发布时间倒序排
        sortBuilder = SortBuilders.fieldSort("warnTime").order(SortOrder.DESC);
      }
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
      searchSourceBuilder.query(queryBuilder);
      searchSourceBuilder.sort(sortBuilder);
      EsResult<Map<String, Object>> esResult = EsSearch
          .getEsResult(searchSourceBuilder, pageNum, pageSize, MonitorUtil.indexAppPrefix + evnName + indexName);
      resultList = esResult.getEsResultList();
      total = esResult.getTotal();
    } catch (Exception ex) {
      logger.error("query error:", ex);
    }
    return JsonModel.newSuccess(resultList, total);
  }

  @RequestMapping("/initViewCount")
  public JsonModel initViewCount(@RequestBody Map<String, Object> params) {
    if (!hasLogin()) {
      return JsonModel.newFail("未登录");
    }
    String evnName = MonitorUtil.monitorConfig == null ? "" : MonitorUtil.monitorConfig.getEvnName();
    List<Map<String, Object>> resultList = new ArrayList<>();
    long total = 0;
    try {
      int viewSize = 5000;
      String indexName = MonitorUtil.getNowDate("yyyyMMdd");
      String theDate = MonitorUtil.getMapString(params, "theDate");//获取传入时间
      if (!MonitorUtil.isNullOrEmpty(theDate)) {
        indexName = theDate.replace("-", "");
      }
      String messageType = MonitorUtil.getMapString(params, "messageType");//获取传入时间
      if (MonitorUtil.isNullOrEmpty(messageType)) {
        messageType = "1";
      }
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(0);
      BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
      QueryBuilder messageTypeBuilder = QueryBuilders.termQuery("messageType", messageType); //  只统计请求访问量
      queryBuilder.must(messageTypeBuilder);
      searchSourceBuilder.query(queryBuilder);
      // 聚合查询
      AggregationBuilder requestAgg;
      if (MonitorUtil.equals(messageType, "1")) {
        requestAgg = AggregationBuilders.terms("request").field("requestUrl").size(viewSize);
      } else {
        requestAgg = AggregationBuilders.terms("request").field("classMethod").size(viewSize);
      }
      requestAgg.subAggregation(AggregationBuilders.avg("avg_num").field("costTime"));  // 请求时长平均值
      requestAgg.subAggregation(AggregationBuilders.max("max_num").field("costTime"));  // 请求时长最大值
      requestAgg.subAggregation(AggregationBuilders.min("min_num").field("costTime"));  // 请求时长最小值
//      ((TermsAggregationBuilder) requestAgg).order(BucketOrder.aggregation())
      searchSourceBuilder.aggregation(requestAgg);
      EsResult<Map<String, Object>> esResult = EsSearch.getEsResult(searchSourceBuilder, MonitorUtil.indexMessagePrefix + evnName + indexName);
      Aggregations requestAggResult = esResult.getAggregations();
      if (null != requestAggResult) {
        Terms requestResult = requestAggResult.get("request");
        total = requestResult.getSumOfOtherDocCounts();
        List<? extends Terms.Bucket> bucketList = requestResult.getBuckets();
        if (null != bucketList && bucketList.size() > 0) {
          int i = 1;
          for (Terms.Bucket item : bucketList) {
            Map<String, Object> resultMap = new HashMap<>();
            String requestUrl = item.getKey() == null ? "" : item.getKey().toString();
            Long docCount = item.getDocCount();
            Double avgNumber = 0d;
            Double maxNumber = 0d;
            Double minNumber = 0d;
            Aggregations aggregations = item.getAggregations();
            if (null != aggregations) {
              Avg avgAgg = aggregations.get("avg_num");
              Double avgNum = avgAgg.getValue();
              BigDecimal bigDecimal = new BigDecimal(avgNum);
              avgNumber = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
              Max maxAgg = aggregations.get("max_num");
              Double maxNum = maxAgg.getValue();
              bigDecimal = new BigDecimal(maxNum);
              maxNumber = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
              Min minAgg = aggregations.get("min_num");
              Double minNum = minAgg.getValue();
              bigDecimal = new BigDecimal(minNum);
              minNumber = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            resultMap.put("avgNumber", avgNumber);
            resultMap.put("maxNumber", maxNumber);
            resultMap.put("minNumber", minNumber);
            resultMap.put("num", i);
            resultMap.put("requestUrl", requestUrl);
            resultMap.put("docCount", docCount);
            resultList.add(resultMap);
            i++;
          }
        }
      }
    } catch (Exception ex) {
      logger.error("query error:", ex);
    }
    return JsonModel.newSuccess(resultList, total);
  }


  @RequestMapping("/monitorLogin")
  public JsonModel monitorLogin(HttpServletRequest request, HttpServletResponse response) {
    String username = request.getParameter("username");//获取登录名
    String password = request.getParameter("password");//获取登录名
    String authorizedUser = MonitorUtil.authorizedUser;
    if (null == authorizedUser) {
      return JsonModel.newFail("登录失败");
    }
    String[] authorizedArr = authorizedUser.split(",");
    if (null == authorizedArr || authorizedArr.length != 2) {
      return JsonModel.newFail("登录失败");
    }
    if (null != username && username.equals(authorizedArr[0]) && null != password && password.equals(authorizedArr[1])) {
      try {
        final byte[] authorizedByte = authorizedUser.getBytes("UTF-8");
        String token = DigestUtils.md5DigestAsHex(authorizedByte);
        MonitorUtil.setCookieValue(response, MonitorUtil.TOKEN, token, MonitorUtil.maxAge * 60, true);
        return JsonModel.newSuccess("登录成功");
      } catch (Exception ex) {
        logger.error("登录失败", ex);
        return JsonModel.newFail("登录失败");
      }
    }
    return JsonModel.newFail("登录失败");

  }

  private Double getAvgNum(Avg agg) {
    Double avgNum = agg.getValue();
    BigDecimal bigDecimal = new BigDecimal(avgNum == null ? 0d : avgNum);
    avgNum = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    return avgNum;
  }

  private double getScollBar(String startTime, String endTime) {
    double startScollBar = 20;
    if (null != startTime && !"".equals(startTime) && null != endTime && !"".equals(endTime)) {
      long startTimeTemp = MonitorUtil.getTimestamp(startTime, "yyyy-MM-dd HH:mm:ss");
      long endTimeTemp = MonitorUtil.getTimestamp(endTime, "yyyy-MM-dd HH:mm:ss");
      long nowTimeTemp = System.currentTimeMillis();
      double scollBar = (nowTimeTemp - startTimeTemp) / (double) (endTimeTemp - startTimeTemp);
      scollBar = scollBar * 100;
      BigDecimal scollBarDecimal = new BigDecimal(scollBar);
      startScollBar = scollBarDecimal.setScale(0, BigDecimal.ROUND_DOWN).doubleValue();
    }
    return startScollBar;
  }


  @RequestMapping("/monitorCityplatformSearch")
  public JsonModel monitorCityplatformSearch(@RequestBody Map<String, Object> params) {
    Map<String, Object> result = new HashMap<>();
    List<Map<String, Object>> resultList = new ArrayList<>();
    Map<String, Object> detail = new HashMap<>();
    try {
      String businessId = MonitorUtil.getMapString(params, "businessId");
      // 默认当天,1-当天,2-近7天,3-近30天
      String scopeType = MonitorUtil.getMapString(params, "scopeType");
      String startTime = MonitorUtil.getEarlyInTheDay(LocalDate.now());
      String endTime = MonitorUtil.getLastInTheDay(LocalDate.now());
      if (null != scopeType && scopeType.equals("2")) {
        startTime = MonitorUtil.localDateTimeFormat(LocalDateTime.now().minusDays(7), MonitorUtil.FORMAT_PATTERN3);
      } else if (null != scopeType && scopeType.equals("3")) {
        startTime = MonitorUtil.localDateTimeFormat(LocalDateTime.now().minusDays(30), MonitorUtil.FORMAT_PATTERN3);
      }
      // 查详情数据
      if (null != businessId && !"".equals(businessId)) {
        SearchSourceBuilder detailSourceBuilder = new SearchSourceBuilder().size(1);
        QueryBuilder businessIdBuilder = QueryBuilders.termQuery("businessId", businessId);
        BoolQueryBuilder detailQueryBuilder = QueryBuilders.boolQuery();
        detailQueryBuilder.must(businessIdBuilder);
        detailSourceBuilder.query(detailQueryBuilder);
        EsResult<Map<String, Object>> detailEsResult = EsSearch.getEsResult(detailSourceBuilder, "index_city_platform_monitor");
        List<Map<String, Object>> detailList = detailEsResult.getEsResultList();
        if (null != detailList && detailList.size() > 0) {
          detail = detailList.get(0);
          detail.put("count", 0);
        }
      }
      // 查聚合数据
      List<CompositeValuesSourceBuilder<?>> sources = new ArrayList<>();
      TermsValuesSourceBuilder companyName = new TermsValuesSourceBuilder("companyName").field("companyName").missingBucket(true);
      sources.add(companyName);
      TermsValuesSourceBuilder projectName = new TermsValuesSourceBuilder("projectName").field("projectName").missingBucket(true);
      sources.add(projectName);
      TermsValuesSourceBuilder warnReason = new TermsValuesSourceBuilder("warnReason").field("warnReason").missingBucket(true);
      sources.add(warnReason);
      CompositeAggregationBuilder composite = new CompositeAggregationBuilder("composite", sources).size(500);
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(0);
      QueryBuilder timeBuilder = QueryBuilders.rangeQuery("startTime").gte(startTime).lte(endTime);
      BoolQueryBuilder compositeQueryBuilder = QueryBuilders.boolQuery();
      compositeQueryBuilder.must(timeBuilder);
      searchSourceBuilder.query(compositeQueryBuilder);
      searchSourceBuilder.aggregation(composite);
      EsResult<Map<String, Object>> esResult = EsSearch.getEsResult(searchSourceBuilder, "index_city_platform_monitor");
      Aggregations aggregations = esResult.getAggregations();
      ParsedComposite parsedComposite = aggregations.get("composite");
      List<ParsedBucket> list = parsedComposite.getBuckets();
      for (ParsedBucket parsedBucket : list) {
        Map<String, Object> data = new HashMap<>();
        for (Map.Entry<String, Object> m : parsedBucket.getKey().entrySet()) {
          data.put(m.getKey(), m.getValue());
        }
        data.put("count", parsedBucket.getDocCount());
        resultList.add(data);
        if (null != detail && !detail.isEmpty()) {
          String detailProjectName = MonitorUtil.getMapString(detail, "projectName");
          String detailWarnReason = MonitorUtil.getMapString(detail, "warnReason");
          String dataProjectName = MonitorUtil.getMapString(data, "projectName");
          String dataWarnReason = MonitorUtil.getMapString(data, "warnReason");
          if (null != dataProjectName && dataProjectName.equals(detailProjectName)) {
            if (null != dataWarnReason && dataWarnReason.equals(detailWarnReason)) {
              detail.put("count", data.get("count"));
            }
          }
        }
      }
    } catch (Exception ex) {
      logger.error("query error:", ex);
    }
    if (null != resultList && resultList.size() > 0) {
      Collections.sort(resultList, new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
          Long countOne = MonitorUtil.getMapLong(o1, "count");
          Long countTwo = MonitorUtil.getMapLong(o2, "count");
          if (null != countOne && countOne > countTwo) {
            return 1;
          } else if (null != countOne && countOne < countTwo) {
            return -1;
          }
          return 0;
        }
      });
    }
    result.put("resultList", resultList);
    result.put("detail", detail);
    return JsonModel.newSuccess(result);
  }
}
