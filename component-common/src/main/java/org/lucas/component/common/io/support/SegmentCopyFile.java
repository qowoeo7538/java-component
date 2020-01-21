package org.lucas.component.common.io.support;

import org.lucas.component.common.util.ExceptionUtils;
import org.lucas.component.common.util.StreamHelper;

import java.io.RandomAccessFile;

/**
 * @create: 2017-11-07
 * @description: 分段拷贝文件
 */
public class SegmentCopyFile {
    /**
     * 文件名
     */
    private String srcName;
    /**
     * 拷贝文件名
     */
    private String copyName;
    /**
     * 文件大小
     */
    private long fileSize;
    /**
     * 并发数
     */
    private int count;

    public SegmentCopyFile(String srcName, String copyName, int count) {
        this.srcName = srcName;
        try {
            this.fileSize = StreamHelper.getFileSize(SegmentCopyFile.this.srcName);
        } catch (Exception e) {
            throw ExceptionUtils.unchecked(e);
        }
        this.copyName = copyName;
        this.count = count;
    }

    /**
     * 分段拷贝文件
     */
    public void copyFile() throws Exception {
        for (int i = 1; i <= count; i++) {
            new BlockCopyFile(i).start();
        }
        if (fileSize % count != 0) {
            new BlockCopyFile(count + 1).start();
        }
    }

    class BlockCopyFile extends Thread {
        /**
         * 文件名
         */
        private String srcName;
        /**
         * 拷贝文件名
         */
        private String copyName;
        /**
         * 文件大小
         */
        private long fileSize;
        /**
         * 写入大小
         */
        private int writeSize;
        /**
         * 并发次数
         */
        private int count;
        /**
         * 当前线程拷贝块
         */
        private int block;

        public BlockCopyFile(int block) {
            this.fileSize = SegmentCopyFile.this.fileSize;
            this.srcName = SegmentCopyFile.this.srcName;
            this.copyName = SegmentCopyFile.this.copyName;
            this.count = SegmentCopyFile.this.count;
            this.block = block;
            this.writeSize = (int) fileSize / SegmentCopyFile.this.count;
        }

        @Override
        public void run() {
            try (RandomAccessFile randomAccessFileRead = new RandomAccessFile(this.srcName, "r");
                 RandomAccessFile randomAccessFileWrite = new RandomAccessFile(this.copyName, "rw")
            ) {
                byte[] bytes;
                randomAccessFileRead.seek((this.block - 1) * this.writeSize);
                if (this.block <= this.count) {
                    bytes = new byte[this.writeSize];
                } else {
                    bytes = new byte[(int) this.fileSize % this.count];
                }
                int length = randomAccessFileRead.read(bytes, 0, bytes.length);
                if (length > 0) {
                    randomAccessFileWrite.seek((this.block - 1) * this.writeSize);
                    randomAccessFileWrite.write(bytes);
                }
            } catch (Exception e) {
                throw ExceptionUtils.unchecked(e);
            }
        }

        /**
         * 发生异常将会进行回调，线程池并不会立即生效
         *
         * @param eh
         */
        @Override
        public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler eh) {
            super.setUncaughtExceptionHandler(eh);
        }
    }
}
