package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

    @Resource
    private SuccessKilledDao successKilledDao;
    //接口调用方法的过程：接口-->接口实现类-->实现类调用方法
    //利用Spring注解，将接口实现类的实现过程交给了spring容器？

    @Test
    public void testInsertSuccessKilled(){

        long seckillId=1004L;
        long userPhone=13871545261L;
        int insertCount=successKilledDao.insertSuccessKilled(seckillId, userPhone);
        System.out.println("insertCount="+insertCount);
        //执行一次，insertCount=1
        //执行两次，insertCount=0，不允许同一个用户连续购买同一件秒杀商品

    }

    @Test
    public void testQueryByIdWithSeckill(){

        long seckillId=1004L;
        long userPhone=13871545261L;
        SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
        System.out.println(successKilled);
        System.out.println(successKilled.getSeckill());


    }

}