package com.activity.monitor.domain.impl;

import com.activity.monitor.AppConstants;
import com.activity.monitor.common.SysService;
import com.activity.monitor.domain.OperatingSystem;
import com.activity.monitor.common.SysProcess;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.ProcTaskInfo;

import java.io.File;
import java.util.*;

import static com.activity.monitor.AppConstants.MAC_PATH_LAUNCHAGENTS;
import static com.activity.monitor.AppConstants.MAC_PATH_LAUNGDAEMONS;
import static com.activity.monitor.common.SysService.State.RUNNING;
import static com.activity.monitor.common.SysService.State.STOPPED;
import static com.activity.monitor.domain.OperatingSystem.ProcessSort.PID;

public class MacOperatingSystem extends OperatingSystem {

    @Override
    public int getProcessCount() {
        return SystemB.INSTANCE.proc_listpids(SystemB.PROC_ALL_PIDS, 0, null, 0) / SystemB.INT_SIZE;
    }

    @Override
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

    @Override
    public List<SysProcess> getChildProcesses(int ppid, ProcessSort sort) {
        return null;
    }

    @Override
    public List<SysService> getServices() {

        List<SysService> services = new ArrayList<>();
        Set<String> running = new HashSet<>();
        for (SysProcess p : getChildProcesses(1, PID)) { //find for 'launchd' [pid = 1] ||  remove limit from signature
            SysService s = new SysService(p.getName(), p.getProcessID(), RUNNING);
            services.add(s);
            running.add(p.getName());
        }
        // Get Directories for stopped services
        ArrayList<File> files = new ArrayList<>();
        File dir = new File(MAC_PATH_LAUNCHAGENTS);
        if (dir.exists() && dir.isDirectory()) {
            files.addAll(Arrays.asList(dir.listFiles((f, name) -> name.toLowerCase().endsWith(".plist"))));
        } else {
//            LOG.error("Directory: /System/Library/LaunchAgents does not exist");
        }
        dir = new File(MAC_PATH_LAUNGDAEMONS);
        if (dir.exists() && dir.isDirectory()) {
            files.addAll(Arrays.asList(dir.listFiles((f, name) -> name.toLowerCase().endsWith(".plist"))));
        } else {
//            LOG.error("Directory: /System/Library/LaunchDaemons does not exist");
        }
        for (File f : files) {
            // remove .plist extension
            String name = f.getName().substring(0, f.getName().length() - 6);
            int index = name.lastIndexOf('.');
            String shortName = (index < 0 || index > name.length() - 2) ? name : name.substring(index + 1);
            if (!running.contains(name) && !running.contains(shortName)) {
                SysService s = new SysService(name, 0, STOPPED);
                services.add(s);
            }
        }
        return services;
    }

    @Override
    public int getBitness() {
        return 0;
    }

    @Override
    public long getSystemUptime() {
        return 0;
    }

    @Override
    public long getSystemBootTime() {
        return 0;
    }

    @Override
    public List<SysProcess> getProcesses() {
        return null;
    }

    @Override
    public SysProcess getProcess(int pid) {
        return null;
    }

    @Override
    public String getFamily() {
        return null;
    }

    @Override
    protected String manufacturer() {
        return "Apple";
    }

    @Override
    protected boolean elevated() {
        return System.getenv("SUDO_COMMAND") != null;
    }


}
