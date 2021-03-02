package com.org.monitor.utils;

import com.org.monitor.model.MailConfig;
import com.sun.mail.util.MailSSLSocketFactory;
import java.security.GeneralSecurityException;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailUtil {

  private static final String timeOut = "30000";

  private static final Long limitTime = 5 * 60 * 1000L; // 邮件发送限流,默认5分钟内如果发过邮件则不再发送

  private static final String mailAuth = "true";

  private static final String contentType = "text/html;charset=UTF-8";

  private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);


  public static void sendMail(MailConfig mailConfig, String mailKey, String mailTittle, String mailText) {
    if (null == mailConfig) {
      logger.error("mailConfig can not be null or empty");
      return;
    }
    String mailHost = mailConfig.getMailHost();
    if (null == mailHost || mailHost.equals("")) {
      logger.error("mailHost can not be null or empty");
      return;
    }
    String mailSmtp = mailConfig.getMailSmtp();
    if (null == mailSmtp || mailSmtp.equals("")) {
      logger.error("mailSmtp can not be null or empty");
      return;
    }
    String mailFrom = mailConfig.getMailFrom();
    if (null == mailFrom || mailFrom.equals("")) {
      logger.error("mailFrom can not be null or empty");
      return;
    }
    String mailUsername = mailConfig.getMailUsername();
    if (null == mailUsername || mailUsername.equals("")) {
      logger.error("mailUsername can not be null or empty");
      return;
    }
    String mailPassword = mailConfig.getMailPassword();
    if (null == mailPassword || mailPassword.equals("")) {
      logger.error("mailPassword can not be null or empty");
      return;
    }
    String mailRecipient = mailConfig.getMailRecipient();
    if (null == mailRecipient || mailRecipient.equals("")) {
      logger.error("mailRecipient can not be null or empty");
      return;
    }
    Long mailLimitTime = mailConfig.getMailLimitTime();
    if (null == mailLimitTime || mailLimitTime <= 0) {
      mailLimitTime = limitTime;
    }
    if (!canSendMail(mailKey, mailLimitTime)) {
      logger.error("mailKey " + mailKey + " send too frequently");
      return;
    }
    Properties prop = new Properties();
    prop.setProperty("mail.host", mailHost);
    prop.setProperty("mail.transport.protocol", mailSmtp);
    prop.setProperty("mail.smtp.auth", mailAuth);
    String mailPort = mailConfig.getMailPort();
    if (null != mailPort && !mailPort.equals("")) {
      // 使用SSL，企业邮箱必需！
      // 开启安全协议
      MailSSLSocketFactory sf = null;
      try {
        sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
      } catch (GeneralSecurityException e1) {
        e1.printStackTrace();
      }
      prop.put("mail.smtp.ssl.enable", "true");
      prop.put("mail.smtp.ssl.socketFactory", sf);
    } else {
      prop.setProperty("mail.smtp.connectiontimeout", timeOut);
      prop.setProperty("mail.smtp.timeout", timeOut);
      prop.setProperty("mail.smtp.writetimeout", timeOut);
      prop.setProperty("mail.smtp.starttls.enable", mailAuth);
      prop.setProperty("mail.smtp.starttls.required", mailAuth);
    }
    Transport ts = null;
    try {
// 1、创建session
      Session session = Session.getInstance(prop);
      // 开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
      session.setDebug(false);
      // 2、通过session得到transport对象
      ts = session.getTransport();
      // 3、连上邮件服务器，需要发件人提供邮箱的用户名和密码进行验证
      ts.connect(mailHost, mailUsername, mailPassword);// 需要修改
      // 4、创建邮件
      Message message = createSimpleMail(session, mailFrom, mailRecipient, mailTittle, mailText);
      // 5、发送邮件
      ts.sendMessage(message, message.getAllRecipients());
    } catch (Exception ex) {
      logger.error("send mail error", ex);
    } finally {
      if (null != ts) {
        try {
          ts.close();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  /**
   * @Method: createSimpleMail
   * @Description: 创建一封只包含文本的邮件
   */
  public static MimeMessage createSimpleMail(Session session, String mailfrom, String mailTo, String mailTittle,
      String mailText) throws Exception {

    if (null == mailTo || mailTo.equals("")) {
      throw new Exception("mailTo can not be null or empty");
    }
    String[] mailToArray = mailTo.split(",");
    // 创建邮件对象
    MimeMessage message = new MimeMessage(session);
    // 指明邮件的发件人
    message.setFrom(new InternetAddress(mailfrom));
    // 指明邮件的收件人，现在发件人和收件人是一样的，那就是自己给自己发
//    message.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
    Address[] mailToAddress = new Address[mailToArray.length];
    for (int i = 0; i < mailToArray.length; i++) {
      mailToAddress[i] = new InternetAddress(mailToArray[i]);
    }
    message.setRecipients(Message.RecipientType.TO, mailToAddress);
    // 邮件的标题
    message.setSubject(mailTittle);
    // 邮件的文本内容
    message.setContent(mailText, contentType);
    // 返回创建好的邮件对象
    return message;
  }

  private static synchronized boolean canSendMail(String mailKey, Long mailLimitTime) {
    Long nowTime = System.currentTimeMillis();
    Long lastTime = MonitorMomeryUtil.mailCanSendMap.get(mailKey);
    if (null == lastTime) {
      MonitorMomeryUtil.mailCanSendMap.put(mailKey, nowTime);
      return true;
    }
    Long leftTime = nowTime - lastTime;
    if (leftTime > mailLimitTime) {
      MonitorMomeryUtil.mailCanSendMap.put(mailKey, nowTime);
      return true;
    }
    return false;
  }
}
