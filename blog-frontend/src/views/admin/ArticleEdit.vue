<template>
  <div class="article-edit-page">
    <div class="page-header">
      <h3>{{ isEdit ? '编辑文章' : '新建文章' }}</h3>
      <div class="header-actions">
        <el-button @click="$router.back()">
          <el-icon><Back /></el-icon>
          取消
        </el-button>
        <el-button @click="saveDraft" :loading="saving">
          <el-icon><Document /></el-icon>
          保存草稿
        </el-button>
        <el-button type="primary" @click="publish" :loading="saving">
          <el-icon><Promotion /></el-icon>
          发布
        </el-button>
      </div>
    </div>

    <div class="edit-content">
      <div class="main-editor">
        <el-input
          v-model="form.title"
          placeholder="请输入文章标题"
          class="title-input"
        />
        <div class="editor-wrapper">
          <RichEditor v-model="form.content" />
        </div>
      </div>

      <div class="sidebar-settings">
        <div class="card">
          <div class="card-title">文章设置</div>
          <el-form label-position="top">
            <el-form-item label="摘要">
              <el-input
                v-model="form.summary"
                type="textarea"
                :rows="3"
                placeholder="文章摘要"
              />
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%;">
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="标签">
              <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width: 100%;">
                <el-option
                  v-for="tag in tags"
                  :key="tag.id"
                  :label="tag.name"
                  :value="tag.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="私密">
              <el-switch v-model="form.isPrivate" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { ElMessage } from 'element-plus'
import { articleApi } from '@/api/article'
import { categoryApi } from '@/api/category'
import { tagApi } from '@/api/tag'
import RichEditor from '@/components/RichEditor.vue'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const categories = ref([])
const tags = ref([])
const skipLeaveAutoSave = ref(false)
const leaveAutoSaveDone = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  title: '',
  summary: '',
  content: '',
  categoryId: null,
  tagIds: [],
  status: 0,
  isPrivate: 0
})

const loadCategories = async () => {
  const res = await categoryApi.getAll()
  if (res.code === 200) {
    categories.value = res.data
  }
}

const loadTags = async () => {
  const res = await tagApi.getAll()
  if (res.code === 200) {
    tags.value = res.data
  }
}

const loadArticle = async () => {
  if (!route.params.id) return
  const res = await articleApi.getForEdit(route.params.id)
  if (res.code === 200) {
    form.id = res.data.id
    form.title = res.data.title
    form.summary = res.data.summary
    form.content = res.data.content
    form.categoryId = res.data.categoryId
    form.tagIds = res.data.tags?.map(t => t.id) || []
    form.isPrivate = res.data.isPrivate
  }
}

const saveDraft = async () => {
  form.status = 0
  await saveArticle()
}

const publish = async () => {
  form.status = 1
  await saveArticle()
}

const saveArticle = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  saving.value = true
  try {
    let res
    if (isEdit.value) {
      res = await articleApi.update(form)
    } else {
      res = await articleApi.create(form)
    }

    if (res.code === 200) {
      skipLeaveAutoSave.value = true
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      router.push('/admin/articles')
    }
  } finally {
    saving.value = false
  }
}

const shouldLeaveAutoSaveDraft = () => {
  if (isEdit.value) return false
  if (skipLeaveAutoSave.value) return false
  if (leaveAutoSaveDone.value) return false
  const hasTitle = !!form.title?.trim()
  const hasContent = !!form.content?.trim()
  return hasTitle || hasContent
}

const leaveAutoSaveDraft = async () => {
  if (!shouldLeaveAutoSaveDraft()) return
  leaveAutoSaveDone.value = true

  const title = form.title?.trim()
  const content = form.content?.trim()
  const draftTitle = title || '未命名草稿'

  try {
    const res = await articleApi.create({
      ...form,
      title: draftTitle,
      content: content || '',
      status: 0
    })
    if (res.code === 200) {
      ElMessage.success('已自动保存为草稿')
    }
  } catch (e) {
    ElMessage.warning('自动保存草稿失败')
  }
}

onBeforeRouteLeave(async (to, from, next) => {
  try {
    await leaveAutoSaveDraft()
  } finally {
    next()
  }
})

onMounted(() => {
  loadCategories()
  loadTags()
  loadArticle()
})
</script>

<style scoped lang="scss">
.article-edit-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #1a1a2e;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .edit-content {
    display: flex;
    gap: 20px;

    .main-editor {
      flex: 1;

      .title-input {
        margin-bottom: 16px;

        :deep(.el-input__inner) {
          font-size: 18px;
          font-weight: 600;
          padding: 14px 16px;
          border-radius: 10px;
        }
      }

      .editor-wrapper {
        background: #fff;
        border-radius: 12px;
        overflow: hidden;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
      }
    }

    .sidebar-settings {
      width: 300px;
      flex-shrink: 0;

      .card {
        border-radius: 12px;
        padding: 24px;
        position: sticky;
        top: 24px;

        .card-title {
          font-size: 16px;
          font-weight: 600;
          color: #1a1a2e;
          margin-bottom: 20px;
          padding-bottom: 14px;
          border-bottom: 1px solid #f0f0f0;
        }
      }
    }
  }
}
</style>
