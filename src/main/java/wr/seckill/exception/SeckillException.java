package wr.seckill.exception;

/**
 * 秒杀相关业务异常
 * Created by wr on 2018/11/19.
 */
public class SeckillException extends RuntimeException{
    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
