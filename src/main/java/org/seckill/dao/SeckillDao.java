package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SeckillDao {

    /**
     * 减库存
     * 当一个用户秒杀某件商品成功时，该件商品的相应库存会减少（-1）
     * @param seckillId：秒杀商品的id
     * @param killTime:用户下单的时间？必须在秒杀开始时间和结束时间之间才是有效秒杀
     * @return 返回值表示更新操作所影响的行数，更新一条记录则返回1，
     *          故如果返回值>1,则操作成功，反之，操作失败
     */
    int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);
    //利用mybatis提供的@Param()注解，自动将接口方法中的形参与sql语句中的参数匹配起来

    /**
     * 根据id查询秒杀商品
     * @param seckillId：秒杀商品id
     * @return 返回该id所对应的秒杀商品信息
     */
    Seckill queryById(long seckillId);

    /**
     *根据偏移量来查询秒杀商品列表
     * @param offset ：表示起始查询的位置
     * @param limit ：表示从起始查询位置开始的limit条数据？查询长度？
     * @return 返回所有符合查询条件的秒杀商品信息，因为结果集可能有一条记录，也可能有多条记录，故用List来封装结果集
     */
    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 使用存储过程执行秒杀
     * @param paramMap
     */
    void killByProcedure(Map<String, Object> paramMap);


}
