package com.activity.monitor;

import com.activity.monitor.common.SysProcess;
import com.activity.monitor.common.SysService;
import com.activity.monitor.domain.OperatingSystem;
import com.activity.monitor.domain.Reporter;
import com.activity.monitor.domain.impl.MacOperatingSystem;

import java.util.List;

public class Application {

    //todo: include also a monitoring component, injecting an OS instance to it via constructor, which watches the FS for changes
    //the component should choose the appropriate path to watch for based on the actual type of the OS injected in it

    static OperatingSystem os = new MacOperatingSystem();

    public static void main(String[] args) {
//        OperatingSystem os = new MacOperatingSystem();
//        System.out.println(MacOperatingSystem.class == os.getClass());
//        System.out.println("the class: " + MacOperatingSystem.class);
//        System.out.println("the class: " + os.getClass());
//        System.out.println("manufacturer: " + os.getManufacturer());
//        System.out.println("elevated: " + os.isElevated());
//        System.out.println("proc count: " + os.getProcessCount());
//        System.out.println("thread count: " + os.getThreadCount());
//        System.out.println("*******");
//        List<SysProcess> sysProcesses = os.getProcesses();
//        System.out.println("sysProcesses.size: " + sysProcesses.size());
//        sysProcesses.forEach(System.out::println);
//        System.out.println("*******");
        Reporter reporter = new Reporter(os, Application::initialReport);
        reporter.start(Application::stats);


//        List<SysService> sysServices = os.getServices();
//        System.out.println("sysServices.size: " + sysServices.size());
//        sysServices.forEach(System.out::println);
//        SysProcess proc = os.getProcess(40345);
//        System.out.println(proc);

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
