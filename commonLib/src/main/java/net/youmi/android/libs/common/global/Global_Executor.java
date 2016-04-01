package net.youmi.android.libs.common.global;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 创建所需的线程池，不建议创建多个线程池
 *
 * @author zhitaocai
 *
 */
public class Global_Executor {

	/**
	 * 全局公用线程池，没什么特殊需求都用这个吧
	 *
	 */
	private final static Executor mCachedThreadPool = Executors.newCachedThreadPool(new DefaultThreadFactory(
			Thread.NORM_PRIORITY - 2, null));

	// /**
	// * 下载模块的线程池
	// */
	// private final static Executor mDownloadThreadPool = Executors.newFixedThreadPool(30, new DefaultThreadFactory(
	// Thread.NORM_PRIORITY - 1, "download"));

	// private final static Executor mDownloadThreadPool = new ThreadPoolExecutor(Runtime.getRuntime()
	// .availableProcessors() * 5, Runtime.getRuntime().availableProcessors() * 5, 30L, TimeUnit.SECONDS,
	// new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory(Thread.NORM_PRIORITY - 1, "download"));

	/**
	 * 公用线程池
	 *
	 * @return
	 */
	public static Executor getCachedThreadPool() {
		return mCachedThreadPool;
	}

	// 以下线程池的创建慎用， 不要创建多个线程池

	/**
	 * 创建下载任务相关的线程池
	 * <p>
	 * 线程池大小：30<br>
	 * 线程工厂：线程优先级4（Normal-1），线程前缀标签download<br>
	 *
	 * <ol>
	 * <li>使用大型队列和小型池可以最大限度地降低 CPU使用率、操作系统资源和上下文切换开销，但是可能导致人工降低吞吐量;如果任务频繁阻塞（例如，如果它们是 I/O边界），则系统可能为超过您许可的更多线程安排时间</li>
	 * <li>使用小型队列通常要求较大的池大小，CPU 使用率较高，但是可能遇到不可接受的调度开销，这样也会降低吞吐量。</li>
	 * </ol>
	 *
	 * @return
	 */

	/**
	 * 暂时先使用cache线程池
	 *
	 * @return
	 */
	public static Executor getDownloadThreadPool() {
		return mCachedThreadPool;
	}

	// /**
	// * 创建下载任务相关的线程池
	// * <p>
	// * 最少维持线程数：cpu数量个数<br>
	// * 线程池最大容量：cpu数量个数*2（待测试）<br>
	// * idle：30s<br>
	// * 缓存队列：LinkedBlockingQueue size使用默认值 Integer.MAX_VALUE;<br>
	// * 线程工厂：线程优先级4（Normal-1），线程前缀标签download<br>
	// *
	// * <ol>
	// * <li>使用大型队列和小型池可以最大限度地降低 CPU使用率、操作系统资源和上下文切换开销，但是可能导致人工降低吞吐量;如果任务频繁阻塞（例如，如果它们是 I/O边界），则系统可能为超过您许可的更多线程安排时间</li>
	// * <li>使用小型队列通常要求较大的池大小，CPU 使用率较高，但是可能遇到不可接受的调度开销，这样也会降低吞吐量。</li>
	// * </ol>
	// *
	// * @return
	// */
	// public static Executor getDownloadThreadPool() {
	// // if (mDownloadThreadPool == null) {
	// // try {
	// // mDownloadThreadPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime
	// // .getRuntime().availableProcessors() * 2, 30L, TimeUnit.SECONDS,
	// // new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory(Thread.NORM_PRIORITY - 1,
	// // "download"));
	// // } catch (Exception e) {
	// // if (Debug_SDK.isDebug) {
	// // Debug_SDK.de(e);
	// // }
	// // }
	// // }
	// return mDownloadThreadPool;
	// }

	public static class DefaultThreadFactory implements ThreadFactory {

		protected final static AtomicInteger mPoolNumber = new AtomicInteger(1);
		protected final AtomicInteger mThreadNumber = new AtomicInteger(1);

		protected final ThreadGroup mGroup;
		protected final String mNamePrefix;
		protected final int mThreadPriority;

		public DefaultThreadFactory(int threadPriority, String poolPreName) {
			mThreadPriority = threadPriority;
			SecurityManager s = System.getSecurityManager();
			mGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			if (poolPreName == null || "".equals(poolPreName.trim())) {
				poolPreName = "net/youmi/android/libs/common";
			}
			mNamePrefix = "pool-" + poolPreName + "-" + mPoolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = null;
			try {
				t = new Thread(mGroup, r, mNamePrefix + mThreadNumber.getAndIncrement(), 0);
				if (t.isDaemon())
					t.setDaemon(false);
				t.setPriority(mThreadPriority);
			} catch (Exception e) {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, this, e);
				}
			} finally {
				if (Debug_SDK.isDebug) {
					if (t != null) {
						Debug_SDK.dd(t.getName(), "第%d个线程创建成功", mThreadNumber.get() - 1);
					}
				}
			}
			return t;
		}

	}
}
