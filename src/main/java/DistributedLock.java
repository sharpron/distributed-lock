/**
 * 分布式锁
 *
 * @author ron 2020/6/16
 */
public interface DistributedLock {

  /**
   * 尝试获取锁
   *
   * @return true获取成功，否则获取失败
   */
  boolean tryLock();

  /**
   * 释放已经持有的锁
   */
  void release();
}
