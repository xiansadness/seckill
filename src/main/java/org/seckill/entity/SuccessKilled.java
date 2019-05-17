package org.seckill.entity;

import java.util.Date;

/**
 * 该类可以类比为秒杀阶段下达的订单
 */

public class SuccessKilled {

    private long seckillId;//秒杀商品Id

    private long userPhone;//用户手机号，用来标识不同的用户

    private short state;//秒杀订单状态，-1：无效，0：成功，1：已付款，2:已发货，默认为-1

    private Date createTime;//秒杀订单的创建时间

    private Seckill seckill;
    //秒杀时，有多个秒杀商品，一个用户只可以购买一个同id的秒杀商品，但一个用户可以同时购买多个不同id的秒杀商品

    public Seckill getSeckill() {
        return seckill;
    }

    public void setSeckill(Seckill seckill) {
        this.seckill = seckill;
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public long getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(long userPhone) {
        this.userPhone = userPhone;
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "SuccessKilled{" +
                "seckillId=" + seckillId +
                ", userPhone=" + userPhone +
                ", state=" + state +
                ", createTime=" + createTime +
                '}';
    }
}
