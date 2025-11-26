# Virtual Threads Learning Path - Visual Guide

## 🗺️ Your Journey Through Virtual Threads for I/O

```
                    START HERE
                        ↓
        ┌──────────────────────────────┐
        │     README.md (Overview)      │
        │  - What you'll learn          │
        │  - Project structure          │
        │  - Quick start                │
        └──────────────┬───────────────┘
                       ↓
        ┌──────────────────────────────┐
        │ VIRTUAL_THREADS_IO_GUIDE.md  │
        │  - Read Sections 1-2 first   │
        │  - Core concepts explained   │
        └──────────────┬───────────────┘
                       ↓
            ╔══════════════════════╗
            ║   WEEK 1: BASICS     ║
            ╚══════════════════════╝
                       ↓
        ┌──────────────────────────────┐
        │   maxthreads-loom/           │
        │   VirtualMethodsPlay.java    │
        │                              │
        │   Learn: 5 ways to create    │
        │   virtual threads            │
        │   Time: 1 hour               │
        └──────────────┬───────────────┘
                       ↓
        ┌──────────────────────────────┐
        │   maxthreads/                │
        │   MainJacket.java            │
        │                              │
        │   Learn: Platform vs Virtual │
        │   comparison                 │
        │   Time: 30 minutes           │
        └──────────────┬───────────────┘
                       ↓
            ╔══════════════════════╗
            ║   WEEK 2: I/O        ║
            ║   ⭐ CRITICAL ⭐      ║
            ╚══════════════════════╝
                       ↓
        ┌──────────────────────────────┐
        │   http-play/                 │
        │   HttpPlayer.java            │
        │   NetworkCaller.java         │
        │   UserRequestHandler.java    │
        │                              │
        │   Learn: 4 I/O PATTERNS      │
        │   1. Sequential (7s)         │
        │   2. Futures (5s)            │
        │   3. Functional (5s)         │
        │   4. CompletableFuture (9s)  │
        │                              │
        │   Time: 3 hours              │
        │   ⭐ MOST IMPORTANT! ⭐       │
        └──────────────┬───────────────┘
                       ↓
                [CHECKPOINT]
                Can you explain:
                - When to use each pattern?
                - Why virtual threads help I/O?
                - What unmounting means?
                       ↓
                    [YES] → Continue
                    [NO]  → Review guide Section 3
                       ↓
            ╔══════════════════════╗
            ║   WEEK 3: SPRING     ║
            ╚══════════════════════╝
                       ↓
        ┌──────────────────────────────┐
        │   springboot-projects/       │
        │   loomdemo/                  │
        │                              │
        │   Learn: Spring Boot basics  │
        │   - Configuration            │
        │   - Auto virtual threads     │
        │   - Simple endpoints         │
        │                              │
        │   Time: 1 hour               │
        └──────────────┬───────────────┘
                       ↓
            ╔══════════════════════╗
            ║   WEEK 4: PRODUCTION ║
            ║   ⭐ ESSENTIAL ⭐     ║
            ╚══════════════════════╝
                       ↓
        ┌──────────────────────────────┐
        │   springboot-projects/       │
        │   bestpricebookstore/        │
        │                              │
        │   Learn: PRODUCTION PATTERN  │
        │   - StructuredTaskScope      │
        │   - ScopedValue              │
        │   - Fan-out/Fan-in           │
        │   - Error handling           │
        │                              │
        │   Time: 4 hours              │
        │   ⭐ USE THIS IN PROD! ⭐     │
        └──────────────┬───────────────┘
                       ↓
        ┌──────────────────────────────┐
        │   structured-play/           │
        │                              │
        │   Learn: More structured     │
        │   concurrency patterns       │
        │                              │
        │   Time: 2 hours              │
        └──────────────┬───────────────┘
                       ↓
        ┌──────────────────────────────┐
        │   scoped-play/               │
        │                              │
        │   Learn: ScopedValue vs      │
        │   ThreadLocal                │
        │                              │
        │   Time: 1 hour               │
        └──────────────┬───────────────┘
                       ↓
                [FINAL CHECKPOINT]
                Can you build:
                - Spring Boot app with virtual threads?
                - Parallel API queries?
                - Request-scoped context?
                       ↓
                    [YES] → MASTERY! 🎉
                    [NO]  → Review bestpricebookstore
                       ↓
            ╔══════════════════════╗
            ║   BUILD YOUR OWN     ║
            ╚══════════════════════╝
```

---

## 📚 Reading Order for Documentation

