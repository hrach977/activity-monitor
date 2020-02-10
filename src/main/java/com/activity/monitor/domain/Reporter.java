package com.activity.monitor.domain;

import com.activity.monitor.AppConstants;
import com.activity.monitor.common.SysProcess;
import com.activity.monitor.domain.impl.MacOperatingSystem;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

//todo this is that component mentioned in Application.class
//todo maybe this should not depend on the os, but rather os methods are called from the callback passed to the start()?
public class Reporter {

    private final OperatingSystem os;

    private WatchService watcher;

    private final Runnable initialReport;

    private final List<WatchKey> watchKeys = new LinkedList<>();

    private static final Map<Class<?>, String> PROCFS_PATH = new HashMap<Class<?>, String>() {{
       put(MacOperatingSystem.class, AppConstants.MAC_DIR_TO_WATCH);
    }};

    public Reporter(OperatingSystem os, Runnable initialReport) {
        this.os = os;
        this.initialReport = initialReport;
    }

    {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::hook));
    }

    public void start(Runnable stats) {
        initialReport.run();
        //todo the watchservice loop, which invokes run in case of new key
        while (watchKeys.size() > 0) {

        }
    }

    private void hook() {
        if (watcher != null) {
            try {
                watcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


//    private void initialReport() {
//        System.out.println("manufacturer: " + os.getManufacturer());
//        System.out.println("elevated: " + os.isElevated());
//        System.out.println("proc count: " + os.getProcessCount());
//        System.out.println("thread count: " + os.getThreadCount());
//        System.out.println("*******");
//        List<SysProcess> sysProcesses = os.getProcesses();
//        System.out.println("sysProcesses.size: " + sysProcesses.size());
//        sysProcesses.forEach(System.out::println);
//        System.out.println("*******");
//    }

    private void addWatchKeys(final Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                watchKeys.add(key); //todo adding so that to track the size when removing keys in case of invalidity
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
