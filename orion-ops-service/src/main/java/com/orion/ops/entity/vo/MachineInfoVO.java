package com.orion.ops.entity.vo;

import com.orion.ops.entity.domain.MachineInfoDO;
import com.orion.utils.convert.TypeStore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 机器信息响应
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2021/4/14 22:10
 */
@Data
@ApiModel(value = "机器信息响应")
public class MachineInfoVO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "代理id")
    private Long proxyId;

    @ApiModelProperty(value = "代理主机")
    private String proxyHost;

    @ApiModelProperty(value = "代理端口")
    private Integer proxyPort;

    @ApiModelProperty(value = "代理类型")
    private Integer proxyType;

    @ApiModelProperty(value = "主机ip")
    private String host;

    @ApiModelProperty(value = "ssh端口")
    private Integer sshPort;

    @ApiModelProperty(value = "机器名称")
    private String name;

    @ApiModelProperty(value = "机器唯一标识")
    private String tag;

    @ApiModelProperty(value = "机器描述")
    private String description;

    @ApiModelProperty(value = "机器账号")
    private String username;

    /**
     * @see com.orion.ops.consts.machine.MachineAuthType
     */
    @ApiModelProperty(value = "机器认证方式 1: 账号认证 2: key认证")
    private Integer authType;

    /**
     * @see com.orion.ops.consts.Const#ENABLE
     * @see com.orion.ops.consts.Const#DISABLE
     */
    @ApiModelProperty(value = "机器状态 1有效 2无效")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    static {
        TypeStore.STORE.register(MachineInfoDO.class, MachineInfoVO.class, p -> {
            MachineInfoVO vo = new MachineInfoVO();
            vo.setId(p.getId());
            vo.setProxyId(p.getProxyId());
            vo.setHost(p.getMachineHost());
            vo.setSshPort(p.getSshPort());
            vo.setName(p.getMachineName());
            vo.setTag(p.getMachineTag());
            vo.setDescription(p.getDescription());
            vo.setUsername(p.getUsername());
            vo.setAuthType(p.getAuthType());
            vo.setStatus(p.getMachineStatus());
            vo.setCreateTime(p.getCreateTime());
            vo.setUpdateTime(p.getUpdateTime());
            return vo;
        });
    }

}
