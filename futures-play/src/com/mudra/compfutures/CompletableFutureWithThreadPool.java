package com.mudra.compfutures;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mudra.futures.FuturesPlay;

/*
 * CompletableFuture started on a different Pool (mypool) altogether.
 * Its a good idea to use a different Thread pool if the tasks are 
 * IO bound. The Fork Join Pool must be used only CPU intensive tasks. 
 * 
 */
public class CompletableFutureWithThreadPool {

  // Usually the Threadpool would be created upfront
  private static ExecutorService mypool = Executors.newCachedThreadPool();

  public static void main(String[] args) {

    try {
      examplePipelineApplyAcceptAsyncWithExecutor().join();
    } finally {
      mypool.close();
    }

  }

  private static CompletableFuture<Void> examplePipelineApplyAcceptAsyncWithExecutor() {

    // Execute a task in common pool
    // then Apply a function on another executor service thread
    // then Accept the result on another executor service thread
    CompletableFuture<Void> pipeline = CompletableFuture
      .supplyAsync(() -> FuturesPlay.doTask("SomeTask", 3, false), mypool)
      .thenApplyAsync(taskResult -> {
        System.out.println("Apply Thread Name : " + Thread.currentThread());
        return taskResult.secs();
      }, mypool)
      .thenAcceptAsync(time -> {
        System.out.println("Accept Thread Name : " + Thread.currentThread());
        System.out.println(time);
      }, mypool);

    return pipeline;
  }

}
