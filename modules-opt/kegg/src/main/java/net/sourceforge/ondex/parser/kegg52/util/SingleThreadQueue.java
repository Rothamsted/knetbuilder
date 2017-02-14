package net.sourceforge.ondex.parser.kegg52.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author hindlem
 */
public class SingleThreadQueue {

    private ExecutorService executor;
    private boolean debug;
    private MyThreadFactory tf;

    public SingleThreadQueue(String name) {
        tf = new MyThreadFactory("stq-" + name);
        initExec();
    }

    public void initExec() {
        this.executor = new ThreadPoolExecutor(1, 1,
                Integer.MAX_VALUE, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), tf
        );
    }

    public void setDEBUG(boolean debug) {
        this.debug = debug;
    }

    public Future<?> addRunnable(Runnable run) {
        if (!executor.isShutdown()) {
            return executor.submit(run);
        } else if (executor.isTerminated()) {
            if (debug) System.out.println("Creating new queue");
            initExec();
            return executor.submit(run);
        } else {
            System.err.println("Job added to finalizeing queue");
            return null;
        }
    }

    public void waitToFinish(String caller) {
        if (executor != null && !executor.isTerminated()) {
            if (!executor.isShutdown()) executor.shutdown();
            try {
                while (!executor.isTerminated()) {
                    System.out.println("Waiting");
                    executor.awaitTermination(1, TimeUnit.SECONDS);
                    System.out.println("done");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void finalize() {
        waitToFinish("finalize()");
        executor = null;
    }

    static class MyThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        private ThreadGroup eh;

        public MyThreadFactory(String name) {
            namePrefix = name + "-pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";

            eh = new ThreadGroup(name) {
                public void uncaughtException(Thread thread, Throwable e) {
                    if (e instanceof ThreadDeath) {
                        return;
                    }
                    e.printStackTrace(); //errors to be propergated to the main thread
                }
            };

        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(eh, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    10);
            t.setUncaughtExceptionHandler(eh);
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}
