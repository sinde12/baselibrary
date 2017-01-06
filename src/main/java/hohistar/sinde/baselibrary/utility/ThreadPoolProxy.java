package hohistar.sinde.baselibrary.utility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolProxy {

	private ThreadPoolExecutor mExecutor; // 线程池
	private int mCorePoolSize;
	private int mMaximumPoolSize;
	private long mKeepAliveTime;

	public ThreadPoolProxy(int corePoolSize,//核心线程数
			int maximumPoolSize,//最大线程数
			long keepAliveTime) {
		this.mCorePoolSize = corePoolSize;
		this.mMaximumPoolSize = maximumPoolSize;
		this.mKeepAliveTime = keepAliveTime;
	}

	/**
	 * 执行任务
	 * 
	 * @param task
	 */
	public void execute(Runnable task) {

		initThreadPoolExecutor();
		// 执行线程
		mExecutor.execute(task);
	}

	public Future<?> submit(Runnable task) {
		initThreadPoolExecutor();
		return mExecutor.submit(task);
	}

	private synchronized void initThreadPoolExecutor() {
		if (mExecutor == null || mExecutor.isShutdown()
				|| mExecutor.isTerminated()) {
			TimeUnit unit = TimeUnit.MILLISECONDS;
			BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();// 阻塞队列

			ThreadFactory threadFactory = Executors.defaultThreadFactory();

			RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();// 如果出现错误，不做处理

			mExecutor = new ThreadPoolExecutor(mCorePoolSize,// 核心线程数
					mMaximumPoolSize,// 最大线程数
					mKeepAliveTime,// 保持的时间长度
					unit,// keepAliveTime单位
					workQueue,// 任务队列
					threadFactory,// 线程工厂
					handler);// 错误捕获器
		}
	}

	public void remove(Runnable task) {
		if (mExecutor != null) {
			mExecutor.remove(task);
		}
	}

}
