package com.github.devgcoder.model;

public class JsonModel<T> {

  public static <T> JsonModel<T> newSuccess(T data) {
    return newSuccess(data, "操作成功");
  }

  public static <T> JsonModel<T> newSuccess(T data, long total) {
    return new JsonModel<T>().setData(data).setCode(0).setTotal(total).setMessage("查询成功");
  }

  public static <T> JsonModel<T> newSuccess(T data, String message) {
    return new JsonModel<T>().setMessage(message).setSuccess(true).setData(data).setCode(1);
  }

  public static <T> JsonModel<T> newFail() {
    return newFail("操作失败");
  }

  public static <T> JsonModel<T> newFail(String message) {
    return new JsonModel<T>().setMessage(message).setSuccess(false).setData(null).setCode(0);
  }

  private boolean success = false;
  private String message = "";
  private long total;
  private T data;
  private int code;

  public boolean isSuccess() {
    return success;
  }

  public JsonModel<T> setSuccess(boolean success) {
    this.success = success;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public JsonModel<T> setMessage(String message) {
    this.message = message;
    return this;
  }

  public T getData() {
    return data;
  }

  public JsonModel<T> setData(T data) {
    this.data = data;
    return this;
  }

  public int getCode() {
    return code;
  }

  public JsonModel<T> setCode(int code) {
    this.code = code;
    return this;
  }

  public JsonModel<T> setTotal(long total) {
    this.total = total;
    return this;
  }

  public long getTotal() {
    return total;
  }
}
