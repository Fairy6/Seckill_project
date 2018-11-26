package wr.seckill.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import wr.seckill.dao.SeckillDao;
import wr.seckill.dao.SuccessKilledDao;
import wr.seckill.dao.cache.RedisDao;
import wr.seckill.dto.Exposer;
import wr.seckill.dto.SeckillExecution;
import wr.seckill.entity.Seckill;
import wr.seckill.entity.SuccessKilled;
import wr.seckill.enums.SeckillStateEnum;
import wr.seckill.exception.RepeatKillException;
import wr.seckill.exception.SeckillCloseException;
import wr.seckill.exception.SeckillException;

import java.util.Date;
import java.util.List;

/**
 * Created by wr on 2018/11/19.
 */
//@Component @Service @Dao @Controller
@Service
public class SeckillServiceImpl implements SeckillService{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //注入service依赖
    @Autowired   //Resource,@Inject
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    //md5盐值字符串，用来混淆md5
    private final String salt = "jdkahdajlhdshdjfjakfalalf^#@123_Dfec";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    @Override
    public Seckill getById(long secKillId) {
        return seckillDao.queryById(secKillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点：缓存优化:超时的基础上维护一致性
        /**
         * get from cache
         * if null
         *  get db
         * else
         *  put cache
         */
        //1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null){
            //2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null){
                return new Exposer(false,seckillId);
            }else{
                //3.放入redis
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() <startTime.getTime() || nowTime.getTime()>endTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }
        //转换特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }
    private  String getMD5(long seckillId){
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1.开发团队达成一致约定，明确标注事物方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作
     * 3.不是所有的方法都需要事务，如只有一条修改数据，只读操作不需要事务控制
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存+记录购买行为
        Date nowTime = new Date();
        try{
            //降低一倍的网络延迟时间
            //记录购买行为（sql语句中含有ignore,如果主键冲突贼返回0）
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insertCount <= 0){
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            }else{
                //减库存
                int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
                if (updateCount <= 0){
                    //没有更新到记录，秒杀结束
                    throw new SeckillCloseException("seckill is closed");
                }//唯一：seckillId，userPhone
                else{
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS,successKilled);
                }
            }
        } catch (SeckillCloseException e1){
            throw  e1;
        } catch(RepeatKillException e2){
            throw  e2;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            //所有编译期异常 转化为运行期异常
            throw new SeckillException("seckill inner error"+e.getMessage());
        }
    }
}
