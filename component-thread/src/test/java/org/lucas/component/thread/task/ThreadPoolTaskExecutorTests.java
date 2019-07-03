package org.lucas.component.thread.task;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.junit.Test;
import org.lucas.component.thread.task.ThreadPoolTaskExecutor;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @create: 2017-12-26
 * @description:
 */
public class ThreadPoolTaskExecutorTests {

    @Test
    public void TestThreadExecutor() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(1, 1);

        executor.setBefore((r, t) -> System.out.println("运行任务：" + r));
        executor.setAfter(((r, t) -> System.out.println("任务结束：" + r)));

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

    @Test
    public void TestListenableFuture() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(1, 1);

        executor.setBefore((r, t) -> System.out.println("运行任务：" + r));
        executor.setAfter(((r, t) -> System.out.println("任务结束：" + r)));

        Future<String> task1 = null;
        try {
            task1 = executor.submitListenable(() -> {
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
            task2 = executor.submitListenable(() -> {
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
            task3 = executor.submitListenable(() -> {
                System.out.println("测试3----开始");
                Thread.sleep(10000);
                System.out.println("测试3----结束");
                return "测试3";
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            executor.submitListenable(() -> {
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
            executor.submitListenable(() -> {
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
            executor.submitListenable(() -> {
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

    public static void main(String[] args) throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        Random random = new SecureRandom();
        MutableList<Callable<String>> cables = new FastList<>();
        for (int i = 0, size = 10; i < size; i++) {
            final int value = i;
            cables.add(() -> {
                TimeUnit.SECONDS.sleep(random.nextInt(10));
                return value + "\n";
            });
        }

        String value = executor.invokeAny(cables);
        System.out.println(value);
    }
}
