package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {

    //日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //如果只是单纯的用MD5加密，用户可能会利用某些手段获得加密后的值即MD5值
    //而加入一个混淆字符串的盐度salt，可以避免用户猜出我们的md5值，该值可以任意给，越复杂越好
    private final String salt = "qmkwjmlqccwadrdhxn520";

    //业务层调用Dao层，所以这里需要创建Dao层对象，而Dao层的实现则交给了Spring容器
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;


    /**
     * 获取所有的秒杀商品
     * @return
     */


    public List<Seckill> getSeckillList() {

        return seckillDao.queryAll(0, 5);
    }

    /**
     * 获取指定id的秒杀商品
     * @param seckillId
     * @return
     */

    public Seckill getById(long seckillId) {

        return seckillDao.queryById(seckillId);
    }

    /**
     * 为指定秒杀id进行MD5加密
     *
     * @param seckillId
     * @return
     */
    private String getMD5(long seckillId) {//为秒杀商品的id加密？

        String base = seckillId + "/" + salt;
        //spring的工具类
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;

    }

    /**
     * 判断是否暴露秒杀地址
     * @param seckillId 秒杀商品的id
     * @return
     */

    //这里利用 redis缓存 来对秒杀地址暴露接口进行优化
    //未优化之前：直接从数据库中取数据
    //优化后：先从缓存中取数据，如果缓存中没有相应数据，再从数据库中取数据，并将取到的数据添加到缓存中

    //为什么可以将秒杀地址用缓存机制来优化？
    //每一个秒杀商品的秒杀地址对不同用户都是相同的，
    //当多个用户同时去抢购同一秒杀商品时，如果每次都需要从数据中获取秒杀商品的地址信息，无疑会增加数据库的压力。
    //先将秒杀地址缓存到redis中，可以减少对数据库的访问，可以令数据库主要去应对执行秒杀的操作即update和insert操作

    /**
     *一般工程师在优化时，可能会将缓存写进service中，其实缓存本质上也是与数据库相关的操作
     * 故最好将缓存封装成方法写在dao层，service层来调用
     *
     * 缓存逻辑
     *
     * get from cache
     * if null
     *    get db
     *    if !null
     *       put cache
     *       其他后续逻辑
     */

    public Exposer exportSeckillUrl(long seckillId) {
        //1.先从redis中取
        Seckill seckill=redisDao.getSeckill(seckillId);

        //2.判断是否为空，为空则从数据库中取，不为空则执行后续逻辑
        if(seckill == null){
            seckill=seckillDao.queryById(seckillId);
            if(seckill == null){//从数据库中获取也为空，则说明秒杀商品不存在
                return new Exposer(false, seckillId);
            }
            redisDao.putSeckill(seckill);//将从数据库中查询出来的对象放入缓存中
        }
        //执行后续逻辑
        Date startTime = seckill.getStartTime();//获取秒杀商品的秒杀开始时间
        Date endTime = seckill.getEndTime();//获取秒杀商品的秒杀结束时间
        Date nowTime = new Date();//获取系统的当前时间
        if (nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()) {//将所有时间都转化为毫秒(利用getTime转化)来比较
            //当前时间不符合秒杀要求的时候，不能暴露秒杀地址
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());

        }

        //符合所有要求(即秒杀商品存在且当前时间符合秒杀要求)时，暴露秒杀地址
        //转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);//这里注意一下同一个类中，不同方法之间的调用，可以省略掉类名或者this
        return new Exposer(true, md5, seckillId);

    }

    /**未优化的exportSeckillUrl方法
    public Exposer exportSeckillUrl(long seckillId) {

        //1.首先，根据id获取秒杀商品
        Seckill seckill = seckillDao.queryById(seckillId);

        //2.判断
        if (seckill == null) {//获取的秒杀商品不存在
            return new Exposer(false, seckillId);
        }

        Date startTime = seckill.getStartTime();//获取秒杀商品的秒杀开始时间
        Date endTime = seckill.getEndTime();//获取秒杀商品的秒杀结束时间
        Date nowTime = new Date();//获取系统的当前时间
        if (nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()) {//将所有时间都转化为毫秒(利用getTime转化)来比较
            //当前时间不符合秒杀要求的时候，不能暴露秒杀地址
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());

        }

        //符合所有要求(即秒杀商品存在且当前时间符合秒杀要求)时，暴露秒杀地址
        //转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);//这里注意一下同一个类中，不同方法之间的调用，可以省略掉类名或者this
        return new Exposer(true, md5, seckillId);

    }**/

    /**
     * 执行秒杀
     * @param seckillId 秒杀商品的id
     * @param userPhone 用户手机号码
     * @param md5 md5加密后的值，主要是对秒杀商品的id加密?防止篡改?
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    /**
     * 使用注解控制事务方法的优点：
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短（对高并发的系统特别重要），不要穿插其他网络操作RPC/HTTP请求（如缓存）或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
     */

    /**
     *优化执行秒杀接口
     * 1.先通过改变update和insert的执行顺序来进行简单优化
     *      优化前：先update再insert
     *      优化后：先insert再update,优化后事务持有行级锁的时间被缩小一半
     *
     */
    @Transactional//标注为事务方法
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {

        //1.首先判断md5值是否被篡改
        if (md5 == null || !md5.equals(getMD5(seckillId))) {//用户传递过来的md5为空或者与传递过来的id经过md5加密后的值不相等，均会报出异常
            throw new SeckillException("seckill data rewrite");
        }

        //2.执行秒杀逻辑：减库存(更新)+记录购买行为(插入)
        Date nowTime = new Date();//系统当前时间可以认为是用户想要执行秒杀的时间

        /**
         * 将 减库存 插入购买明细 提交
         * 改为 插入购买明细 减库存 提交
         * 降低了网络延迟和GC(垃圾处理机制),同时减少了rowLock(行级锁)的持有时间
         *先进行插入操作，会过滤掉重复秒杀的行为，减少了数据库的压力
         */
        try {
            //1.先执行插入操作
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insertCount <= 0) {//插入失败
                //重复秒杀
                throw new RepeatKillException("seckill repeated");//事务回滚rollback
            } else {
                //2.插入成功，再执行更新操作
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);//更新操作
                if (updateCount <= 0) {//更新操作执行失败，即没有更新到记录，此时则秒杀结束
                    throw new SeckillCloseException("seckill is closed");//事务回滚rollback
                } else {//更新操作执行成功，说明秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);//秒杀成功后返回相应的秒杀明细
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);//事务提交commit
                }
            }
        } catch (SeckillCloseException e1) {//先用小范围异常来捕获，若小范围异常捕获不了，则用大范围异常捕获
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //将编译期异常转化为运行期异常
            //因为spring的声明式事务只能处理运行期异常？
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }

    /**
     * 利用存储过程来执行秒杀操作
     * 将update和insert放在mysql本地执行，该方法接收执行后的结果，
     * 并根据结果来判断秒杀成功与否
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {

        if(md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId, SeckillStatEnum.DATE_REWRITE);
        }

        Date killTime = new Date();

        Map<String, Object> map= new HashMap<String, Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);

        //执行存储过程，result被赋值
        try {
            seckillDao.killByProcedure(map);
            //获取result,如果没有,则为-2
            int result = MapUtils.getInteger(map, "result",-2);

            if(result == 1){//秒杀成功
                SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
            }else{
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
        }
    }

    /**未优化的executeSeckill方法
    @Transactional//标注为事务方法
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {

        //1.首先判断md5值是否被篡改
        if (md5 == null || !md5.equals(getMD5(seckillId))) {//用户传递过来的md5为空或者与传递过来的id经过md5加密后的值不相等，均会报出异常
            throw new SeckillException("seckill data rewrite");
        }

        //2.执行秒杀逻辑：减库存(更新)+记录购买行为(插入)
        Date nowTime = new Date();//系统当前时间可以认为是用户想要执行秒杀的时间

        try {
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);//更新操作
            if (updateCount <= 0) {//更新操作执行失败，即没有更新到记录，此时则秒杀结束
                throw new SeckillCloseException("seckill is closed");//事务回滚rollback
            } else {//更新操作执行成功，接着执行插入操作
                int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
                if (insertCount <= 0) {//插入失败
                    //重复秒杀
                    throw new RepeatKillException("seckill repeated");//事务回滚rollback
                } else {
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);//秒杀成功后返回相应的秒杀明细
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);//事务提交commit
                }
            }
        } catch (SeckillCloseException e1) {//先用小范围异常来捕获，若小范围异常捕获不了，则用大范围异常捕获
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //将编译期异常转化为运行期异常
            //因为spring的声明式事务只能处理运行期异常？
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }**/



}
