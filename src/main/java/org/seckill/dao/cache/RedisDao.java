package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    public RedisDao(String ip,int port){
        jedisPool=new JedisPool(ip,port);//这里采用默认配置
    }

    //利用自定义序列化，需要自己来设置schema
    private RuntimeSchema<Seckill> schema=RuntimeSchema.createFrom(Seckill.class);


    /**
     * 根据id从缓存中获取相应的秒杀商品对象
     * @param seckillId
     * @return 秒杀商品对象
     */
    public Seckill getSeckill(long seckillId){

        try {
            Jedis jedis=jedisPool.getResource();//相当于与redis建立连接
            try {
                String key="seckill:"+seckillId;//redis，通过key来获取相应的value
                //redis 并没有实现内部的序列化操作
                //get->byte[]->反序列化->Object(Seckill)
                //这里为了时间和空间效益，采用自定义的序列化:protostuff，而不用java自带的序列化

                byte[] bytes=jedis.get(key.getBytes());//获取key所对应的value

                if(bytes !=null){//从缓存中获取到了对应值
                    //1.先创建一个空对象
                    Seckill seckill=schema.newMessage();
                    //2.将bytes数组中的值按照schema模式一一对应到seckill中
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    //3.返回seckill
                    return seckill;

                }
            } finally {
                jedis.close();//连接建立后，必然需要关闭
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 将秒杀商品对象放入缓存中
     *Object(Seckill)->序列化->byte[]
     * @param seckill
     * @return 存入成功返回ok,失败返回错误信息
     */
    public String putSeckill(Seckill seckill){

        try {
            Jedis jedis=jedisPool.getResource();
            try {
                //1.先设置一个key,以便与要存入的对象相互对应
                String key="seckill:"+seckill.getSeckillId();

                //2.将要缓存的对象按照相应的schema来转换为字节数组
                byte[] bytes=ProtostuffIOUtil.toByteArray(seckill,schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));//一个缓冲器，采用默认大小
                //3.将对象放入redis中,采用超时缓存
                int timeout=60*60;//1小时
                String result=jedis.setex(key.getBytes(),timeout,bytes);//执行成功返回ok,失败则返回错误信息

                return result;

            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }
}
