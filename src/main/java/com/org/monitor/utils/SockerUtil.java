package com.org.monitor.utils;

import java.net.InetSocketAddress;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SockerUtil {

  private final static Logger log = LoggerFactory.getLogger(SockerUtil.class);

  private String host;
  private Integer port;

  public SockerUtil(String host, Integer port) {
    this.host = host;
    this.port = port;
  }

  public boolean checkHostLogin() {
    return getHostLogin(host, port, 3000);
  }

  public boolean getHostLogin(String host, Integer port, Integer timeout) {
    boolean login = false;
    try {
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(host, port), timeout);
      login = true;
    } catch (Exception ex) {
      log.error("连接失败:" + ex.getMessage());
    }
    return login;
  }

}
