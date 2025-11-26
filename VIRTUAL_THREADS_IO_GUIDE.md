# Java Virtual Threads with Request I/O Services - Complete Guide

## Table of Contents
1. [Introduction](#introduction)
2. [Core Concepts](#core-concepts)
3. [Standalone Applications](#standalone-applications)
4. [Spring Boot Integration](#spring-boot-integration)
5. [Advanced Patterns](#advanced-patterns)
6. [Performance Considerations](#performance-considerations)
7. [Best Practices](#best-practices)
8. [Quick Reference](#quick-reference)

---

## Introduction

Java Virtual Threads (Project Loom) revolutionize how we handle I/O-bound operations by making the thread-per-request model scalable. This guide demonstrates how to use virtual threads effectively in request I/O services.

### Why Virtual Threads for I/O?

**Traditional Platform Threads:**
- Heavy weight (~2MB stack memory each)
- Limited by OS resources (typically thousands)
- Thread pool management complexity
- Context switching overhead

**Virtual Threads:**
- Lightweight (~1KB memory)
- Millions can run concurrently
- Managed by JVM, not OS
- Simple thread-per-request model
- **Blocking is OK!** - Virtual threads are unmounted during I/O

---

## Core Concepts

### Platform Threads vs Virtual Threads

```java
// Platform Thread (Heavy)
Thread platformThread = new Thread(() -> {
    // This blocks an OS thread during I/O
    makeNetworkCall();
});

// Virtual Thread (Lightweight)
Thread virtualThread = Thread.ofVirtual().start(() -> {
    // This releases the carrier thread during I/O
    makeNetworkCall();
});
```

### The Virtual Thread Lifecycle

1. **Running**: Executing on a carrier thread (platform thread)
2. **Mounted**: Virtual thread is scheduled on a carrier
3. **Unmounted**: During blocking I/O, releases carrier for other virtual threads
4. **Resumed**: Continues on potentially different carrier after I/O completes

### Key Insight: Blocking is Acceptable

With virtual threads, you can write **simple blocking code** that scales:

```java
// This scales to thousands of concurrent requests!
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10000; i++) {
        executor.submit(() -> {
            String data = httpClient.get(url);  // Blocking call - OK!
            processData(data);
        });
    }
}
```

---

## Standalone Applications

Your `http-play` project demonstrates virtual threads handling concurrent I/O operations.

### Example 1: HttpPlayer - Entry Point

**Location**: `http-play/src/com/mudra/HttpPlayer.java`

```java
ThreadFactory factory = Thread.ofVirtual()
    .name("request-handler-", 0)  // Names: request-handler-0, request-handler-1, etc.
    .factory();

try (ExecutorService executor = Executors.newThreadPerTaskExecutor(factory)) {
    IntStream.range(0, NUM_USERS).forEach(j -> {
        executor.submit(new UserRequestHandler());
    });
}
```

**Key Points:**
- Creates virtual thread factory with custom naming
- `newThreadPerTaskExecutor()` creates a new virtual thread for each task
- Try-with-resources ensures all threads complete before exit
- Scales to thousands/millions of users

### Example 2: NetworkCaller - Simulating I/O

**Location**: `http-play/src/com/mudra/NetworkCaller.java`

```java
public String makeCall(int secs) throws Exception {
    System.out.println(callName + " : BEG call : " + Thread.currentThread());
    
    URI uri = new URI("http://httpbin.org/delay/" + secs);
    try (InputStream stream = uri.toURL().openStream()) {
        return new String(stream.readAllBytes());
    }
    // Virtual thread is unmounted during network I/O
}
```

**What Happens:**
1. Virtual thread starts the HTTP request
2. During network I/O, virtual thread **unmounts** from carrier
3. Carrier thread is free to run other virtual threads
4. When response arrives, virtual thread **remounts** and continues

### Example 3: UserRequestHandler - Four I/O Patterns

**Location**: `http-play/src/com/mudra/UserRequestHandler.java`

#### Pattern 1: Sequential (Simple but Slow)

```java
private String sequentialCall() throws Exception {
    long start = System.currentTimeMillis();
    
    String result1 = dbCall();    // 2 seconds - blocks
    String result2 = restCall();  // 5 seconds - blocks
    
    String result = String.format("[%s,%s]", result1, result2);
    
    long end = System.currentTimeMillis();
    // Total time: ~7 seconds (2 + 5)
    
    return result;
}
```

**Pros:** Simple, easy to understand  
**Cons:** Slow - operations run one after another  
**Use When:** Operations depend on each other

#### Pattern 2: Concurrent with Futures (Fast)

```java
private String concurrentCallWithFutures() throws Exception {
    try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
        long start = System.currentTimeMillis();
        
        Future<String> dbFuture   = service.submit(this::dbCall);   // 2 secs
        Future<String> restFuture = service.submit(this::restCall); // 5 secs
        
        String result = String.format("[%s,%s]", dbFuture.get(), restFuture.get());
        
        long end = System.currentTimeMillis();
        // Total time: ~5 seconds (max of 2 and 5)
        
        return result;
    }
}
```

**Pros:** Fast - operations run in parallel  
**Cons:** Slightly more complex  
**Use When:** Operations are independent

#### Pattern 3: Functional Style (Clean)

```java
private String concurrentCallFunctional() throws Exception {
    try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
        
        String result = service.invokeAll(Arrays.asList(this::dbCall, this::restCall))
            .stream()
            .map(f -> {
                try {
                    return (String)f.get();
                } catch (Exception e) {
                    return null;
                }
            })
            .collect(Collectors.joining(","));
        
        return "[" + result + "]";
    }
}
```

**Pros:** Functional, composable, clean  
**Cons:** Exception handling can be awkward  
**Use When:** You prefer functional programming style

#### Pattern 4: CompletableFuture (Most Flexible)

```java
private String concurrentCallCompletableFuture() {
    try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
        
        String output = CompletableFuture
            .supplyAsync(this::dbCall, service)
            .thenCombine(
                CompletableFuture.supplyAsync(this::restCall, service),
                (result1, result2) -> "[" + result1 + "," + result2 + "]"
            )
            .thenApply(result -> {
                // Both dbCall and restCall have completed
                String r = externalCall();
                return "[" + result + "," + r + "]";
            })
            .join();
        
        return output;
    }
}
```

**Pros:** Composable, supports complex workflows, excellent error handling  
**Cons:** Most complex, learning curve  
**Use When:** You need complex async workflows with dependencies

### Pattern Comparison Summary

| Pattern | Time | Complexity | Best For |
|---------|------|------------|----------|
| Sequential | ~7s | Low | Dependent operations |
| Futures | ~5s | Medium | Independent parallel operations |
| Functional | ~5s | Medium | Functional programming style |
| CompletableFuture | ~5s | High | Complex async workflows |

---

## Spring Boot Integration

Virtual threads integrate seamlessly with Spring Boot, making every HTTP request run on a virtual thread.

### Basic Setup: loomdemo

**Location**: `springboot-projects/loomdemo/`

#### Configuration

**File**: `src/main/resources/application.properties`

```properties
# Enable Virtual Threads for all requests
spring.threads.virtual.enabled=true

# Optional: Logging configuration
logging.level.web=DEBUG
logging.pattern.console=%d{HH:mm:ss} [%15thread] %msg%n
```

#### Simple Controller

**File**: `src/main/java/com/mudra/loomdemo/DemoController.java`

```java
@RestController
public class DemoController {
    
    @GetMapping(path = "/demo")
    public String getThreadInfo() {
        return Thread.currentThread().toString();
    }
}
```

**What Happens:**
1. Spring Boot creates virtual thread for each incoming request
2. You'll see output like: `VirtualThread[#21]/runnable@ForkJoinPool-1-worker-1`
3. Simple blocking code now scales to thousands of concurrent requests

**Test it:**
```bash
curl http://localhost:8080/demo
# Output: VirtualThread[#23]/runnable@ForkJoinPool-1-worker-3
```

### Advanced Example: Best Price Bookstore

**Location**: `springboot-projects/bestpricebookstore/`

This example demonstrates a real-world use case: querying multiple bookstores concurrently to find the best price.

#### Controller

**File**: `src/main/java/com/mudra/bestpricebookstore/BestPriceBookController.java`

```java
@RestController
@RequestMapping("/virtualstore")
public class BestPriceBookController {
    
    // ScopedValue for request-scoped statistics
    public static final ScopedValue<RestCallStatistics> TIMEMAP = ScopedValue.newInstance();
    
    @Autowired
    private BookRetrievalService retrievalService;
    
    @GetMapping("/book")
    public BestPriceResult getBestPriceForBook(@RequestParam String name) {
        long start = System.currentTimeMillis();
        
        RestCallStatistics timeObj = new RestCallStatistics();
        try {
            // Run with ScopedValue context
            List<Book> books = ScopedValue.callWhere(
                TIMEMAP, timeObj,
                () -> retrievalService.getBookFromAllStores(name)
            );
            
            // Find best price
            Book bestPriceBook = books.stream()
                .min(Comparator.comparing(Book::cost))
                .orElseThrow();

            return new BestPriceResult(timeObj, bestPriceBook, books);
        } catch (Exception e) {
            throw new RuntimeException("Exception while calling getBestPrice", e);
        } finally {
            long end = System.currentTimeMillis();
            timeObj.addTiming("Best Price Store", end - start);
            timeObj.dumpTiming();
        }
    }
}
```

**Key Concepts:**
1. **Request runs on virtual thread** (via `spring.threads.virtual.enabled=true`)
2. **ScopedValue**: Thread-safe request-scoped data (better than ThreadLocal)
3. **Service delegates** to structured concurrency

#### Service with Structured Concurrency

**File**: `src/main/java/com/mudra/bestpricebookstore/BookRetrievalService.java`

```java
@Service
public class BookRetrievalService {

    @Value("#{${book.store.baseurls}}")
    private Map<String,String> storeUrlMap;
    
    RestClient restClient = RestClient.create();
    
    public List<Book> getBookFromAllStores(String bookName) throws InterruptedException {
        
        // Create virtual thread factory for child tasks
        ThreadFactory factory = Thread.ofVirtual().name("book-store-thr", 0).factory();
        
        try (var scope = new StructuredTaskScope<Book>("virtualstore", factory)) {
            
            // Fork a subtask for each bookstore
            List<Subtask<Book>> bookTasks = new ArrayList<>();
            storeUrlMap.forEach((name, url) -> {
                bookTasks.add(scope.fork(() -> getBookFromStore(name, url, bookName)));
            });
            
            // Wait for all subtasks to complete
            scope.join();
            
            // Log any failures
            bookTasks.stream()
                .filter(t -> t.state() == State.FAILED)
                .map(Subtask::exception)
                .forEach(e -> e.printStackTrace());
            
            // Return successful results
            return bookTasks.stream()
                .filter(t -> t.state() == State.SUCCESS)
                .map(Subtask::get)
                .toList();
        }
    }

    private Book getBookFromStore(String storeName, String url, String bookName) {
        long start = System.currentTimeMillis();
        
        // Blocking REST call - OK with virtual threads!
        Book book = restClient.get()
            .uri(url + "/store/book", t -> t.queryParam("name", bookName).build())
            .retrieve()
            .body(Book.class);
        
        long end = System.currentTimeMillis();
        
        // Access ScopedValue from parent
        RestCallStatistics timeObj = BestPriceBookController.TIMEMAP.get();
        timeObj.addTiming(storeName, end - start);
        
        return book;
    }
}
```

**Architecture Flow:**

```
HTTP Request → Virtual Thread A (Spring)
    ↓
    ScopedValue.callWhere() sets TIMEMAP
    ↓
    StructuredTaskScope.fork() → Virtual Thread B (Store 1)
                              → Virtual Thread C (Store 2)
                              → Virtual Thread D (Store 3)
    ↓
    Each child thread:
    - Makes blocking HTTP call
    - Accesses parent's ScopedValue
    - Records timing
    ↓
    scope.join() waits for all
    ↓
    Return results to client
```

---

## Advanced Patterns

### Pattern 1: Structured Concurrency

**Structured Concurrency** ensures that child tasks don't outlive their parent.

```java
try (var scope = new StructuredTaskScope<String>()) {
    
    Subtask<String> task1 = scope.fork(() -> fetchUserData());
    Subtask<String> task2 = scope.fork(() -> fetchOrderData());
    Subtask<String> task3 = scope.fork(() -> fetchPaymentData());
    
    scope.join();  // Wait for all tasks
    
    // All tasks complete before leaving try block
    if (task1.state() == State.SUCCESS) {
        String userData = task1.get();
    }
}
// Tasks CANNOT run beyond this point
```

**Benefits:**
- No orphaned threads
- Clear lifecycle management
- Better resource cleanup
- Easier error handling

### Pattern 2: Fan-Out / Fan-In

Query multiple services concurrently and aggregate results:

```java
public List<Result> queryMultipleServices(List<String> serviceUrls) {
    ThreadFactory factory = Thread.ofVirtual().factory();
    
    try (var scope = new StructuredTaskScope<Result>("fan-out", factory)) {
        
        // Fan-Out: Fork tasks for each service
        List<Subtask<Result>> tasks = serviceUrls.stream()
            .map(url -> scope.fork(() -> callService(url)))
            .toList();
        
        scope.join();  // Wait for all
        
        // Fan-In: Collect successful results
        return tasks.stream()
            .filter(t -> t.state() == State.SUCCESS)
            .map(Subtask::get)
            .toList();
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

### Pattern 3: ScopedValue for Request Context

**ScopedValue** replaces ThreadLocal with better semantics for virtual threads:

```java
public class RequestProcessor {
    
    // Define scoped value
    private static final ScopedValue<RequestContext> CONTEXT = ScopedValue.newInstance();
    
    public void handleRequest(String userId, String requestId) {
        RequestContext ctx = new RequestContext(userId, requestId);
        
        // Set value for this scope
        ScopedValue.runWhere(CONTEXT, ctx, () -> {
            processRequest();
            // Child virtual threads inherit CONTEXT
        });
    }
    
    private void processRequest() {
        // Access context anywhere in the call stack
        RequestContext ctx = CONTEXT.get();
        log("Processing for user: " + ctx.getUserId());
        
        // Context automatically propagates to child virtual threads
        try (var scope = new StructuredTaskScope<String>()) {
            scope.fork(() -> {
                // This child thread also has access to CONTEXT!
                RequestContext childCtx = CONTEXT.get();
                return "User: " + childCtx.getUserId();
            });
            scope.join();
        }
    }
}
```

**Why ScopedValue over ThreadLocal:**
- **Immutable**: Cannot be changed once set
- **Scoped**: Automatically cleared when scope exits
- **Inheritable**: Child virtual threads inherit values
- **Performance**: More efficient than ThreadLocal

### Pattern 4: First Success (Race Pattern)

Return as soon as any task succeeds:

```java
public String getFirstSuccessfulResult(List<String> urls) throws Exception {
    
    try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
        
        // Fork all tasks
        urls.forEach(url -> scope.fork(() -> fetchData(url)));
        
        // Wait for first success
        scope.join();
        
        // Return first successful result
        return scope.result();  // Cancels other tasks
    }
}
```

### Pattern 5: Fail Fast

Cancel all tasks if any fails:

```java
public List<String> getAllOrNone(List<String> urls) throws Exception {
    
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        
        List<Subtask<String>> tasks = urls.stream()
            .map(url -> scope.fork(() -> fetchData(url)))
            .toList();
        
        // Wait for all, but shutdown on first failure
        scope.join();
        scope.throwIfFailed();  // Throws if any task failed
        
        return tasks.stream()
            .map(Subtask::get)
            .toList();
    }
}
```

---

## Performance Considerations

### Memory Usage

**Platform Threads:**
```
1,000 threads × 2MB stack = 2GB memory
```

**Virtual Threads:**
```
1,000,000 threads × ~1KB = ~1GB memory
```

### Throughput Comparison

**Scenario**: Handle 10,000 concurrent requests, each making a 1-second I/O call

**Platform Thread Pool (200 threads):**
- Time: ~50 seconds (10,000 / 200 = 50 batches)
- Requests/sec: ~200

**Virtual Threads:**
- Time: ~1 second (all concurrent)
- Requests/sec: ~10,000

### When to Use Virtual Threads

✅ **GOOD Use Cases:**
- I/O-bound operations (HTTP, database, file I/O)
- High concurrency requirements
- Simple blocking code preferred
- Microservices with many external calls
- WebSocket connections
- Long-running requests

❌ **BAD Use Cases:**
- CPU-bound operations (heavy computation)
- Already using reactive programming effectively
- Need thread pooling for rate limiting
- Synchronized blocks (can pin carrier threads)
- Thread pooling required for other reasons

### Common Pitfalls

#### Pitfall 1: Synchronized Blocks

```java
// BAD: Virtual thread pins carrier during synchronized block
synchronized (lock) {
    makeNetworkCall();  // Virtual thread CANNOT unmount!
}

// GOOD: Use ReentrantLock instead
lock.lock();
try {
    makeNetworkCall();  // Virtual thread CAN unmount
} finally {
    lock.unlock();
}
```

#### Pitfall 2: Thread Pools with Virtual Threads

```java
// BAD: Unnecessary thread pool
ExecutorService pool = Executors.newFixedThreadPool(200);  // Don't do this!
pool.submit(() -> {
    Thread.ofVirtual().start(() -> doWork());  // Wasteful
});

// GOOD: Direct virtual threads
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> doWork());
}
```

#### Pitfall 3: ThreadLocal Accumulation

```java
// BAD: ThreadLocal with virtual threads
ThreadLocal<LargeObject> local = new ThreadLocal<>();
// Virtual threads are reused, can cause memory leaks

// GOOD: Use ScopedValue
ScopedValue<LargeObject> scoped = ScopedValue.newInstance();
```

---

## Best Practices

### 1. Use Try-With-Resources

```java
// Ensures all virtual threads complete
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> doWork());
}
// All tasks complete before continuing
```

### 2. Name Your Virtual Threads

```java
Thread.ofVirtual()
    .name("api-caller-", 0)  // api-caller-0, api-caller-1, etc.
    .factory();
```

### 3. Use Structured Concurrency

```java
// Prevents orphaned threads
try (var scope = new StructuredTaskScope<String>()) {
    scope.fork(() -> task1());
    scope.fork(() -> task2());
    scope.join();
}
```

### 4. Prefer ScopedValue over ThreadLocal

```java
private static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();

ScopedValue.runWhere(CURRENT_USER, user, () -> {
    processRequest();  // All child threads see user
});
```

### 5. Keep Blocking Code Simple

```java
// This is fine with virtual threads!
String data = httpClient.get(url);
process(data);
String result = database.query(sql);
return result;
```

### 6. Use ReentrantLock Instead of Synchronized

```java
private final ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    // Virtual thread can unmount during I/O
    doWork();
} finally {
    lock.unlock();
}
```

### 7. Monitor Virtual Threads

```java
// Enable JFR (Java Flight Recorder)
// Monitor virtual thread events
-XX:+FlightRecorder
-XX:StartFlightRecording=duration=60s,filename=recording.jfr
```

---

## Quick Reference

### Creating Virtual Threads

```java
// Method 1: Start directly
Thread.startVirtualThread(() -> doWork());

// Method 2: Using builder
Thread vThread = Thread.ofVirtual()
    .name("worker")
    .start(() -> doWork());

// Method 3: Factory
ThreadFactory factory = Thread.ofVirtual()
    .name("worker-", 0)
    .factory();
Thread t = factory.newThread(() -> doWork());
t.start();

// Method 4: Executor (RECOMMENDED)
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> doWork());
}

