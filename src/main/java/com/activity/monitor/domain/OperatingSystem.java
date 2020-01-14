package com.activity.monitor.domain;

import com.google.common.base.Suppliers;

import java.util.List;
import java.util.function.Supplier;

public abstract class OperatingSystem {

    private final Supplier<String> manufacturer = Suppliers.memoize(this::manufacturer);

    public abstract int getProcessCount();

    public abstract int getThreadCount();

    public abstract int getBitness();

    public String getManufacturer() {
        return manufacturer.get();
    }

    protected abstract String manufacturer();

    public abstract long getSystemUptime();

    public abstract long getSystemBootTime();

    public abstract List<SysProcess> getProcesses();

    public abstract SysProcess getProcess(int pid);

    public abstract String getFamily();

    public enum SysProcessSort {
        CPU, MEMORY, PID, PARENTPID, NAME, OLDEST, NEWEST
    }

}
