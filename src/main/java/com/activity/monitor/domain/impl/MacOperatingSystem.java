package com.activity.monitor.domain.impl;

import com.activity.monitor.domain.OperatingSystem;
import com.activity.monitor.domain.SysProcess;
import com.sun.jna.platform.mac.SystemB;

import java.util.List;

public class MacOperatingSystem extends OperatingSystem {

    public int getProcessCount() {
//        return 0;
        return SystemB.INSTANCE.proc_listpids(SystemB.PROC_ALL_PIDS, 0, null, 0) / SystemB.INT_SIZE;

    }

    public int getThreadCount() {
        return 0;
    }

    public int getBitness() {
        return 0;
    }

    public long getSystemUptime() {
        return 0;
    }

    public long getSystemBootTime() {
        return 0;
    }

    public List<SysProcess> getProcesses() {
        return null;
    }

    public SysProcess getProcess(int pid) {
        return null;
    }

    public String getFamily() {
        return null;
    }

    public String getManufacturer() {
        return null;
    }
}
