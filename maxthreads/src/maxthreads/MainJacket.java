package maxthreads;

/**
 * This program intends to find the maximum platform threads at
 * which the machine will have a problem. In practice, we should
 * never start platform threads without using a Thread Pool. A
 * Platform thread is an expensive resource because its associated
 * with an Operating System Thread. 
 * 
 * Vary the NUM_THREADS constant to find the value for your machine.    
 * 
 * @author vshetty
 *
 */
public class MainJacket {
	
	private static final int NUM_THREADS = 10000; 
	
	private static void handleUserRequest() {
		System.out.println("Starting thread " + Thread.currentThread());
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Ending thread " + Thread.currentThread());

	}
	
	public static void main(String[] args) {
		
		System.out.println("Starting main");
		
		for (int j= 0; j < NUM_THREADS; j++) {
			new Thread(() -> handleUserRequest()).start();
		}
		
		System.out.println("Ending main");
	}

}
