<template>
  <div class="sftp-container">
    <div class="sftp-main-container" ref="sftpMainContainer">
      <!-- 左侧主体 -->
      <div id="sftp-left-fixed" v-if="leftFolderVisible" :style="{width: leftFolderVisible ? '16%' : '0px'}">
        <!-- 左侧地址输入框 -->
        <div class="sftp-left-top-redirector">
          <a-input ref="redirectPathInput" v-model="redirectPath" placeholder="路径" @pressEnter="redirectDirectory"/>
        </div>
        <!-- 左侧文件夹树 -->
        <div class="sftp-folder-left-fixed" @contextmenu.prevent>
          <!-- 文件树 -->
          <SftpFolderTree ref="folderTree"
                          v-if="leftFolderVisible && sessionToken"
                          :sessionToken="sessionToken"
                          @redirect="listFiles"/>
          <!-- 骨架屏 -->
          <a-skeleton v-if="leftFolderVisible && !sessionToken && loading" active :paragraph="{rows: 12}"/>
          <!-- 空数据 -->
          <a-empty v-if="leftFolderVisible && !sessionToken && !loading" :image="emptyImage" style="margin-top: 50px;"/>
        </div>
      </div>
      <!-- 右侧主体 -->
      <div class="sftp-body-container" :style="{width: leftFolderVisible ? '83.3%' : '100%'}">
        <!-- 头部 -->
        <div class="table-tools-bar">
          <div class="sftp-bar-left">
            <!-- home -->
            <div class="sftp-home">
              <a-icon type="home" theme="twoTone" :title="home" @click="listFiles(home)"/>
            </div>
            <!-- 路径 -->
            <div class="sftp-navigator-paths">
              <a-breadcrumb v-for="(pathItem,index) in pathAnalysis" :key="pathItem.path">
                <a-breadcrumb-item>
                  <div class="path-item">
                    <span v-if="index !== 0">/</span>
                    <a class="sftp-navigator-paths-item" @click="listFiles(pathItem.path)" :title="pathItem.path">{{ pathItem.name }}</a>
                  </div>
                </a-breadcrumb-item>
              </a-breadcrumb>
            </div>
          </div>
          <div class="sftp-tools-bar">
            <!-- 显示隐藏 -->
            <div class="sftp-tools-hide-switch">
              <span class="text normal-label mr4">显示隐藏文件</span>
              <a-switch :disabled="!sessionToken" v-model="showHideFile" @change="changeShowHideFile"/>
            </div>
            <!-- 工具栏 -->
            <div class="sftp-file-exec-buttons">
              <a-button-group>
                <!-- 删除 -->
                <a-button :disabled="!sessionToken || selectedRowKeys.length === 0"
                          @click="batchRemove" title="删除" icon="delete"/>
                <!-- 下载 -->
                <a-popconfirm :disabled="!sessionToken || selectedRowKeys.length === 0"
                              placement="bottomRight"
                              overlayClassName="download-popover-confirm"
                              :getPopupContainer="() => $refs.sftpMainContainer">
                  <!-- 内容 -->
                  <template #title>
                    <!-- 标题 -->
                    <div class="download-popover-title-wrapper">
                      <span>{{ `是否下载当前选中的 ${selectedRowKeys.length} 个文件?` }}</span>
                    </div>
                    <!-- 按钮 -->
                    <div class="download-prpover-buttons-wrapper">
                      <a-button class="download-popover-button span-blue" size="small" @click="triggerDownloadTrigger">取消</a-button>
                      <a-button class="download-popover-button" size="small" type="primary" @click="batchDownload">下载</a-button>
                      <a-button class="download-popover-button" size="small" type="primary" @click="batchPackageDownload">打包下载</a-button>
                    </div>
                  </template>
                  <!-- 触发器 -->
                  <a-button :disabled="!sessionToken || selectedRowKeys.length === 0"
                            title="下载"
                            icon="cloud-download"
                            @click="clickDownloadTrigger"/>
                </a-popconfirm>
                <!-- 复制路径 -->
                <a-button :disabled="!sessionToken" @click="$copy(path)" title="复制路径" icon="link"/>
                <!-- 刷新 -->
                <a-button :disabled="!sessionToken" @click="listFiles()" title="刷新" icon="reload"/>
                <!-- 创建 -->
                <a-button :disabled="!sessionToken" @click="openTouch" title="创建" icon="file-add"/>
                <!-- 文件上传 -->
                <a-popover v-model="uploadVisible" trigger="click" placement="bottomRight" overlayClassName="sftp-upload-list-popover">
                  <template #content>
                    <SftpUpload ref="upload"
                                @changeVisible="changeUploadVisible"
                                :currentPath="path"
                                :sessionToken="sessionToken"/>
                  </template>
                  <a-button :disabled="!sessionToken" title="上传" icon="cloud-upload"/>
                </a-popover>
                <!-- 传输列表 -->
                <a-popover trigger="click" placement="bottomRight" overlayClassName="sftp-transfer-list-popover">
                  <template #content>
                    <FileTransferList ref="transferList" :sessionToken="sessionToken"/>
                  </template>
                  <a-button :disabled="!sessionToken" title="传输列表" icon="unordered-list" @click="loadTransferList"/>
                </a-popover>
              </a-button-group>
            </div>
          </div>
        </div>
        <!-- 文件列表 -->
        <div class="table-main-container table-scroll-x-auto">
          <a-table :columns="columns"
                   :dataSource="files"
                   :pagination="pagination"
                   :rowSelection="rowSelection"
                   rowKey="path"
                   @change="changePage"
                   :scroll="{x: '100%'}"
                   :loading="loading"
                   :customRow="customClick"
                   size="small">
            <!-- 名称 -->
            <template v-slot:name="record">
              <span class="file-name-cols">
                <!-- 图标 -->
                <a-icon :type="record.attr.charAt(0) | formatFileType('icon')"
                        :title="record.attr.charAt(0) | formatFileType('label')"
                        class="file-name-cols-icon pointer"
                        @click="$copy(record.name, '已复制文件名称')"/>
                <!-- 名称 -->
                <span v-if="record.isDir"
                      class="span-blue pointer"
                      :title="record.name"
                      @click="listFiles(record.path)">{{ record.name }}</span>
                <span v-else :title="record.name">{{ record.name }}</span>
              </span>
            </template>
            <!-- 名称筛选图标 -->
            <template v-slot:nameFilterIcon="filtered">
              <a-icon type="search" :style="{ color: filtered ? '#108EE9' : undefined }"/>
            </template>
            <!-- 名称筛选输入框 -->
            <template v-slot:nameFilterDropdown="{setSelectedKeys, selectedKeys, confirm, clearFilters}">
              <div style="padding: 8px">
                <a-input v-ant-ref="c => (nameSearchInput = c)"
                         placeholder="名称"
                         :value="selectedKeys[0]"
                         style="width: 188px; margin-bottom: 8px; display: block;"
                         @change="e => setSelectedKeys(e.target.value ? [e.target.value] : [])"
                         @pressEnter="() => confirm()"
                         allowClear/>
                <a-button type="primary" icon="search" size="small"
                          style="width: 90px; margin-right: 8px"
                          @click="() => confirm()">
                  搜索
                </a-button>
                <a-button size="small" style="width: 90px" @click="() => resetFileName(selectedKeys, clearFilters)">
                  重置
                </a-button>
              </div>
            </template>
            <!-- 修改时间 -->
            <template v-slot:modifyTime="record">
              {{ record.modifyTime | formatDate }}
            </template>
            <!-- 操作 -->
            <template v-slot:action="record">
              <!-- 复制路径 -->
              <a @click="$copy(record.path, '已复制文件路径')" title="复制路径">
                <a-icon type="copy"/>
              </a>
              <a-divider type="vertical"/>
              <!-- 下载 -->
              <a-popconfirm v-if="record.isDir"
                            placement="bottomRight"
                            overlayClassName="download-popover-confirm"
                            :getPopupContainer="() => $refs.sftpMainContainer">
                <!-- 内容 -->
                <template #title>
                  <!-- 标题 -->
                  <div class="download-popover-title-wrapper">
                    <span>确定要下载当前文件夹?</span>
                  </div>
                  <!-- 按钮 -->
                  <div class="download-prpover-buttons-wrapper">
                    <a-button class="download-popover-button span-blue" size="small" @click="triggerDownloadTrigger">取消</a-button>
                    <a-button class="download-popover-button" size="small" type="primary" @click="download(record)">下载</a-button>
                    <a-button class="download-popover-button" size="small" type="primary" @click="packageDownload(record)">打包下载</a-button>
                  </div>
                </template>
                <!-- 触发器 -->
                <a title="下载" @click="clickDownloadTrigger">
                  <a-icon type="cloud-download"/>
                </a>
              </a-popconfirm>
              <a @click="download(record)" title="下载" v-else>
                <a-icon type="cloud-download"/>
              </a>
              <a-divider type="vertical"/>
              <!-- 删除 -->
              <a-button class="p0"
                        type="link"
                        :title="record.isSafe ? '删除' : '禁止删除'"
                        style="height: 22px"
                        :disabled="!record.isSafe"
                        @click="remove(record)">
                <a-icon type="delete"/>
              </a-button>
              <a-divider type="vertical"/>
              <!-- 移动 -->
              <a-button class="p0"
                        type="link"
                        :title="record.isSafe ? '移动' : '无法移动'"
                        style="height: 22px"
                        :disabled="!record.isSafe"
                        @click="openMove(record)">
                <a-icon type="block"/>
              </a-button>
              <a-divider type="vertical"/>
              <!-- 提权 -->
              <a @click="openChmod(record)" title="提权">
                <a-icon type="team"/>
              </a>
            </template>
          </a-table>
        </div>
      </div>
    </div>
    <!-- sftp事件 -->
    <div class="sftp-event-container">
      <!-- 创建文件模态框 -->
      <SftpTouchModal ref="touchModal" :sessionToken="sessionToken" @listFiles="listFiles()"/>
      <!-- 移动文件模态框 -->
      <SftpMoveModal ref="moveModal" :sessionToken="sessionToken" :files="files"/>
      <!-- 文件提权模态框 -->
      <SftpChmodModal ref="chmodModal" :sessionToken="sessionToken" :files="files"/>
      <!-- 右键菜单 -->
      <RightClickMenu ref="fileRightMenu"
                      :x="e => e.clientX + 2"
                      :y="e => e.y + 2"
                      @clickRight="clickFileRightMenuItem">
        <template #items v-if="curr">
          <a-menu-item key="copyName">
            <span class="right-menu-item"><a-icon type="copy"/>复制名称</span>
          </a-menu-item>
          <a-menu-item key="copyPath">
            <span class="right-menu-item"><a-icon type="snippets"/>复制路径</span>
          </a-menu-item>
          <a-menu-item key="downloadFile">
            <span class="right-menu-item"><a-icon type="cloud-download"/>下载</span>
          </a-menu-item>
          <a-menu-item key="deleteFile" v-if="curr.isSafe">
            <span class="right-menu-item"><a-icon type="delete"/>删除文件</span>
          </a-menu-item>
          <a-menu-item key="moveFile" v-if="curr.isSafe">
            <span class="right-menu-item"><a-icon type="block"/>移动</span>
          </a-menu-item>
          <a-menu-item key="chomdFile">
            <span class="right-menu-item"><a-icon type="team"/>提权</span>
          </a-menu-item>
        </template>
      </RightClickMenu>
      <!-- 右键菜单下载文件夹 -->
      <a-modal :title="null"
               :maskStyle="{opacity: 0.55, animation: 'none'}"
               :width="240"
               v-model="visibleRightMenuDownloadModal">
        <p style="margin: 1em 0 0 0;">确定要下载当前文件夹?</p>
        <template #footer>
          <a-button size="small" @click="() => visibleRightMenuDownloadModal = false">取消</a-button>
          <a-button size="small" @click="download(curr)" type="primary">下载</a-button>
          <a-button size="small" @click="packageDownload(curr)" type="primary">打包下载</a-button>
        </template>
      </a-modal>
    </div>
  </div>
