<template>
  <div>
    <el-card shadow="never">
      <div class="header-row">
        <el-radio-group v-model="activeType" @change="load">
          <el-radio-button value="EXPENSE">支出分类</el-radio-button>
          <el-radio-button value="INCOME">收入分类</el-radio-button>
        </el-radio-group>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
      </div>

      <el-table :data="visibleCategories" v-loading="loading" stripe>
        <el-table-column label="图标" width="80" align="center">
          <template #default="{ row }">
            <span class="icon">{{ row.icon || '🏷️' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="来源" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.builtin" size="small" type="info">系统预设</el-tag>
            <el-tag v-else size="small" type="success">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <template v-if="!row.builtin">
              <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
            </template>
            <span v-else class="muted">预设分类不可修改</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑分类' : '新增分类'"
      width="460px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button value="EXPENSE">支出</el-radio-button>
            <el-radio-button value="INCOME">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="如：咖啡、房租" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" maxlength="50" placeholder="输入 emoji，如 🍜（可选）" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="1" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { categoryApi } from '../api'

const loading = ref(false)
const saving = ref(false)
const activeType = ref('EXPENSE')
const categories = ref([])

const dialogVisible = ref(false)
const editing = ref(null)
const formRef = ref()
const form = reactive({
  name: '',
  type: 'EXPENSE',
  icon: '',
  sortOrder: 1,
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

const visibleCategories = computed(() =>
  categories.value.filter((c) => c.type === activeType.value),
)

async function load() {
  loading.value = true
  try {
    categories.value = await categoryApi.list()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  form.type = activeType.value
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = row
  Object.assign(form, {
    name: row.name,
    type: row.type,
    icon: row.icon || '',
    sortOrder: row.sortOrder ?? 1,
  })
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.clearValidate()
  Object.assign(form, { name: '', type: activeType.value, icon: '', sortOrder: 1 })
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { name: form.name, type: form.type, icon: form.icon || null, sortOrder: form.sortOrder }
    if (editing.value) {
      await categoryApi.update(editing.value.id, payload)
      ElMessage.success('已更新')
    } else {
      await categoryApi.create(payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？相关账目将变为未分类。`, '删除确认', {
    type: 'warning',
  })
  await categoryApi.remove(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.icon {
  font-size: 22px;
}

.muted {
  color: #c0c4cc;
  font-size: 13px;
}
</style>
