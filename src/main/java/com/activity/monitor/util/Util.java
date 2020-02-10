package com.activity.monitor.util;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.ptr.IntByReference;

public class Util {

    //for mac maxproc info
    public static int sysctl(String name, int def) {
        IntByReference size = new IntByReference(SystemB.INT_SIZE);
        Pointer p = new Memory(size.getValue());
        if (0 != SystemB.INSTANCE.sysctlbyname(name, p, size, null, 0)) {
            return def;
        }
        return p.getInt(0);
    }
}
