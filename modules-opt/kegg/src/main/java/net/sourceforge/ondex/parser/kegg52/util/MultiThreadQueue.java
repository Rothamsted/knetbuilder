package net.sourceforge.ondex.parser.kegg52.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author hindlem
 */
public class MultiThreadQueue {

    private static final long serialVersionUID = 8059211122995538242L;

    protected boolean DEBUG = false;

    private static boolean systemTerminateOnException = true;

    private WaitableExecuter threadPool;
    private String name;

    private int objectsAllowed;

    class WaitableExecuter extends ThreadPoolExecutor {

        public WaitableExecuter(int corePoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, corePoolSize, keepAliveTime, unit, workQueue);
            this.prestartAllCoreThreads();
        }

        AtomicInteger awaiting = new AtomicInteger(0);
        private ReentrantLock finishLock = new ReentrantLock();
        private Condition finished = finishLock.newCondition();

        @Override
        public void execute(Runnable command) {
            if (command == null) throw new NullPointerException();
            awaiting.getAndIncrement();
            super.execute(command);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            if (awaiting.getAndDecrement() == 0 && queueToPut.get() == 0) {
                finished.signalAll();
            }
            super.afterExecute(r, t);
        }

        protected void await(String caller) {
            finishLock.lock();
            int count = 0;
            while (awaiting.get() > 0 || queueToPut.get() > 0) {
                if (count > 3) if (DEBUG)
                    System.out.println(caller + " is waiting on queue " + name + " size " + awaiting + " threads " + this.getActiveCount());
                if (count > 4) {

                    try {
                        finished.await(1, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                count++;
            }
            finishLock.unlock();
        }

        final AtomicInteger queueToPut = new AtomicInteger(0);

        public void put(Runnable run) {
            queueToPut.incrementAndGet();
            try {
                while (awaiting.get() >= objectsAllowed) {
                    Thread.sleep(250);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                execute(run);
                queueToPut.decrementAndGet();
            }

        }
    }

    public MultiThreadQueue(int objectsAllowed, String name, boolean singleThread, Integer threads) {
        this.name = name;
        if (objectsAllowed < 1) objectsAllowed = 1;
        this.objectsAllowed = objectsAllowed;

        if (threads == null) {
            threads = Runtime.getRuntime().availableProcessors() * 4;
        }

        if (singleThread) threads = 1;
        if (threads > 1)
            threadPool = new WaitableExecuter(threads, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        else
            threadPool = new WaitableExecuter(1, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        threadPool.setThreadFactory(new MyThreadFactory(name));
        threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {

            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.err.println("?pool " + threadPool.getPoolSize());
                System.err.println("?core " + threadPool.getCorePoolSize());
                System.err.println("?max " + threadPool.getMaximumPoolSize());
                System.err.println("?full at concepts " + threadPool.getTaskCount());
                System.err.println("?is terminating " + threadPool.isShutdown());
                throw new RuntimeException("A item has been rejected from the queue of type :" + r.getClass());
            }
        });

    }

    public MultiThreadQueue(int objectsAllowed, String name) {
        this(objectsAllowed, name, false, null);
    }

    public void addRunnable(Runnable run) {
        threadPool.put(run);
    }

    public void waitToFinish(String caller) {
        threadPool.await(caller);
    }

    public boolean isFinalized() {
        return (threadPool.isTerminated() || threadPool.isTerminating());
    }

    @Override
    public void finalize() {

        waitToFinish("Termination for " + this.getClass().getName());

        while (!threadPool.isTerminated()) {
            try {
                if (DEBUG) System.out.println("Awaiting Termination ");
                threadPool.await(name);
                threadPool.shutdown();
                threadPool.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setDEBUG(boolean debug) {
        DEBUG = debug;
    }

    public int getObjectsAllowed() {
        return objectsAllowed;
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

    /**
     * Global static rule
     *
     * @return
     */
    public static boolean isSystemTerminateOnException() {
        return systemTerminateOnException;
    }

    /**
     * Global static rule
     *
     * @param systemTerminateOnException
     */
    public static void setSystemTerminateOnException(boolean systemTerminateOnExceptions) {
        systemTerminateOnException = systemTerminateOnExceptions;
	}
	
}
