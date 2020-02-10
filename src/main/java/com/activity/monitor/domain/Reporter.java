package com.activity.monitor.domain;

import com.activity.monitor.AppConstants;
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

    private final WatchService watcher;

    private final List<WatchKey> watchKeys = new LinkedList<>();

    private static final Map<Class<?>, String> PROCFS_PATH = new HashMap<Class<?>, String>() {{
       put(MacOperatingSystem.class, AppConstants.MAC_DIR_TO_WATCH);
    }};

    public Reporter(OperatingSystem os) throws IOException {
        this.os = os;
    }

    {
        watcher = FileSystems.getDefault().newWatchService();
        Runtime.getRuntime().addShutdownHook(new Thread(this::hook));
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

    public void start() {

    }

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
