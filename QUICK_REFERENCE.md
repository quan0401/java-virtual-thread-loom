# Virtual Threads I/O Quick Reference

## 🚀 Quick Start

### Enable in Spring Boot
```properties
# application.properties
spring.threads.virtual.enabled=true
```

### Create Virtual Thread Executor
```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> doWork());
}
```

---

## 📋 Code Patterns

### 1. Simple Virtual Thread
```java
Thread.startVirtualThread(() -> {
    // Your I/O code here
    String data = httpClient.get(url);
});
```

### 2. Named Virtual Threads
```java
ThreadFactory factory = Thread.ofVirtual()
    .name("worker-", 0)
    .factory();

try (ExecutorService executor = Executors.newThreadPerTaskExecutor(factory)) {
    executor.submit(() -> doWork());
}
```

### 3. Structured Concurrency
```java
try (var scope = new StructuredTaskScope<String>()) {
    Subtask<String> task1 = scope.fork(() -> fetchData1());
    Subtask<String> task2 = scope.fork(() -> fetchData2());
    
    scope.join();  // Wait for all
    
    String result1 = task1.get();
    String result2 = task2.get();
}
```

### 4. ScopedValue (Better than ThreadLocal)
```java
// Define
private static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();

// Set
ScopedValue.runWhere(CURRENT_USER, user, () -> {
    processRequest();
});

// Get (in same thread or child virtual threads)
User user = CURRENT_USER.get();
```

### 5. Concurrent with Futures
```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<String> future1 = executor.submit(() -> fetchData1());
    Future<String> future2 = executor.submit(() -> fetchData2());
    
    String result1 = future1.get();  // Blocking is OK!
    String result2 = future2.get();
}
```

### 6. CompletableFuture
```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    CompletableFuture<String> future = CompletableFuture
        .supplyAsync(() -> fetchData1(), executor)
        .thenCombine(
            CompletableFuture.supplyAsync(() -> fetchData2(), executor),
            (r1, r2) -> r1 + r2
        );
    
    String result = future.join();
}
```

---

## 🎯 When to Use What

| Pattern | Use When | Example |
|---------|----------|---------|
| Sequential | Operations depend on each other | Get user → Get orders → Get details |
| Futures | Independent parallel operations | Query multiple databases |
| Functional | Many similar parallel tasks | Process 100 files |
| CompletableFuture | Complex workflows with dependencies | Get data → Transform → Save → Notify |
| StructuredTaskScope | Need guaranteed cleanup | Fan-out to multiple services |

---

## ✅ Best Practices

### DO ✓
```java
// ✓ Use try-with-resources
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> work());
}

// ✓ Use ReentrantLock
ReentrantLock lock = new ReentrantLock();
lock.lock();
try { doWork(); } finally { lock.unlock(); }

// ✓ Use ScopedValue
ScopedValue<Context> CONTEXT = ScopedValue.newInstance();

// ✓ Write blocking I/O code
String data = httpClient.get(url);  // Simple!

// ✓ Name your threads
Thread.ofVirtual().name("api-worker-", 0)
```

### DON'T ✗
```java
// ✗ Don't use synchronized blocks
synchronized (lock) {
    makeNetworkCall();  // Pins carrier thread!
}

// ✗ Don't create thread pools for virtual threads
ExecutorService pool = Executors.newFixedThreadPool(200);
pool.submit(() -> Thread.ofVirtual().start(...));  // Wasteful!

// ✗ Don't use ThreadLocal
ThreadLocal<Data> local = new ThreadLocal<>();  // Memory leaks with virtual threads

// ✗ Don't use for CPU-bound tasks
Thread.ofVirtual().start(() -> {
    calculatePrimes(1_000_000);  // Use platform threads instead!
});
```

---

## 🔧 Common Operations

### Check if Virtual Thread
```java
if (Thread.currentThread().isVirtual()) {
    System.out.println("Running on virtual thread!");
}
```

### Spring Boot Async with Virtual Threads
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    // With spring.threads.virtual.enabled=true,
    // @Async automatically uses virtual threads!
}

