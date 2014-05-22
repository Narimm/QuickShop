package org.maxgamer.QuickShop.Watcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.QuickShop.QuickShop;

public class LogWatcher implements Runnable {
    private PrintStream             ps;

    private final ArrayList<String> logs = new ArrayList<String>(5);
    public BukkitTask               task;

    public LogWatcher(QuickShop plugin, File log) {
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            final FileOutputStream fos = new FileOutputStream(log, true);
            ps = new PrintStream(fos);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Log file not found!");
        } catch (final IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not create log file!");
        }
    }

    @Override
    public void run() {
        synchronized (logs) {
            for (final String s: logs) {
                ps.println(s);
            }
            logs.clear();
        }
    }

    public void add(String s) {
        synchronized (logs) {
            logs.add(s);
        }
    }

    public void close() {
        ps.close();
    }
}