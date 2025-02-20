package com.orion.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.orion.office.excel.writer.exporting.ExcelExport;
import com.orion.ops.consts.Const;
import com.orion.ops.consts.event.EventKeys;
import com.orion.ops.consts.event.EventParamsHolder;
import com.orion.ops.consts.export.ExportType;
import com.orion.ops.consts.system.SystemEnvAttr;
import com.orion.ops.dao.*;
import com.orion.ops.entity.domain.*;
import com.orion.ops.entity.dto.exporter.*;
import com.orion.ops.entity.request.DataExportRequest;
import com.orion.ops.service.api.DataExportService;
import com.orion.ops.utils.Currents;
import com.orion.ops.utils.PathBuilders;
import com.orion.servlet.web.Servlets;
import com.orion.utils.Strings;
import com.orion.utils.convert.Converts;
import com.orion.utils.io.FileWriters;
import com.orion.utils.io.Files1;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据导出服务
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2022/5/26 16:18
 */
@Service("dataExportService")
public class DataExportServiceImpl implements DataExportService {

    @Resource
    private MachineInfoDAO machineInfoDAO;

    @Resource
    private MachineProxyDAO machineProxyDAO;

    @Resource
    private MachineTerminalLogDAO machineTerminalLogDAO;

    @Resource
    private FileTailListDAO fileTailListDAO;

    @Resource
    private ApplicationProfileDAO applicationProfileDAO;

    @Resource
    private ApplicationInfoDAO applicationInfoDAO;

    @Resource
    private ApplicationVcsDAO applicationVcsDAO;

    @Resource
    private CommandTemplateDAO commandTemplateDAO;

    @Resource
    private WebSideMessageDAO webSideMessageDAO;

    @Resource
    private UserEventLogDAO userEventLogDAO;

    @Override
    public void exportMachine(DataExportRequest request, HttpServletResponse response) throws IOException {
        // 查询数据
        List<MachineInfoDO> machineList = machineInfoDAO.selectList(null);
        List<MachineInfoExportDTO> exportList = Converts.toList(machineList, MachineInfoExportDTO.class);
        if (!Const.ENABLE.equals(request.getExportPassword())) {
            exportList.forEach(s -> s.setEncryptPassword(null));
        }
        // 导出
        ExcelExport<MachineInfoExportDTO> exporter = new ExcelExport<>(MachineInfoExportDTO.class).init();
        exporter.addRows(exportList);
        // 写入
        this.writeWorkbook(request, response, exporter, ExportType.MACHINE);
    }

    @Override
    public void exportMachineProxy(DataExportRequest request, HttpServletResponse response) throws IOException {
        // 查询数据
        List<MachineProxyDO> proxyList = machineProxyDAO.selectList(null);
        List<MachineProxyExportDTO> exportList = Converts.toList(proxyList, MachineProxyExportDTO.class);
        if (!Const.ENABLE.equals(request.getExportPassword())) {
            exportList.forEach(s -> s.setEncryptPassword(null));
        }
        // 导出
        ExcelExport<MachineProxyExportDTO> exporter = new ExcelExport<>(MachineProxyExportDTO.class).init();
        exporter.addRows(exportList);
        // 写入
        this.writeWorkbook(request, response, exporter, ExportType.MACHINE_PROXY);
    }

    @Override
    public void exportMachineTerminalLog(DataExportRequest request, HttpServletResponse response) throws IOException {
        // 查询数据
        Long machineId = request.getMachineId();
        LambdaQueryWrapper<MachineTerminalLogDO> wrapper = new LambdaQueryWrapper<MachineTerminalLogDO>()
                .eq(Objects.nonNull(machineId), MachineTerminalLogDO::getMachineId, machineId)
                .orderByDesc(MachineTerminalLogDO::getCreateTime);
        List<MachineTerminalLogDO> terminalList = machineTerminalLogDAO.selectList(wrapper);
        List<MachineTerminalLogExportDTO> exportList = Converts.toList(terminalList, MachineTerminalLogExportDTO.class);
        // 导出
        ExcelExport<MachineTerminalLogExportDTO> exporter = new ExcelExport<>(MachineTerminalLogExportDTO.class).init();
        exporter.addRows(exportList);
        // 写入
        this.writeWorkbook(request, response, exporter, ExportType.MACHINE_TERMINAL_LOG);
        // 设置日志参数
        EventParamsHolder.addParam(EventKeys.MACHINE_ID, machineId);
    }

