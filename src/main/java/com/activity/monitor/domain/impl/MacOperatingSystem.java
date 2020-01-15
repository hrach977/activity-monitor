package com.activity.monitor.domain.impl;

import com.activity.monitor.AppConstants;
import com.activity.monitor.common.SysService;
import com.activity.monitor.domain.OperatingSystem;
import com.activity.monitor.common.SysProcess;
import com.activity.monitor.util.Util;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.ProcTaskInfo;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.activity.monitor.AppConstants.MAC_PATH_LAUNCHAGENTS;
import static com.activity.monitor.AppConstants.MAC_PATH_LAUNGDAEMONS;
import static com.activity.monitor.common.SysProcess.State.*;
import static com.activity.monitor.domain.OperatingSystem.ProcessSort.PID;

public class MacOperatingSystem extends OperatingSystem {

    private final String osXVersion;
    private final int maxProc;

    // 64-bit flag
    private static final int P_LP64 = 0x4;
    /*
     * OS X States:
     */
    private static final int SSLEEP = 1; // sleeping on high priority
    private static final int SWAIT = 2; // sleeping on low priority
    private static final int SRUN = 3; // running
    private static final int SIDL = 4; // intermediate state in process creation
    private static final int SZOMB = 5; // intermediate state in process
    // termination
    private static final int SSTOP = 6; // process being traced

    public MacOperatingSystem() {
        this.maxProc = Util.sysctl("kern.maxproc", 0x1000);
        this.osXVersion = System.getProperty("os.version");
        System.out.println("maxProc: " + maxProc);
        System.out.println("osXVersion: " + osXVersion);
    }

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
        List<SysProcess> procs = new ArrayList<>();
        int[] buffer = new int[this.maxProc];
        int numberOfProcesses = SystemB.INSTANCE.proc_listpids(SystemB.PROC_ALL_PIDS, 0, buffer,
                buffer.length * SystemB.INT_SIZE) / SystemB.INT_SIZE;

        for (int i = 0; i < numberOfProcesses; i++) {
            // Handle OB1 error in proc_listpids where the size returned is: SystemB.INT_SIZE * (buffer + 1)
            if (buffer[i] == 0) {
                continue;
            }
            if (ppid == getParentProcessPid(buffer[i])) {
                SysProcess proc = getProcess(buffer[i]); //todo implement this
                if (proc != null) {
                    procs.add(proc);
                }
            }
        }

        procs.sort(OperatingSystem.COMPARATORS.get(PID));

