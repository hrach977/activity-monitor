package com.activity.monitor.platform;

import com.activity.monitor.platform.mac.Kauth;

public class TestMain {

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("den loading");
        Kauth.KAUTH_SCOPE_CALLBACK cb = new Kauth.KAUTH_SCOPE_CALLBACK();
//        Kauth.INSTANCE.kauth_listen_scope(Kauth.KAUTH_SCOPE_FILEOP, cb, null);
        System.out.println("success");
    }
}
