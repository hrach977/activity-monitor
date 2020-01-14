package com.activity.monitor.domain.impl;

import com.activity.monitor.AppConstants;
import com.activity.monitor.domain.OperatingSystem;
import com.activity.monitor.domain.SysProcess;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.ProcTaskInfo;

import java.util.List;

public class MacOperatingSystem extends OperatingSystem {

    public int getProcessCount() {
        return SystemB.INSTANCE.proc_listpids(SystemB.PROC_ALL_PIDS, 0, null, 0) / SystemB.INT_SIZE;
    }

    public int getThreadCount() {
        int count = 0;

        int[] buffer = new int[getProcessCount() + AppConstants.MAC_BUFFER_PADDING]; //jic new process starts during allocation

        int processCount = SystemB.INSTANCE.proc_listpids(SystemB.PROC_ALL_PIDS, 0, buffer, buffer.length) / SystemB.INT_SIZE;

        ProcTaskInfo taskInfoBuffer = new ProcTaskInfo();

        for (int i = 0; i < processCount; i++) {
            SystemB.INSTANCE.proc_pidinfo(buffer[i], SystemB.PROC_PIDTASKINFO, 0, taskInfoBuffer, taskInfoBuffer.size());
            count += taskInfoBuffer.pti_threadnum;
        }

        return count;
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
