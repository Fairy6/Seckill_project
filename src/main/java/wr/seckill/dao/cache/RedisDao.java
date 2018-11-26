package wr.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import wr.seckill.entity.Seckill;

/**
 * Created by wr on 2018/11/24.
 */
public class RedisDao {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private JedisPool jedisPool;

    public RedisDao(String ip, int port){
        jedisPool = new JedisPool(ip,port);
    }

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);
    public Seckill getSeckill(long seckillId){
        //redis操作逻辑
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckillId;
                //并没有实现序列化操作
                //get -> byte[] -> 反序列化 -> Object(Seckill)
                //采用自定义序列化
                //protosuff:pojo
               byte[] bytes = jedis.get(key.getBytes());
               if (bytes != null){
                   //空对象
                   Seckill seckill = schema.newMessage();
                   ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                   //seckill被反序列化
                   return seckill;  //作用是可以把空间压缩到原来的十分之一到五分之一
               }
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    //把seckill对象传递到redis中
    public String putSeckill(Seckill seckill){
        //Object(seckill) ->序列化 -> byte[]
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:"+seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill,schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));//缓存器，当对象很大的时候会有个缓冲过程
               //超时缓存
                int timeout = 60 * 60;  // 1小时
                String result = jedis.setex(key.getBytes(),timeout,bytes);
                return result; //OK
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }
}
