# Java Virtual Threads - Learning Repository

Master Java Virtual Threads (Project Loom) with practical examples focusing on **I/O-bound request handling** and **concurrent service operations**.

Based on: [Udemy Course - Virtual Threads](https://www.udemy.com/course/virtual-threads/learn/lecture/31002914?start=0#overview)

---

## 🎯 What You'll Learn

This repository demonstrates how Java Virtual Threads revolutionize I/O-bound applications by making the **thread-per-request** model scalable. Learn how to:

- ✅ Write simple blocking I/O code that scales to thousands of concurrent requests
- ✅ Integrate virtual threads with Spring Boot applications
- ✅ Use **Structured Concurrency** for safe parallel operations
- ✅ Apply **ScopedValue** for request-scoped data (better than ThreadLocal)
- ✅ Compare different I/O patterns (sequential, futures, functional, async)
- ✅ Build production-ready microservices with virtual threads

---

## 📚 Documentation

### Start Here 👇

1. **[VIRTUAL_THREADS_IO_GUIDE.md](VIRTUAL_THREADS_IO_GUIDE.md)** - Complete guide with theory, examples, and best practices
2. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Quick lookup for code patterns and commands

---

## 🗂️ Project Structure

### 🔰 Beginner: Basic Virtual Threads

#### `maxthreads-loom/`
**5 ways to create virtual threads**
- Using `Thread.ofVirtual()` builder
- Thread factory pattern
- Executor services
- Direct thread creation

**Start with**: `src/maxthreads/VirtualMethodsPlay.java`

#### `maxthreads/`
**Comparison: Platform vs Virtual Threads**
- See scalability differences
- Memory usage comparison
- Performance benchmarks

---

### 🌐 Intermediate: I/O Operations

#### `http-play/` ⭐ **ESSENTIAL FOR I/O UNDERSTANDING**
**4 patterns for handling I/O with virtual threads**

Files:
- `HttpPlayer.java` - Entry point, creates virtual threads
- `NetworkCaller.java` - Simulates real I/O (demonstrates unmounting)
- `UserRequestHandler.java` - **4 different I/O patterns**:
  1. Sequential (simple but slow: ~7s)
  2. Concurrent with Futures (fast: ~5s)
  3. Functional style (clean: ~5s)
  4. CompletableFuture (composable: ~9s with dependencies)

**Why This Matters**: Learn which pattern to use for your use case!

---

### 🚀 Advanced: Production Patterns

#### `springboot-projects/loomdemo/` 
**Spring Boot + Virtual Threads (Basic)**
- Enable with one line: `spring.threads.virtual.enabled=true`
- Every HTTP request automatically runs on virtual thread
- See thread information in responses

**Try it**:
```bash
cd springboot-projects/loomdemo
./mvnw spring-boot:run
curl http://localhost:8080/demo
```

#### `springboot-projects/bestpricebookstore/` ⭐ **PRODUCTION PATTERN**
**Real-world example: Query multiple bookstores concurrently**

Features:
- ✨ **StructuredTaskScope**: Safe concurrent operations
- ✨ **ScopedValue**: Request-scoped context (replaces ThreadLocal)
- ✨ **Fan-out/Fan-in**: Parallel queries with aggregation
- ✨ **Error handling**: Graceful failure handling
- ✨ **Performance tracking**: Timing statistics

Architecture:
```
HTTP Request → Virtual Thread (Spring Boot)
    ↓
    ScopedValue (timing stats)
    ↓
    StructuredTaskScope.fork()
    ├─ Virtual Thread A → Bookstore 1 (2s)
    ├─ Virtual Thread B → Bookstore 2 (2s)  } All in parallel!
    └─ Virtual Thread C → Bookstore 3 (2s)
    ↓
    scope.join() - wait for all
    ↓
    Find best price → Return result
```

**This is the pattern to use in production!**

#### `structured-play/`
**Structured Concurrency examples**
- `StructuredTaskScope` basic usage
- Custom executors
- Failure handling strategies
- Success/failure callbacks

#### `scoped-play/`
**ScopedValue vs ThreadLocal**
- Why ScopedValue is better for virtual threads
- Inheritable values across virtual threads
- Request-scoped context management

---

### 🔄 Supporting Examples

#### `futures-play/`
Traditional Futures and CompletableFutures (for comparison)

---

## 🚀 Quick Start

### Prerequisites
- Java 21+ (Virtual Threads are GA in Java 21)
- Maven (for Spring Boot projects)

### Run Standalone Examples

```bash
# 1. Basic Virtual Threads Creation
cd maxthreads-loom
javac --enable-preview --source 21 -d bin src/maxthreads/*.java
java --enable-preview -cp bin maxthreads.VirtualMethodsPlay

# 2. I/O Patterns (RECOMMENDED)
cd http-play
javac --enable-preview --source 21 -d bin src/com/mudra/*.java
java --enable-preview -cp bin com.mudra.HttpPlayer
# Watch the output - see different timing patterns!
```

### Run Spring Boot Examples

```bash
# 1. Basic Demo
cd springboot-projects/loomdemo
./mvnw spring-boot:run
# In another terminal:
curl http://localhost:8080/demo

# 2. Production Pattern
cd springboot-projects/bestpricebookstore
./mvnw spring-boot:run
# In another terminal:
curl "http://localhost:8080/virtualstore/book?name=Java+Concurrency"
```

---

## 🎓 Learning Path

### Day 1: Basics
1. Read **Core Concepts** in [VIRTUAL_THREADS_IO_GUIDE.md](VIRTUAL_THREADS_IO_GUIDE.md)
2. Run `maxthreads-loom/VirtualMethodsPlay.java`
3. Understand: mounting, unmounting, carrier threads

### Day 2: I/O Patterns ⭐
1. Read **Standalone Applications** section in guide
2. Run `http-play/HttpPlayer.java`
3. Study `UserRequestHandler.java` - 4 different patterns
4. **Exercise**: Modify `call()` method to try each pattern
5. Compare execution times

### Day 3: Spring Boot
1. Run `loomdemo` - see virtual threads in Spring Boot
2. Read **Spring Boot Integration** in guide
3. Understand configuration: `spring.threads.virtual.enabled=true`

### Day 4: Production Patterns ⭐
1. Study `bestpricebookstore` project
2. Understand **StructuredTaskScope** (safe concurrency)
3. Understand **ScopedValue** (request context)
4. Read **Advanced Patterns** in guide
5. This is what you'll use in real applications!

### Day 5: Practice
1. Build your own example
2. Try: Query multiple APIs in parallel
3. Use StructuredTaskScope for lifecycle management
4. Use ScopedValue for request context

---

## 💡 Key Concepts

### Why Virtual Threads for I/O?

**Traditional Platform Threads**:
- ~2MB per thread (expensive!)
- Limited to few thousand threads
- Complex thread pool management
- Blocking = wasting resources

**Virtual Threads**:
- ~1KB per thread (lightweight!)
- Millions possible concurrently
- Simple thread-per-request model
- Blocking = efficient (thread unmounts during I/O!)

### The Magic: Unmounting

```java
// This simple blocking code now scales!
String data = httpClient.get(url);  // Virtual thread UNMOUNTS here
process(data);                      // Continues after I/O completes
```

During the HTTP call:
1. Virtual thread **unmounts** from carrier (platform) thread
2. Carrier thread runs other virtual threads
3. When data arrives, virtual thread **remounts** (possibly different carrier)
4. Execution continues

**Result**: Thousands of virtual threads share a small pool of carriers efficiently!

---

## 📊 I/O Pattern Decision Guide

| Your Need | Use This Pattern | Example File |
|-----------|------------------|--------------|
| Operations depend on each other | Sequential | `UserRequestHandler.sequentialCall()` |
| Independent parallel operations | Futures | `UserRequestHandler.concurrentCallWithFutures()` |
| Functional style preferred | Functional | `UserRequestHandler.concurrentCallFunctional()` |
| Complex workflow with dependencies | CompletableFuture | `UserRequestHandler.concurrentCallCompletableFuture()` |
| Need lifecycle guarantees | StructuredTaskScope | `BookRetrievalService.getBookFromAllStores()` |
| Need request context | ScopedValue | `BestPriceBookController` + `BookRetrievalService` |

---

## ⚡ Performance Comparison

### Scenario: 10,000 concurrent requests, each makes 1-second I/O call

| Approach | Time | Throughput | Memory |
|----------|------|------------|--------|
| Platform threads (pool of 200) | ~50s | 200 req/s | ~400MB |
| Virtual threads | ~1s | 10,000 req/s | ~10MB |

**Virtual threads are 50x faster for this workload!**

---

## ✅ Best Practices Summary

### DO ✓
- Write simple blocking I/O code
- Use `try-with-resources` with ExecutorService
- Use StructuredTaskScope for structured concurrency
- Use ScopedValue instead of ThreadLocal
- Use ReentrantLock instead of synchronized
- Name your virtual threads for debugging

### DON'T ✗
- Don't use for CPU-bound tasks
- Don't create thread pools for virtual threads
- Don't use synchronized blocks (pins carrier thread)
- Don't use ThreadLocal (memory leaks)
- Don't forget to enable in Spring Boot config

See [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for code examples!

---

## 🔗 Resources

- **JEP 444**: [Virtual Threads](https://openjdk.org/jeps/444) (Java 21 - GA)
- **JEP 453**: [Structured Concurrency](https://openjdk.org/jeps/453) (Preview)
- **JEP 446**: [Scoped Values](https://openjdk.org/jeps/446) (Preview)
- **Course**: [Udemy - Virtual Threads](https://www.udemy.com/course/virtual-threads/)

---

## 🎯 Next Steps

1. **Understand the basics**: Read [VIRTUAL_THREADS_IO_GUIDE.md](VIRTUAL_THREADS_IO_GUIDE.md)
2. **Run examples**: Start with `http-play` project
3. **Learn patterns**: Study the 4 I/O patterns in `UserRequestHandler`
4. **Go production**: Study `bestpricebookstore` for real-world pattern
5. **Quick lookup**: Bookmark [QUICK_REFERENCE.md](QUICK_REFERENCE.md)

---

## 📝 Examples Summary

| Complexity | Project | Key Learning |
|------------|---------|--------------|
| 🔰 Basic | `maxthreads-loom` | How to create virtual threads |
| 🔰 Basic | `maxthreads` | Platform vs Virtual comparison |
| 📘 Intermediate | **`http-play`** ⭐ | **4 I/O patterns** |
| 📘 Intermediate | `futures-play` | Traditional async patterns |
| 🚀 Advanced | `loomdemo` | Spring Boot basic |
| 🚀 Advanced | **`bestpricebookstore`** ⭐ | **Production pattern** |
| 🚀 Advanced | `structured-play` | Structured concurrency |
| 🚀 Advanced | `scoped-play` | ScopedValue usage |

⭐ = Essential for understanding I/O with virtual threads

---

**Happy Learning! 🎉**

For questions or issues, refer to the comprehensive guide or course materials.

https://wiki.openjdk.org/display/loom/Main