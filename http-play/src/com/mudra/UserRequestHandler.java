package com.mudra;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * User Request Handler - Four Patterns for I/O with Virtual Threads
 * 
 * This class demonstrates four different approaches to handling I/O operations
 * with virtual threads, each with different trade-offs:
 * 
 * PATTERN COMPARISON:
 * 
 * 1. SEQUENTIAL (sequentialCall)
 *    - Execution: One after another
 *    - Time: 2s + 5s = ~7 seconds
 *    - Complexity: LOW - easiest to understand
 *    - Use When: Operations depend on each other
 * 
 * 2. CONCURRENT WITH FUTURES (concurrentCallWithFutures)
 *    - Execution: Parallel using Futures
 *    - Time: max(2s, 5s) = ~5 seconds
 *    - Complexity: MEDIUM - straightforward parallelism
 *    - Use When: Independent operations need to run in parallel
 * 
 * 3. FUNCTIONAL STYLE (concurrentCallFunctional)
 *    - Execution: Parallel using functional streams
 *    - Time: max(2s, 5s) = ~5 seconds
 *    - Complexity: MEDIUM - clean functional code
 *    - Use When: You prefer functional programming style
 * 
 * 4. COMPLETABLE FUTURE (concurrentCallCompletableFuture)
 *    - Execution: Parallel with composition
 *    - Time: max(2s, 5s) + 4s = ~9 seconds (but first two parallel)
 *    - Complexity: HIGH - most flexible and composable
 *    - Use When: Complex workflows with dependencies
 * 
 * TIP: Change the return value in call() to try different patterns!
 * 
 * @author vshetty
 */
@SuppressWarnings("preview")
public class UserRequestHandler implements Callable<String> {
  // public static void main(String[] args) throws Exception {
  //   UserRequestHandler handler = new UserRequestHandler();
  //   String result = handler.call();
  //   System.out.println("Result = " + result);
  // }

	/**
	 * Entry point - Currently uses sequential pattern.
	 * 
	 * EXERCISE: Try changing to different patterns:
	 * - return concurrentCallWithFutures();
	 * - return concurrentCallFunctional();
	 * - return concurrentCallCompletableFuture();
	 * 
	 * Compare the execution times printed to console!
	 */
	@Override
	public String call() throws Exception {
		return sequentialCall();
		// return concurrentCallCompletableFuture();
		// return concurrentCallFunctional();
    // return concurrentCallWithFutures();
		
	}

