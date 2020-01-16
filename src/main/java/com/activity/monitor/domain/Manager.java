package com.activity.monitor.domain;

import com.activity.monitor.AppConstants;
import com.activity.monitor.domain.impl.MacOperatingSystem;

import java.util.HashMap;
import java.util.Map;

public class Manager {

    private final OperatingSystem os;

    private final Map<Class<?>, String> procfs = new HashMap<Class<?>, String>() {{
       put(MacOperatingSystem.class, AppConstants.MAC_DIR_TO_WATCH);
    }};

    public Manager(OperatingSystem os) {
        this.os = os;
    }
}
