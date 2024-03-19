import java.util.Random;
import java.util.concurrent.*;

public class ThreadDemo {

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    demo();
                } catch (Exception e) {
                    System.out.println("exception is:" + e.getMessage());
                }
            }).start();
        }
    }

    public static void demo() {
        long start = System.currentTimeMillis();
        String data = getName(); // assume under virtual thread

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            var subtask1 = scope.fork(ThreadDemo::hello);
            var subtask2 = scope.fork(ThreadDemo::world);

            scope.join().throwIfFailed(RuntimeException::new);

            System.out.println("msg is: " + subtask1.get() + " " + subtask2.get() + " " + data);
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("time taken in sec:" + timeElapsed / 1000);
        } catch (InterruptedException e) {
            System.out.println("Interruption happened");
            throw new RuntimeException(e);
        }
    }

    public static String hello() throws InterruptedException {
        System.out.println("hello() before sleep " + Thread.currentThread());
        Thread.sleep(2000);
//        if (true) // simulating failure
//            throw new RuntimeException("something went wrong");
        System.out.println("hello() after sleep " + Thread.currentThread());
        return "Hello";
    }

    public static String world() throws InterruptedException {
        System.out.println("world() before sleep " + Thread.currentThread());
        Thread.sleep(3000);
//        if(true) // simulating failure
//            throw new RuntimeException("something went wrong");
        System.out.println("world() after sleep " + Thread.currentThread());
        return "World";
    }

    public static String getName() {
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> data = executorService.submit(ThreadDemo::data);
            try {
                return data.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String data() {
        System.out.println("data() before sleep " + Thread.currentThread());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("data() after sleep " + Thread.currentThread());
        return "Java" + (new Random().nextInt(21 - 8) + 8);
    }
}
