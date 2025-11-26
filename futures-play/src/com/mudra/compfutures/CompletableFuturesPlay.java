package com.mudra.compfutures;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.mudra.futures.FuturesPlay;
import com.mudra.futures.TaskResult;

@SuppressWarnings("unused")
public class CompletableFuturesPlay {

  public static void main(String[] args) throws Exception {

    long start = System.currentTimeMillis();

    // Example of CompletableFuture.runAsync
    // exampleRunAsync();

    // Example of CompletableFuture.supplyAsync
    // exampleSupplyAsync();

    // Example of Simple CompletableFuture Pipeline
    // examplePipeline().join();

    // Example of Multiple Accepts in a Pipeline
    // examplePipelineMultipleApply().join();

    // Example of recovering from Error
    // examplePipelineWithErrorRecovery().join();

    // Example of recovering from Error 2
    // examplePipelineWithErrorRecovery2().join();

    // Example of thenCompose
    // exampleCompose().join();

    // Example of thenCombine
    // exampleCombine().join();

    // Example of multiple tasks handled by thenCombine
    // exampleMultipleCombine().join();

    // Example Problem of complicated Parallelism
    // exampleProblem().join();

    // Example of thenApplyAsync, thenAcceptAsync
    // examplePipelineApplyAcceptAsync().join();

    // Example of Displaying thread names
    // examplePipelineShowThreadNames().join();

    // Example of Displaying thread names during thenCombine
    // exampleCombineShowThreadNames().join();

    // Example of using CompletableFuture allOf()
    // exampleAllOf().join();

    // Example of using CompletableFuture anyOf()
    exampleAnyOf().join();

    // Example of using CompletableFuture whenComplete
    // exampleAllOfWithWhenComplete().join();

    // Example of orTimeout() method
    // exampleTimeout().join();

    // Example of Asynchronous HTTP
    // exampleAsyncHttp().join();

    // Example of Asynchronous File Read
    // exampleAsyncFileRead().join();

    long end = System.currentTimeMillis();
    System.out.printf("Total time taken = %d ms", end - start);

  }

  private static CompletableFuture<Void> exampleAsyncFileRead() throws IOException {

    return readFileAsync(".project")
        .thenAccept(System.out::println);
  }

