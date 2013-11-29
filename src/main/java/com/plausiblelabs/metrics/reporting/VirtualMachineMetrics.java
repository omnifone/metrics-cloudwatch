/*
 * © 2003-2013 Omnifone™ Ltd. All rights reserved.
 */
package com.plausiblelabs.metrics.reporting;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author mrouaux
 */
public class VirtualMachineMetrics {

    public static class GarbageCollectorStats {
        private long time;
        private long runs;

        public GarbageCollectorStats(long time, long runs) {
            this.time = time;
            this.runs = runs;
        }
        
        public long getTime(TimeUnit unit) {
            return unit.convert(time, TimeUnit.MILLISECONDS);
        }
        
        public long getRuns() {
            return runs;
        }
    }
    
    private static final String JVM_METRIC_HEAP_USAGE = "heap.usage";
    private static final String JVM_METRIC_NON_HEAP_USAGE = "non-heap.usage";
    private static final String JVM_METRIC_THREAD_COUNT = "count";
    private static final String JVM_METRIC_THREAD_DAEMON_COUNT = "daemon.count";
    
    private static final String JVM_METRIC_THREAD_STATE_PRE = "";
    private static final String JVM_METRIC_THREAD_STATE_POST = ".count";

    private GarbageCollectorMetricSet jvmGcMetrics;
    private MemoryUsageGaugeSet jvmMemoryMetrics;
    private ThreadStatesGaugeSet jvmThreadMetrics;    
    
    private static VirtualMachineMetrics instance;
    
    public static VirtualMachineMetrics getInstance() {
        if(instance == null) {
            instance = new VirtualMachineMetrics();
        }
        
        return instance;
    }
    
    protected VirtualMachineMetrics() {
        this.jvmGcMetrics = new GarbageCollectorMetricSet();
        this.jvmMemoryMetrics = new MemoryUsageGaugeSet();
        this.jvmThreadMetrics = new ThreadStatesGaugeSet();        
    }
    
    public double heapUsage() {
        return ((Double)((Gauge)jvmMemoryMetrics.getMetrics()
                    .get(JVM_METRIC_HEAP_USAGE)).getValue()).doubleValue();
    }

    public double nonHeapUsage() {
        return ((Double)((Gauge)jvmMemoryMetrics.getMetrics()
                    .get(JVM_METRIC_NON_HEAP_USAGE)).getValue()).doubleValue();
    }
    
    public long threadCount() {
        return ((Integer)((Gauge)jvmThreadMetrics.getMetrics()
                    .get(JVM_METRIC_THREAD_COUNT)).getValue()).longValue();
    }
    
    public long daemonThreadCount() {
        return ((Integer)((Gauge)jvmThreadMetrics.getMetrics()
                    .get(JVM_METRIC_THREAD_DAEMON_COUNT)).getValue()).longValue();
    }
    
    public Map<Thread.State, Double> threadStatePercentages() {
        Map<Thread.State, Double> percentages = new HashMap<Thread.State, Double>();
        
        Thread.State[] states = Thread.State.values();
        for(Thread.State state : states) {
            String metricName = JVM_METRIC_THREAD_STATE_PRE + 
                    state.toString().toLowerCase() + JVM_METRIC_THREAD_STATE_POST;
            
            double stateCount = ((Integer)((Gauge)jvmThreadMetrics.getMetrics()
                    .get(metricName)).getValue()).doubleValue();
                    
            percentages.put(state, stateCount);
        }
        
        return percentages;
    }

    public Map<String, VirtualMachineMetrics.GarbageCollectorStats> garbageCollectors() {
        Map<String, VirtualMachineMetrics.GarbageCollectorStats> gcStats = 
            new HashMap<String, VirtualMachineMetrics.GarbageCollectorStats>();
        
        for(Map.Entry<String, Metric> metric : jvmGcMetrics.getMetrics().entrySet()) {
            long time = 0;
            long runs = 0;
            
            if(metric.getKey().contains("count")) {
                runs = ((Long)((Gauge)metric.getValue()).getValue()).longValue();
            }
            
            if(metric.getKey().contains("time")) {
                time = ((Long)((Gauge)metric.getValue()).getValue()).longValue();
            }
            
            String collectorName = "";
            String[] vecKey = metric.getKey().split("\\.");
            if((vecKey != null) && (vecKey.length == 4)) {
                collectorName = vecKey[2];
            }
            
            VirtualMachineMetrics.GarbageCollectorStats gcStat = 
                    new VirtualMachineMetrics.GarbageCollectorStats(time, runs);
            gcStats.put(collectorName, gcStat);
        }

        return gcStats;
    }
}
