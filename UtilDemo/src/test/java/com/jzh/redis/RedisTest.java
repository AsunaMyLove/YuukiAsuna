package com.jzh.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTest {

    public void test1(){
//        RedisUtils redisUtils = new RedisUtils();
//        RedisUtils.save("a","qweqwe");
//        System.out.println(RedisUtils.get("aa"));
        JedisPoolConfig config = new JedisPoolConfig();
        //配置最大jedis实例数
        config.setMaxTotal(1000);
        //配置资源池最大闲置数
        config.setMaxIdle(200);
        //等待可用连接的最大时间
        config.setMaxWaitMillis(10000);
        //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的
        config.setTestOnBorrow(true);
        JedisPool jedisPool = new JedisPool(config,"192.168.31.250",6379);
        Jedis jedis = jedisPool.getResource();
        for (int i = 1 ; i<6 ; i++){
            jedis.set("zzz"+i,"zzzz"+i);
        }
    }

    public static void main(String[] args){
        RedisUtils.save("test","1111");
//        for (int i=1;i<6;i++){
//            System.out.println(RedisUtils.get("zzz"+i));
//        }
        System.out.println(RedisUtils.get("test"));
    }

}
