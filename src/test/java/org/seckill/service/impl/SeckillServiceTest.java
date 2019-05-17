package org.seckill.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillListTest() {

        List<Seckill> list=seckillService.getSeckillList();
        logger.info("list={}",list);

    }

    @Test
    public void getByIdTest() {

        long id=1000;
        Seckill seckill=seckillService.getById(id);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void exportSeckillUrlTest() {

        long id=1000;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        logger.info("exposer={}",exposer);

        //exposer=Exposer{exposed=true,
        //                md5='43ac911a5cf264fe6d68058f313824a1',
        //                seckillId=1004,
        //                now=0, start=0, end=0}

    }

    @Test
    public void executeSeckillTest() {

        long id=1004;
        long phone=13852467562l;
        String md5="43ac911a5cf264fe6d68058f313824a1";

        try {

            SeckillExecution seckillExecution=seckillService.executeSeckill(id,phone,md5);
            logger.info("result={}",seckillExecution);
            /**
             * 在执行秒杀的时候，有可能会存在重复秒杀、秒杀结束等异常，
             * 而这些异常是被允许的，所以需要用try-catch来捕获一下，以便通过单元测试
             */

        } catch (RepeatKillException e) {
            logger.error(e.getMessage());
        } catch (SeckillCloseException e){
            logger.error(e.getMessage());
        }

        //result=SeckillExecution{seckillId=1004,
        //                        state=1, stateInfo='秒杀成功',
        //                        successKilled=SuccessKilled{
        //                          seckillId=1004,
        //                          userPhone=1385246756,
        //                          state=0,
        //                          createTime=Mon May 13 09:27:11 CST 2019}
        //                        }

    }

    //将秒杀地址的暴露和执行秒杀放在一起进行测试
    //即集成测试代码的完整逻辑，这样测试可以重复执行？
    @Test
    public void testSeckillLogic() throws Exception{

        long id=1004;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()){//如果秒杀地址被暴露，说明可以执行秒杀
            logger.info("exposer={}",exposer);//先打印地址的相关信息

            long phone=13502171127l;
            String md5=exposer.getMd5();

            try {
                SeckillExecution seckillExecution=seckillService.executeSeckill(id,phone,md5);//执行秒杀
                logger.info("result={}",seckillExecution);
            } catch (RepeatKillException e) {
                logger.error(e.getMessage());
            } catch (SeckillCloseException e) {
                logger.error(e.getMessage());
            }

        }else{//不暴露秒杀地址说明秒杀未开始或者已结束,给出警告信息
            logger.warn("exposer={}",exposer);
        }
    }

    @Test
    public void testSeckillByPricedure(){
        long id=1004;
        long phone=12536984562l;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()){
            String md5=exposer.getMd5();
            SeckillExecution seckillExecution=seckillService.executeSeckillProcedure(id,phone,md5);
            logger.info(seckillExecution.getStateInfo());
        }
    }



}