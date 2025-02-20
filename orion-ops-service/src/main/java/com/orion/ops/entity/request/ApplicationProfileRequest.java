package com.orion.ops.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 应用环境配置请求
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2021/7/2 18:03
 */
@Data
@ApiModel(value = "应用环境配置请求")
public class ApplicationProfileRequest {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "唯一标识")
    private String tag;

    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * @see com.orion.ops.consts.Const#ENABLE
     * @see com.orion.ops.consts.Const#DISABLE
     */
    @ApiModelProperty(value = "发布是否需要审核 1需要 2无需")
    private Integer releaseAudit;

}