</template>

<script>
import { Empty } from 'ant-design-vue'
import { getPathAnalysis } from '@/lib/utils'
import { enumValueOf, FILE_TYPE } from '@/lib/enum'
import { formatDate } from '@/lib/filters'
import SftpFolderTree from './SftpFolderTree'
import SftpTouchModal from './SftpTouchModal'
import SftpMoveModal from './SftpMoveModal'
import SftpChmodModal from './SftpChmodModal'
import FileTransferList from './FileTransferList'
import RightClickMenu from '@/components/common/RightClickMenu'
import SftpUpload from './SftpUpload'

/**
 * 表格列
 */
const fileListColumns = function() {
  return [
    {
      title: '名称',
      key: 'name',
      width: 330,
      ellipsis: true,
      scopedSlots: {
        customRender: 'name',
        filterDropdown: 'nameFilterDropdown',
        filterIcon: 'nameFilterIcon'
      },
      sorter: (a, b) => a.name.localeCompare(b.name),
      onFilter: (value, record) => record.name.toString().toLowerCase().includes(value.toLowerCase()),
      onFilterDropdownVisibleChange: visible => {
        if (visible) {
          setTimeout(() => {
            this.nameSearchInput.focus()
          })
        }
      }
    },
    {
      title: '大小',
      dataIndex: 'size',
      key: 'size',
      width: 100,
      sorter: (a, b) => a.sizeByte - b.sizeByte
    },
    {
      title: '属性',
      dataIndex: 'attr',
      key: 'attr',
      width: 100
    },
    {
      title: '修改时间',
      key: 'modifyTime',
      width: 150,
      align: 'center',
      sorter: (a, b) => a.modifyTime - b.modifyTime,
      scopedSlots: { customRender: 'modifyTime' }
    },
    {
      title: '操作',
      key: 'operation',
      fixed: 'right',
      align: 'center',
      width: 170,
      scopedSlots: { customRender: 'action' }
    }
  ]
}

