package org.shaw.task;

import org.junit.Test;

import java.util.concurrent.Future;

/**
 * @create: 2017-12-26
 * @description:
 */
public class StandardThreadExecutorTests {

    @Test
    public void TestThreadExecutor() throws Exception {
        /*ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1),
                new CustomizableThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());*/

        StandardThreadExecutor executor = new StandardThreadExecutor(1, 1) {
            @Override
            protected void before(Thread t, Runnable r) {
                System.out.println("运行：" + r);
            }

            @Override
            protected void after(Runnable r, Throwable t) {
                System.out.println("结束：" + r);
            }
        };

        Future<String> task1 = null;
        try {
            task1 = executor.submit(() -> {
                System.out.println("测试1----开始");
                Thread.sleep(10000);
                System.out.println("测试1----结束");
                return "测试1";
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }

        Future<String> task2 = null;
        try {
            task2 = executor.submit(() -> {
                System.out.println("测试2----开始");
                Thread.sleep(10000);
                System.out.println("测试2----结束");
                return "测试2";
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }

        Future<String> task3 = null;
        try {
            task3 = executor.submit(() -> {
                System.out.println("测试3----开始");
                Thread.sleep(10000);
                System.out.println("测试3----结束");
                return "测试3";
            }, "默认返回值：测试3");
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            executor.execute(() -> {
                try {
                    System.out.println("测试4----开始");
                    Thread.sleep(10000);
                    System.out.println("测试4----结束");
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            executor.submit(() -> {
                try {
                    System.out.println("测试5----开始");
                    Thread.sleep(10000);
                    System.out.println("测试5----结束");
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            executor.submit(() -> {
                try {
                    System.out.println("测试6----开始");
                    Thread.sleep(10000);
                    System.out.println("测试6----结束");
                } catch (final Exception e) {
                    e.printStackTrace();
                }


            });
        } catch (final Exception e) {
            e.printStackTrace();
        }

        System.out.println(task1.get());
        System.out.println(task2.get());
        System.out.println(task3.get());
        System.out.println("==================");
    }

}
