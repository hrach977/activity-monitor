package com.activity.monitor.domain;

import com.activity.monitor.common.SysProcess;
import com.activity.monitor.common.SysService;
import com.google.common.base.Suppliers;

import java.util.*;
import java.util.function.Supplier;

public abstract class OperatingSystem {

    public enum ProcessSort {
        CPU, MEMORY, OLDEST, NEWEST, PID, PARENTPID, NAME
    }

    private static final Map<ProcessSort, Comparator<SysProcess>> comparatorMap =
            new HashMap<ProcessSort, Comparator<SysProcess>>() {{
                put(ProcessSort.PID, Comparator.comparing(SysProcess::getProcessID));
            }};

    public static final Map<ProcessSort, Comparator<SysProcess>> COMPARATORS = Collections.unmodifiableMap(comparatorMap);

    private final Supplier<String> manufacturer = Suppliers.memoize(this::manufacturer);

    private final Supplier<Boolean> elevated = Suppliers.memoize(this::elevated);

    public abstract int getProcessCount();

    public abstract int getThreadCount();

    public abstract int getBitness();

    public String getManufacturer() {
        return manufacturer.get();
    }

    protected abstract String manufacturer();

    protected abstract boolean elevated();

    public boolean isElevated() {
        return elevated.get();
    }

    public abstract long getSystemUptime();

    public abstract long getSystemBootTime();

    public abstract List<SysProcess> getProcesses();

    public abstract List<SysService> getServices();

    public abstract List<SysProcess> getChildProcesses(int ppid, ProcessSort sort);

    public abstract SysProcess getProcess(int pid);

    public abstract String getFamily();

}
