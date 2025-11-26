package com.mudra;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

/**
 * Virtual Threads for I/O-Bound Request Handling
 * 
 * This program demonstrates how Virtual Threads enable the thread-per-request
 * model to scale efficiently for I/O-bound operations.
 * 
 * KEY CONCEPTS:
 * 1. Each user request gets its own virtual thread
 * 2. Virtual threads are lightweight (~1KB vs 2MB for platform threads)
 * 3. Can handle thousands/millions of concurrent requests
 * 4. Blocking I/O is OK - virtual threads unmount during I/O operations
 * 
 * TRADITIONAL APPROACH (Platform Threads):
 * - Limited to few thousand threads due to memory (each ~2MB)
 * - Required thread pools and complex async code
 * - Context switching overhead
 * 
 * VIRTUAL THREADS APPROACH:
 * - Millions of threads possible (each ~1KB)
 * - Simple blocking code that scales
 * - JVM manages mounting/unmounting on carrier threads
 * 
 * The UserRequestHandler simulates database and REST API calls using
 * http://httpbin.org/ to demonstrate real I/O operations.
 * 
 * @author vshetty
 */
public class HttpPlayer {
	
	// TIP: Try increasing this to 1000+ to see virtual threads scale!
	// With platform threads, this would consume ~2GB+ memory
	// With virtual threads, it's only ~1MB
	private static final int NUM_USERS = 2000;
	
	@SuppressWarnings("preview")
	public static void main(String[] args) {
		
		// Create a virtual thread factory with custom naming
		// Each thread will be named: request-handler-0, request-handler-1, etc.
		// This helps with debugging and monitoring
		ThreadFactory factory = Thread.ofVirtual().name("virtual-request", 0).factory();
		
		// Create an ExecutorService that creates one virtual thread per task
		// The try-with-resources ensures all threads complete before exiting
		try (ExecutorService executor = Executors.newThreadPerTaskExecutor(factory)) {
			
			// Submit a task for each user
			// Each task runs in its own virtual thread
			IntStream.range(0, NUM_USERS).forEach(j -> {
				executor.submit(new UserRequestHandler());
			});
			
			// When the try block exits, executor.close() is called automatically
			// This waits for all submitted tasks to complete
		}
		
		System.out.println("All user requests completed!");
		
		// EXERCISE: Try changing NUM_USERS to 10000 and observe:
		// 1. All requests still complete successfully
		// 2. Memory usage remains low
		// 3. This is the power of virtual threads!
	}

}
