package org.lucas.component.common.core.idcenter.support.buffer;

import org.lucas.component.common.core.idcenter.support.NamingThreadFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BufferPaddingExecutor {

    /**
     * Constants
     */
    private static final String WORKER_NAME = "RingBuffer-Padding-Worker";
    private static final String SCHEDULE_NAME = "RingBuffer-Padding-Schedule";
    private static final long DEFAULT_SCHEDULE_INTERVAL = 5 * 60L;

    /**
     * Whether buffer padding is running
     */
    private final AtomicBoolean running;

    /**
     * We can borrow UIDs from the future, here store the last second we have consumed
     */
    private final PaddedAtomicLong lastSecond;

    /**
     * RingBuffer & BufferUidProvider
     */
    private final RingBuffer ringBuffer;
    private final BufferedUidProvider uidProvider;

    /**
     * Padding immediately by the thread pool
     */
    private final ExecutorService bufferPadExecutors;

    /**
     * Padding schedule thread
     */
    private final ScheduledExecutorService bufferPadSchedule;

    /**
     * Schedule interval Unit as seconds, 5 minutes
     */
    private long scheduleInterval = DEFAULT_SCHEDULE_INTERVAL;

    public BufferPaddingExecutor(RingBuffer ringBuffer, BufferedUidProvider uidProvider) {
        this(ringBuffer, uidProvider, true);
    }

    public BufferPaddingExecutor(RingBuffer ringBuffer, BufferedUidProvider uidProvider, boolean usingSchedule) {
        this.running = new AtomicBoolean(false);
        this.lastSecond = new PaddedAtomicLong(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        this.ringBuffer = ringBuffer;
        this.uidProvider = uidProvider;

        // initialize thread pool
        int cores = Runtime.getRuntime().availableProcessors();
        bufferPadExecutors = Executors.newFixedThreadPool(cores * 2, new NamingThreadFactory(WORKER_NAME));

        // initialize schedule thread
        if (usingSchedule) {
            bufferPadSchedule = Executors.newSingleThreadScheduledExecutor(new NamingThreadFactory(SCHEDULE_NAME));
        } else {
            bufferPadSchedule = null;
        }
    }

    /**
     * Start executors such as schedule
     */
    public void start() {
        if (bufferPadSchedule != null) {
            bufferPadSchedule.scheduleWithFixedDelay(() -> paddingBuffer(), scheduleInterval, scheduleInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * Shutdown executors
     */
    public void shutdown() {
        if (!bufferPadExecutors.isShutdown()) {
            bufferPadExecutors.shutdownNow();
        }

        if (bufferPadSchedule != null && !bufferPadSchedule.isShutdown()) {
            bufferPadSchedule.shutdownNow();
        }
    }

    /**
     * Padding buffer in the thread pool
     */
    public void asyncPadding() {
        bufferPadExecutors.submit(this::paddingBuffer);
    }

    /**
     * Padding buffer fill the slots until to catch the cursor
     */
    public void paddingBuffer() {
        // is still running
        if (!running.compareAndSet(false, true)) {
            // 日志
            System.out.println("Padding buffer is still running. " + ringBuffer);
            return;
        }
        // fill the rest slots until to catch the cursor
        boolean isFullRingBuffer = false;
        while (!isFullRingBuffer) {
            // 通过incrementAndGet()方法获取下一次的时间，从而脱离了对服务器时间的依赖，也就不会有时钟回拨的问
            // 题（这种做法也有一个小问题，即分布式ID中的时间信息可能并不是这个ID真正产生的时间点
            List<Long> uidList = uidProvider.provide(lastSecond.incrementAndGet());
            for (Long uid : uidList) {
                isFullRingBuffer = !ringBuffer.put(uid);
                if (isFullRingBuffer) {
                    break;
                }
            }
        }
        // not running now
        running.compareAndSet(true, false);
    }

    /**
     * Setters
     */
    public void setScheduleInterval(long scheduleInterval) {
        Assert.isTrue(scheduleInterval > 0, "Schedule interval must positive!");
        this.scheduleInterval = scheduleInterval;
    }

}
