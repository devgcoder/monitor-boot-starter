package com.github.devgcoder.elasticsearch;

import java.util.List;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;

public class EsResult<T> {

  private long total;
  private Aggregations aggregations;
  private List<T> esResultList;

  public static EsResult getEsResult(List esResultList, SearchResponse searchResponse) {
    SearchHits hits = searchResponse.getHits();
    EsResult esResult = new EsResult();
    esResult.setTotal(hits.getTotalHits().value);
    esResult.setAggregations(searchResponse.getAggregations());
    esResult.setEsResultList(esResultList);
    return esResult;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public Aggregations getAggregations() {
    return aggregations;
  }

  public void setAggregations(Aggregations aggregations) {
    this.aggregations = aggregations;
  }

  public List<T> getEsResultList() {
    return esResultList;
  }

  public void setEsResultList(List<T> esResultList) {
    this.esResultList = esResultList;
  }
}
