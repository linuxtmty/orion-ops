package com.orion.ops.entity.request;

import com.orion.lang.wrapper.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 历史值快照请求
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2021/6/9 19:03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "历史值快照请求")
public class HistoryValueRequest extends PageRequest {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "值id")
    private Long valueId;

    /**
     * @see com.orion.ops.consts.history.HistoryValueType
     */
    @ApiModelProperty(value = "值类型")
    private Integer valueType;

}