  private static CompletableFuture<String> readFileAsync(String fileName) throws IOException {

    // Create a Completable Future
    CompletableFuture<String> future = new CompletableFuture<>();

    // Create a Path to a file in the current working directory.
    Path path = Paths.get(".").resolve(fileName);
    AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
    ByteBuffer buffer = ByteBuffer.allocate((int) path.toFile().length());
    fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
      @Override
      public void completed(Integer result, ByteBuffer attachment) {

        // extract the data from the attachment
        attachment.flip();
        byte[] data = new byte[attachment.limit()];
        attachment.get(data);
        attachment.clear();

        // complete successfully
        future.complete(new String(data));

      }

      @Override
      public void failed(Throwable exc, ByteBuffer attachment) {

        // complete exceptionally
        future.completeExceptionally(exc);
      }
    });

    // Return the CompletableFuture
    return future;
  }

  private static CompletableFuture<Void> exampleAsyncHttp() throws URISyntaxException {

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().GET()
        .uri(new URI("https://httpbin.org/delay/10"))
        .build();

    // Sends an Http request asynchronously. The Thread is NOT tied up for 10 secs
    CompletableFuture<Void> pipeline = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .whenComplete((r, throwable) -> {
          if (throwable == null) {
            if (r.statusCode() >= 400) {
              throw new RuntimeException("Invalid Error Code");
            }
          }
        })
        .thenApply(r -> r.body())
        .thenAccept(System.out::println);

    return pipeline;
  }

  private static CompletableFuture<Void> exampleTimeout() {

    Supplier<TaskResult> task1 = () -> FuturesPlay.doTask("task1", 3, false);
    CompletableFuture<Void> pipeline = CompletableFuture.supplyAsync(task1)
        .orTimeout(1, TimeUnit.SECONDS)
        .thenAccept(System.out::println);

    return pipeline;
  }

  private static CompletableFuture<Void> exampleAllOfWithWhenComplete() {

    // Tasks we want to run in parallel
    Supplier<TaskResult> task1 = () -> FuturesPlay.doTask("task1", 3, true);
    Supplier<TaskResult> task2 = () -> FuturesPlay.doTask("task2", 4, false);
    Supplier<TaskResult> task3 = () -> FuturesPlay.doTask("task3", 5, false);
    Supplier<TaskResult> task4 = () -> FuturesPlay.doTask("task4", 6, false);

    // Let's run all of them in parallel
    var future1 = CompletableFuture.supplyAsync(task1);
    var future2 = CompletableFuture.supplyAsync(task2);
    var future3 = CompletableFuture.supplyAsync(task3);
    var future4 = CompletableFuture.supplyAsync(task4);

    // Returns a CompletableFuture which completes when all 4 futures are completed
    CompletableFuture<Void> pipeline = CompletableFuture
        .allOf(future1, future2, future3, future4)
        .whenComplete((unused, throwable) -> {

          if (throwable == null) {
            System.out.println(
                List.of(future1.join(), future2.join(), future3.join(), future4.join()));
          } else {
            handleErrors(throwable);
          }

        });

    return pipeline;
  }

  private static CompletableFuture<Void> exampleAnyOf() {

    // Tasks we want to run in parallel
    Supplier<TaskResult> task1 = () -> FuturesPlay.doTask("task1", 3, false);
    Supplier<TaskResult> task2 = () -> FuturesPlay.doTask("task2", 4, false);
    Supplier<TaskResult> task3 = () -> FuturesPlay.doTask("task3", 5, false);
    Supplier<TaskResult> task4 = () -> FuturesPlay.doTask("task4", 6, false);

    // Let's run all of them in parallel
    var future1 = CompletableFuture.supplyAsync(task1);
    var future2 = CompletableFuture.supplyAsync(task2);
    var future3 = CompletableFuture.supplyAsync(task3);
    var future4 = CompletableFuture.supplyAsync(task4);

    // Returns a CompletableFuture which completes when any of the 4 futures
    // complete
    // The remaining tasks are not cancelled
    CompletableFuture<Object> future = CompletableFuture.anyOf(future1, future2, future3, future4);
    CompletableFuture<Void> pipeline = future.thenAccept(result -> {
      System.out.println("Handling Accept :: " + result);
      // future2.cancel(true);
    })
        .exceptionally(throwable -> handleErrors(throwable));

    return pipeline;
  }

  private static CompletableFuture<Void> exampleAllOf() {

    // Tasks we want to run in parallel
    Supplier<TaskResult> task1 = () -> FuturesPlay.doTask("task1", 3, false);
    Supplier<TaskResult> task2 = () -> FuturesPlay.doTask("task2", 4, false);
    Supplier<TaskResult> task3 = () -> FuturesPlay.doTask("task3", 5, false);
    Supplier<TaskResult> task4 = () -> FuturesPlay.doTask("task4", 6, false);

    // Let's run all of them in parallel
    var future1 = CompletableFuture.supplyAsync(task1);
    var future2 = CompletableFuture.supplyAsync(task2);
    var future3 = CompletableFuture.supplyAsync(task3);
    var future4 = CompletableFuture.supplyAsync(task4);

    // Returns a CompletableFuture which completes when all 4 futures are completed
    // Note :: allOf(..) does not "wait" for all 4 to complete. It simply returns a
    // CompletableFuture
    CompletableFuture<Void> future = CompletableFuture.allOf(future1, future2, future3, future4);

    CompletableFuture<Void> pipeline = future.thenAccept(unused -> {
      System.out.println(List.of(future1.join(), future2.join(), future3.join(), future4.join()));
    })
        .exceptionally(throwable -> handleErrors(throwable));

    return pipeline;
  }

  private static Void handleErrors(Throwable throwable) {
    // do nothing
    System.out.println(throwable);
    return null;
  }

  private static CompletableFuture<Void> exampleCombineShowThreadNames() {

    // Tasks to execute asynchronously and in parallel
    Supplier<TaskResult> task1 = () -> FuturesPlay.doTask("task1", 5, false);
    Supplier<TaskResult> task2 = () -> FuturesPlay.doTask("task2", 3, false);

    // thenCombine will combine the results of Task1 and Task2
    // thenApply will operate on this combined Result
    CompletableFuture<Void> pipeline = CompletableFuture.supplyAsync(task1)
        .thenCombine(
            CompletableFuture.supplyAsync(task2),
            (result1, result2) -> {
              System.out.println("Combine Thread Name : " + Thread.currentThread());
              return fuze(result1.taskName(), result2.taskName());
            })
        .thenApplyAsync(data -> {
          System.out.println("thenApply Thread Name : " + Thread.currentThread());
          return data + " :: Handled Apply";
        })
        .thenAcceptAsync(data -> {
          System.out.println("thenAccept Thread Name : " + Thread.currentThread());
          System.out.println(data + " :: Handled Accept");
        });

    return pipeline;
  }

  private static CompletableFuture<Void> examplePipelineShowThreadNames() {

    // Execute a task in common pool
    // then Apply a function
    // then Accept the result which will be consumed by Consumer
    CompletableFuture<TaskResult> pipeline = CompletableFuture
        .supplyAsync(() -> FuturesPlay.doTask("SomeTask", 3, false))
        .thenApply(taskResult -> {
          System.out.println(Thread.currentThread());
          System.out.println(taskResult);
          return taskResult;
        });

    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }



    CompletableFuture<Void> pipeline2 = pipeline.thenApply(taskResult -> taskResult.secs())
        .thenAccept(time -> {
          System.out.println(Thread.currentThread());
          System.out.println(time);
        });

    return pipeline2;
  }

  private static CompletableFuture<Void> exampleProblem() {

    // Tasks I want to run
    Supplier<TaskResult> task1 = () -> FuturesPlay.doTask("task1", 3, false);
    Supplier<TaskResult> task2 = () -> FuturesPlay.doTask("task2", 4, false);
    Supplier<TaskResult> task3 = () -> FuturesPlay.doTask("task3", 5, false);
    Supplier<TaskResult> task4 = () -> FuturesPlay.doTask("task4", 6, false);

    // Lets run task1 and task 2 in parallel
    var future1 = CompletableFuture.supplyAsync(task1);
    var future2 = CompletableFuture.supplyAsync(task2);

    CompletableFuture<Void> pipeline = future1
        .thenCombine(future2, (result1, result2) -> fuze(result1.taskName(), result2.taskName()))
        .thenApply(s -> s + " :: Glue")
        .thenCompose(s -> {

          // Let's run task 3 and task 4 in parallel.
          // Note we do not start the tasks until tasks 1 and 2 are completed
          var future3 = CompletableFuture.supplyAsync(task3);
          var future4 = CompletableFuture.supplyAsync(task4);
          return future3.thenCombine(
              future4, (result1, result2) -> s + " :: " + fuze(result1.taskName(), result2.taskName()));
        })
        .thenAccept(data -> {
          System.out.println(data + " :: Handled Accept");
        });

    return pipeline;
  }


  private static CompletableFuture<Void> exampleProblemSolving() {
    Supplier<TaskResult> task1 = () -> FuturesPlay.doTask("task1", 3, false);
    Supplier<TaskResult> task2 = () -> FuturesPlay.doTask("task2", 4, false);
    Supplier<TaskResult> task3 = () -> FuturesPlay.doTask("task3", 5, false);
    Supplier<TaskResult> task4 = () -> FuturesPlay.doTask("task4", 6, false);

    var future1 = CompletableFuture.supplyAsync(task1);
    var future2 = CompletableFuture.supplyAsync(task2);

    return future1.thenCombine(future2, (result1, result2) -> fuze(result1.taskName(), result2.taskName()))
      .thenApply(s -> s + " :: Glue")
      .thenCompose(s -> {
        var future3 = CompletableFuture.supplyAsync(task3);
        var future4 = CompletableFuture.supplyAsync(task4);
        return future3.thenCombine(future4, (result1, result2) -> s + " :: " + fuze(result1.taskName(), result2.taskName()));
      })
      .thenAccept(data -> {
        System.out.println(data + " :: Handled Accept");
      });


  }

  private static CompletableFuture<Void> exampleMultipleCombine() {

    // Tasks we want to run in parallel
    Supplier<TaskResult> task1 = () -> FuturesPlay.doTask("task1", 3, false);
    Supplier<TaskResult> task2 = () -> FuturesPlay.doTask("task2", 4, false);
    Supplier<TaskResult> task3 = () -> FuturesPlay.doTask("task3", 5, false);
    Supplier<TaskResult> task4 = () -> FuturesPlay.doTask("task4", 6, false);

    // Let's run all of them in parallel. Note that tasks have started
    // running in parallel in next 4 lines
    var future1 = CompletableFuture.supplyAsync(task1);
    var future2 = CompletableFuture.supplyAsync(task2);
    var future3 = CompletableFuture.supplyAsync(task3);
    var future4 = CompletableFuture.supplyAsync(task4);

    // Now chain the task executions
    CompletableFuture<Void> pipeline = future1
        .thenCombine(future2, (result1, result2) -> fuze(result1.taskName(), result2.taskName()))
        .thenCombine(future3, (s, taskResult) -> fuze(s, taskResult.taskName()))
        .thenCombine(future4, (s, taskResult) -> fuze(s, taskResult.taskName()))
        .thenApply(data -> data + " :: Handled Apply")
        .thenAccept(data -> {
          System.out.println(data + " :: Handled Accept");
        });

    return pipeline;
  }

  private static CompletableFuture<Void> exampleCombine() {

    // Tasks to execute asynchronously and in parallel
    Supplier<TaskResult> task1 = () -> FuturesPlay.doTask("task1", 3, false);
    Supplier<TaskResult> task2 = () -> FuturesPlay.doTask("task2", 5, false);

    // thenCombine will combine the results of Task1 and Task2
    // thenApply will operate on this combined Result
    CompletableFuture<Void> pipeline = CompletableFuture.supplyAsync(task1)
        .thenCombine(
            CompletableFuture.supplyAsync(task2),
            (result1, result2) -> fuze(result1.taskName(), result2.taskName()))
        .thenApply(data -> data + " :: Handled Apply")
        .thenAccept(data -> {
          System.out.println(data + " :: Handled Accept");
        });

    return pipeline;
  }

  private static String fuze(String s1, String s2) {
    return String.format("Combined (%s : %s)", s1, s2);
  }

  private static CompletableFuture<Void> exampleCompose() {

    // Execute a task in common pool
    // thenCompose handles function which returns a CompletableStage<Output>
    CompletableFuture<Void> pipeline = CompletableFuture.supplyAsync(() -> FuturesPlay.doTask("task", 3, false))
        .thenCompose(taskResult -> CompletableFuturesPlay.handleTaskResult(taskResult))
        .thenApply(data -> data + " :: Handled Apply")
        .thenAccept(data -> {
          System.out.println(data + ":: Handled Accept");
        });

    return pipeline;
  }

  private static CompletableFuture<String> handleTaskResult(TaskResult taskResult) {
    return CompletableFuture.supplyAsync(() -> {
      return taskResult + " :: Handled Compose";
    });
  }

  private static CompletableFuture<Void> examplePipelineWithErrorRecovery2() {

    // Execute a task in common pool
    // then Apply a function
    // then recover from exception if necessary
    // then Accept the result which will be consumed by Consumer
    CompletableFuture<Void> pipeline = CompletableFuture.supplyAsync(() -> FuturesPlay.doTask("SomeTask", 3, true))
        .thenApply(taskResult -> taskResult.secs())
        .exceptionally(t -> 0)
        .thenAccept(time -> {
          System.out.println(time);
        });

    return pipeline;
  }

  private static CompletableFuture<Void> examplePipelineWithErrorRecovery() {

    // Execute a task in common pool
    // then recover from exception if necessary
    // then Apply a function
    // then Accept the result which will be consumed by Consumer
    CompletableFuture<Void> pipeline = CompletableFuture.supplyAsync(() -> FuturesPlay.doTask("SomeTask", 3, true))
        .exceptionally(t -> new TaskResult("SomeTask", 0))
        .thenApply(taskResult -> taskResult.secs())
        .thenAccept(time -> {
          System.out.println(time);
        });

    return pipeline;
  }

  private static CompletableFuture<Void> examplePipelineApplyAcceptAsync() {

    // Execute a task in common pool
    // then Apply a function on another common pool thread
    // then Accept the result on another common pool thread
    CompletableFuture<Void> pipeline = CompletableFuture.supplyAsync(() -> FuturesPlay.doTask("SomeTask", 3, false))
        .thenApplyAsync(taskResult -> taskResult.secs())
        .thenAcceptAsync(time -> {
          System.out.println("Accept Thread : " + Thread.currentThread());
          System.out.println(time);
        });

    return pipeline;
  }

  private static CompletableFuture<Void> examplePipelineMultipleApply() {

    // Execute a task in common pool
    // then Apply a function
    // then Accept the result which will be consumed by Consumer
    CompletableFuture<Void> pipeline = CompletableFuture.supplyAsync(() -> FuturesPlay.doTask("SomeTask", 3, false))
        .thenApply(taskResult -> taskResult.secs())
        .thenApply(time -> time * 1000)
        .thenAccept(time -> {
          System.out.println(time);
        });

    return pipeline;
  }

  private static CompletableFuture<Void> examplePipeline() {

    // Execute a task in common pool
    // then Apply a function
    // then Accept the result which will be consumed by Consumer
    CompletableFuture<Void> pipeline = CompletableFuture.supplyAsync(() -> FuturesPlay.doTask("SomeTask", 3, false))
        .thenApply(taskResult -> taskResult.secs())
        .thenAcceptAsync(null)
        .thenAccept(time -> {
          System.out.println(time);
        });

    return pipeline;

  }

  private static void exampleSupplyAsync() {

    // Execute a task in the Common ForkJoin Pool of JVM
    CompletableFuture<TaskResult> taskFuture = CompletableFuture
        .supplyAsync(() -> FuturesPlay.doTask("SomeTask", 2, false));
    try {

      // wait till Task Future is Completed (Task Result is available)
      TaskResult taskResult = taskFuture.get();
      System.out.println(taskResult);

      // proceed to handle task result

    } catch (InterruptedException | ExecutionException e) {
      System.out.println(e);
    }
  }

  private static void exampleRunAsync() {

    // Execute a task in the Common ForkJoin Pool of JVM
    CompletableFuture<Void> taskFuture = CompletableFuture.runAsync(() -> FuturesPlay.doSimpleTask());
    try {

      // wait till Task Future is Completed (No Return data)
      taskFuture.get();

      // proceed to do other things

    } catch (InterruptedException | ExecutionException e) {
      System.out.println(e);
    }
  }

}
