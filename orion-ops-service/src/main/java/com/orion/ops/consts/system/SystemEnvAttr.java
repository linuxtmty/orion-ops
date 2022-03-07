package com.orion.ops.consts.system;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统环境变量
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2022/2/15 16:58
 */
@Getter
public enum SystemEnvAttr {

    /**
     * 存放秘钥文件目录
     */
    KEY_PATH("秘钥目录", false),

    /**
     * 存放图片目录
     */
    PIC_PATH("图片目录", false),

    /**
     * 交换目录
     */
    SWAP_PATH("交换目录", false),

    /**
     * 日志目录
     */
    LOG_PATH("日志目录", false),

    /**
     * 临时文件目录
     */
    TEMP_PATH("临时目录", false),

    /**
     * 应用版本仓库目录
     */
    VCS_PATH("应用版本仓库目录", false),

    /**
     * 构建产物目录
     */
    DIST_PATH("构建产物目录", false),

    /**
     * 文件追踪模式 tracker或tail 默认tracker
     */
    TAIL_MODE("文件追踪模式 (tracker/tail)", false),

    /**
     * ip 白名单
     */
    WHITE_IP_LIST("ip 白名单", true),

    /**
     * ip 黑名单
     */
    BLACK_IP_LIST("ip 黑名单", true),

    /**
     * 是否启用 IP 过滤
     */
    ENABLE_IP_FILTER("是否启用IP过滤", true),

    /**
     * 是否启用 IP 白名单
     */
    ENABLE_WHITE_IP_LIST("是否启用IP白名单", true),

    /**
     * 文件清理阈值 (天)
     */
    FILE_CLEAN_THRESHOLD("文件清理阈值", true),

    /**
     * 是否启用自动清理
     */
    ENABLE_AUTO_CLEAN_FILE("是否启用自动清理", true),

    /**
     * 是否允许多端登陆
     */
    ALLOW_MULTIPLE_LOGIN("允许多端登陆", true),

    /**
     * 是否启用登陆失败锁定
     */
    LOGIN_FAILURE_LOCK("是否启用登陆失败锁定", true),

    /**
     * 是否启用登陆IP绑定
     */
    LOGIN_IP_BIND("是否启用登陆IP绑定", true),

    /**
     * 是否启用凭证自动续签
     */
    LOGIN_TOKEN_AUTO_RENEW("是否启用凭证自动续签", true),

    /**
     * 登陆凭证有效期 (时)
     */
    LOGIN_TOKEN_EXPIRE("登陆凭证有效期", true),

    /**
     * 登陆失败锁定阈值 (次)
     */
    LOGIN_FAILURE_LOCK_THRESHOLD("登陆失败锁定阈值", true),

    /**
     * 登陆自动续签阈值 (时)
     */
    LOGIN_TOKEN_AUTO_RENEW_THRESHOLD("登陆自动续签阈值", true),

    /**
     * 自动恢复启用的调度任务
     */
    RESUME_ENABLE_SCHEDULER_TASK("自动恢复启用的调度任务", true),

    /**
     * sftp上传文件最大阈值(MB)
     */
    SFTP_UPLOAD_THRESHOLD("sftp 上传文件最大阈值(MB)", true),

    ;

    /**
     * key
     */
    private final String key;

    /**
     * 描述
     */
    private final String description;

    private final boolean systemEnv;

    @Setter
    private String value;

    SystemEnvAttr(String description, boolean systemEnv) {
        this.description = description;
        this.systemEnv = systemEnv;
        this.key = this.name().toLowerCase();
    }

    public static List<String> getKeys() {
        return Arrays.stream(values())
                .map(SystemEnvAttr::getKey)
                .collect(Collectors.toList());
    }

    public static SystemEnvAttr of(String key) {
        if (key == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(a -> a.key.equals(key))
                .findFirst()
                .orElse(null);
    }

}
