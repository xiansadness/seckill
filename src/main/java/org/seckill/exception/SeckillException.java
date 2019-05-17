package org.seckill.exception;

/**
 * 秒杀相关业务异常：用于处理秒杀业务层（service层）中抛出的所有异常？
 */

public class SeckillException extends RuntimeException {

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
