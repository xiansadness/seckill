package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SuccessKilled;

public interface SuccessKilledDao {

    /**
     * 插入购买明细（相当于生成某秒杀商品的一条购买记录）
     * 注意：因为success_killed这张表是以（seckill_id,user_phone）为联合主键的，
     *          所以该操作可以防止同一用户多次购买同一秒杀商品.
     * @param seckillId：秒杀商品的id
     * @param userPhone：用户的手机号码
     * @return 返回值表示插入操作所影响的行数，当只插入一条信息时，该插入操作只影响了一行，因此返回值为1,
     *          故可以认为返回值>1时，插入成功，反之，插入失败
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * 根据id和用户电话号码来查询秒杀订单
     * @param seckillId：秒杀商品的id
     * @param userPhone：用户的电话号码
     * @return 返回时，会一起返回与订单相关的秒杀商品的相应信息(因为在SuccessKilled实体类中定义了Seckill属性)
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);
}
