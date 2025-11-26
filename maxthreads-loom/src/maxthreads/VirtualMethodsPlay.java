package maxthreads;

import java.lang.Thread.Builder.OfVirtual;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 
 * This program shows the various ways in which a Virtual
 * Thread can be created. It does not include the 
 * Thread.startVirtualThread(..) style.
 * 
 * @author vshetty
 *
 */
@SuppressWarnings({ "unused" })
public class VirtualMethodsPlay {
	
	private static void handleUserRequest() {
		System.out.println("Starting thread " + Thread.currentThread());
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Ending thread " + Thread.currentThread());

	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting main ");
		
		// Replace this call as needed
		playWithVirtualExecutorService();
		
		System.out.println("Ending main ");

	}

	/**
	 * 
	 * Creates Virtual Threads using a Virtual Builder
	 * @throws Exception
	 */
	private static void playWithVirtualBuilder() throws Exception {
		
		// Create a Virtual Builder object with name and initial index
		OfVirtual vBuilder = Thread.ofVirtual().name("userthread", 0);
		
		// Start two virtual threads using the builder 
		Thread vThread1 = 
			vBuilder.start(VirtualMethodsPlay::handleUserRequest);
		Thread vThread2 = 
			vBuilder.start(VirtualMethodsPlay::handleUserRequest);
		
		// Make sure the threads terminate 
		vThread1.join();
		vThread2.join();
		
		// Control reaches here once the two virtual threads complete
	}

	/**
	 * 
	 * Creates Virtual Threads using a Thread Factory
	 * @throws Exception
	 */
	private static void playWithFactory() throws Exception {
		
		// Create a Thread factory 
		ThreadFactory factory 
			= Thread.ofVirtual().name("userthread", 0).factory();

		// Start two virtual threads using the factory 
		Thread vThread1 
			= factory.newThread(VirtualMethodsPlay::handleUserRequest);
		vThread1.start(); 
		
		Thread vThread2 
			= factory.newThread(VirtualMethodsPlay::handleUserRequest);
		vThread2.start(); 
		
		// Make sure the threads terminate 
		vThread1.join();
		vThread2.join();
		
		// Control reaches here once the two virtual threads complete

	}


	/**
	 * 
	 * Create a Virtual Thread using a Virtual Thread Executor
	 */
	private static void playWithVirtualExecutorService() {
		
		
		// Create an Virtual Thread ExecutorService 
		// Note the try with resource which will make sure all Virtual threads
		// are terminated 
		try (ExecutorService srv = Executors.newVirtualThreadPerTaskExecutor()) {
			
			// Submit two tasks to the Executor service 
			srv.submit(VirtualMethodsPlay::handleUserRequest);
			srv.submit(VirtualMethodsPlay::handleUserRequest);
			
      System.err.println("Virtual Thread ExecutorService completed");
		} // it waits at this point
		
		// Control reaches here once the two virtual threads complete
		
		
	}


	/**
	 * 
	 * Create a Virtual Thread using a "Thread Per Task" Executor Service
	 */
	private static void playWithExecutorService() {
		
		// Create a Virtual Thread factory with custom name
		ThreadFactory factory 
			= Thread.ofVirtual().name("userthread", 0).factory();
		
		// Create an ExecutorService for this factory
		// Note the try with resource which will make sure all Virtual threads
		// are terminated 
		try (ExecutorService srv = Executors.newThreadPerTaskExecutor(factory)) {
			
			// Submit two tasks to the Executor service 
			srv.submit(VirtualMethodsPlay::handleUserRequest);
			srv.submit(VirtualMethodsPlay::handleUserRequest);
		}
		
		// Control reaches here once the two virtual threads complete

	}



}