```
┌─────────────────────────────────────────────┐
│  First Time Learning                        │
├─────────────────────────────────────────────┤
│  1. README.md (Overview)                    │
│     ↓                                       │
│  2. VIRTUAL_THREADS_IO_GUIDE.md             │
│     - Section 1: Introduction               │
│     - Section 2: Core Concepts              │
│     ↓                                       │
│  3. Run: maxthreads-loom examples           │
│     ↓                                       │
│  4. VIRTUAL_THREADS_IO_GUIDE.md             │
│     - Section 3: Standalone Applications    │
│     ↓                                       │
│  5. Run: http-play examples                 │
│     ↓                                       │
│  6. VIRTUAL_THREADS_IO_GUIDE.md             │
│     - Section 4: Spring Boot Integration    │
│     ↓                                       │
│  7. Run: loomdemo                           │
│     ↓                                       │
│  8. VIRTUAL_THREADS_IO_GUIDE.md             │
│     - Section 5: Advanced Patterns          │
│     ↓                                       │
│  9. Run: bestpricebookstore                 │
│     ↓                                       │
│  10. VIRTUAL_THREADS_IO_GUIDE.md            │
│      - Section 6: Performance               │
│      - Section 7: Best Practices            │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  Daily Reference                            │
├─────────────────────────────────────────────┤
│  → QUICK_REFERENCE.md                       │
│    Keep this bookmarked!                    │
│    - Code patterns                          │
│    - Commands                               │
│    - Best practices                         │
└─────────────────────────────────────────────┘
```

---

## 🎯 Learning Objectives by Week

### Week 1: Virtual Thread Basics
**Goal**: Understand what virtual threads are

- [ ] Can explain mounting/unmounting
- [ ] Know 5 ways to create virtual threads
- [ ] Understand platform vs virtual differences
- [ ] Can run basic virtual thread examples

**Time**: 3-4 hours  
**Projects**: `maxthreads-loom`, `maxthreads`

---

### Week 2: I/O Patterns ⭐
**Goal**: Master I/O handling with virtual threads

- [ ] Understand why virtual threads excel at I/O
- [ ] Can implement sequential pattern
- [ ] Can implement parallel pattern with Futures
- [ ] Can implement functional style
- [ ] Can implement CompletableFuture pattern
- [ ] Know when to use each pattern

**Time**: 6-8 hours  
**Projects**: `http-play`  
**This is the most important week!**

---

### Week 3: Spring Boot Integration
**Goal**: Use virtual threads in web applications

- [ ] Can configure Spring Boot for virtual threads
- [ ] Understand how requests use virtual threads
- [ ] Can create blocking endpoints that scale
- [ ] Can test virtual thread behavior

**Time**: 2-3 hours  
**Projects**: `loomdemo`

---

### Week 4: Production Patterns ⭐
**Goal**: Build production-ready applications

- [ ] Can use StructuredTaskScope
- [ ] Understand structured concurrency benefits
- [ ] Can use ScopedValue for request context
- [ ] Can implement fan-out/fan-in pattern
- [ ] Can handle errors gracefully
- [ ] Can monitor performance

**Time**: 6-8 hours  
**Projects**: `bestpricebookstore`, `structured-play`, `scoped-play`  
**This pattern is what you'll use in production!**

---

## 🎓 Difficulty Progression

```
                        MASTERY
                           ▲
                           │
                           │
            ┌──────────────┴──────────────┐
            │  Build Production App       │
            │  (Your Own Project)         │
            └──────────────┬──────────────┘
                           │
            ┌──────────────┴──────────────┐
            │  bestpricebookstore         │ ← Production Pattern
            │  + structured-play          │
            │  + scoped-play              │
            └──────────────┬──────────────┘
                           │
            ┌──────────────┴──────────────┐
            │  loomdemo                   │ ← Spring Boot Basic
            └──────────────┬──────────────┘
                           │
            ┌──────────────┴──────────────┐
            │  http-play                  │ ← I/O Patterns ⭐
            └──────────────┬──────────────┘
                           │
            ┌──────────────┴──────────────┐
            │  maxthreads-loom            │ ← Create Threads
            └──────────────┬──────────────┘
                           │
                        START
```

---

## 💡 Two Learning Approaches

### Approach A: Thorough (Recommended)
**For**: Complete understanding  
**Time**: 4 weeks

```
Week 1: Basics → Complete all exercises
Week 2: I/O Patterns → Master all 4 patterns
Week 3: Spring Boot → Build test apps
Week 4: Production → Understand every line

Result: Deep understanding, ready for production
```

### Approach B: Fast Track
**For**: Quick start (already know concurrency basics)  
**Time**: 1 week

```
Day 1: Read guide sections 1-2, run maxthreads-loom
Day 2: Study http-play, master 4 patterns
Day 3: Run loomdemo, understand Spring config
Day 4: Deep dive bestpricebookstore
Day 5: Build simple app using learned patterns

Result: Working knowledge, learn more as you go
```

