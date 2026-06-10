<template>
  <div class="upload-page">
    <div class="page-header">
      <h3>文件上传</h3>
    </div>

    <div class="upload-zone card"
      @dragover.prevent
      @drop.prevent="onDrop"
    >
      <el-icon size="48" color="#c0c4cc"><UploadFilled /></el-icon>
      <p class="zone-text">将文件拖拽到此处，或 <label class="select-link" for="file-input">点击选择</label></p>
      <p class="zone-hint">支持任意大小文件，分片并行上传</p>
      <input id="file-input" type="file" multiple style="display:none" @change="onFileSelect" />
    </div>

    <!-- 上传队列 -->
    <div class="file-list" v-if="fileList.length">
      <div class="section-title">上传队列</div>
      <div class="file-item card" v-for="item in fileList" :key="item.id">
        <div class="file-info">
          <el-icon size="20" class="file-icon"><Document /></el-icon>
          <div class="file-meta">
            <div class="file-name">{{ item.file.name }}</div>
            <div class="file-size">{{ formatSize(item.file.size) }}</div>
          </div>
        </div>

        <div class="file-status">
          <el-tag v-if="item.status === 'uploading'" type="warning" size="small">上传中 {{ item.progress }}%</el-tag>
          <el-tag v-else-if="item.status === 'success'" type="success" size="small">上传成功</el-tag>
          <el-tag v-else-if="item.status === 'error'" type="danger" size="small">上传失败</el-tag>
          <el-tag v-else-if="item.status === 'cancelled'" type="info" size="small">已取消</el-tag>
          <el-tag v-else-if="item.status === 'pending'" type="info" size="small">等待中</el-tag>
        </div>

        <div class="file-actions">
          <el-button
            v-if="item.status === 'uploading'"
            text type="danger" size="small"
            @click="cancelUpload(item)"
          >
            <el-icon><CloseBold /></el-icon>
            中断
          </el-button>
          <el-button
            v-if="item.status === 'success' && item.resultUrl"
            text type="primary" size="small"
            @click="copyUrl(item.resultUrl)"
          >
            <el-icon><CopyDocument /></el-icon>
            复制链接
          </el-button>
          <el-button
            v-if="item.status === 'error'"
            text type="primary" size="small"
            @click="retryUpload(item)"
          >
            <el-icon><RefreshRight /></el-icon>
            重试
          </el-button>
          <el-button
            v-if="item.status !== 'uploading'"
            text type="danger" size="small"
            @click="removeItem(item)"
          >
            <el-icon><Delete /></el-icon>
            移除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 已上传文件列表 -->
    <div class="uploaded-section">
      <div class="section-title">已上传文件</div>
      <div class="card">
        <el-table :data="uploadedFiles" v-loading="loadingList" stripe>
          <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
          <el-table-column label="大小" width="100" align="center">
            <template #default="{ row }">
              {{ formatSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="fileType" label="类型" width="140" show-overflow-tooltip />
          <el-table-column label="上传时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="action-btns">
                <el-button text type="primary" size="small" @click="copyUrl(row.fileUrl)">
                  <el-icon><CopyDocument /></el-icon>
                  复制链接
                </el-button>
                <el-button v-if="isImage(row.fileType)" text type="info" size="small" @click="openUrl(row.fileUrl)">
                  <el-icon><View /></el-icon>
                  预览
                </el-button>
                <el-button v-else text type="info" size="small" @click="downloadFile(row.fileUrl, row.fileName)">
                  <el-icon><Download /></el-icon>
                  下载
                </el-button>
                <el-popconfirm title="确定删除此文件吗？" @confirm="deleteUploaded(row.id)">
                  <template #reference>
                    <el-button text type="danger" size="small">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="uploadedPage"
          :page-size="uploadedPageSize"
          :total="uploadedTotal"
          layout="total, prev, pager, next"
          @current-change="loadUploadedFiles"
          class="pagination"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadApi } from '@/api/upload'

const CHUNK_SIZE = 5 * 1024 * 1024 // 5MB
const CONCURRENCY = 3

let idCounter = 0
const fileList = ref([])

// 已上传文件
const uploadedFiles = ref([])
const loadingList = ref(false)
const uploadedPage = ref(1)
const uploadedPageSize = ref(8)
const uploadedTotal = ref(0)

const generateIdentifier = (file) => {
  let hash = 0
  const str = `${file.name}-${file.size}-${file.lastModified}`
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash) + str.charCodeAt(i)
    hash |= 0
  }
  return Math.abs(hash).toString(36)
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const onFileSelect = (e) => {
  const files = Array.from(e.target.files)
  files.forEach(f => addFile(f))
  e.target.value = ''
}

const onDrop = (e) => {
  const files = Array.from(e.dataTransfer.files)
  files.forEach(f => addFile(f))
}

const addFile = (file) => {
  const item = {
    id: ++idCounter,
    file,
    identifier: generateIdentifier(file),
    status: 'pending',
    progress: 0,
    resultUrl: null,
    abortController: null
  }
  fileList.value.push(item)
  startUpload(item)
}

