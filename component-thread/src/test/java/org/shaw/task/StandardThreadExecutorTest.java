package org.shaw.task;

import org.junit.Test;

/**
 * @create: 2017-12-26
 * @description:
 */
public class StandardThreadExecutorTest {

    public static void main(String[] args) {
        StandardThreadExecutor standardThreadExecutor = new StandardThreadExecutor();
        standardThreadExecutor.execute(() -> {
            try {
                System.out.println("测试1----开始");
                Thread.sleep(10000);
                System.out.println("测试1----结束");
            }catch (InterruptedException e){
                Thread.interrupted();
            }
        });
        standardThreadExecutor.execute(() -> {
            try {
                System.out.println("测试2----开始");
                Thread.sleep(10000);
                System.out.println("测试2----结束");
            }catch (InterruptedException e){
                Thread.interrupted();
            }
        });
        standardThreadExecutor.execute(() -> {
            try {
                System.out.println("测试3----开始");
                Thread.sleep(10000);
                System.out.println("测试3----结束");
            }catch (InterruptedException e){
                Thread.interrupted();
            }
        });
        standardThreadExecutor.execute(() -> {
            try {
                System.out.println("测试4----开始");
                Thread.sleep(10000);
                System.out.println("测试4----结束");
            }catch (InterruptedException e){
                Thread.interrupted();
            }
        });
        standardThreadExecutor.execute(() -> {
            try {
                System.out.println("测试5----开始");
                Thread.sleep(10000);
                System.out.println("测试5----结束");
            }catch (InterruptedException e){
                Thread.interrupted();
            }
        });
    }

    @Test
    public void testStandardThreadExecutor() {

    }
}