	/**
	 * PATTERN 4: CompletableFuture - Composable Async Operations
	 * 
	 * This pattern uses CompletableFuture for complex async workflows with dependencies.
	 * 
	 * WORKFLOW:
	 * 1. dbCall (2s) and restCall (5s) run in PARALLEL
	 * 2. When both complete, results are combined
	 * 3. THEN externalCall (4s) runs with combined results
	 * 
	 * Total time: max(2s, 5s) + 4s = ~9 seconds
	 * 
	 * ADVANTAGES:
	 * - Highly composable (thenCombine, thenApply, etc.)
	 * - Excellent error handling (exceptionally, handle)
	 * - Can express complex dependency chains
	 * - Supports timeout, fallback patterns
	 * 
	 * DISADVANTAGES:
	 * - Most complex syntax
	 * - Steeper learning curve
	 * - Can become difficult to read with many operations
	 * 
	 * WHEN TO USE:
	 * - Complex workflows with dependencies (A + B → C → D)
	 * - Need sophisticated error handling
	 * - Building async API/library
	 * - Composition of multiple async operations
	 * 
	 * NOTE: Even with virtual threads, CompletableFuture is still useful
	 * for composing operations and expressing dependencies clearly!
	 */
	private String concurrentCallCompletableFuture() throws Exception {
    System.out.println("concurrentCallCompletableFuture: " + Thread.currentThread().getName());
		try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
			
			String output = CompletableFuture
								.supplyAsync(this::dbCall, service)
								.thenCombine(
										CompletableFuture.supplyAsync(this::restCall, service)
										, (result1, result2) -> {
											return "[" + result1 + "," + result2 + "]";
										})
								.thenApply(result -> {
									
									// both dbCall and restCall have completed 
									String r = externalCall();
									return "[" + result + "," + r + "]";
									
								})
                .get(5, TimeUnit.SECONDS);
			
			log(output);
			return output;
			
		}
	}

	/**
	 * PATTERN 3: Functional Style - Clean Parallel Execution
	 * 
	 * This pattern uses functional programming with invokeAll() for
	 * clean parallel execution of independent tasks.
	 * 
	 * WORKFLOW:
	 * 1. Submit collection of tasks using invokeAll()
	 * 2. invokeAll() waits for ALL tasks to complete
	 * 3. Use streams to process results functionally
	 * 
	 * Total time: max(2s, 5s) = ~5 seconds
	 * 
	 * ADVANTAGES:
	 * - Clean, functional code style
	 * - Concise - less boilerplate than Pattern 2
	 * - invokeAll() automatically waits for all tasks
	 * - Easy to extend (just add more callables to list)
	 * 
	 * DISADVANTAGES:
	 * - Exception handling can be awkward in streams
	 * - Less control over individual task results
	 * - All-or-nothing approach (waits for all)
	 * 
	 * WHEN TO USE:
	 * - Multiple independent tasks that can run in parallel
	 * - Prefer functional programming style
	 * - All tasks equally important (no early exit needed)
	 * - Simple error handling (null on failure is OK)
	 * 
	 * @return Comma-separated results from all tasks
	 * @throws Exception if invokeAll fails
	 */
	private String concurrentCallFunctional() throws Exception {
    System.out.println("concurrentCallFunctional");
		try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
			
			String result = service.invokeAll(Arrays.asList(this::dbCall, this::restCall))
				.stream()
				.map(f -> {
					
					try {
						return (String)f.get();
					}
					catch (Exception e) {
						return null;
					}
					
				})
				.collect(Collectors.joining(","));
			
			return "[" + result + "]";
			
		}
	}

	/**
	 * PATTERN 2: Concurrent with Futures - Parallel Execution
	 * 
	 * This pattern runs independent I/O operations in parallel using Futures,
	 * reducing total execution time from 7s to 5s.
	 * 
	 * WORKFLOW:
	 * 1. Create virtual thread executor
	 * 2. Submit tasks that return Future objects immediately
	 * 3. Tasks run in parallel on separate virtual threads
	 * 4. Call future.get() to wait for results (blocks if not ready)
	 * 
	 * Total time: max(2s, 5s) = ~5 seconds (vs 7s sequential)
	 * 
	 * ADVANTAGES:
	 * - Significant performance improvement for independent operations
	 * - Straightforward parallel execution
	 * - Easy to understand control flow
	 * - Good error handling (exceptions from get())
	 * 
	 * DISADVANTAGES:
	 * - More code than sequential
	 * - Must manage Futures manually
	 * - Blocking on get() (but OK with virtual threads!)
	 * 
	 * WHEN TO USE:
	 * - Operations are independent (don't depend on each other)
	 * - Want simple parallelism without complexity
	 * - Need results from all operations
	 * 
	 * KEY INSIGHT: future.get() blocks, but that's fine with virtual threads!
	 * The virtual thread will unmount, freeing the carrier for others.
	 * 
	 * @return Combined results from parallel operations
	 * @throws Exception if any task fails
	 */
	private String concurrentCallWithFutures() throws Exception {
    System.out.println("concurrentCallWithFutures");
		try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
			
			long start = System.currentTimeMillis();
			Future<String> dbFuture   = service.submit(this::dbCall);
			Future<String> restFuture = service.submit(this::restCall);
			
			String result = String.format("[%s,%s]", dbFuture.get(), restFuture.get());
			
			long end = System.currentTimeMillis();
			log("time = " + (end - start));

			log(result);
			return result;
			
		}
	}

	/**
	 * PATTERN 1: Sequential - Simple but Slow
	 * 
	 * This pattern executes I/O operations one after another, waiting for
	 * each to complete before starting the next.
	 * 
	 * WORKFLOW:
	 * 1. Call dbCall() - waits 2 seconds
	 * 2. Then call restCall() - waits 5 seconds
	 * 
	 * Total time: 2s + 5s = ~7 seconds
	 * 
	 * ADVANTAGES:
	 * - Simplest code - easy to read and understand
	 * - Easy to debug - linear execution
	 * - Natural for operations with dependencies
	 * - No concurrency complexity
	 * 
	 * DISADVANTAGES:
	 * - Slowest - operations don't overlap
	 * - Wastes time when operations are independent
	 * - Not utilizing available parallelism
	 * 
	 * WHEN TO USE:
	 * - Operations depend on each other (result1 needed for operation2)
	 * - Simplicity more important than performance
	 * - Operations are very fast (overhead not worth parallelizing)
	 * - Debugging or initial implementation
	 * 
	 * NOTE: Even though this is slow, it's still using a virtual thread!
	 * The virtual thread will unmount during each I/O operation, allowing
	 * other virtual threads to run.
	 * 
	 * @return Combined results from sequential operations
	 * @throws Exception if any operation fails
	 */
	private String sequentialCall() throws Exception {
    System.out.println("sequentialCall");
		long start = System.currentTimeMillis();
		
		String result1 = dbCall(); // 2 secs
		String result2 = restCall();  // 5 secs
    Thread.sleep(Duration.ofMinutes(10));
		
		String result = String.format("[%s,%s]", result1, result2);
		
		long end = System.currentTimeMillis();
		log("time= " + (end - start));

		log(result);
		return result;
	}
	
	/**
	 * Simulates a database query with 2 second latency.
	 * 
	 * In a real application, this might be:
	 * - SELECT * FROM users WHERE id = ?
	 * - Typical database query with network round-trip
	 * - I/O-bound operation (perfect for virtual threads)
	 * 
	 * @return Response data from simulated database
	 */
	private String dbCall() {
		try {
			NetworkCaller caller = new NetworkCaller("data");
			return caller.makeCall(1);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Simulates a REST API call with 5 second latency.
	 * 
	 * In a real application, this might be:
	 * - External service API call (payment, shipping, etc.)
	 * - Microservice communication
	 * - Third-party API integration
	 * - I/O-bound operation (perfect for virtual threads)
	 * 
	 * @return Response data from simulated REST endpoint
	 */
	private String restCall() {
		try {
			NetworkCaller caller = new NetworkCaller("rest");
			return caller.makeCall(2);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}

	/**
	 * Simulates an external service call with 4 second latency.
	 * 
	 * In a real application, this might be:
	 * - Analytics service
	 * - Notification service
	 * - Caching service
	 * - Another I/O-bound operation
	 * 
	 * @return Response data from simulated external service
	 */
	private String externalCall() {
		try {
			NetworkCaller caller = new NetworkCaller("extn");
			return caller.makeCall(4);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}

  private void log(String message) {
    // System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    System.out.println("[" + Thread.currentThread().getName() + "] ");
  }

	
}