---

## 🎯 Skill Checkpoints

### Checkpoint 1: Can Create Virtual Threads
```java
// Can you write this from memory?
Thread.ofVirtual().start(() -> {
    // Your code
});

try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> doWork());
}
```
✅ Ready for I/O patterns

---

### Checkpoint 2: Understand I/O Patterns
**Can you answer:**
- When would you use sequential vs parallel patterns?
- What's the difference between Futures and CompletableFuture?
- Why is blocking OK with virtual threads?

✅ Ready for Spring Boot

---

### Checkpoint 3: Can Configure Spring Boot
```properties
# Can you add this and explain what it does?
spring.threads.virtual.enabled=true
```
✅ Ready for production patterns

---

### Checkpoint 4: Master Production Patterns
**Can you implement:**
- StructuredTaskScope for parallel API calls
- ScopedValue for request context
- Error handling for multiple concurrent operations

✅ Ready to build production apps! 🎉

---

## 📖 Reference Materials by Use Case

### Use Case: "I need to call multiple APIs in parallel"
1. Study: `http-play/UserRequestHandler.java` → Pattern 2 (Futures)
2. Production: `bestpricebookstore/BookRetrievalService.java` → StructuredTaskScope
3. Reference: `QUICK_REFERENCE.md` → "Fan-Out / Fan-In Pattern"

---

### Use Case: "I need request-scoped data across threads"
1. Study: `VIRTUAL_THREADS_IO_GUIDE.md` → Section 5, Pattern 3
2. Production: `bestpricebookstore/BestPriceBookController.java` → ScopedValue
3. Reference: `QUICK_REFERENCE.md` → "ScopedValue Usage"

---

### Use Case: "I want to add virtual threads to existing Spring Boot app"
1. Study: `loomdemo/application.properties` → Configuration
2. Study: `VIRTUAL_THREADS_IO_GUIDE.md` → Section 4
3. Add: One line to application.properties
4. Test: Verify thread names in logs

---

### Use Case: "I need to choose between async patterns"
1. Study: `http-play/UserRequestHandler.java` → All 4 patterns
2. Read: `VIRTUAL_THREADS_IO_GUIDE.md` → "Pattern Comparison Summary"
3. Reference: `QUICK_REFERENCE.md` → "When to Use What"

---

## 🚀 Quick Start by Role

### Java Developer (New to Virtual Threads)
1. Start: README.md overview
2. Learn: VIRTUAL_THREADS_IO_GUIDE.md sections 1-3
3. Practice: http-play project
4. Time: 1-2 days

### Spring Boot Developer
1. Start: loomdemo project
2. Learn: Spring Boot Integration section
3. Study: bestpricebookstore pattern
4. Apply: To your existing apps
5. Time: 1 day

### Architect / Team Lead
1. Read: VIRTUAL_THREADS_IO_GUIDE.md (full)
2. Review: bestpricebookstore production pattern
3. Study: Performance section
4. Reference: Best Practices section
5. Time: 3-4 hours

### Student / Learning
1. Follow: 4-week learning path
2. Complete: All exercises
3. Build: Own project at end
4. Time: 4 weeks (1-2 hours/day)

---

## 🎉 Success Indicators

### You're ready for production when you can:

1. ✅ Write a Spring Boot endpoint with virtual threads
2. ✅ Implement parallel API calls with StructuredTaskScope
3. ✅ Use ScopedValue for request context
4. ✅ Handle errors from concurrent operations
5. ✅ Explain why your code scales better than before
6. ✅ Know when NOT to use virtual threads
7. ✅ Debug virtual thread issues
8. ✅ Monitor virtual thread applications

---

## 📚 Documentation Map

```
VIRTUAL_THREADS_IO_GUIDE.md
├── Introduction ................. Why virtual threads?
├── Core Concepts ................ How they work
├── Standalone Applications ...... Basic patterns
├── Spring Boot Integration ...... Web applications
├── Advanced Patterns ............ Production-ready
├── Performance .................. Optimization
├── Best Practices ............... Do's and Don'ts
└── Quick Reference .............. Code patterns

QUICK_REFERENCE.md
├── Quick Start .................. Get running fast
├── Code Patterns ................ Copy-paste ready
├── When to Use What ............. Decision guide
├── Best Practices ............... Summary
└── Troubleshooting .............. Common issues

README.md
├── Overview ..................... What's included
├── Project Structure ............ File organization
├── Learning Path ................ How to learn
└── Quick Start .................. Run examples

LEARNING_PATH.md (this file)
└── Visual Guide ................. Learning journey
```

---

**Ready to start your journey? Open README.md and begin! 🚀**

