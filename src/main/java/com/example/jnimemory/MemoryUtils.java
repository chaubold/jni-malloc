package com.example.jnimemory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class MemoryUtils {

    public static long getUsedHeap() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memBean.getHeapMemoryUsage();
        return heapUsage.getUsed(); // bytes of objects on the heap
    }

    public static long getUsedNonHeap() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage nonHeapUsage = memBean.getNonHeapMemoryUsage();
        return nonHeapUsage.getUsed(); // e.g., metaspace, etc.
    }

    public static String humanReadableBytes(long bytes) {
        // Uses binary (1024) units
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1) + ""; // K, M, G, T, P, E
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}