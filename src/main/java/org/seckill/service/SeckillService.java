package org.seckill.service;


import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

import java.util.List;

public interface SeckillService {

    /**
     * 查询所有秒杀商品记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 通过商品的秒杀id来查询秒杀商品信息
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 在秒杀开启时输出秒杀接口的地址（url?），否则输出系统时间和秒杀时间
     * 该方法的主要用于service与web的交互？
     * @param seckillId 秒杀商品的id
     * @return 根据对应的状态返回对应的状态实体
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     *
     * 注意点1：该操作具备事务性，相当于一个update操作和一个insert操作需要一起执行，两者一起成功，一起失败
     * 注意点2：方法执行返回时，事务才被提交或者回滚（回归初始状态，不做任何操作），
     *                  而要使方法返回，有两种情况：遇到return语句；程序抛出异常
     * 注意点3：该操作有可能失败（事务回滚），也有可能成功（事务提交），并且失败的原因可能有多种：
     *                  如一个用户重复秒杀同一个商品，地址被篡改等，这里不同的失败原因可以通过抛出不同的异常来处理
     *
     * @param seckillId 秒杀商品的id
     * @param userPhone 用户手机号码
     * @param md5 md5加密后的值，主要是对秒杀商品的url加密?防止篡改?
     * @return
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException;

    /**
     * 执行秒杀操作 by 存储过程
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5);


}
