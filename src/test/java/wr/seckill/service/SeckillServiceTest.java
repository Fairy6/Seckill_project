package wr.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wr.seckill.dto.Exposer;
import wr.seckill.dto.SeckillExecution;
import wr.seckill.entity.Seckill;
import wr.seckill.exception.RepeatKillException;
import wr.seckill.exception.SeckillCloseException;
import wr.seckill.exception.SeckillException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by wr on 2018/11/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml",
                       "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;
    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list = { }",list);
    }

    @Test
    public void getById() throws Exception {
        long id = 1000;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill = {}",seckill);

    }

    //测试代码重复执行，中注意可重复执行
    @Test
    public void SeckillLogic() throws Exception {
        long id = 1001;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()){
            logger.info("exposer={}",exposer);
            long phone = 12345678909L;
            String md5 = exposer.getMd5();
            try{
                SeckillExecution execution =  seckillService.executeSeckill(id,phone,md5);
                logger.info("result={}",execution);
            } catch (RepeatKillException e2){
                logger.error(e2.getMessage(),e2);
            }catch (SeckillCloseException e1){
                logger.error(e1.getMessage(),e1);
            }
        }else{
            //秒杀未开启
            logger.warn("exposer={}",exposer);
        }
    }

    @Test
    public void executeSeckill() throws Exception {
        long id = 1000;
        long phone = 12345678909L;
        String md5 = null;
        try{
            SeckillExecution execution =  seckillService.executeSeckill(id,phone,md5);
            logger.info("result={}",execution);
        } catch (RepeatKillException e2){
            logger.error(e2.getMessage(),e2);
        }catch (SeckillCloseException e1){
            logger.error(e1.getMessage(),e1);
        }

    }

}