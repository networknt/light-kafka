/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.kafka.common;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test cases for CircularFifoQueue.
 *
 * @since 4.0
 */
public class CircularFifoQueueTest<E> {

    public CircularFifoQueueTest() {
    }

    //-----------------------------------------------------------------------
    /**
     * Tests that the removal operation actually removes the first element.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCircularFifoQueueCircular() {
        final List<E> list = new ArrayList<>();
        list.add((E) "A");
        list.add((E) "B");
        list.add((E) "C");
        final Queue<E> queue = new CircularFifoQueue<>(list);

        assertEquals(true, queue.contains("A"));
        assertEquals(true, queue.contains("B"));
        assertEquals(true, queue.contains("C"));

        queue.add((E) "D");

        assertEquals(false, queue.contains("A"));
        assertEquals(true, queue.contains("B"));
        assertEquals(true, queue.contains("C"));
        assertEquals(true, queue.contains("D"));

        assertEquals("B", queue.peek());
        assertEquals("B", queue.remove());
        assertEquals("C", queue.remove());
        assertEquals("D", queue.remove());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveError1() throws Exception {
        // based on bug 33071
        final CircularFifoQueue<E> fifo = new CircularFifoQueue<>(5);
        fifo.add((E) "1");
        fifo.add((E) "2");
        fifo.add((E) "3");
        fifo.add((E) "4");
        fifo.add((E) "5");

        assertEquals("[1, 2, 3, 4, 5]", fifo.toString());

        fifo.remove("3");
        assertEquals("[1, 2, 4, 5]", fifo.toString());

        fifo.remove("4");
        assertEquals("[1, 2, 5]", fifo.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveError2() throws Exception {
        // based on bug 33071
        final CircularFifoQueue<E> fifo = new CircularFifoQueue<>(5);
        fifo.add((E) "1");
        fifo.add((E) "2");
        fifo.add((E) "3");
        fifo.add((E) "4");
        fifo.add((E) "5");
        fifo.add((E) "6");

        assertEquals(5, fifo.size());
        assertEquals("[2, 3, 4, 5, 6]", fifo.toString());

        fifo.remove("3");
        assertEquals("[2, 4, 5, 6]", fifo.toString());

        fifo.remove("4");
        assertEquals("[2, 5, 6]", fifo.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveError3() throws Exception {
        // based on bug 33071
        final CircularFifoQueue<E> fifo = new CircularFifoQueue<>(5);
        fifo.add((E) "1");
        fifo.add((E) "2");
        fifo.add((E) "3");
        fifo.add((E) "4");
        fifo.add((E) "5");

        assertEquals("[1, 2, 3, 4, 5]", fifo.toString());

        fifo.remove("3");
        assertEquals("[1, 2, 4, 5]", fifo.toString());

        fifo.add((E) "6");
        fifo.add((E) "7");
        assertEquals("[2, 4, 5, 6, 7]", fifo.toString());

        fifo.remove("4");
        assertEquals("[2, 5, 6, 7]", fifo.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveError4() throws Exception {
        // based on bug 33071
        final CircularFifoQueue<E> fifo = new CircularFifoQueue<>(5);
        fifo.add((E) "1");
        fifo.add((E) "2");
        fifo.add((E) "3");
        fifo.add((E) "4");
        fifo.add((E) "5");  // end=0
        fifo.add((E) "6");  // end=1
        fifo.add((E) "7");  // end=2

        assertEquals("[3, 4, 5, 6, 7]", fifo.toString());

        fifo.remove("4");  // remove element in middle of array, after start
        assertEquals("[3, 5, 6, 7]", fifo.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveError5() throws Exception {
        // based on bug 33071
        final CircularFifoQueue<E> fifo = new CircularFifoQueue<>(5);
        fifo.add((E) "1");
        fifo.add((E) "2");
        fifo.add((E) "3");
        fifo.add((E) "4");
        fifo.add((E) "5");  // end=0
        fifo.add((E) "6");  // end=1
        fifo.add((E) "7");  // end=2

        assertEquals("[3, 4, 5, 6, 7]", fifo.toString());

        fifo.remove("5");  // remove element at last pos in array
        assertEquals("[3, 4, 6, 7]", fifo.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveError6() throws Exception {
        // based on bug 33071
        final CircularFifoQueue<E> fifo = new CircularFifoQueue<>(5);
        fifo.add((E) "1");
        fifo.add((E) "2");
        fifo.add((E) "3");
        fifo.add((E) "4");
        fifo.add((E) "5");  // end=0
        fifo.add((E) "6");  // end=1
        fifo.add((E) "7");  // end=2

        assertEquals("[3, 4, 5, 6, 7]", fifo.toString());

        fifo.remove("6");  // remove element at position zero in array
        assertEquals("[3, 4, 5, 7]", fifo.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveError7() throws Exception {
        // based on bug 33071
        final CircularFifoQueue<E> fifo = new CircularFifoQueue<>(5);
        fifo.add((E) "1");
        fifo.add((E) "2");
        fifo.add((E) "3");
        fifo.add((E) "4");
        fifo.add((E) "5");  // end=0
        fifo.add((E) "6");  // end=1
        fifo.add((E) "7");  // end=2

        assertEquals("[3, 4, 5, 6, 7]", fifo.toString());

        fifo.remove("7");  // remove element at position one in array
        assertEquals("[3, 4, 5, 6]", fifo.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveError8() throws Exception {
        // based on bug 33071
        final CircularFifoQueue<E> fifo = new CircularFifoQueue<>(5);
        fifo.add((E) "1");
        fifo.add((E) "2");
        fifo.add((E) "3");
        fifo.add((E) "4");
        fifo.add((E) "5");  // end=0
        fifo.add((E) "6");  // end=1
        fifo.add((E) "7");  // end=2
        fifo.add((E) "8");  // end=3

        assertEquals("[4, 5, 6, 7, 8]", fifo.toString());

        fifo.remove("7");  // remove element at position one in array, need to shift 8
        assertEquals("[4, 5, 6, 8]", fifo.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveError9() throws Exception {
        // based on bug 33071
        final CircularFifoQueue<E> fifo = new CircularFifoQueue<>(5);
        fifo.add((E) "1");
        fifo.add((E) "2");
        fifo.add((E) "3");
        fifo.add((E) "4");
        fifo.add((E) "5");  // end=0
        fifo.add((E) "6");  // end=1
        fifo.add((E) "7");  // end=2
        fifo.add((E) "8");  // end=3

        assertEquals("[4, 5, 6, 7, 8]", fifo.toString());

        fifo.remove("8");  // remove element at position two in array
        assertEquals("[4, 5, 6, 7]", fifo.toString());
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Test
    public void testRepeatedSerialization() throws Exception {
        // bug 31433
        final CircularFifoQueue<E> b = new CircularFifoQueue<>(2);
        b.add((E) "a");
        assertEquals(1, b.size());
        assertEquals(true, b.contains("a"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(b);

        final CircularFifoQueue<E> b2 = (CircularFifoQueue<E>) new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();

        assertEquals(1, b2.size());
        assertEquals(true, b2.contains("a"));
        b2.add((E) "b");
        assertEquals(2, b2.size());
        assertEquals(true, b2.contains("a"));
        assertEquals(true, b2.contains("b"));

        bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(b2);

        final CircularFifoQueue<E> b3 = (CircularFifoQueue<E>) new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();

        assertEquals(2, b3.size());
        assertEquals(true, b3.contains("a"));
        assertEquals(true, b3.contains("b"));
        b3.add((E) "c");
        assertEquals(2, b3.size());
        assertEquals(true, b3.contains("b"));
        assertEquals(true, b3.contains("c"));
    }

}