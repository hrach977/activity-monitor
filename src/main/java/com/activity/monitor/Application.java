package com.activity.monitor;

import com.activity.monitor.common.SysProcess;
import com.activity.monitor.common.SysService;
import com.activity.monitor.domain.OperatingSystem;
import com.activity.monitor.domain.impl.MacOperatingSystem;

import java.util.List;

public class Application {

    public static void main(String[] args) {
        OperatingSystem os = new MacOperatingSystem();
        System.out.println("manufacturer: " + os.getManufacturer());
        System.out.println("elevated: " + os.isElevated());
        System.out.println("proc count: " + os.getProcessCount());
        System.out.println("thread count: " + os.getThreadCount());

//        List<SysProcess> sysProcesses = os.getProcesses();
//        System.out.println("sysProcesses.size: " + sysProcesses.size());
//        sysProcesses.forEach(System.out::println);
        System.out.println("*******");
        List<SysService> sysServices = os.getServices();
        System.out.println("sysServices.size: " + sysServices.size());
        sysServices.forEach(System.out::println);

    }
}
