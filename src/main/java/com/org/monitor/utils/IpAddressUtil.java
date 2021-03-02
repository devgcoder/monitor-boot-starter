package com.org.monitor.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpAddressUtil {

  private static Logger logger = LoggerFactory.getLogger(IpAddressUtil.class);

  public final static String getIp(HttpServletRequest request) {
    String ip = null;
    try {
      ip = IpAddressUtil.getIpAddress(request);
    } catch (Exception e) {
      logger.info(e.getMessage());
    }
    String ipTwo = IpAddressUtil.getRemoteHost(request);
    String ipThree = IpAddressUtil.getIpHost(request);
    if (null == ip || ip.equals("")) {
      ip = ipTwo;
    }
    if (null == ip || ip.equals("")) {
      ip = ipThree;
    }
    return ip;
  }

  public final static String getIpAddress(HttpServletRequest request) throws Exception {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("Proxy-Client-IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("WL-Proxy-Client-IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("HTTP_CLIENT_IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getRemoteAddr();
      }
    } else if (ip.length() > 15) {
      String[] ips = ip.split(",");
      for (int index = 0; index < ips.length; index++) {
        String strIp = (String) ips[index];
        if (!("unknown".equalsIgnoreCase(strIp))) {
          ip = strIp;
          break;
        }
      }
    }
    return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
  }

  public final static String getRemoteHost(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
  }

  public final static String getIpHost(HttpServletRequest request) {
    String Xip = request.getHeader("X-Real-IP");
    String XFor = request.getHeader("X-Forwarded-For");
    if (null != XFor && !XFor.equals("") && !"unKnown".equalsIgnoreCase(XFor)) {
      int index = XFor.indexOf(",");
      if (index != -1) {
        return XFor.substring(0, index);
      } else {
        return XFor;
      }
    }
    XFor = Xip;
    if (null != XFor && !XFor.equals("") && !"unKnown".equalsIgnoreCase(XFor)) {
      return XFor;
    }
    if (null == XFor || XFor.equals("") || "unknown".equalsIgnoreCase(XFor)) {
      XFor = request.getHeader("Proxy-Client-IP");
    }
    if (null == XFor || XFor.equals("") || "unknown".equalsIgnoreCase(XFor)) {
      XFor = request.getHeader("WL-Proxy-Client-IP");
    }
    if (null == XFor || XFor.equals("") || "unknown".equalsIgnoreCase(XFor)) {
      XFor = request.getHeader("HTTP_CLIENT_IP");
    }
    if (null == XFor || XFor.equals("") || "unknown".equalsIgnoreCase(XFor)) {
      XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (null == XFor || XFor.equals("") || "unknown".equalsIgnoreCase(XFor)) {
      XFor = request.getRemoteAddr();
    }
    return XFor.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : XFor;
  }

  public final static String getIpAddr(HttpServletRequest request) {
    String ipAddress = request.getHeader("x-forwarded-for");
    if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
      if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
        InetAddress inet = null;
        try {
          inet = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
          e.printStackTrace();
        }
        ipAddress = inet.getHostAddress();
      }
    }
    if (ipAddress != null && ipAddress.length() > 15) {
      if (ipAddress.indexOf(",") > 0) {
        ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
      }
    }
    return ipAddress;
  }

  /**
   * 获取当前网络ip
   *
   * @param request
   * @return
   */
  public static String getIpAddress2(HttpServletRequest request) {
    String ipAddress = request.getHeader("x-forwarded-for");
    if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("X-Real-IP");
    }
    if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
      if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
        //根据网卡取本机配置的IP
        InetAddress inet = null;
        try {
          inet = InetAddress.getLocalHost();
          ipAddress = inet.getHostAddress();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
    if (ipAddress != null && ipAddress.length() > 15) { //"***.***.***.***".length() = 15
      if (ipAddress.indexOf(",") > 0) {
        ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
      }
    }
    return ipAddress;
  }

  /**
   * @return
   * @throws MalformedObjectNameException 获取当前机器的端口号
   */
  public static String getLocalPort() throws Exception {
    MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
    Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
        Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
    String port = objectNames.iterator().next().getKeyProperty("port");
    return port;
  }

  /**
   * @return 获取当前机器的IP
   */
  public static String getLocalIp() {
    InetAddress addr = null;
    try {
      addr = InetAddress.getLocalHost();
    } catch (Exception e) {
      e.printStackTrace();
    }

    byte[] ipAddr = addr.getAddress();
    String ipAddrStr = "";
    for (int i = 0; i < ipAddr.length; i++) {
      if (i > 0) {
        ipAddrStr += ".";
      }
      ipAddrStr += ipAddr[i] & 0xFF;
    }
    return ipAddrStr;
  }
}
