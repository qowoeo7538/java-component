package org.lucas.task.support;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * 通过 ring buffer 实现线程生产者消费者模型。
 * 单生产者 -> 多个消费者
 * 多个生产者 -> 多个消费者
 */
public class RingQueue<E extends EventFactory> implements Queue {

    private final Disruptor<E> disruptor;

    public RingQueue(final int queueSize, E event) {
        disruptor = new Disruptor(event, queueSize, DaemonThreadFactory.INSTANCE);
    }

    /**
     * 添加任务
     *
     * @param o
     * @return
     */
    @Override
    public boolean add(Object o) {
        return false;
    }

    @Override
    public boolean offer(Object o) {
        return false;
    }

    @Override
    public Object remove() {
        return null;
    }

    @Override
    public Object poll() {
        return null;
    }

    @Override
    public Object element() {
        return null;
    }

    @Override
    public Object peek() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public Object[] toArray(Object[] a) {
        return new Object[0];
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection c) {
        return false;
    }

    @Override
    public boolean addAll(Collection c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection c) {
        return false;
    }

    @Override
    public void clear() {

    }
}
