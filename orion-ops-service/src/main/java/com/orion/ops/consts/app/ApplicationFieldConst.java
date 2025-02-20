package com.orion.ops.consts.app;

import com.orion.ops.entity.dto.importer.ApplicationImportDTO;
import com.orion.ops.utils.Valid;
import com.orion.utils.Strings;

/**
 * 应用表字段常量
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2022/6/1 10:51
 */
public class ApplicationFieldConst {

    private ApplicationFieldConst() {
    }

    public static final int NAME_MAX_LEN = 32;

    public static final int TAG_MAX_LEN = 32;

    public static final int DESCRIPTION_MAX_LEN = 64;

    public static final String NAME_EMPTY_MESSAGE = "应用名称不能为空";

    public static final String NAME_LEN_MESSAGE = "应用名称长度不能大于 " + NAME_MAX_LEN + "位";

    public static final String TAG_EMPTY_MESSAGE = "唯一标识不能为空";

    public static final String TAG_LEN_MESSAGE = "唯一标识长度不能大于 " + TAG_MAX_LEN + "位";

    public static final String DESCRIPTION_LEN_MESSAGE = "描述长度不能大于 " + DESCRIPTION_MAX_LEN + "位";

    /**
     * 验证数据合法性
     *
     * @param o o
     */
    public static void validData(Object o) {
        if (o instanceof ApplicationImportDTO) {
            validImport((ApplicationImportDTO) o);
        }
    }

    /**
     * 验证导入数据
     *
     * @param row row
     */
    public static void validImport(ApplicationImportDTO row) {
        String name = row.getName();
        String tag = row.getTag();
        String description = row.getDescription();
        Valid.notBlank(name, NAME_EMPTY_MESSAGE);
        Valid.validLengthLte(name, NAME_MAX_LEN, NAME_LEN_MESSAGE);
        Valid.notBlank(tag, TAG_EMPTY_MESSAGE);
        Valid.validLengthLte(tag, TAG_MAX_LEN, TAG_LEN_MESSAGE);
        if (!Strings.isBlank(description)) {
            Valid.validLengthLte(description, DESCRIPTION_MAX_LEN, DESCRIPTION_LEN_MESSAGE);
        }
    }

}
