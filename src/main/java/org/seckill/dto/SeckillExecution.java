package org.seckill.dto;

import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;

/**
 * 封装执行秒杀后的结果：是否秒杀成功
 *  秒杀成功返回信息：秒杀商品的id，代表秒杀成功的状态码，该状态码标识的信息，秒杀成功的对象（可以看成数据库中对应的订单记录）
 *                  调用第一个构造方法
 *  秒杀失败返回信息：秒杀商品的id，代表秒杀失败的状态码，该状态码标识的信息
 *                  调用第二个构造方法
 */
public class SeckillExecution {

    private long seckillId;//秒杀商品的id

    private int state;//秒杀执行结果的状态

    private String stateInfo;//相应状态所对应的标识信息

    private SuccessKilled successKilled;//若秒杀执行成功，则还需要传递秒杀成功的对象

    //利用不同的构造方法，为不同的场景初始化对象

    //秒杀成功时返回所有信息
    public SeckillExecution(long seckillId, SeckillStatEnum seckillStatEnum, SuccessKilled successKilled) {
        this.seckillId = seckillId;
        this.state = seckillStatEnum.getState();
        this.stateInfo = seckillStatEnum.getInfo();
        this.successKilled = successKilled;
    }

    //秒杀失败时返回的信息
    public SeckillExecution(long seckillId, SeckillStatEnum seckillStatEnum) {
        this.seckillId = seckillId;
        this.state = seckillStatEnum.getState();
        this.stateInfo = seckillStatEnum.getInfo();
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public SuccessKilled getSuccessKilled() {
        return successKilled;
    }

    public void setSuccessKilled(SuccessKilled successKilled) {
        this.successKilled = successKilled;
    }

    @Override
    public String toString() {
        return "SeckillExecution{" +
                "seckillId=" + seckillId +
                ", state=" + state +
                ", stateInfo='" + stateInfo + '\'' +
                ", successKilled=" + successKilled +
                '}';
    }
}