    @Override
    public void exportMachineTailFile(DataExportRequest request, HttpServletResponse response) throws IOException {
        // 查询数据
        Long queryMachineId = request.getMachineId();
        LambdaQueryWrapper<FileTailListDO> wrapper = new LambdaQueryWrapper<FileTailListDO>()
                .eq(Objects.nonNull(queryMachineId), FileTailListDO::getMachineId, queryMachineId);
        List<FileTailListDO> fileList = fileTailListDAO.selectList(wrapper);
        List<MachineTailFileExportDTO> exportList = Converts.toList(fileList, MachineTailFileExportDTO.class);
        // 机器名称
        List<Long> machineIdList = fileList.stream()
                .map(FileTailListDO::getMachineId)
                .collect(Collectors.toList());
        if (!machineIdList.isEmpty()) {
            List<MachineInfoDO> machineNameList = machineInfoDAO.selectNameByIdList(machineIdList);
            // 设置机器名称
            for (MachineTailFileExportDTO export : exportList) {
                Long machineId = export.getMachineId();
                if (machineId == null) {
                    continue;
                }
                machineNameList.stream()
                        .filter(s -> s.getId().equals(machineId))
                        .findFirst()
                        .ifPresent(s -> {
                            export.setMachineName(s.getMachineName());
                            export.setMachineTag(s.getMachineTag());
                        });
            }
        }
        // 导出
        ExcelExport<MachineTailFileExportDTO> exporter = new ExcelExport<>(MachineTailFileExportDTO.class).init();
        exporter.addRows(exportList);
        // 写入
        this.writeWorkbook(request, response, exporter, ExportType.MACHINE_TAIL_FILE);
        // 设置日志参数
        EventParamsHolder.addParam(EventKeys.MACHINE_ID, queryMachineId);
    }

    @Override
    public void exportAppProfile(DataExportRequest request, HttpServletResponse response) throws IOException {
        // 查询数据
        List<ApplicationProfileDO> profileList = applicationProfileDAO.selectList(null);
        List<ApplicationProfileExportDTO> exportList = Converts.toList(profileList, ApplicationProfileExportDTO.class);
        // 导出
        ExcelExport<ApplicationProfileExportDTO> exporter = new ExcelExport<>(ApplicationProfileExportDTO.class).init();
        exporter.addRows(exportList);
        // 写入
        this.writeWorkbook(request, response, exporter, ExportType.PROFILE);
    }

    @Override
    public void exportApplication(DataExportRequest request, HttpServletResponse response) throws IOException {
        // 查询数据
        List<ApplicationInfoDO> appList = applicationInfoDAO.selectList(null);
        List<ApplicationExportDTO> exportList = Converts.toList(appList, ApplicationExportDTO.class);
        // 仓库名称
        List<Long> vcsIdList = appList.stream()
                .map(ApplicationInfoDO::getVcsId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!vcsIdList.isEmpty()) {
            List<ApplicationVcsDO> vcsNameList = applicationVcsDAO.selectNameByIdList(vcsIdList);
            // 设置仓库名称
            for (ApplicationExportDTO export : exportList) {
                Long vcsId = export.getVcsId();
                if (vcsId == null) {
                    continue;
                }
                vcsNameList.stream()
                        .filter(s -> s.getId().equals(vcsId))
                        .findFirst()
                        .ifPresent(s -> export.setVcsName(s.getVcsName()));
            }
        }
        // 导出
        ExcelExport<ApplicationExportDTO> exporter = new ExcelExport<>(ApplicationExportDTO.class).init();
        exporter.addRows(exportList);
        // 写入
        this.writeWorkbook(request, response, exporter, ExportType.APPLICATION);
    }

    @Override
    public void exportAppVcs(DataExportRequest request, HttpServletResponse response) throws IOException {
        // 查询数据
        List<ApplicationVcsDO> vcsList = applicationVcsDAO.selectList(null);
        List<ApplicationVcsExportDTO> exportList = Converts.toList(vcsList, ApplicationVcsExportDTO.class);
        if (!Const.ENABLE.equals(request.getExportPassword())) {
            exportList.forEach(s -> s.setEncryptAuthValue(null));
        }
        // 导出
        ExcelExport<ApplicationVcsExportDTO> exporter = new ExcelExport<>(ApplicationVcsExportDTO.class).init();
        exporter.addRows(exportList);
        // 写入
        this.writeWorkbook(request, response, exporter, ExportType.VCS);
    }

