<template>
  <div class="rich-editor" :class="{ 'fullscreen': isFullscreen }">
    <Toolbar
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      mode="default"
      class="toolbar"

    />
    <Editor
      v-model="valueHtml"
      :defaultConfig="editorConfig"
      mode="default"
      class="editor"
      @onCreated="handleCreated"
      @onChange="handleChange"
      style="height: 600px; overflow-y: hidden; resize: vertical;"

    />
  </div>
</template>

<script setup>
import { ref, shallowRef, onBeforeUnmount, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { fileApi } from '@/api/file'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = shallowRef()
const valueHtml = ref('')
const isFullscreen = ref(false)

const toolbarConfig = {
  excludeKeys: [
    'group-video',
    'fullScreen'
  ]
}

const editorConfig = {
  placeholder: '请输入文章内容...',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        try {
          const res = await fileApi.upload(file)
          if (res.code === 200) {
            insertFn(res.data.fileUrl, res.data.fileName, res.data.fileUrl)
          } else {
            ElMessage.error('上传失败：' + (res.message || '未知错误'))
          }
        } catch (e) {
          console.error('上传失败', e)
          ElMessage.error('上传失败，请检查网络连接')
        }
      }
    }
  }
}

watch(() => props.modelValue, (val) => {
  if (val !== valueHtml.value) {
    valueHtml.value = val
  }
})

const handleCreated = (editor) => {
  editorRef.value = editor
  if (props.modelValue) {
    valueHtml.value = props.modelValue
  }
}

const handleChange = (editor) => {
  emit('update:modelValue', valueHtml.value)
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
</script>

<style scoped lang="scss">
.rich-editor {
  border: 1px solid #ccc;
  border-radius: 4px;
  overflow: hidden;
  
  .toolbar {
    border-bottom: 1px solid #ccc;
  }
  
  .editor {
    height: 400px;
    overflow-y: auto;
  }
  
  &.fullscreen {
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
    z-index: 9999 !important;
    background: #fff;
    
    .editor {
      height: calc(100vh - 40px) !important;
    }
  }
}
</style>
