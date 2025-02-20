package com.orion.ops.consts;

/**
 * ANSI 高亮颜色转义码
 * <p>
 * \u001B = \x1b = 27 = esc
 * <p>
 * 基本8色   基本高对比色  xterm 256 色
 * 30 ~ 37  90 ~ 97     0 ~ 256
 * <p>
 * \033[0m    关闭所有属性
 * \033[1m    设置高亮度
 * \033[4m    下划线
 * \033[5m    闪烁
 * \033[7m    反显
 * \033[8m    消隐
 * \033[30m   至 \33[37m 设置前景色
 * \033[40m   至 \33[47m 设置背景色
 * \033[nA    光标上移n行
 * \033[nB    光标下移n行
 * \033[nC    光标右移n行
 * \033[nD    光标左移n行
 * \033[y;xH  设置光标位置
 * \033[2J    清屏
 * \033[K     清除从光标到行尾的内容
 * \033[s     保存光标位置
 * \033[u     恢复光标位置
 * \033[?25l  隐藏光标
 * \033[?25h  显示光标
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2022/4/20 23:16
 */
public class StainCode {

    private StainCode() {
    }

    /**
     * 结束
     * \x1b[0m
     */
    public static String SUFFIX = (char) 27 + "[0m";

    // -------------------- 颜色 --------------------

    /**
     * 黑色
     */
    public static final int BLACK = 30;

    /**
     * 红色
     */
    public static final int RED = 31;

    /**
     * 绿色
     */
    public static final int GREEN = 32;

    /**
     * 黄色
     */
    public static final int YELLOW = 33;

    /**
     * 蓝色
     */
    public static final int BLUE = 34;

    /**
     * 紫色
     */
    public static final int PURPLE = 35;

    /**
     * 青色
     */
    public static final int CYAN = 36;

    /**
     * 白色
     */
    public static final int WHITE = 37;

    // -------------------- 背景色 --------------------

    /**
     * 黑色 背景色
     */
    public static final int BG_BLACK = 40;

    /**
     * 红色 背景色
     */
    public static final int BG_RED = 41;

    /**
     * 绿色 背景色
     */
    public static final int BG_GREEN = 42;

    /**
     * 黄色 背景色
     */
    public static final int BG_YELLOW = 43;

    /**
     * 蓝色 背景色
     */
    public static final int BG_BLUE = 44;

    /**
     * 紫色 背景色
     */
    public static final int BG_PURPLE = 45;

    /**
     * 青色 背景色
     */
    public static final int BG_CYAN = 46;

    /**
     * 白色 背景色
     */
    public static final int BG_WHITE = 47;

    // -------------------- 亮色 --------------------

    /**
     * 亮黑色 (灰)
     */
    public static final int GLOSS_BLACK = 90;

    /**
     * 亮红色
     */
    public static final int GLOSS_RED = 91;

    /**
     * 亮绿色
     */
    public static final int GLOSS_GREEN = 92;

    /**
     * 亮黄色
     */
    public static final int GLOSS_YELLOW = 93;

    /**
     * 亮蓝色
     */
    public static final int GLOSS_BLUE = 94;

    /**
     * 亮紫色
     */
    public static final int GLOSS_PURPLE = 95;

    /**
     * 亮青色
     */
    public static final int GLOSS_CYAN = 96;

    /**
     * 白色
     */
    public static final int GLOSS_WHITE = 97;

    // -------------------- 亮背景色 --------------------

    /**
     * 亮黑色 (灰) 背景色
     */
    public static final int BG_GLOSS_BLACK = 100;

    /**
     * 亮红色 背景色
     */
    public static final int BG_GLOSS_RED = 101;

    /**
     * 亮绿色 背景色
     */
    public static final int BG_GLOSS_GREEN = 102;

    /**
     * 亮黄色 背景色
     */
    public static final int BG_GLOSS_YELLOW = 103;

    /**
     * 亮蓝色 背景色
     */
    public static final int BG_GLOSS_BLUE = 104;

    /**
     * 亮紫色 背景色
     */
    public static final int BG_GLOSS_PURPLE = 105;

    /**
     * 亮青色 背景色
     */
    public static final int BG_GLOSS_CYAN = 106;

    /**
     * 白色 背景色
     */
    public static final int BG_GLOSS_WHITE = 107;

    /**
     * 获取颜色前缀
     * .e.g \x1b[31m
     *
     * @param code code
     * @return 前缀
     */
    public static String prefix(int code) {
        return (char) 27 + "[" + code + "m";
    }

}
