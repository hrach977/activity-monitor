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
        try {
            addWatchKeys(Paths.get(PROCFS_PATH.get(os.getClass())));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        while (watchKeys.size() > 0) {

            WatchKey key;

            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            if (key.pollEvents().stream().allMatch(it -> it.kind() == OVERFLOW)) {
                continue;
            }

            stats.run();

            if (!key.reset()) {
                watchKeys.remove(key);
            }
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

    private void addWatchKeys(final Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                watchKeys.add(key);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
