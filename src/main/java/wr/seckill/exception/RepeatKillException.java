package wr.seckill.exception;

/**
 * 重复秒杀异常（运行期异常）
 * Created by wr on 2018/11/19.
 */
public class RepeatKillException extends SeckillException{
    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