const startUpload = async (item) => {
  item.status = 'uploading'
  item.progress = 0
  item.abortController = new AbortController()

  const totalChunks = Math.ceil(item.file.size / CHUNK_SIZE)
  const identifier = item.identifier

  try {
    let uploadedChunks = []
    try {
      const res = await uploadApi.getUploadedChunks(identifier)
      if (res.code === 200) {
        uploadedChunks = res.data || []
      }
    } catch {
      // ignore
    }

    const pendingChunks = []
    for (let i = 0; i < totalChunks; i++) {
      if (!uploadedChunks.includes(i)) {
        pendingChunks.push(i)
      }
    }

    if (pendingChunks.length === 0) {
      item.progress = 100
    } else {
      let completedCount = 0

      await parallelUpload(pendingChunks, CONCURRENCY, async (chunkIndex) => {
        if (item.abortController.signal.aborted) {
          throw new Error('cancelled')
        }

        const start = chunkIndex * CHUNK_SIZE
        const end = Math.min(start + CHUNK_SIZE, item.file.size)
        const chunk = item.file.slice(start, end)

        const formData = new FormData()
        formData.append('file', chunk, item.file.name)
        formData.append('identifier', identifier)
        formData.append('chunkNumber', chunkIndex)

        await uploadApi.uploadChunk(formData, {
          signal: item.abortController.signal
        })

        completedCount++
        const uploadedBefore = uploadedChunks.length
        item.progress = Math.round(((uploadedBefore + completedCount) / totalChunks) * 100)
      })

      if (item.abortController.signal.aborted) {
        item.status = 'cancelled'
        return
      }
    }

    const mergeRes = await uploadApi.merge(identifier, item.file.name, totalChunks)
    if (mergeRes.code === 200) {
      item.status = 'success'
      item.progress = 100
      item.resultUrl = mergeRes.data.fileUrl
      ElMessage.success(`${item.file.name} 上传成功`)
      loadUploadedFiles()
    } else {
      item.status = 'error'
      ElMessage.error(mergeRes.message || '合并失败')
    }
  } catch (err) {
    if (err.message === 'cancelled' || item.abortController.signal.aborted) {
      item.status = 'cancelled'
    } else {
      item.status = 'error'
      ElMessage.error(`${item.file.name} 上传失败`)
    }
  }
}

const parallelUpload = (items, concurrency, fn) => {
  return new Promise((resolve, reject) => {
    let index = 0
    let running = 0
    let failed = false

    const runNext = () => {
      if (failed) return
      if (index >= items.length && running === 0) {
        resolve()
        return
      }

      while (running < concurrency && index < items.length) {
        const currentIndex = index++
        running++
        fn(items[currentIndex])
          .then(() => {
            running--
            runNext()
          })
          .catch((err) => {
            failed = true
            reject(err)
          })
      }
    }

    runNext()
  })
}

const cancelUpload = (item) => {
  if (item.abortController) {
    item.abortController.abort()
  }
  item.status = 'cancelled'
}

const retryUpload = (item) => {
  item.progress = 0
  item.resultUrl = null
  startUpload(item)
}

const removeItem = (item) => {
  const idx = fileList.value.findIndex(f => f.id === item.id)
  if (idx !== -1) fileList.value.splice(idx, 1)
}

const copyUrl = async (url) => {
  const fullUrl = window.location.origin + url
  try {
    await navigator.clipboard.writeText(fullUrl)
    ElMessage.success('链接已复制')
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = fullUrl
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('链接已复制')
  }
}

const openUrl = (url) => {
  window.open(url, '_blank')
}

const isImage = (fileType) => {
  return fileType && fileType.startsWith('image/')
}

const downloadFile = (url, fileName) => {
  const a = document.createElement('a')
  a.href = url
  a.download = fileName || 'download'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

const loadUploadedFiles = async () => {
  loadingList.value = true
  try {
    const res = await uploadApi.getAttachments(uploadedPage.value, uploadedPageSize.value)
    if (res.code === 200) {
      uploadedFiles.value = res.data.list
      uploadedTotal.value = res.data.total
    }
  } finally {
    loadingList.value = false
  }
}

const deleteUploaded = async (id) => {
  const res = await uploadApi.delete(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    loadUploadedFiles()
  }
}

onMounted(() => {
  loadUploadedFiles()
})
</script>

<style scoped lang="scss">
.upload-page {
  .page-header {
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #1a1a2e;
    }
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a2e;
    margin-bottom: 14px;
  }

  .upload-zone {
    border-radius: 12px;
    padding: 48px 24px;
    text-align: center;
    border: 2px dashed #dcdfe6;
    transition: border-color 0.2s;
    cursor: pointer;

    &:hover {
      border-color: #409eff;
    }

    .zone-text {
      margin: 16px 0 8px;
      font-size: 15px;
      color: #606266;

      .select-link {
        color: #409eff;
        cursor: pointer;

        &:hover {
          text-decoration: underline;
        }
      }
    }

    .zone-hint {
      font-size: 13px;
      color: #c0c4cc;
      margin: 0;
    }
  }

  .file-list {
    margin-top: 24px;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .file-item {
    border-radius: 12px;
    padding: 16px 20px;
    display: flex;
    align-items: center;
    gap: 16px;

    .file-info {
      display: flex;
      align-items: center;
      gap: 10px;
      min-width: 200px;

      .file-icon {
        color: #409eff;
        flex-shrink: 0;
      }

      .file-meta {
        .file-name {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
          max-width: 260px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .file-size {
          font-size: 12px;
          color: #c0c4cc;
          margin-top: 2px;
        }
      }
    }

    .file-status {
      min-width: 90px;
      text-align: center;
    }

    .file-actions {
      display: flex;
      align-items: center;
      gap: 2px;
      margin-left: auto;
    }
  }

  .uploaded-section {
    margin-top: 32px;

    .card {
      border-radius: 12px;
      padding: 20px;
    }

    .action-btns {
      display: flex;
      align-items: center;
    }

    .pagination {
      margin-top: 20px;
      justify-content: flex-end;
      display: flex;
    }
  }
}
</style>