        return procs;
    }

    private int getParentProcessPid(int pid) {
        SystemB.ProcTaskAllInfo taskAllInfo = new SystemB.ProcTaskAllInfo();
        if (0 > SystemB.INSTANCE.proc_pidinfo(pid, SystemB.PROC_PIDTASKALLINFO, 0, taskAllInfo, taskAllInfo.size())) {
            return 0;
        }
        return taskAllInfo.pbsd.pbi_ppid;
    }

    @Override
    public List<SysService> getServices() {

        List<SysService> services = new ArrayList<>();
        Set<String> running = new HashSet<>();
        for (SysProcess p : getChildProcesses(1, PID)) { //find for 'launchd' [pid = 1] ||  remove limit from signature
            SysService s = new SysService(p.getName(), p.getProcessID(), SysService.State.RUNNING);
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
                SysService s = new SysService(name, 0, SysService.State.STOPPED);
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
        SystemB.ProcTaskAllInfo taskAllInfo = new SystemB.ProcTaskAllInfo();
        if (0 > SystemB.INSTANCE.proc_pidinfo(pid, SystemB.PROC_PIDTASKALLINFO, 0, taskAllInfo, taskAllInfo.size())) {
            return null;
        }
        String name = null;
        String path = "";
        Pointer buf = new Memory(SystemB.PROC_PIDPATHINFO_MAXSIZE);
        if (0 < SystemB.INSTANCE.proc_pidpath(pid, buf, SystemB.PROC_PIDPATHINFO_MAXSIZE)) {
            path = buf.getString(0).trim();
            // Overwrite name with last part of path
            String[] pathSplit = path.split("/");
            if (pathSplit.length > 0) {
                name = pathSplit[pathSplit.length - 1];
            }
        }
        // If process is gone, return null
        if (taskAllInfo.ptinfo.pti_threadnum < 1) {
            return null;
        }
        if (name == null) {
            // pbi_comm contains first 16 characters of name
            // null terminated
            for (int t = 0; t < taskAllInfo.pbsd.pbi_comm.length; t++) {
                if (taskAllInfo.pbsd.pbi_comm[t] == 0) {
                    name = new String(taskAllInfo.pbsd.pbi_comm, 0, t, StandardCharsets.UTF_8);
                    break;
                }
            }
        }
        long bytesRead = 0;
        long bytesWritten = 0;
//        if (this.minor >= 9) { //todo recall to it later
            SystemB.RUsageInfoV2 rUsageInfoV2 = new SystemB.RUsageInfoV2();
            if (0 == SystemB.INSTANCE.proc_pid_rusage(pid, SystemB.RUSAGE_INFO_V2, rUsageInfoV2)) {
                bytesRead = rUsageInfoV2.ri_diskio_bytesread;
                bytesWritten = rUsageInfoV2.ri_diskio_byteswritten;
            }
//        }
        long now = System.currentTimeMillis();
        SysProcess proc = new SysProcess();
        proc.setName(name);
        proc.setPath(path);
        switch (taskAllInfo.pbsd.pbi_status) {
            case SSLEEP:
                proc.setState(SLEEPING);
                break;
            case SWAIT:
                proc.setState(WAITING);
                break;
            case SRUN:
                proc.setState(RUNNING);
                break;
            case SIDL:
                proc.setState(NEW);
                break;
            case SZOMB:
                proc.setState(ZOMBIE);
                break;
            case SSTOP:
                proc.setState(STOPPED);
                break;
            default:
                proc.setState(OTHER);
                break;
        }
        proc.setProcessID(pid);
        proc.setParentProcessID(taskAllInfo.pbsd.pbi_ppid);
        proc.setUserID(Integer.toString(taskAllInfo.pbsd.pbi_uid));
        SystemB.Passwd user = SystemB.INSTANCE.getpwuid(taskAllInfo.pbsd.pbi_uid);
        proc.setUser(user == null ? proc.getUserID() : user.pw_name);
        proc.setGroupID(Integer.toString(taskAllInfo.pbsd.pbi_gid));
        SystemB.Group group = SystemB.INSTANCE.getgrgid(taskAllInfo.pbsd.pbi_gid);
        proc.setGroup(group == null ? proc.getGroupID() : group.gr_name);
        proc.setThreadCount(taskAllInfo.ptinfo.pti_threadnum);
        proc.setPriority(taskAllInfo.ptinfo.pti_priority);
        proc.setVirtualSize(taskAllInfo.ptinfo.pti_virtual_size);
        proc.setResidentSetSize(taskAllInfo.ptinfo.pti_resident_size);
        proc.setKernelTime(taskAllInfo.ptinfo.pti_total_system / 1000000L);
        proc.setUserTime(taskAllInfo.ptinfo.pti_total_user / 1000000L);
        proc.setStartTime(taskAllInfo.pbsd.pbi_start_tvsec * 1000L + taskAllInfo.pbsd.pbi_start_tvusec / 1000L);
        proc.setUpTime(now - proc.getStartTime());
        proc.setBytesRead(bytesRead);
        proc.setBytesWritten(bytesWritten);
//        proc.setCommandLine(getCommandLine(pid)); //todo recall to it later
        proc.setOpenFiles(taskAllInfo.pbsd.pbi_nfiles);
        proc.setBitness((taskAllInfo.pbsd.pbi_flags & P_LP64) == 0 ? 32 : 64);

        SystemB.VnodePathInfo vpi = new SystemB.VnodePathInfo();
        if (0 < SystemB.INSTANCE.proc_pidinfo(pid, SystemB.PROC_PIDVNODEPATHINFO, 0, vpi, vpi.size())) {
            int len = 0;
            for (byte b : vpi.pvi_cdir.vip_path) {
                if (b == 0) {
                    break;
                }
                len++;
            }
            proc.setCurrentWorkingDirectory(new String(vpi.pvi_cdir.vip_path, 0, len, StandardCharsets.US_ASCII));
        }
        return proc;
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
