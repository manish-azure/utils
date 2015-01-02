package com.threadtricks.utils;

import java.util.concurrent.BlockingQueue;

public class SuperCoolExecutorFactory<K> {
	
	public SuperCoolExecutor<K> getExecutor(int numberOfThreads, BlockingQueue<Runnable> workQueue, int keepAliveTime) {
		return new SuperCoolExecutorImpl<K>(numberOfThreads, workQueue, keepAliveTime);
	}
}
