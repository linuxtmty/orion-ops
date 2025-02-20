package com.orion.ops.utils;

import com.orion.id.UUIds;
import com.orion.ops.consts.Const;
import com.orion.ops.consts.StainCode;
import com.orion.ops.consts.system.SystemEnvAttr;
import com.orion.ops.consts.system.ThreadPoolMetricsType;
import com.orion.ops.entity.vo.ThreadPoolMetricsVO;
import com.orion.ops.service.api.MachineEnvService;
import com.orion.spring.SpringHolder;
import com.orion.utils.Exceptions;
import com.orion.utils.Strings;
import com.orion.utils.io.FileReaders;
import com.orion.utils.io.Files1;
import com.orion.utils.io.Streams;
import com.orion.utils.net.IPs;
import com.orion.utils.time.Dates;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 工具类
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2021/12/3 9:28
 */
public class Utils {

    private Utils() {
    }

    /**
     * 获取随机后缀
     *
     * @return suffix
     */
    public static String getRandomSuffix() {
        return UUIds.random32().substring(0, 5).toUpperCase();
    }

    /**
     * 获取后缀
     *
     * @param symbol symbol
     * @return suffix
     */
    public static String getSymbolSuffix(String symbol) {
        return " - " + symbol + Strings.SPACE + UUIds.random32().substring(0, 5).toUpperCase();
    }

    /**
     * 获取file 尾行数据
     *
     * @param path path
     * @return lines
     */
    public static String getFileTailLine(String path) {
        RandomAccessFile reader = null;
        try {
            if (Strings.isBlank(path)) {
                return Strings.EMPTY;
            }
            File file = new File(path);
            // 文件不存在
            if (!Files1.isFile(file)) {
                return Strings.EMPTY;
            }
            // 行数
            Integer offset = SpringHolder.getBean(MachineEnvService.class).getTailOffset(Const.HOST_MACHINE_ID);
            // 获取偏移量
            reader = Files1.openRandomAccess(file, Const.ACCESS_R);
            return FileReaders.readTailLines(reader, offset);
        } catch (IOException e) {
            throw Exceptions.ioRuntime(e);
        } finally {
            Streams.close(reader);
        }
    }

    /**
     * 检查是否是合法 ip 配置行
     *
     * @param line line
     * @return 是否合法
     */
    public static boolean validIpLine(String line) {
        // 普通ip
        line = line.trim();
        if (!line.contains(Const.SLASH)) {
            return IPs.isIpv4(line);
        }
        return validIpRange(line);
    }

    /**
     * 检查是否为合法 ip 区间
     *
     * @param range range
     * @return 是否合法
     */
    public static boolean validIpRange(String range) {
        // ip区间
        String[] split = range.split(Const.SLASH);
        String first = split[0];
        String last = split[1];
        if (split.length != 2) {
            return false;
        }
        // 检查第一段
        if (!IPs.isIpv4(first)) {
            return false;
        }
        // 尾ip
        if (!last.contains(Const.DOT)) {
            last = first.substring(0, first.lastIndexOf(Const.DOT)) + Const.DOT + last;
        }
        return IPs.isIpv4(last);
    }

    /**
     * 检查 ip 是否在范围内
     *
     * @param ip     ip
     * @param filter filter
     * @return 是否在范围内
     */
    public static boolean checkIpIn(String ip, String filter) {
        filter = filter.trim();
        // 单个ip
        if (!filter.contains(Const.SLASH)) {
            return ip.equals(filter);
        }
        // ip区间
        String[] split = filter.split(Const.SLASH);
        String first = split[0];
        String last = split[1];
        // 尾ip
        if (!last.contains(Const.DOT)) {
            last = first.substring(0, first.lastIndexOf(Const.DOT)) + Const.DOT + last;
        }
        return IPs.ipInRange(first, last, ip);
    }

    /**
     * 获取事件仓库路径
     *
     * @param id id
     * @return 路径
     */
    public static String getVcsEventDir(Long id) {
        return Files1.getPath(SystemEnvAttr.VCS_PATH.getValue(), Const.EVENT_DIR + "/" + id);
    }

    /**
     * 获取时差
     *
     * @param ms ms
     * @return 时差
     */
    public static String interval(Long ms) {
        if (ms == null) {
            return null;
        }
        return Dates.interval(ms, false, "d ", "h ", "m ", "s");
    }

    /**
     * 获取线程池指标
     *
     * @param metricsType 指标类型
     * @return 指标
     */
    public static ThreadPoolMetricsVO getThreadPoolMetrics(ThreadPoolMetricsType metricsType) {
        ThreadPoolMetricsVO metrics = new ThreadPoolMetricsVO();
        metrics.setType(metricsType.getType());
        ThreadPoolExecutor executor = metricsType.getExecutor();
        metrics.setActiveThreadCount(executor.getActiveCount());
        metrics.setTotalTaskCount(executor.getTaskCount());
        metrics.setCompletedTaskCount(executor.getCompletedTaskCount());
        metrics.setQueueSize(executor.getQueue().size());
        return metrics;
    }

    /**
     * 获取 ANSI 高亮颜色行
     *
     * @param key  key
     * @param code code
     * @return 高亮字体
     * @see com.orion.ops.consts.StainCode
     */
    public static String getStainKeyWords(Object key, int code) {
        return StainCode.prefix(code) + key + StainCode.SUFFIX;
    }

    /**
     * 清除 ANSI 高亮颜色行
     *
     * @param s s
     * @return 清除 ANSI 属性
     * @see com.orion.ops.consts.StainCode
     */
    public static String cleanStainAnsiCode(String s) {
        return s.replaceAll("\\u001B\\[\\w{1,3}m", Const.EMPTY);
    }

    /**
     * 判断是否为 \n 结尾
     * 是则返回 \n
     * 否则返回 \n\n
     *
     * @param s s
     * @return lf
     */
    public static String getEndLfWithEof(String s) {
        return s.endsWith(Const.LF) ? s + Const.LF : s + Const.LF_2;
    }

    /**
     * 获取 sftp 压缩命令
     *
     * @param zipPath zipPath
     * @param paths   paths
     * @return zip 命令
     */
    public static String getSftpPackageCommand(String zipPath, List<String> paths) {
        return "mkdir -p " + Files1.getParentPath(zipPath) + " && zip -r \"" + zipPath + "\" "
                + paths.stream()
                .map(Files1::getPath)
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(Const.SPACE));
    }

    /**
     * 清空高亮标签
     *
     * @param m m
     * @return clean
     */
    public static String cleanStainTag(String m) {
        if (Strings.isEmpty(m)) {
            return m;
        }
        return m.replaceAll("<sb>", Const.EMPTY)
                .replaceAll("<sb 0>", Const.EMPTY)
                .replaceAll("<sb 2>", Const.EMPTY)
                .replaceAll("</sb>", Const.EMPTY)
                .replaceAll("<sr>", Const.EMPTY)
                .replaceAll("<sr 0>", Const.EMPTY)
                .replaceAll("<sr 2>", Const.EMPTY)
                .replaceAll("</sr>", Const.EMPTY);
    }

}
