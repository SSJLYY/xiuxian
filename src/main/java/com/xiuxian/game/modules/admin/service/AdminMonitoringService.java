package com.xiuxian.game.modules.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMonitoringService {

    private final SystemInfo systemInfo = new SystemInfo();
    private final OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
    private final HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();

    /**
     * 获取系统基本信息
     *
     * @return 系统基本信息
     */
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        // 操作系统信息
        info.put("osName", operatingSystem.getFamily() + " " + operatingSystem.getVersionInfo().getVersion());
        info.put("osArch", System.getProperty("os.arch"));

        // JVM信息
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));

        // 应用信息
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        info.put("uptime", formatUptime(runtimeMXBean.getUptime()));
        info.put("startTime", new Date(runtimeMXBean.getStartTime()));

        return info;
    }

    /**
     * 获取CPU使用情况
     *
     * @return CPU使用情况
     */
    public Map<String, Object> getCpuInfo() {
        Map<String, Object> info = new HashMap<>();

        CentralProcessor processor = hardwareAbstractionLayer.getProcessor();
        info.put("physicalCoreCount", processor.getPhysicalProcessorCount());
        info.put("logicalCoreCount", processor.getLogicalProcessorCount());
        info.put("systemLoadAverage", processor.getSystemLoadAverage(1)); // 传入参数1

        // 获取CPU使用�?
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        info.put("cpuUsage", String.format("%.2f%%", cpuUsage));

        return info;
    }

    /**
     * 获取内存使用情况
     *
     * @return 内存使用情况
     */
    public Map<String, Object> getMemoryInfo() {
        Map<String, Object> info = new HashMap<>();

        GlobalMemory memory = hardwareAbstractionLayer.getMemory();
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;

        info.put("totalMemory", formatBytes(totalMemory));
        info.put("usedMemory", formatBytes(usedMemory));
        info.put("availableMemory", formatBytes(availableMemory));
        info.put("memoryUsage", String.format("%.2f%%", (double) usedMemory / totalMemory * 100));

        // JVM内存信息
        Runtime runtime = Runtime.getRuntime();
        long jvmMaxMemory = runtime.maxMemory();
        long jvmTotalMemory = runtime.totalMemory();
        long jvmFreeMemory = runtime.freeMemory();
        long jvmUsedMemory = jvmTotalMemory - jvmFreeMemory;

        info.put("jvmMaxMemory", formatBytes(jvmMaxMemory));
        info.put("jvmTotalMemory", formatBytes(jvmTotalMemory));
        info.put("jvmUsedMemory", formatBytes(jvmUsedMemory));
        info.put("jvmFreeMemory", formatBytes(jvmFreeMemory));
        info.put("jvmMemoryUsage", String.format("%.2f%%", (double) jvmUsedMemory / jvmTotalMemory * 100));

        return info;
    }

    /**
     * 获取磁盘使用情况
     *
     * @return 磁盘使用情况
     */
    public Map<String, Object> getDiskInfo() {
        Map<String, Object> info = new HashMap<>();

        // 获取应用根路�?
        String rootPath = System.getProperty("user.dir");
        java.io.File rootDir = new java.io.File(rootPath);

        long totalSpace = rootDir.getTotalSpace();
        long freeSpace = rootDir.getFreeSpace();
        long usableSpace = rootDir.getUsableSpace();
        long usedSpace = totalSpace - freeSpace;

        info.put("totalSpace", formatBytes(totalSpace));
        info.put("usedSpace", formatBytes(usedSpace));
        info.put("freeSpace", formatBytes(freeSpace));
        info.put("usableSpace", formatBytes(usableSpace));
        info.put("diskUsage", String.format("%.2f%%", (double) usedSpace / totalSpace * 100));

        return info;
    }

    /**
     * 格式化字节大�?
     *
     * @param bytes 字节�?
     * @return 格式化后的字符串
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * 格式化运行时�?
     *
     * @param uptime 运行时间（毫秒）
     * @return 格式化后的字符串
     */
    private String formatUptime(long uptime) {
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("�?");
        }
        if (hours > 0) {
            sb.append(hours).append("小时 ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟 ");
        }
        sb.append(seconds).append("�?);

        return sb.toString().trim();
    }
}
