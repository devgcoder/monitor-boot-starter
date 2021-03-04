package com.github.devgcoder.monitor.wrapper;

import com.alibaba.fastjson.JSONObject;
import com.github.devgcoder.monitor.utils.MonitorUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

public class RequestBodyParameterWrapper extends HttpServletRequestWrapper {

  private static final Logger logger = LoggerFactory.getLogger(RequestBodyParameterWrapper.class);

  private byte[] requestBody = null;

  public RequestBodyParameterWrapper(HttpServletRequest request) {
    super(request);
    //缓存请求body
    try {
      requestBody = StreamUtils.copyToByteArray(request.getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public RequestBodyParameterWrapper(HttpServletRequest request, String str) {

    super(request);

    //缓存请求body
    try {
      requestBody = strToByteArray(str);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static byte[] strToByteArray(String str) {
    if (str == null) {
      return null;
    }
    byte[] byteArray = str.getBytes();
    return byteArray;
  }

  /**
   * 重写 getInputStream()
   */
  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (requestBody == null) {
      requestBody = new byte[0];
    }
    final ByteArrayInputStream bais = new ByteArrayInputStream(requestBody);
    return new ServletInputStream() {

      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener listener) {

      }

      @Override
      public int read() throws IOException {
        return bais.read();
      }
    };
  }

  /**
   * 重写 getReader()
   */
  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStream()));
  }

  public static JSONObject getInputStr(HttpServletRequest request) {
    BufferedReader br = null;
    InputStreamReader sr = null;
    StringBuilder sb = new StringBuilder();
    JSONObject jsonObject = null;
    try {
      String line = null;
      sr = new InputStreamReader(request.getInputStream(), "UTF-8");
      br = new BufferedReader(sr);
      do {
        line = br.readLine();
        if (line != null) {
          sb.append(line);
        }
      } while (line != null);
    } catch (IOException e) {
      logger.error("change parmas error");
    } finally {
      try {
        br.close();
        sr.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    String str = sb.toString();
    if (MonitorUtil.isNullOrEmpty(str)) {
      jsonObject = new JSONObject();
    } else {
      jsonObject = JSONObject.parseObject(str);
    }
    return jsonObject;
  }
}