    @Override
    public void exportCommandTemplate(DataExportRequest request, HttpServletResponse response) throws IOException {
        // 查询数据
        List<CommandTemplateDO> templateList = commandTemplateDAO.selectList(null);
        List<CommandTemplateExportDTO> exportList = Converts.toList(templateList, CommandTemplateExportDTO.class);
        // 导出
        ExcelExport<CommandTemplateExportDTO> exporter = new ExcelExport<>(CommandTemplateExportDTO.class).init();
        exporter.addRows(exportList);
        // 写入
        this.writeWorkbook(request, response, exporter, ExportType.COMMAND_TEMPLATE);
    }

    @Override
    public void exportWebSideMessage(DataExportRequest request, HttpServletResponse response) throws IOException {
        // 查询数据
        Integer classify = request.getClassify();
        Integer status = request.getStatus();
        LambdaQueryWrapper<WebSideMessageDO> wrapper = new LambdaQueryWrapper<WebSideMessageDO>()
                .eq(WebSideMessageDO::getToUserId, Currents.getUserId())
                .eq(Objects.nonNull(classify), WebSideMessageDO::getMessageClassify, classify)
                .eq(Objects.nonNull(status), WebSideMessageDO::getReadStatus, status)
                .orderByDesc(WebSideMessageDO::getCreateTime);
        List<WebSideMessageDO> messageList = webSideMessageDAO.selectList(wrapper);
        List<WebSideMessageExportDTO> exportList = Converts.toList(messageList, WebSideMessageExportDTO.class);
        // 导出
        ExcelExport<WebSideMessageExportDTO> exporter = new ExcelExport<>(WebSideMessageExportDTO.class).init();
        exporter.addRows(exportList);
        // 写入
        this.writeWorkbook(request, response, exporter, ExportType.WEB_SIDE_MESSAGE);
        // 设置日志参数
        EventParamsHolder.addParam(EventKeys.CLASSIFY, classify);
        EventParamsHolder.addParam(EventKeys.STATUS, status);
    }

    @Override
    public void exportEventLog(DataExportRequest request, HttpServletResponse response) throws IOException {
        // 查询数据
        Long userId = request.getUserId();
        Integer classify = request.getClassify();
        LambdaQueryWrapper<UserEventLogDO> wrapper = new LambdaQueryWrapper<UserEventLogDO>()
                .eq(UserEventLogDO::getExecResult, Const.ENABLE)
                .eq(Objects.nonNull(userId), UserEventLogDO::getUserId, userId)
                .eq(Objects.nonNull(classify), UserEventLogDO::getEventClassify, classify)
                .orderByDesc(UserEventLogDO::getCreateTime);
        List<UserEventLogDO> logList = userEventLogDAO.selectList(wrapper);
        List<EventLogExportDTO> exportList = Converts.toList(logList, EventLogExportDTO.class);
        // 导出
        ExcelExport<EventLogExportDTO> exporter = new ExcelExport<>(EventLogExportDTO.class).init();
        exporter.addRows(exportList);
        // 写入
        this.writeWorkbook(request, response, exporter, ExportType.WEB_SIDE_MESSAGE);
        // 设置日志参数
        EventParamsHolder.addParam(EventKeys.CLASSIFY, classify);
        EventParamsHolder.addParam(EventKeys.USER_ID, userId);
    }

    /**
     * 写入 workbook
     *
     * @param request  request
     * @param response response
     * @param exporter exporter
     * @param type     type
     * @throws IOException IOException
     */
    private void writeWorkbook(DataExportRequest request, HttpServletResponse response,
                               ExcelExport<?> exporter, ExportType type) throws IOException {
        // 设置 http 响应头
        Servlets.setDownloadHeader(response, type.getNameSupplier().get());
        // 写入 workbook 到 byte
        ByteArrayOutputStream store = new ByteArrayOutputStream();
        String password = request.getProtectPassword();
        if (!Strings.isBlank(password)) {
            exporter.write(store, password);
        } else {
            exporter.write(store);
        }
        // 设置 http 返回
        byte[] excelData = store.toByteArray();
        response.getOutputStream().write(excelData);
        // 保存至本地
        String exportPath = PathBuilders.getExportDataJsonPath(Currents.getUserId(), type.getType(), Strings.def(password));
        String path = Files1.getPath(SystemEnvAttr.LOG_PATH.getValue(), exportPath);
        FileWriters.write(path, excelData);
        // 设置日志参数
        EventParamsHolder.addParam(EventKeys.EXPORT_PASSWORD, request.getExportPassword());
        EventParamsHolder.addParam(EventKeys.PROTECT, request.getProtectPassword() != null);
        EventParamsHolder.addParam(EventKeys.COUNT, exporter.getRows());
    }


}
