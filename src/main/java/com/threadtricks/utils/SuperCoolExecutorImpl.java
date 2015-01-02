package com.threadtricks.utils;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SuperCoolExecutorImpl<K> extends ThreadPoolExecutor implements SuperCoolExecutor<K> {
	
	private final ConcurrentMap<K, TaskList> map = new ConcurrentHashMap<K, TaskList>();
	
	SuperCoolExecutorImpl(int numberOfThreads, BlockingQueue<Runnable> workQueue, int keepAliveTime) {
		super(numberOfThreads, numberOfThreads, keepAliveTime, TimeUnit.SECONDS, workQueue);
		this.allowCoreThreadTimeOut(true);
	}
	
	
	public void execute(final K key, final Runnable runnable) {
		TaskList list = null;
		if(!map.containsKey(key)) {
			list = map.putIfAbsent(key, new TaskList());
		}
		
		list = map.get(key);
		list.runnables.addLast(runnable);
		
		if(list.processing.compareAndSet(false, true)) {
			this.execute(new CustomRunnable<K>(key, list.runnables.peekFirst()));
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		CustomRunnable<K> cr = (CustomRunnable<K>) r;
		TaskList list = map.get(cr.key);
		//Remove the task just processed
		list.runnables.pollFirst();

		//If there are more tasks to process
		if(list.runnables.peekFirst() != null) {
			this.execute(new CustomRunnable<K>(cr.key, list.runnables.peekFirst()));
		} else {//If there are no more tasks to process then turn processing off
			if(list.processing.compareAndSet(true, false)){
				//Send a Dummy runnable so that we don't forget the last minute additions to runnable list
				this.execute(new CustomRunnable<K>(cr.key, null));
			}
		}
	}
	
	
	private class TaskList {
		private LinkedList<Runnable> runnables;
		private AtomicBoolean processing;
		
		private TaskList(){
			runnables = new LinkedList<Runnable>();
			processing = new AtomicBoolean();
		}
	}

	
	private class CustomRunnable<K> implements Runnable {
		private K key;
		private Runnable actualTask;
		
		private CustomRunnable(K key, Runnable runnable) {
			this.key = key;
			this.actualTask = runnable;
		}
		
		public void run() {
			if(actualTask != null) {
				actualTask.run();
			}
		}
	}

}
