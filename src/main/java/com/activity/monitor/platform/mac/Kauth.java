package com.activity.monitor.platform.mac;

import com.sun.jna.*;

public interface Kauth extends Library {

    Kauth INSTANCE = Native.load("kauth", Kauth.class);

    String KAUTH_SCOPE_FILEOP = "com.apple.kauth.fileop";

    class KAUTH_LISTENER_T extends Structure {

    }

    class KAUTH_SCOPE_CALLBACK extends Structure {}

    KAUTH_LISTENER_T kauth_listen_scope(String identifier, KAUTH_SCOPE_CALLBACK callback, Pointer idata);
}
