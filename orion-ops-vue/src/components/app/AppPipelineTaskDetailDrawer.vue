<template>
  <a-drawer title="执行详情"
            placement="right"
            :visible="visible"
            :maskStyle="{opacity: 0, animation: 'none'}"
            :width="430"
            @close="onClose">
    <!-- 加载中 -->
    <div v-if="loading">
      <a-skeleton active :paragraph="{rows: 12}"/>
    </div>
    <!-- 加载完成 -->
    <div v-else>
      <!-- 流水线信息 -->
      <a-descriptions size="middle">
        <a-descriptions-item label="流水线名称" :span="3">
          {{ detail.pipelineName }}
        </a-descriptions-item>
        <a-descriptions-item label="执行标题" :span="3">
          {{ detail.title }}
        </a-descriptions-item>
        <a-descriptions-item label="环境名称" :span="3">
          {{ detail.profileName }}
        </a-descriptions-item>
        <a-descriptions-item label="执行描述" :span="3" v-if="detail.description != null">
          {{ detail.description }}
        </a-descriptions-item>
        <a-descriptions-item label="执行状态" :span="3">
          <a-tag :color="detail.status | formatPipelineStatus('color')">
            {{ detail.status | formatPipelineStatus('label') }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="调度时间" :span="3" v-if="detail.timedExecTime !== null">
          {{ detail.timedExecTime | formatDate }}
        </a-descriptions-item>
        <a-descriptions-item label="创建用户" :span="3">
          {{ detail.createUserName }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="3" v-if="detail.createTime !== null">
          {{ detail.createTime | formatDate }}
        </a-descriptions-item>
        <a-descriptions-item label="审核用户" :span="3" v-if="detail.auditUserName !== null">
          {{ detail.auditUserName }}
        </a-descriptions-item>
        <a-descriptions-item label="审核时间" :span="3" v-if="detail.auditTime !== null">
          {{ detail.auditTime | formatDate }}
        </a-descriptions-item>
        <a-descriptions-item label="审核批注" :span="3" v-if="detail.auditReason !== null">
          {{ detail.auditReason }}
        </a-descriptions-item>
        <a-descriptions-item label="执行用户" :span="3" v-if=" detail.execUserName !== null">
          {{ detail.execUserName }}
        </a-descriptions-item>
        <a-descriptions-item label="开始时间" :span="3" v-if="detail.execStartTime !== null">
          {{ detail.execStartTime | formatDate }} ({{ detail.execStartTimeAgo }})
        </a-descriptions-item>
        <a-descriptions-item label="结束时间" :span="3" v-if="detail.execEndTime !== null">
          {{ detail.execEndTime | formatDate }} ({{ detail.execEndTimeAgo }})
        </a-descriptions-item>
        <a-descriptions-item label="持续时间" :span="3" v-if="detail.used !== null">
          {{ `${detail.keepTime} (${detail.used}ms)` }}
        </a-descriptions-item>
      </a-descriptions>
      <!-- 流水线操作 -->
      <a-divider>流水线操作</a-divider>
      <a-list size="small" :dataSource="detail.details">
        <template v-slot:renderItem="item">
          <a-list-item>
            <a-descriptions size="middle">
              <a-descriptions-item label="执行操作" :span="3">
                <span class="span-blue">
                  {{ item.stageType | formatStageType('label') }}
                </span>
                {{ item.appName }}
              </a-descriptions-item>
              <a-descriptions-item label="执行状态" :span="3">
                <a-tag :color="item.status | formatPipelineDetailStatus('color')">
                  {{ item.status | formatPipelineDetailStatus('label') }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="构建分支" :span="3" v-if="item.stageType === STAGE_TYPE.BUILD.value && item.config.branchName">
                <a-icon type="branches"/>
                {{ item.config.branchName }}
                <a-tooltip v-if="item.config.commitId">
                  <template #title>
                    {{ item.config.commitId }}
                  </template>
                  <span class="span-blue">
                    #{{ item.config.commitId.substring(0, 7) }}
                  </span>
                </a-tooltip>
              </a-descriptions-item>
              <a-descriptions-item label="发布版本" :span="3" v-if="item.stageType === STAGE_TYPE.RELEASE.value">
                <span class="span-blue">
                  {{ item.config.buildSeq ? `#${item.config.buildSeq}` : '最新版本' }}
                </span>
              </a-descriptions-item>
              <a-descriptions-item label="发布机器" :span="3" v-if="item.stageType === STAGE_TYPE.RELEASE.value">
                <span v-if="item.config.machineIdList && item.config.machineIdList.length" class="span-blue">
                  {{ item.config.machineList.map(s => s.name).join(', ') }}
                </span>
                <span v-else class="span-blue">全部机器</span>
              </a-descriptions-item>
              <a-descriptions-item label="开始时间" :span="3" v-if="item.startTime !== null">
                {{ item.startTime | formatDate }} ({{ item.startTimeAgo }})
              </a-descriptions-item>
              <a-descriptions-item label="结束时间" :span="3" v-if="item.endTime !== null">
                {{ item.endTime | formatDate }} ({{ item.endTimeAgo }})
              </a-descriptions-item>
              <a-descriptions-item label="持续时间" :span="3" v-if="item.keepTime !== null">
                {{ `${item.keepTime} (${item.used}ms)` }}
              </a-descriptions-item>
            </a-descriptions>
          </a-list-item>
        </template>
      </a-list>
    </div>
  </a-drawer>
</template>

<script>
import { formatDate } from '@/lib/filters'
import { enumValueOf, PIPELINE_DETAIL_STATUS, PIPELINE_STATUS, STAGE_TYPE } from '@/lib/enum'

export default {
  name: 'AppPipelineTaskDetailDrawer',
  data() {
    return {
      STAGE_TYPE,
      visible: false,
      loading: true,
      pollId: null,
      detail: {}
    }
  },
  methods: {
    open(id) {
      // 关闭轮询状态
      if (this.pollId) {
        clearInterval(this.pollId)
        this.pollId = null
      }
      this.detail = {}
      this.visible = true
      this.loading = true
      this.$api.getAppPipelineTaskDetail({
        id
      }).then(({ data }) => {
        this.loading = false
        this.detail = data
        // 轮询状态
        if (data.status === PIPELINE_STATUS.WAIT_AUDIT.value ||
          data.status === PIPELINE_STATUS.AUDIT_REJECT.value ||
          data.status === PIPELINE_STATUS.WAIT_RUNNABLE.value ||
          data.status === PIPELINE_STATUS.WAIT_SCHEDULE.value ||
          data.status === PIPELINE_STATUS.RUNNABLE.value) {
          this.pollId = setInterval(this.pollStatus, 5000)
        }
      }).catch(() => {
        this.loading = false
      })
    },
    onClose() {
      this.visible = false
      // 关闭轮询状态
      if (this.pollId) {
        clearInterval(this.pollId)
        this.pollId = null
      }
    },
    pollStatus() {
      if (!this.detail || !this.detail.status) {
        return
      }
      if (this.detail.status !== PIPELINE_STATUS.WAIT_AUDIT.value &&
        this.detail.status !== PIPELINE_STATUS.AUDIT_REJECT.value &&
        this.detail.status !== PIPELINE_STATUS.WAIT_RUNNABLE.value &&
        this.detail.status !== PIPELINE_STATUS.WAIT_SCHEDULE.value &&
        this.detail.status !== PIPELINE_STATUS.RUNNABLE.value) {
        clearInterval(this.pollId)
        this.pollId = null
        return
      }
      this.$api.geAppPipelineTaskStatus({
        id: this.detail.id
      }).then(({ data }) => {
        this.detail.status = data.status
        this.detail.used = data.used
        this.detail.keepTime = data.keepTime
        this.detail.execStartTime = data.startTime
        this.detail.execStartTimeAgo = data.startTimeAgo
        this.detail.execEndTime = data.endTime
        this.detail.execEndTimeAgo = data.endTimeAgo
        if (data.details && data.details.length) {
          for (const detail of data.details) {
            this.detail.details.filter(s => s.id === detail.id).forEach(s => {
              s.status = detail.status
              s.keepTime = detail.keepTime
              s.used = detail.used
              s.startTime = detail.startTime
              s.startTimeAgo = detail.startTimeAgo
              s.endTime = detail.endTime
              s.endTimeAgo = detail.endTimeAgo
            })
          }
        }
      })
    }
  },
  filters: {
    formatDate,
    formatPipelineStatus(status, f) {
      return enumValueOf(PIPELINE_STATUS, status)[f]
    },
    formatPipelineDetailStatus(status, f) {
      return enumValueOf(PIPELINE_DETAIL_STATUS, status)[f]
    },
    formatStageType(type, f) {
      return enumValueOf(STAGE_TYPE, type)[f]
    }
  }
}
</script>

<style lang="less" scoped>
</style>
