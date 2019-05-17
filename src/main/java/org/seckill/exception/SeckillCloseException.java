package org.seckill.exception;

/**
 * 秒杀关闭异常
 * 秒杀关闭的原因有多种，如：
 *          1.库存问题(即秒杀商品被售完)
 *          2.秒杀商品的秒杀时间已截止
 *     上述抛出异常的原因，需要封装成信息返回给用户？
 */

public class SeckillCloseException extends SeckillException{

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