@Async
public CompletableFuture<String> asyncOperation() {
    // Runs on virtual thread
    return CompletableFuture.completedFuture(doWork());
}
```

### Fan-Out / Fan-In Pattern
```java
public List<Result> queryMultipleServices(List<String> urls) {
    try (var scope = new StructuredTaskScope<Result>()) {
        // Fan-Out
        List<Subtask<Result>> tasks = urls.stream()
            .map(url -> scope.fork(() -> callService(url)))
            .toList();
        
        scope.join();  // Wait
        
        // Fan-In
        return tasks.stream()
            .filter(t -> t.state() == State.SUCCESS)
            .map(Subtask::get)
            .toList();
    }
}
```

### First Success Pattern
```java
try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
    scope.fork(() -> tryServer1());
    scope.fork(() -> tryServer2());
    scope.fork(() -> tryServer3());
    
    scope.join();
    return scope.result();  // First one to succeed
}
```

### Fail Fast Pattern
```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    scope.fork(() -> task1());
    scope.fork(() -> task2());
    
    scope.join();
    scope.throwIfFailed();  // Throws if any failed
}
```

---

## 🐛 Troubleshooting

### Thread Pinning Issues

**Problem**: Carrier thread is "pinned" and can't run other virtual threads

**Causes**:
1. `synchronized` blocks
2. Native methods
3. Some JNI calls

**Solutions**:
```java
// Instead of synchronized
synchronized (lock) { work(); }  // ✗ Pins

// Use ReentrantLock
ReentrantLock lock = new ReentrantLock();
lock.lock();
try { work(); } finally { lock.unlock(); }  // ✓ Doesn't pin
```

### Memory Leaks with ThreadLocal

**Problem**: Virtual threads are reused, ThreadLocal accumulates data

**Solution**: Use ScopedValue instead
```java
// Old way - can leak
ThreadLocal<LargeObject> tl = new ThreadLocal<>();

// New way - auto cleanup
ScopedValue<LargeObject> sv = ScopedValue.newInstance();
ScopedValue.runWhere(sv, object, () -> work());
```

---

## 📊 Performance Tips

### Virtual Threads are Great For:
- ✅ Database queries
- ✅ HTTP/REST calls
- ✅ File I/O
- ✅ Message queues
- ✅ WebSocket connections
- ✅ Microservice calls

### Virtual Threads are Bad For:
- ❌ CPU-intensive computation
- ❌ Cryptography
- ❌ Image processing
- ❌ Data compression
- ❌ Machine learning

### Monitoring
```bash
# Enable JFR for virtual threads
java -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=recording.jfr \
     YourApp

# View with JMC (Java Mission Control)
```

---

## 📚 Code Examples in This Repo

| Example | Location | Demonstrates |
|---------|----------|--------------|
| Basic Virtual Threads | `maxthreads-loom/VirtualMethodsPlay.java` | 5 ways to create virtual threads |
| HTTP I/O Patterns | `http-play/UserRequestHandler.java` | 4 I/O patterns comparison |
| Spring Boot Basic | `springboot-projects/loomdemo/` | Simple Spring Boot integration |
| Production Pattern | `springboot-projects/bestpricebookstore/` | StructuredTaskScope + ScopedValue |
| Structured Concurrency | `structured-play/` | Various StructuredTaskScope examples |
| Scoped Values | `scoped-play/` | ScopedValue vs ThreadLocal |

---

## 🎓 Learning Path

1. **Start Here**: Read `VIRTUAL_THREADS_IO_GUIDE.md` for complete understanding
2. **Try**: Run `maxthreads-loom/VirtualMethodsPlay.java` - basic virtual thread creation
3. **Practice**: Modify `http-play/UserRequestHandler.java` - try different patterns
4. **Spring**: Run `springboot-projects/loomdemo/` - see Spring Boot integration
5. **Advanced**: Study `springboot-projects/bestpricebookstore/` - production patterns

---

## 🔗 Key JEPs (Java Enhancement Proposals)

- **JEP 444**: Virtual Threads (Java 21 - GA)
- **JEP 453**: Structured Concurrency (Preview)
- **JEP 446**: Scoped Values (Preview)

---

## 💡 Decision Tree

```
Do you have I/O-bound operations?
├─ YES → Use Virtual Threads
│   ├─ Spring Boot? → spring.threads.virtual.enabled=true
│   ├─ Standalone? → Executors.newVirtualThreadPerTaskExecutor()
│   ├─ Need lifecycle? → StructuredTaskScope
│   └─ Need context? → ScopedValue
│
└─ NO → Don't use Virtual Threads
    ├─ CPU-bound? → Use platform thread pool
    ├─ Need backpressure? → Use reactive (WebFlux)
    └─ Simple app? → Platform threads fine
```

---

## 📖 Quick Reference Commands

### Compile and Run (Standalone)
```bash
# Navigate to project
cd http-play

# Compile
javac --enable-preview --source 21 -d bin src/com/mudra/*.java

# Run
java --enable-preview -cp bin com.mudra.HttpPlayer
```

### Run Spring Boot
```bash
cd springboot-projects/loomdemo
./mvnw spring-boot:run

# Test
curl http://localhost:8080/demo
```

---

**For detailed explanations, see [VIRTUAL_THREADS_IO_GUIDE.md](VIRTUAL_THREADS_IO_GUIDE.md)**

