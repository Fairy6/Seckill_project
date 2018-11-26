package wr.seckill.service;

/**
 * Created by wr on 2018/11/17.
 */

import wr.seckill.dto.Exposer;
import wr.seckill.dto.SeckillExecution;
import wr.seckill.entity.Seckill;
import wr.seckill.exception.RepeatKillException;
import wr.seckill.exception.SeckillCloseException;
import wr.seckill.exception.SeckillException;

import java.util.List;

/**
 * 业务接口,站在使用者角度设计接口
 * 三个方面：方法定义粒度，参数，返回类型（return 类型/异常）
 *
 */
public interface SeckillService {
    /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param secKillId
     * @return
     */
    Seckill getById(long secKillId);

    /**
     * 秒杀开启时输出秒杀接口地址
     * 否则输出系统时间和秒杀时间
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
        throws SeckillException,RepeatKillException,SeckillCloseException;



}

