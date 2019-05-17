--数据库初始化脚本

--创建数据库
CREATE DATABASE seckill;
--使用数据库
use seckill;

--创建秒杀库存表
CREATE TABLE seckill(

  'seckill_id' BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  'name' VARCHAR(120) NOT NULL COMMENT '商品名称',
  'number' int NOT NULL COMMENT '库存数量',
  'start_time' TIMESTAMP NOT NULL DEFAULT COMMENT '秒杀开始时间',
  'end_time' TIMESTAMP NOT NULL COMMENT '秒杀结束时间',
  'create_time' TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (seckill_id),
  key idx_start_time(start_time),
  key idx_end_time(end_time),
  key idx_create_time(create_time)

)ENGINE=INNODB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';

--初始化数据
INSERT INTO seckill(name,number,start_time,end_time)
VALUES
  ('800元秒杀iPhonex',100,'2019-05-01 00:00:00','2019-05-02 00:00:00'),
  ('350元秒杀ipad',200,'2019-05-03 00:00:00','2019-05-04 00:00:00'),
  ('1800元秒杀mac book pro',300,'2019-05-03 00:00:00','2019-05-04 00:00:00'),
  ('1500元秒杀iMac',400,'2019-05-05 00:00:00','2019-05-06 00:00:00');

--秒杀成功明细表
--用户登录认证相关信息(简化为手机号)
CREATE TABLE success_killed(
  'seckill_id' BIGINT NOT NULL COMMENT '秒杀商品id',
  'user_phone' BIGINT NOT NULL COMMENT '用户手机号',
  'state' TINYINT NOT NULL DEFAULT -1 COMMENT '状态标识： -1：无效 0：成功 1：已付款 2：已发货',
  'create_time' TIMESTAMP NOT NULL COMMENT '创建时间',
  PRIMARY KEY(seckill_id,user_phone),
  KEY idx_create_time(create_time)
)ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';

--show create table seckill;#显示表的创建信息

--连接数据库控制台

mysql -u root -prepare

--为什么手写DDL
--记录每次上线的DDL修改
--上线V1.1
ALTER TABLE seckill
DROP INDEX idx_create_time,
ADD INDEX idx_c_s(start_time,create_time);

--上线V1.2
--DDL














