package com.activity.monitor;

import com.activity.monitor.domain.OperatingSystem;
import com.activity.monitor.domain.impl.MacOperatingSystem;

public class Application {

    public static void main(String[] args) {
        OperatingSystem os = new MacOperatingSystem();
        System.out.println("proc count: " + os.getProcessCount());
    }
}
