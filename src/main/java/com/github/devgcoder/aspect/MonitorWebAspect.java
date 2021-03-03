package com.github.devgcoder.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.devgcoder.anno.MonitorController;
import com.github.devgcoder.anno.MonitorMapper;
import com.github.devgcoder.model.BasicParams;
import com.github.devgcoder.utils.CommonEnum.messageType;
import com.github.devgcoder.utils.CommonEnum.resultType;
import com.github.devgcoder.utils.IpAddressUtil;
import com.github.devgcoder.utils.MonitorUtil;
import com.github.devgcoder.model.MonitorConfig;
import com.github.devgcoder.model.CommonParams.MonitorModelType;
import com.github.devgcoder.model.MonitorMessage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
public class MonitorWebAspect implements Ordered {

	private final Logger logger = LoggerFactory.getLogger(MonitorWebAspect.class);

	private static final AtomicLong messageNumber = new AtomicLong(0);

	private MonitorConfig monitorConfig;

	private List<String> nonClassMethodlist = null;

	public MonitorWebAspect(MonitorConfig monitorConfig) {
		this.monitorConfig = monitorConfig;
		if (null != monitorConfig && null != monitorConfig.getMonitorNonClassMethod()
				&& monitorConfig.getMonitorNonClassMethod().length > 0) {
			nonClassMethodlist = Arrays.asList(monitorConfig.getMonitorNonClassMethod());
		}
	}

	@Pointcut("execution(* com..*Controller.*(..))")
	public void monitorController() {
	}

	@Pointcut("execution(* com..*Mapper.*(..))")
	public void monitorMappper() {
	}

	@Around(value = "monitorController()")
	public Object monitorController(ProceedingJoinPoint joinPoint) throws Throwable {
		return controllerObject(joinPoint);
	}

	@Around(value = "monitorMappper()")
	public Object monitorMappper(ProceedingJoinPoint joinPoint) throws Throwable {
		return mapperObject(joinPoint);
	}


