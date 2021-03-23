package org.lucas.component.common.util.compress;

import org.junit.jupiter.api.Test;
import org.lucas.component.common.util.compress.impl.Student;
import org.xerial.snappy.Snappy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

class Compress_Tests {

    @Test
    void testSnappyCompress() throws IOException {
        List<Student> students = new ArrayList<>();
        for (int i = 0, length = 10000000; i < length; i++) {
            Student student = new Student("id4300", "张三", i);
            students.add(student);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos)) {
            objectOutputStream.writeObject(students);
            objectOutputStream.flush();
        }
        System.out.println(bos.toByteArray().length);
        System.out.println(Snappy.compress(bos.toByteArray()).length);
        long startTime = System.currentTimeMillis();
        byte[] compressBytes = Snappy.compress(bos.toByteArray());
        byte[] uncompressBytes = Snappy.uncompress(compressBytes);
        System.out.println("总时间：" + (System.currentTimeMillis() - startTime));
        System.out.println(uncompressBytes.length);
    }

}
