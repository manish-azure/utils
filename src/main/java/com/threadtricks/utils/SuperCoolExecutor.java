package com.threadtricks.utils;

public interface SuperCoolExecutor<K> {
	
	public void execute(K key, Runnable runnable);
}
