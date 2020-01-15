package com.activity.monitor.domain;

import com.google.common.base.Suppliers;

import java.util.List;
import java.util.function.Supplier;

public abstract class OperatingSystem {

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

    public abstract SysProcess getProcess(int pid);

    public abstract String getFamily();

    public enum SysProcessSort {
        CPU, MEMORY, PID, PARENTPID, NAME, OLDEST, NEWEST
    }

}