	private Object controllerObject(ProceedingJoinPoint joinPoint) throws Throwable {
		String methodName = joinPoint.getSignature().getName();//目标方法名
		String clazzName = joinPoint.getSignature().getDeclaringTypeName();//目标方法所属类的类名
		String classMethod = clazzName + "." + methodName;
		if (null != nonClassMethodlist && (nonClassMethodlist.contains(clazzName) || nonClassMethodlist.contains(classMethod))) {
			return joinPoint.proceed();
		}
		MonitorController classRequestController = joinPoint.getTarget().getClass().getAnnotation(MonitorController.class);
		MonitorController methodRequestController = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(MonitorController.class);
		Boolean enableMonitorController = monitorConfig.getEnableMonitorController();
		if ((null == enableMonitorController || !enableMonitorController) && methodRequestController == null && classRequestController == null) {
			return joinPoint.proceed();
		}
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		String requestUrl = request.getRequestURL().toString();
		String requestMethod = request.getMethod();
		String requestIp = IpAddressUtil.getIp(request);
		String requestParams = monitorConfig.getRequestParams();
		Map<String, Object> theRequestParams = new HashMap<>(4);
		if (!MonitorUtil.isNullOrEmpty(requestParams)) {
			// 下面两个数组中，参数值和参数名的个数和位置是一一对应的。
			Object[] args = joinPoint.getArgs(); // 参数值
			String[] argNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames(); // 参数名
			if (null != argNames && argNames.length > 0) {
				for (int i = 0; i < argNames.length; i++) {
          /*if (requestParams.indexOf(argNames[i]) >= 0) {
            theRequestParams.put(argNames[i], args[i]);
          }*/
					Object object = args[i];
					if (MonitorUtil.isNullOrEmpty(object)) {
						continue;
					}
					if (object instanceof String || object instanceof Short || object instanceof Long || object instanceof Byte
							|| object instanceof Integer || object instanceof Double || object instanceof Float) {
						if (requestParams.indexOf(argNames[i]) >= 0) {
							theRequestParams.put(argNames[i], args[i]);
						}
					} else if (object instanceof Map) {
						Map<String, Object> resultMap = JSONObject.parseObject(JSONObject.toJSONString(object));
						if (null != resultMap && !resultMap.isEmpty()) {
							for (String key : resultMap.keySet()) {
								if (requestParams.indexOf(key) >= 0) {
									theRequestParams.put(key, resultMap.get(key));
								}
							}
						}
					} else if (object instanceof BasicParams) {
						try {
							Field[] fields = object.getClass().getDeclaredFields();
							if (null != fields && fields.length > 0) {
								for (Field field : fields) {
									field.setAccessible(true);
									String fieldName = field.getName();
									if (requestParams.indexOf(fieldName) >= 0) {
										char[] ch = fieldName.toCharArray();
										if (ch[0] >= 'a' && ch[0] <= 'z') {
											ch[0] = (char) (ch[0] - 32);
										}
										String theMethodName = "get" + new String(ch);
										Method method = object.getClass().getDeclaredMethod(theMethodName, null);
										Object resultObject = method.invoke(object, null);
										theRequestParams.put(fieldName, resultObject);
									}
								}
							}
						} catch (Exception ex) {
							logger.error("get BasicParams value error", ex);
						}
					} else if (object instanceof HttpServletRequest) {
						HttpServletRequest requestObject = (HttpServletRequest) object;
						if (null != requestObject) {
							Map<String, String[]> parameterMap = requestObject.getParameterMap();
							if (null != parameterMap && !parameterMap.isEmpty()) {
								String[] requestKeys = requestParams.split(",");
								for (String requestKey : requestKeys) {
									if (!parameterMap.containsKey(requestKey)) {
										continue;
									}
									String[] requestValue = parameterMap.get(requestKey);
									if (null != requestValue && requestValue.length > 0) {
										theRequestParams.put(requestKey, requestValue[0]);
									}
								}
							}
						}
					}
         /* else {
            try {
              String contentType = request.getContentType();
              JSONObject jsonObject = new JSONObject();
              if (null != contentType && contentType.contains("multipart/form-data")) {
                ParameterRequestWrapper httpServletRequestWrapper = new ParameterRequestWrapper(request);
                Map<String, String[]> parameterMap = httpServletRequestWrapper.getParameterMap();
                if (null != parameterMap && !parameterMap.isEmpty()) {
                  for (String key : parameterMap.keySet()) {
                    jsonObject.put(key, parameterMap.get(key) == null ? null : parameterMap.get(key).toString());
                  }
                }
              } else if (null != contentType && contentType.contains("application/json")) {
                jsonObject = RequestBodyParameterWrapper.getInputStr(request);
              }
              String[] params = requestParams.split(",");
              for (String param : params) {
                theRequestParams.put(param, jsonObject.get(param));
              }
            } catch (Exception ex) {
              logger.error("monitor getRequestParam exception", ex);
            }
          }*/

				}
			}
		}
		// 记录下请求内容
//    logger.info("URL : " + request.getRequestURL().toString() + " ,HTTP_METHOD : " + request.getMethod() + " , IP : " + IpAddressUtil.getIp(request));
		String startTime = MonitorUtil.getStartTime();
		long t1 = System.currentTimeMillis();
		try {
			Object object = joinPoint.proceed(); //执行目标方法
			long t2 = System.currentTimeMillis();
			String modelName = monitorConfig == null ? null : monitorConfig.getModelName();
			long costTime = t2 - t1;
			MonitorMessage monitorMessage = getMonitorMessage(messageType.MsgController.getKey(), classMethod, requestUrl, requestIp, costTime,
					startTime, resultType.INFO.getKey(), modelName, theRequestParams);
			String infoMsg =
					"devg-monitor execute messageKey:" + monitorMessage.getMessageKey() + ",method:" + methodName + ",costTime:" + costTime + "ms";
			logger.info(infoMsg);
			String sendMqMsg = JSON.toJSONString(monitorMessage);
			if (null != monitorConfig && null != monitorConfig.getRabbitConfig()) {
				MonitorUtil.sendMessage(monitorConfig, monitorConfig.getRabbitConfig().getRabbitMonitorMessage(), sendMqMsg);
			}
			return object;
		} catch (Throwable throwable) {
			long t2 = System.currentTimeMillis();
			String modelName = monitorConfig == null ? null : monitorConfig.getModelName();
			MonitorMessage monitorMessage = getMonitorMessage(messageType.MsgController.getKey(), classMethod, requestUrl, requestIp, (t2 - t1),
					startTime, resultType.ERROR.getKey(), modelName, theRequestParams);
			String errorMsg = "devg-monitor execute messageKey:" + monitorMessage.getMessageKey()
					+ ",method:" + methodName + ",errorMessage:" + throwable.getMessage();
			logger.error(errorMsg);
			String sendMqMsg = JSON.toJSONString(monitorMessage);
			if (null != monitorConfig && null != monitorConfig.getRabbitConfig()) {
				MonitorUtil.sendMessage(monitorConfig, monitorConfig.getRabbitConfig().getRabbitMonitorMessage(), sendMqMsg);
			}
			throw throwable;
		}
	}

