package com.jzh.redis;


import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils {
    private static Logger logger = Logger.getLogger(RedisUtils.class);

    /** 默认缓存时间 */
    private static final int DEFAULT_CACHE_SECONDS = 60 * 60 * 1;// 单位秒 设置成一个小时

    /** 连接池 **/
    private static JedisPool jedisPool = null;

    //初始化redis连接池
    public static void init(){
        JedisPoolConfig config = new JedisPoolConfig();
        //配置最大jedis实例数
        config.setMaxTotal(1000);
        //配置资源池最大闲置数
        config.setMaxIdle(200);
        //等待可用连接的最大时间
        config.setMaxWaitMillis(10000);
        //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的
        config.setTestOnBorrow(true);
        jedisPool = new JedisPool(config,"192.168.31.250",6379);
    }

    public static Jedis getJedis() {
        if(jedisPool==null){
            init();
//            jedisPool = (JedisPool) ComponentFactory.getComponentByItsName("jedisPool");
        }
        return jedisPool.getResource();
    }

    /**
     * 释放redis资源
     *
     * @param jedis
     */
    private static void releaseResource(Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnResource(jedis);
        }
    }

    public static Boolean save(Object key, Object object) {
        return save(key, object, DEFAULT_CACHE_SECONDS);
    }

    public static Boolean save(Object key, Object object, int seconds) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.set(SerializeUtils.serialize(key), SerializeUtils.serialize(object));
            jedis.expire(SerializeUtils.serialize(key), seconds);
            return true;
        } catch (Exception e) {
            logger.error("Redis保存失败：" + e);
            return false;
        } finally {
            releaseResource(jedis);
        }
    }

    public static Object get(Object key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] obj = jedis.get(SerializeUtils.serialize(key));
            return obj == null ? null : SerializeUtils.unSerialize(obj);
        } catch (Exception e) {
            logger.error("Redis获取失败：" + e);
            return null;
        } finally {
            releaseResource(jedis);
        }
    }

    public static Boolean del(Object key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.del(SerializeUtils.serialize(key));
            return true;
        } catch (Exception e) {
            logger.error("Redis删除失败：" + e);
            return false;
        } finally {
            releaseResource(jedis);
        }
    }

    public static Boolean expire(Object key, int seconds) {

        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.expire(SerializeUtils.serialize(key), seconds);
            return true;
        } catch (Exception e) {
            logger.error("Redis设置超时时间失败：" + e);
            return false;
        } finally {
            releaseResource(jedis);
        }
    }

}