/**
 * 右键菜单操作
 */
const fileRightMenuHandler = {
  copyName() {
    this.$copy(this.curr.name)
  },
  copyPath() {
    this.$copy(this.curr.path)
  },
  downloadFile() {
    if (this.curr.isDir) {
      this.visibleRightMenuDownloadModal = true
    } else {
      this.download(this.curr)
    }
  },
  deleteFile() {
    this.remove(this.curr)
  },
  moveFile() {
    this.openMove(this.curr)
  },
  chomdFile() {
    this.openChmod(this.curr)
  }
}

export default {
  name: 'MachineSftpMain',
  props: {
    machineId: Number,
    leftFolderDefaultVisible: Boolean,
    visibleRightMenu: Boolean
  },
  components: {
    SftpFolderTree,
    SftpTouchModal,
    SftpMoveModal,
    SftpChmodModal,
    FileTransferList,
    SftpUpload,
    RightClickMenu
  },
  data: function() {
    return {
      showHideFile: false,
      sessionToken: '',
      home: '',
      path: '',
      files: [],
      leftFolderVisible: this.leftFolderDefaultVisible,
      redirectPath: '',
      selectedRowKeys: [],
      selectedRows: [],
      columns: fileListColumns.call(this),
      loading: false,
      nameSearchInput: null,
      uploadVisible: false,
      emptyImage: Empty.PRESENTED_IMAGE_SIMPLE,
      curr: null,
      downloadTrigger: null,
      visibleRightMenuDownloadModal: false,
      customClick: record => ({
        on: {
          contextmenu: e => {
            if (this.visibleRightMenu) {
              e.preventDefault()
              if (e.button === 2) {
                this.curr = record
                this.$refs.fileRightMenu.openRightMenu(e)
              }
            }
          }
        }
      }),
      pagination: {
        current: 1,
        pageSize: 12,
        showTotal: function(total) {
          return `共 ${total} 条`
        }
      }
    }
  },
  computed: {
    pathAnalysis() {
      return getPathAnalysis(this.path)
    },
    rowSelection() {
      return {
        selectedRowKeys: this.selectedRowKeys,
        columnWidth: '30px',
        onChange: (keys, rows) => {
          this.selectedRowKeys = keys
          this.selectedRows = rows
        }
      }
    }
  },
  watch: {
    path(e) {
      this.redirectPath = e
    }
  },
  methods: {
    openSftp(machineName) {
      // 清除数据
      this.cleanData()
      // 初始化
      this.loading = true
      this.$api.sftpOpen({
        machineId: this.machineId
      }).then(({ data }) => {
        this.home = data.home
        this.path = data.path
        this.sessionToken = data.sessionToken
        this.files = data.files
        this.loading = false
        this.$emit('opened', this.machineId, {
          home: data.home,
          path: data.path,
          sessionToken: data.sessionToken
        })
      }).catch(() => {
        this.loading = false
        this.$message.error(`机器 ${machineName} sftp加载失败`)
      })
    },
    changeToken(session) {
      this.cleanData()
      this.home = session.home
      this.path = session.path
      this.sessionToken = session.sessionToken
      this.listFiles()
    },
    cleanData() {
      this.showHideFile = false
      this.sessionToken = ''
      this.home = ''
      this.path = ''
      this.files = []
      this.redirectPath = ''
      this.selectedRowKeys = []
      this.selectedRows = []
      this.loading = false
      this.nameSearchInput = null
      this.uploadVisible = false
      this.pagination.current = 1
      this.pagination.total = 0
      this.$refs.folderTree && this.$refs.folderTree.clean()
    },
    changeFolderVisible(visible) {
      this.leftFolderVisible = visible
    },
    changeShowHideFile(e) {
      this.showHideFile = e
      this.listFiles()
    },
    listFiles(path = this.path, toPage = 1) {
      this.loading = true
      this.$api.sftpList({
        sessionToken: this.sessionToken,
        all: this.showHideFile,
        path
      }).then(({ data }) => {
        this.path = data.path
        const pagination = { ...this.pagination }
        pagination.total = data.files.length
        pagination.current = toPage
        this.pagination = pagination
        this.selectedRowKeys = []
        this.selectedRows = []
        this.files = data.files
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    changePage(page) {
      this.selectedRowKeys = []
      this.selectedRows = []
      const pagination = { ...this.pagination }
      pagination.current = page.current
      this.pagination = pagination
    },
    redirectDirectory() {
      if (!this.redirectPath) {
        this.$message.error('路径不能为空')
        return
      }
      this.listFiles(this.redirectPath)
    },
    resetFileName(selectedKeys, clearFilters) {
      clearFilters()
      selectedKeys = []
    },
    download(record) {
      this.visibleRightMenuDownloadModal = false
      this.triggerDownloadTrigger()
      this.$api.sftpDownloadExec({
        paths: [record.path],
        sessionToken: this.sessionToken
      }).then(() => {
        this.$message.success('已添加至传输列表')
      })
    },
    packageDownload(record) {
      this.visibleRightMenuDownloadModal = false
      this.triggerDownloadTrigger()
      this.$api.sftpPackageDownloadExec({
        paths: [record.path],
        sessionToken: this.sessionToken
      }).then(() => {
        this.$message.success('已添加至传输列表')
      })
    },
    batchDownload() {
      this.visibleRightMenuDownloadModal = false
      this.triggerDownloadTrigger()
      this.$api.sftpDownloadExec({
        paths: this.selectedRowKeys,
        sessionToken: this.sessionToken
      }).then(() => {
        this.$message.success('已添加至传输列表')
        this.selectedRowKeys = []
        this.selectedRows = []
      })
    },
    batchPackageDownload() {
      this.visibleRightMenuDownloadModal = false
      this.triggerDownloadTrigger()
      this.$api.sftpPackageDownloadExec({
        paths: this.selectedRowKeys,
        sessionToken: this.sessionToken
      }).then(() => {
        this.$message.success('已添加至传输列表')
        this.selectedRowKeys = []
        this.selectedRows = []
      })
    },
    clickDownloadTrigger(event) {
      this.downloadTrigger = event.currentTarget
    },
    triggerDownloadTrigger() {
      this.downloadTrigger && this.downloadTrigger.click()
      this.downloadTrigger = null
    },
    remove(record) {
      this.$confirm({
        title: '确认删除',
        content: `是否删除 ${record.isDir ? '文件夹' : '文件'} ${record.path}?`,
        okType: 'danger',
        okText: '确认',
        cancelText: '取消',
        onOk: () => {
          this.$api.sftpRemove({
            sessionToken: this.sessionToken,
            paths: [record.path]
          }).then(() => {
            this.$message.success(`${record.isDir ? '文件夹' : '文件'} ${record.path} 删除成功`)
            for (let i = 0; i < this.files.length; i++) {
              if (this.files[i].path === record.path) {
                this.files.splice(i, 1)
              }
            }
            this.pagination.total = this.pagination.total - 1
          })
        }
      })
    },
    batchRemove() {
      for (const selectedRow of this.selectedRows) {
        if (!selectedRow.isSafe) {
          this.$message.warn('删除的文件包含不安全项')
          this.selectedRows = this.selectedRows.filter(s => s.isSafe)
          this.selectedRowKeys = this.selectedRows.map(s => s.path)
          return
        }
      }
      this.$confirm({
        title: '确认删除',
        content: `是否删除选中的 ${this.selectedRowKeys.length}个 文件?`,
        okType: 'danger',
        okText: '确认',
        cancelText: '取消',
        onOk: () => {
          this.$api.sftpRemove({
            sessionToken: this.sessionToken,
            paths: this.selectedRowKeys
          }).then(() => {
            this.$message.success('批量删除成功')
            for (const path of this.selectedRowKeys) {
              for (let i = 0; i < this.files.length; i++) {
                if (this.files[i].path === path) {
                  this.files.splice(i, 1)
                }
              }
            }
            this.pagination.total = this.pagination.total - this.selectedRowKeys.length
            this.selectedRowKeys = []
            this.selectedRows = []
          })
        }
      })
    },
    changeUploadVisible(visible) {
      this.uploadVisible = visible
    },
    openTouch() {
      this.$refs.touchModal.openTouch({
        homePath: this.home,
        currentPath: this.path
      })
    },
    openMove(record) {
      this.$refs.moveModal.openMove({
        parentPath: this.path,
        filePath: record.path
      })
    },
    openChmod(record) {
      this.$refs.chmodModal.openChmod({
        filePath: record.path,
        permission: record.permission
      })
    },
    loadTransferList() {
      setTimeout(() => {
        this.$refs.transferList.open()
      })
    },
    clickFileRightMenuItem(key) {
      fileRightMenuHandler[key].call(this)
    }
  },
  filters: {
    formatDate,
    formatFileType(type, f) {
      return enumValueOf(FILE_TYPE, type)[f]
    }
  }
}
</script>

<style lang="less" scoped>

.sftp-main-container {
  display: flex;
  justify-content: flex-end;

  .sftp-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .sftp-body-container {
    background-color: #FFF;
    padding: 0 4px 4px 4px;;
    margin-left: 4px;
    border-radius: 4px;

    .file-name-cols-icon {
      font-size: 16px;
      padding-right: 8px;
    }
  }

  /deep/ .download-popover-confirm .ant-popover-message {
    padding: 0 !important;
  }

  /deep/ .download-popover-confirm .ant-popover-buttons {
    display: none !important;
  }

  .download-popover-title-wrapper {
    display: block;
    padding-top: 4px;
    margin-bottom: 16px;
  }

  .download-popover-button {
    height: 24px;
    padding: 0 7px;
    font-size: 14px;
    border-radius: 4px;
    margin-left: 6px;
  }
}

#sftp-left-fixed {
  margin-right: 0.7%;
  padding: 12px 4px 12px 12px;
  background-color: #FFF;
  border-radius: 4px;

  .sftp-left-top-redirector {
    width: 95%;
  }

  .sftp-folder-left-fixed {
    overflow: auto;
    min-height: 25vh;
    max-height: calc(100vh - 148px);
    margin-top: 4px;
  }
}

.sftp-bar-left {
  display: flex;
  align-items: flex-start;

  .sftp-home {
    display: flex;
    align-items: center;
    font-size: 22px;
    cursor: pointer;
    margin: 0 8px 0 4px;
  }

  .sftp-navigator-paths {
    display: flex;
    flex-wrap: wrap;
  }

  .sftp-navigator-paths .sftp-navigator-paths-item {
    margin: 0 5px;
    color: #1C7ED6;
  }

  .path-item {
    display: flex;
  }
}

.sftp-tools-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .sftp-tools-hide-switch {
    margin-right: 12px;
    width: 140px;
    display: flex;
    align-items: center;
  }

  .text {
    color: #868E96;
    font-size: 13px;
  }
}

</style>
