package org.seckill.dao.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {

    private long id = 1001;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SeckillDao seckillDao;

    @Test
    public void testSeckill() throws Exception {
        //get 和 put方法一起测试
        Seckill seckill = redisDao.getSeckill(id);//先查缓存，第一次获取的时候为空
        if (seckill == null) {//缓存为空，则查数据库
            seckill = seckillDao.queryById(id);
            if (seckill != null) {//数据库不为空，则加入到缓存
                String result = redisDao.putSeckill(seckill);//加入缓存中
                System.out.println("result=" + result);
                seckill = redisDao.getSeckill(id);//再从缓存中取
                System.out.println(seckill);
            }
        }
    }

}