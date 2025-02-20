package com.orion.ops.entity.vo;

import com.orion.ops.entity.domain.ApplicationPipelineDO;
import com.orion.utils.convert.TypeStore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 应用流水线响应
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2022/4/2 10:30
 */
@Data
@ApiModel(value = "应用流水线响应")
public class ApplicationPipelineVO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "环境id")
    private Long profileId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "详情")
    private List<ApplicationPipelineDetailVO> details;

    static {
        TypeStore.STORE.register(ApplicationPipelineDO.class, ApplicationPipelineVO.class, p -> {
            ApplicationPipelineVO vo = new ApplicationPipelineVO();
            vo.setId(p.getId());
            vo.setName(p.getPipelineName());
            vo.setDescription(p.getDescription());
            vo.setProfileId(p.getProfileId());
            vo.setCreateTime(p.getCreateTime());
            vo.setUpdateTime(p.getUpdateTime());
            return vo;
        });
    }

}