// Method 5: Custom factory with executor
ThreadFactory factory = Thread.ofVirtual().name("custom-", 0).factory();
try (ExecutorService executor = Executors.newThreadPerTaskExecutor(factory)) {
    executor.submit(() -> doWork());
}
```

### Spring Boot Configuration

```properties
# application.properties
spring.threads.virtual.enabled=true
```

### Structured Concurrency Patterns

```java
// Pattern 1: Wait for all
try (var scope = new StructuredTaskScope<String>()) {
    var task1 = scope.fork(() -> work1());
    var task2 = scope.fork(() -> work2());
    scope.join();
    
    String result1 = task1.get();
    String result2 = task2.get();
}

// Pattern 2: First success
try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
    scope.fork(() -> work1());
    scope.fork(() -> work2());
    scope.join();
    return scope.result();
}

// Pattern 3: Fail fast
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    scope.fork(() -> work1());
    scope.fork(() -> work2());
    scope.join();
    scope.throwIfFailed();
}
```

### ScopedValue Usage

```java
// Define
private static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();

// Set and run
ScopedValue.runWhere(CURRENT_USER, user, () -> {
    doWork();
});

// Set and call (with return value)
String result = ScopedValue.callWhere(CURRENT_USER, user, () -> {
    return processUser();
});

