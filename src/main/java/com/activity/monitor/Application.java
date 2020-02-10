package com.activity.monitor;

import com.activity.monitor.common.SysProcess;
import com.activity.monitor.domain.OperatingSystem;
import com.activity.monitor.domain.Reporter;
import com.activity.monitor.domain.impl.MacOperatingSystem;

import java.util.List;

public class Application {

    static OperatingSystem os = new MacOperatingSystem();

    public static void main(String[] args) {

        Reporter reporter = new Reporter(os, Application::initialReport);
        reporter.start(Application::stats);

    }

    static void initialReport() {
        System.out.println("manufacturer: " + os.getManufacturer());
        System.out.println("elevated: " + os.isElevated());
        stats();
    }

    static void stats() {
        System.out.println("proc count: " + os.getProcessCount());
        System.out.println("thread count: " + os.getThreadCount());
        System.out.println("*******");
        List<SysProcess> sysProcesses = os.getProcesses();
        System.out.println("sysProcesses.size: " + sysProcesses.size());
        sysProcesses.forEach(System.out::println);
    }
}
