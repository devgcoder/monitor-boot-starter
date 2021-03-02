package com.org.monitor.web.controller;

import com.org.monitor.utils.MonitorUtil;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;

@Controller
public class MonitorBasicController {

  private static final Logger logger = LoggerFactory.getLogger(MonitorBasicController.class);

  @Autowired
  private HttpServletRequest request;

/*  @ModelAttribute("hasLogin")
  public Boolean hasLogin() {
    Boolean loginType = false; // 初始化未登录
    HttpSession session = request.getSession();
    if (null != session && session.getAttribute(MonitorUtil.USERINFO) != null) {
      loginType = true;
    }
    return loginType;
  }*/

  public Boolean hasLogin() {
    try {
      String token = MonitorUtil.getCookieValue(request, MonitorUtil.TOKEN);
      if (MonitorUtil.isNullOrEmpty(token)) {
        return false;
      }
      final byte[] authorizedByte = MonitorUtil.authorizedUser.getBytes("UTF-8");
      if (MonitorUtil.equals(token, DigestUtils.md5DigestAsHex(authorizedByte))) {
        return true;
      }
    } catch (Exception ex) {
      logger.error("hasLogin error", ex);
    }
    return false;
  }

}