// Access
User user = CURRENT_USER.get();

// Check if bound
if (CURRENT_USER.isBound()) {
    User user = CURRENT_USER.get();
}
```

### Decision Tree

```
Need high concurrency with I/O?
├─ Yes → Use Virtual Threads
│   ├─ Spring Boot? → Set spring.threads.virtual.enabled=true
│   ├─ Standalone? → Use Executors.newVirtualThreadPerTaskExecutor()
│   ├─ Need structured concurrency? → Use StructuredTaskScope
│   └─ Need request context? → Use ScopedValue
│
└─ No → Consider platform threads or reactive programming
    ├─ CPU-bound? → Use platform thread pool
    ├─ Need backpressure? → Use reactive (Reactor, RxJava)
    └─ Low concurrency? → Platform threads are fine
```

---

## Summary

**Virtual Threads** make the **thread-per-request** model scalable for I/O-bound applications:

1. ✅ Write **simple blocking code** that scales
2. ✅ **Millions of concurrent requests** possible
3. ✅ **No thread pool management** complexity
4. ✅ **Structured concurrency** prevents thread leaks
5. ✅ **ScopedValue** for clean request context

**Key Takeaway**: With virtual threads, you can write straightforward, blocking I/O code that's easy to understand and maintain, while achieving the scalability previously only possible with complex async/reactive programming.

---

## Additional Resources

- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [JEP 453: Structured Concurrency (Preview)](https://openjdk.org/jeps/453)
- [JEP 446: Scoped Values (Preview)](https://openjdk.org/jeps/446)
- Your code examples in this repository!

---

**Happy coding with Virtual Threads! 🚀**

