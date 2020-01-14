package com.activity.monitor.domain;

import java.util.List;

public abstract class OperatingSystem {

    public abstract int getProcessCount();

    public abstract int getThreadCount();

    public abstract int getBitness();

    public abstract long getSystemUptime();

    public abstract long getSystemBootTime();

    public abstract List<SysProcess> getProcesses();

    public abstract SysProcess getProcess(int pid);

    public abstract String getFamily();

    public abstract String getManufacturer();

    public enum SysProcessSort {
        CPU, MEMORY, PID, PARENTPID, NAME, OLDEST, NEWEST
    }

}
