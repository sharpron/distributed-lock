import java.util.Collections;
import java.util.UUID;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

/**
 * redis 单节点最佳分布式锁
 *
 * @author ron 2020/6/16
 */
public class RedisSingleNodeDistributedLock implements DistributedLock {

  /**
   * redis命令成功的响应
   */
  private static final String REDIS_SUCCESS = "OK";

  /**
   * 删除键的lua脚本
   */
  private static final String DEL_LUA_SCRIPT =
      "if redis.call('get',KEYS[1]) == ARGV[1] then " +
          "return redis.call('del',KEYS[1]) else return 0 end";

  /**
   * 默认超时时间
   */
  private static final int DEFAULT_TIMEOUT = 2;

  /**
   * 锁的唯一id
   */
  private final String id = UUID.randomUUID().toString();

  /**
   * 锁的资源名称
   */
  private final String resource;

  /**
   * redis 操作工具
   */
  private final Jedis jedis;

  /**
   * 构造器
   *
   * @param resource 资源名
   * @param jedis    redis工具
   */
  public RedisSingleNodeDistributedLock(String resource, Jedis jedis) {
    this.resource = resource;
    this.jedis = jedis;
  }

  /**
   * 尝试获取锁
   *
   * @return true代表获取成功，否则失败
   */
  @Override
  public boolean tryLock() {
    final String result = jedis.set(
        resource,
        id,
        SetParams.setParams()
            .nx()
            .ex(DEFAULT_TIMEOUT)
    );
    return REDIS_SUCCESS.equals(result);
  }


  @Override
  public void release() {
    final boolean success = jedis.eval(
        DEL_LUA_SCRIPT,
        Collections.singletonList(resource),
        Collections.singletonList(id)
    ).equals(1L);

    if (!success) {
      throw new NonLockException();
    }
  }
}
