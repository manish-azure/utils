package com.threadtricks.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SuperCoolExecutorTest {
	private SuperCoolExecutorFactory<String> factory;
	private SuperCoolExecutor<String> executor;
	private CountDownLatch latch;
	private ConcurrentHashMap<String, List<Integer>> resultMap;
	
	@Before
	public void setUp() {
		factory = new SuperCoolExecutorFactory<String>();
	}
	
	
	@Test
	public void testSingleThreadExecutor() throws Exception {
		executor = factory.getExecutor(1, new LinkedBlockingQueue<Runnable>(), 100);
		resultMap = new ConcurrentHashMap<String, List<Integer>>();
		
		int numberOfProducers = 10;
		int taskPerProducer = 20;
		int taskTimeInMillis = 10;
		latch = new CountDownLatch(numberOfProducers * taskPerProducer);
		
		for(int i = 1; i <= numberOfProducers; i++) {
			String producerName = "Type_" + i;
			resultMap.put(producerName, new ArrayList<Integer>());
			new Thread(new StupidProducer(producerName, taskPerProducer, taskTimeInMillis)).start();
		}
		
		latch.await();	
		
		System.out.println("Result: " + resultMap);
		for(String producer : resultMap.keySet()) {
			int previousTaskNumber = 0;
			for(int taskNumber : resultMap.get(producer)) {
				Assert.assertEquals(producer, ++previousTaskNumber, taskNumber);
			}
		}
	}
	
	
	@Test
	public void testMultiThreadExecutor() throws Exception {
		executor = factory.getExecutor(10, new LinkedBlockingQueue<Runnable>(), 100);
		resultMap = new ConcurrentHashMap<String, List<Integer>>();
		
		int numberOfProducers = 20;
		int taskPerProducer = 100;
		int taskTimeInMillis = 20;
		latch = new CountDownLatch(numberOfProducers * taskPerProducer);
		
		for(int i = 1; i <= numberOfProducers; i++) {
			String producerName = "Type_" + i;
			resultMap.put(producerName, new ArrayList<Integer>());
			new Thread(new StupidProducer(producerName, taskPerProducer, taskTimeInMillis)).start();
		}
		
		latch.await();	
		
		System.out.println("Result: " + resultMap);
		for(String producer : resultMap.keySet()) {
			int previousTaskNumber = 0;
			for(int taskNumber : resultMap.get(producer)) {
				Assert.assertEquals(producer, ++previousTaskNumber, taskNumber);
			}
		}
	}
	
	
	private class StupidProducer implements Runnable {
		private String name;
		private int taskCount;
		private long taskTime;
		
		private StupidProducer(String name, int taskCount, long taskTime) {
			this.name = name;
			this.taskCount = taskCount;
			this.taskTime = taskTime;
		}
		
		public void run() {
			for(int i = 1; i <= taskCount; i++) {
				executor.execute(name, new SleepyFellow(name , i, taskTime));
			}
		}
	}
	
	
	private class SleepyFellow implements Runnable  {
		private long sleepTime;
		private String producer;
		private int taskNumber;
		
		private SleepyFellow(String producer, int taskNumber, long sleepTime) {
			this.sleepTime = sleepTime;
			this.producer = producer;
			this.taskNumber = taskNumber;
		}

		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			resultMap.get(producer).add(taskNumber);
			latch.countDown();
		}
	}

}