	private Object mapperObject(ProceedingJoinPoint joinPoint) throws Throwable {
		String methodName = joinPoint.getSignature().getName();//目标方法名
		String clazzName = joinPoint.getSignature().getDeclaringTypeName();//目标方法所属类的类名
		String classMethod = clazzName + "." + methodName;
		MonitorMapper classRequestMapper = joinPoint.getTarget().getClass().getAnnotation(MonitorMapper.class);
		MonitorMapper methodRequestMapper = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(MonitorMapper.class);
		String startTime = MonitorUtil.getStartTime();
		long t1 = System.currentTimeMillis();
		try {
			Object object = joinPoint.proceed(); //执行目标方法
			long t2 = System.currentTimeMillis();
			if (null != nonClassMethodlist && (nonClassMethodlist.contains(clazzName) || nonClassMethodlist.contains(classMethod))) {
				return object;
			}
			Boolean enableMonitorMapper = monitorConfig.getEnableMonitorMapper();
			if ((null == enableMonitorMapper || !enableMonitorMapper) && methodRequestMapper == null && classRequestMapper == null) {
				return object;
			}
			String modelName = monitorConfig.getModelName();
			long costTime = t2 - t1;
			MonitorMessage monitorMessage = getMonitorMessage(messageType.MsgMapper.getKey(), classMethod, null, null, costTime,
					startTime, resultType.INFO.getKey(), modelName, null);
			String infoMsg =
					"devg-monitor execute messageKey:" + monitorMessage.getMessageKey() + ",method:" + methodName + ",costTime:" + costTime + "ms";
			logger.info(infoMsg);
			String sendMqMsg = JSON.toJSONString(monitorMessage);
			if (null != monitorConfig && null != monitorConfig.getRabbitConfig()) {
				MonitorUtil.sendMessage(monitorConfig, monitorConfig.getRabbitConfig().getRabbitMonitorMessage(), sendMqMsg);
			}
			return object;
		} catch (Throwable throwable) {
			long t2 = System.currentTimeMillis();
			String modelName = monitorConfig == null ? null : monitorConfig.getModelName();
			MonitorMessage monitorMessage = getMonitorMessage(messageType.MsgMapper.getKey(), classMethod, null, null, (t2 - t1),
					startTime, resultType.ERROR.getKey(), modelName, null);
			String errorMsg = "devg-monitor execute messageKey:" + monitorMessage.getMessageKey()
					+ ",method:" + methodName + ",errorMessage:" + throwable.getMessage();
			logger.error(errorMsg);
			String sendMqMsg = JSON.toJSONString(monitorMessage);
			if (null != monitorConfig && null != monitorConfig.getRabbitConfig()) {
				MonitorUtil.sendMessage(monitorConfig, monitorConfig.getRabbitConfig().getRabbitMonitorMessage(), sendMqMsg);
			}
			throw throwable;
		}
	}

	private MonitorMessage getMonitorMessage(int MessageType, String classMethod, String requestUrl, String requestIp, long costTime, String startTime,
			String resultType, String modelName, Map<String, Object> resultParams) {
		MonitorMessage monitorMessage = new MonitorMessage();
		monitorMessage.setMessageType(MessageType);
		monitorMessage.setClassMethod(classMethod);
		if (null != requestUrl && !requestUrl.equals("")) {
			monitorMessage.setRequestUrl(requestUrl);
		}
		if (null != requestIp && !requestIp.equals("")) {
			monitorMessage.setRequestIp(requestIp);
		}
		monitorMessage.setCostTime(costTime);
		monitorMessage.setStartTime(startTime);
		monitorMessage.setResultType(resultType);
		if (null != modelName && !modelName.equals("")) {
			monitorMessage.setModelName(modelName);
		}
		if (null != resultParams && !resultParams.isEmpty()) {
			monitorMessage.setRequestParams(resultParams);
		}
		monitorMessage.setMessageModelType(MonitorModelType.MESSAGE.getKey());
		Long msgNumber = messageNumber.incrementAndGet();
		String localIp = IpAddressUtil.getLocalIp();
		String nowTime = MonitorUtil.localDateTimeFormat(LocalDateTime.now(), MonitorUtil.FORMAT_PATTERN1);
		String messageKey = nowTime + MonitorUtil.horizontalSplit + localIp + MonitorUtil.horizontalSplit + msgNumber;
		monitorMessage.setMessageKey(messageKey);
		return monitorMessage;
	}

	@Override
	public int getOrder() {
		return Integer.MAX_VALUE - 99;
	}
}
