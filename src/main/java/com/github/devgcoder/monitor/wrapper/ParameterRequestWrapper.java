package com.github.devgcoder.monitor.wrapper;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ParameterRequestWrapper extends HttpServletRequestWrapper {

  private Map<String, String[]> parameterMap;

  public ParameterRequestWrapper(HttpServletRequest request) {
    super(request);
    //parameterMap = request.getParameterMap();
    //原对象不可以进行修改
    parameterMap = new HashMap(request.getParameterMap());
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return parameterMap;
  }

  public void setParameterMap(Map<String, String[]> parameterMap) {
    this.parameterMap = parameterMap;
  }
}
