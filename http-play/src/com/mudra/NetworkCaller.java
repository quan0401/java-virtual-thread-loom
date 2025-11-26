package com.mudra;

import java.io.InputStream;
import java.net.URI;

/**
 * Network I/O Simulator - Demonstrating Virtual Thread Unmounting
 * 
 * This class simulates network calls with artificial delays using
 * http://httpbin.org/delay/<secs> to demonstrate how virtual threads
 * handle blocking I/O operations efficiently.
 * 
 * WHAT HAPPENS DURING THE NETWORK CALL:
 * 
 * 1. BEFORE I/O: Virtual thread is "mounted" on a carrier thread (platform thread)
 *    - Executes: System.out.println (shows thread info)
 *    - Executes: Opens connection to httpbin.org
 * 
 * 2. DURING I/O: Virtual thread is "unmounted" from carrier thread
 *    - While waiting for network response (artificial delay)
 *    - Carrier thread is FREE to run other virtual threads
 *    - This is why millions of virtual threads can share few carrier threads!
 * 
 * 3. AFTER I/O: Virtual thread "remounts" on a carrier thread (may be different)
 *    - Continues execution with response data
 *    - Executes: System.out.println (shows completion)
 * 
 * This unmounting mechanism is what makes virtual threads efficient for I/O!
 * 
 * @author vshetty
 */
public class NetworkCaller {
	
	private String callName;

	public NetworkCaller(String callName) {
		this.callName = callName;
	}
	
	/**
	 * Makes a blocking HTTP call with artificial delay.
	 * 
	 * IMPORTANT: This is BLOCKING code, but that's OK with virtual threads!
	 * - Traditional async/reactive code would use callbacks or reactive streams
	 * - With virtual threads, simple blocking code scales efficiently
	 * 
	 * @param secs Number of seconds the HTTP endpoint will delay before responding
	 * @return Response body as String
	 * @throws Exception if network call fails
	 */
	public String makeCall(int secs) throws Exception {
		
		// Print thread info BEFORE the I/O operation
		// You'll see the virtual thread is mounted on a carrier thread
		System.out.println(callName + " : BEG call : " + Thread.currentThread().getName());
		
		try {
			// This HTTP call is BLOCKING - the virtual thread will wait for response
			// But here's the magic: while waiting, the virtual thread UNMOUNTS
			// from its carrier thread, freeing the carrier to run other virtual threads
			URI uri = new URI("http://httpbin.org/delay/" + secs);
			try (InputStream stream = uri.toURL().openStream()) {
				// During stream.readAllBytes(), the virtual thread is unmounted
				return new String(stream.readAllBytes());
			}
			// After I/O completes, the virtual thread REMOUNTS (possibly on different carrier)
		}
		finally {
			// Print thread info AFTER the I/O operation
			// Notice: the virtual thread might be on a different carrier thread now!
			System.out.println(callName + " : END call : " + Thread.currentThread().getName());
		}
		
		// KEY INSIGHT:
		// This simple blocking code can handle thousands of concurrent calls
		// because virtual threads share a small pool of carrier threads efficiently
	}

}
