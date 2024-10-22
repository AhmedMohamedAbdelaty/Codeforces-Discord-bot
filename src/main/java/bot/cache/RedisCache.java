package bot.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
// import redis.clients.jedis.Protocol;

public class RedisCache {
    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    // private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), System.getenv("redis_host"),
    //         Integer.parseInt(System.getenv("redis_port")), Protocol.DEFAULT_TIMEOUT, System.getenv("redis_pass"));

    private static RedisCache instance;
    private static JedisPool pool;

    private RedisCache() {
        initializePool();
    }

    public static void initializePool() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(128);  // Set maximum connections
            pool = new JedisPool(config, "localhost", 6379);  // Update host/port as needed
        }
    }

    // Get the singleton instance
    public static RedisCache getInstance() {
        if (instance == null) {
            synchronized (RedisCache.class) {
                if (instance == null) {
                    instance = new RedisCache();
                }
            }
        }
        return instance;
    }

    // Ping redis to check if it is alive
    public static void ping() {
        logger.debug("Attempting to ping Redis server.");
        try (Jedis jedis = pool.getResource()) {
            logger.debug("Received response from ping to Redis host: {}", jedis.ping());
        }
    }

    // Retrieve the pool instance
    public static JedisPool getPool() {
        return pool;
    }

    // Get value of a key in redis
    public static String get(String key) {
        try (Jedis jedis = pool.getResource()) {
            String response = jedis.get(key);
            logger.debug("Response from Redis for ({}): {}", key, response);
            return response;
        }
    }

    // Check if key exists in redis
    public static boolean exists(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(key);
        }
    }

    // Add or set the value of a key in redis
    public static void set(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            logger.debug("Response from redis for (set: [key: {}, value: {}]): {}", key, value, jedis.set(key, value));
        }
    }

    public static void setWithExpiration(String key, String value, int expirationSeconds) {
        try (Jedis jedis = pool.getResource()) {
            String response = jedis.setex(key, expirationSeconds, value);
            logger.debug("Response from redis for (setex: [key: {}, value: {}, expiration: {}]): {}", key, value, expirationSeconds, response);
        }
    }

    // Close the pool when the bot shuts down
    public static void shutdown() {
        if (pool != null) {
            pool.close();
        }
    }
}
