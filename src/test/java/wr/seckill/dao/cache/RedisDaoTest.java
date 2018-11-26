package wr.seckill.dao.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wr.seckill.dao.SeckillDao;
import wr.seckill.entity.Seckill;

import static org.junit.Assert.*;

/**
 * Created by wr on 2018/11/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉Junit spring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {
    private long id = 1001;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SeckillDao seckillDao;
    @Test
    public void testSeckill() throws Exception {
        //get and put
        Seckill seckill = redisDao.getSeckill(id);
        if (seckill == null){
            seckill = seckillDao.queryById(id);
            if (seckill != null){
              String result = redisDao.putSeckill(seckill);
              System.out.println(result);
              seckill =redisDao.getSeckill(id);
              System.out.println(seckill);
            }
        }
    }



}