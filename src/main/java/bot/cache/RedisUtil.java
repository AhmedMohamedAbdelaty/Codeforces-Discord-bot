package bot.cache;

import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class RedisUtil {

    private <T extends Serializable> byte[] serialize(T obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return bos.toByteArray();
        }
    }

    public <T extends Serializable> void storeObjectInRedis(String key, T obj) {
        try (Jedis jedis = new Jedis("localhost")) {
            byte[] serializedObject = serialize(obj);
            jedis.set(key.getBytes(), serializedObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }

    public <T> T getObjectFromRedis(String key, Class<T> clazz) {
        try (Jedis jedis = new Jedis("localhost")) {
            byte[] serializedObject = jedis.get(key.getBytes());
            if (serializedObject != null) {
                Object obj = deserialize(serializedObject);
                return clazz.cast(obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}