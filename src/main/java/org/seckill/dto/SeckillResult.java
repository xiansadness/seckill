package org.seckill.dto;

//所有ajax请求返回的类型，封装json结果
public class SeckillResult<T> {

    private boolean success;//请求是否成功

    private T data;//封装成功时返回的数据？

    private String error;//封装错误时返回的错误信息？

    public SeckillResult(boolean success, T data) {//成功时返回的信息
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {//失败时返回的信息
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
