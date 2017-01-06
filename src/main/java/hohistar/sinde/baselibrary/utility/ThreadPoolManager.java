package hohistar.sinde.baselibrary.utility;

public class ThreadPoolManager {

	private static ThreadPoolProxy mDownloadPool;
	private static Object mDownloadLock = new Object();
	private static ThreadPoolProxy mBaoBiaoPool;
	private static Object mBaoBiaoLock = new Object();
	/**
	 * 获得离线地图下载操作的线程池
	 *
	 * @return
	 */
	public static ThreadPoolProxy getDownloadPool() {
		if (mDownloadPool == null) {
			synchronized (mDownloadLock) {
				if (mDownloadPool == null) {
					mDownloadPool = new ThreadPoolProxy(1,3, 0);
				}
			}
		}
		return mDownloadPool;
	}
    /**
     * 获得报表访问的线程池
     *
     * @return
     */
	public static ThreadPoolProxy getBaoBiaoPool() {
		if (mBaoBiaoPool == null) {
			synchronized (mBaoBiaoLock) {
				if (mBaoBiaoPool == null) {
					int threadCount = Runtime.getRuntime().availableProcessors()*3+2;
					mBaoBiaoPool = new ThreadPoolProxy(threadCount/2,threadCount,0);
				}
			}
		}
		return mBaoBiaoPool;
	}

	/**
	 * 获得报表访问的线程池
	 *
	 * @return
	 */
	public static ThreadPoolProxy getImageDisplayPool() {
		if (mBaoBiaoPool == null) {
			synchronized (mBaoBiaoLock) {
				if (mBaoBiaoPool == null) {
					int threadCount = Runtime.getRuntime().availableProcessors()*3+2;
					mBaoBiaoPool = new ThreadPoolProxy(threadCount,threadCount,0);
				}
			}
		}
		return mBaoBiaoPool;
	}

}