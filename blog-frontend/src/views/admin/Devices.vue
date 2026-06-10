<template>
  <div class="devices-page">
    <div class="page-header">
      <h2>登录设备管理</h2>
      <p class="tip">查看当前账号的登录设备，可踢出不认识的设备</p>
    </div>

    <el-table :data="devices" stripe style="width: 100%">
      <el-table-column label="设备" min-width="200">
        <template #default="{ row }">
          <div class="device-info">
            <el-icon :size="28" class="device-icon">
              <Monitor v-if="row.deviceType === 'PC'" />
              <Iphone v-else-if="row.deviceType === 'Mobile'" />
              <Cellphone v-else />
            </el-icon>
            <div>
              <div class="device-name">
                {{ row.os }} · {{ row.browser }}
                <el-tag v-if="row.current" type="success" size="small" effect="dark" style="margin-left: 8px">
                  当前设备
                </el-tag>
                <el-tag v-else-if="row.status === 1 && isRecentlyActive(row.lastActiveTime)" type="success" size="small" effect="plain" style="margin-left: 8px">
                  在线
                </el-tag>
                <el-tag v-else-if="row.status === 1" type="warning" size="small" effect="plain" style="margin-left: 8px">
                  活跃中
                </el-tag>
                <el-tag v-else type="info" size="small" effect="plain" style="margin-left: 8px">
                  已下线
                </el-tag>
              </div>
              <div class="device-type">{{ row.deviceType }}</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="IP 地址" prop="ip" width="160" />

      <el-table-column label="登录时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="最后活跃" width="180">
        <template #default="{ row }">
          <span v-if="row.status === 1">{{ formatRelativeTime(row.lastActiveTime) }}</span>
          <span v-else class="offline-label">{{ formatDate(row.lastActiveTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="action-btns">
            <el-popconfirm
              v-if="row.status === 1 && !row.current"
              title="确定踢出该设备？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleLogout(row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small" text>踢出</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm
              v-if="!row.current"
              title="确定删除该设备记录？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button type="warning" size="small" text>删除</el-button>
              </template>
            </el-popconfirm>
            <span v-if="row.current" class="current-label">当前设备</span>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Monitor, Iphone, Cellphone } from '@element-plus/icons-vue'
import { deviceApi } from '@/api/device'

const devices = ref([])
let refreshTimer = null

const loadDevices = async () => {
  const res = await deviceApi.getDevices()
  if (res.code === 200) {
    devices.value = res.data
  }
}

const handleLogout = async (id) => {
  const res = await deviceApi.logoutDevice(id)
  if (res.code === 200) {
    ElMessage.success('已踢出该设备')
    loadDevices()
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

const handleDelete = async (id) => {
  const res = await deviceApi.deleteDevice(id)
  if (res.code === 200) {
    ElMessage.success('已删除设备记录')
    loadDevices()
  } else {
    ElMessage.error(res.message || '删除失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const isRecentlyActive = (dateStr) => {
  if (!dateStr) return false
  return Date.now() - new Date(dateStr).getTime() < 5 * 60 * 1000
}

const formatRelativeTime = (dateStr) => {
  if (!dateStr) return '-'
  const now = Date.now()
  const time = new Date(dateStr).getTime()
  const diff = now - time
  const seconds = Math.floor(diff / 1000)
  if (seconds < 60) return '刚刚活跃'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}分钟前活跃`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前活跃`
  const days = Math.floor(hours / 24)
  return `${days}天前活跃`
}

onMounted(() => {
  loadDevices()
  // 每 30 秒自动刷新设备状态
  refreshTimer = setInterval(loadDevices, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped lang="scss">
.devices-page {
  .page-header {
    margin-bottom: 24px;

    h2 {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 600;
    }

    .tip {
      margin: 0;
      color: #999;
      font-size: 13px;
    }
  }

  .device-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .device-icon {
      color: #666;
    }

    .device-name {
      font-size: 14px;
      font-weight: 500;
      display: flex;
      align-items: center;
    }

    .device-type {
      font-size: 12px;
      color: #999;
      margin-top: 2px;
    }
  }

  .current-label,
  .offline-label {
    font-size: 13px;
    color: #ccc;
  }

  .action-btns {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}
</style>
