package com.example.jnimemory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// This is your main driver class for the JNI memory experiment
public class MemoryTest {

    // A simple container to keep track of allocated memory addresses in Java
    private static class NativeAllocation {
        final long address;
        final long size;

        NativeAllocation(long address, long size) {
            this.address = address;
            this.size = size;
        }
    }

    // A worker thread that performs repeated allocations
    private static class AllocThread implements Runnable {
        private final int id;
        private final int iterations;
        private final long allocSize;
        private final ArrayList<NativeAllocation> allocated;

        public AllocThread(int id, int iterations, long allocSize) {
            this.id = id;
            this.iterations = iterations;
            this.allocSize = allocSize;
            this.allocated = new ArrayList<>();
        }

        @Override
        public void run() {
            for (int i = 0; i < iterations; i++) {
                long address = MemoryTestNative.allocateMemory(allocSize);
                MemoryTestNative.setMemory(address, allocSize, (byte)(i % 255));
                allocated.add(new NativeAllocation(address, allocSize));

                if (i % 100 == 0) {
                    long usage = MemoryTestNative.getResidentSize();
                    long offHeap = usage - (MemoryUtils.getUsedHeap() + MemoryUtils.getUsedNonHeap());
                    System.out.println("Thread " + id + " iteration " + i + 
                                       ": allocated " + MemoryUtils.humanReadableBytes(allocSize * i) +
                                       ", current RSS = " + MemoryUtils.humanReadableBytes(usage) +
                                       ". Estimated OffHeap = " + MemoryUtils.humanReadableBytes(offHeap));
                }

                // Sleep briefly so we can watch memory changes over time
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Optionally free the allocated memory
        public void freeAll() {
            for (NativeAllocation na : allocated) {
                MemoryTestNative.freeMemory(na.address);
            }
            allocated.clear();
        }
    }

    public static void main(String[] args) {

        // Let’s spawn a few threads that each allocate memory
        final int threadCount = 4;
        final int iterations = 1000;  // Each thread will allocate memory 1000 times
        final long allocationSize = 1024 * 1024; // 1MB each time

        System.out.println("Starting memory allocation test with " + threadCount + " threads.");

        ArrayList<AllocThread> threads = new ArrayList<>();
        ArrayList<Thread> javaThreads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            AllocThread worker = new AllocThread(i, iterations, allocationSize);
            threads.add(worker);
            Thread t = new Thread(worker);
            javaThreads.add(t);
            t.start();
        }

        // Wait for all threads to finish
        for (Thread t : javaThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Print final memory usage
        long finalUsage = MemoryTestNative.getResidentSize();
        System.out.println("All threads finished. Final RSS = " + MemoryUtils.humanReadableBytes(finalUsage));

        // Optionally free memory to see if usage decreases
        // (due to glibc arena behavior, the RSS might not drop immediately or might not drop at all)
        System.out.println("Freeing all allocated native memory...");
        for (AllocThread worker : threads) {
            worker.freeAll();
        }

        // Sleep a bit to see if the OS reclaims memory
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        long postFreeUsage = MemoryTestNative.getResidentSize();
        System.out.println("After freeing memory, RSS = " + MemoryUtils.humanReadableBytes(postFreeUsage));
    }
}
