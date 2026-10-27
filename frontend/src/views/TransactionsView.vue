<template>
  <div>
    <el-card shadow="never">
      <div class="filters">
        <el-date-picker
          v-model="filters.month"
          type="month"
          value-format="YYYY-MM"
          placeholder="选择月份（全部）"
          clearable
        />
        <el-select
          v-model="filters.type"
          placeholder="收支类型"
          clearable
          style="width: 140px"
          @change="onTypeChange"
        >
          <el-option label="支出" value="EXPENSE" />
          <el-option label="收入" value="INCOME" />
        </el-select>
        <el-select
          v-model="filters.categoryId"
          placeholder="分类（全部）"
          clearable
          style="width: 180px"
        >
          <el-option
            v-for="c in filteredCategories"
            :key="c.id"
            :label="`${c.icon || ''} ${c.name}`"
            :value="c.id"
          />
        </el-select>
        <div class="spacer"></div>
        <el-button type="primary" :icon="Plus" @click="openCreate">记一笔</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="occurredAt" label="日期" width="120" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 'EXPENSE' ? 'danger' : 'success'" size="small">
              {{ row.type === 'EXPENSE' ? '支出' : '收入' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" min-width="140">
          <template #default="{ row }">
            <span v-if="row.categoryName">{{ row.categoryIcon }} {{ row.categoryName }}</span>
            <span v-else class="muted">未分类</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="160" align="right">
          <template #default="{ row }">
            <span :class="row.type === 'EXPENSE' ? 'expense' : 'income'">
              {{ row.type === 'EXPENSE' ? '-' : '+' }} {{ money(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ muted: !row.note }">{{ row.note || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑账目' : '记一笔'"
      width="520px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type" @change="onTypeChange">
            <el-radio-button value="EXPENSE">支出</el-radio-button>
            <el-radio-button value="INCOME">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类（可选）" clearable style="width: 100%">
            <el-option
              v-for="c in filteredCategories"
              :key="c.id"
              :label="`${c.icon || ''} ${c.name}`"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0.01"
            :precision="2"
            :controls="false"
            placeholder="0.00"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="日期" prop="occurredAt">
          <el-date-picker
            v-model="form.occurredAt"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input v-model="form.note" maxlength="200" placeholder="可选" />
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
import { categoryApi, transactionApi } from '../api'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const categories = ref([])

const filters = reactive({
  month: '',
  type: '',
  categoryId: '',
})

const dialogVisible = ref(false)
const editing = ref(null)
const formRef = ref()
const form = reactive({
  categoryId: null,
  type: 'EXPENSE',
  amount: null,
  note: '',
  occurredAt: today(),
})

const rules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  occurredAt: [{ required: true, message: '请选择日期', trigger: 'change' }],
}

const filteredCategories = computed(() => {
  if (!filters.type) return categories.value
  return categories.value.filter((c) => c.type === filters.type)
})

function today() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
    d.getDate(),
  ).padStart(2, '0')}`
}

function money(value) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

async function loadCategories() {
  categories.value = await categoryApi.list()
}

async function load() {
  loading.value = true
  try {
    const params = {
      page: page.value - 1,
      size: size.value,
    }
    if (filters.month) params.month = filters.month
    if (filters.type) params.type = filters.type
    if (filters.categoryId) params.categoryId = filters.categoryId
    const data = await transactionApi.list(params)
    list.value = data.items
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function onTypeChange() {
  filters.categoryId = ''
  form.categoryId = null
}

function openCreate() {
  editing.value = null
  form.type = 'EXPENSE'
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = row
  Object.assign(form, {
    categoryId: row.categoryId,
    type: row.type,
    amount: Number(row.amount),
    note: row.note || '',
    occurredAt: row.occurredAt,
  })
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.clearValidate()
  Object.assign(form, { categoryId: null, type: 'EXPENSE', amount: null, note: '', occurredAt: today() })
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editing.value) {
      await transactionApi.update(editing.value.id, { ...form })
      ElMessage.success('已更新')
    } else {
      await transactionApi.create({ ...form })
      ElMessage.success('已记录')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除这笔账目吗？金额 ${money(row.amount)}`, '删除确认', {
    type: 'warning',
  })
  await transactionApi.remove(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(async () => {
  await loadCategories()
  await load()
})
</script>

<style scoped>
.filters {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.spacer {
  flex: 1;
}

.income {
  color: #67c23a;
  font-weight: 600;
}

.expense {
  color: #f56c6c;
  font-weight: 600;
}

.muted {
  color: #c0c4cc;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
