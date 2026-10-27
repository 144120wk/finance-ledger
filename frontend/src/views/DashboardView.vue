<template>
  <div class="dashboard">
    <div class="toolbar">
      <el-date-picker
        v-model="month"
        type="month"
        value-format="YYYY-MM"
        placeholder="选择月份"
        :clearable="false"
        size="large"
      />
    </div>

    <el-row :gutter="16" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat">
            <div class="stat-label">本月收入</div>
            <div class="stat-value income">+ {{ money(summary.income) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat">
            <div class="stat-label">本月支出</div>
            <div class="stat-value expense">- {{ money(summary.expense) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat">
            <div class="stat-label">本月结余</div>
            <div class="stat-value" :class="summary.balance >= 0 ? 'income' : 'expense'">
              {{ money(summary.balance) }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>支出构成</template>
          <div ref="pieExpenseRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>收入构成</template>
          <div ref="pieIncomeRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-card">
      <template #header>近 6 个月收支趋势</template>
      <div ref="lineRef" class="chart chart-lg"></div>
    </el-card>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { statsApi } from '../api'

const month = ref(dayjsNowMonth())
const summary = reactive({ income: 0, expense: 0, balance: 0 })

const pieExpenseRef = ref()
const pieIncomeRef = ref()
const lineRef = ref()

let pieExpenseChart = null
let pieIncomeChart = null
let lineChart = null

function dayjsNowMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

function money(value) {
  const n = Number(value || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function initCharts() {
  pieExpenseChart = echarts.init(pieExpenseRef.value)
  pieIncomeChart = echarts.init(pieIncomeRef.value)
  lineChart = echarts.init(lineRef.value)
  window.addEventListener('resize', resizeCharts)
}

function resizeCharts() {
  pieExpenseChart?.resize()
  pieIncomeChart?.resize()
  lineChart?.resize()
}

function pieOption(title, items) {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9254de',
    '#36cfc9', '#ff85c0', '#597ef7', '#73d13d', '#ffc53d', '#ff7a45']
  return {
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll', icon: 'circle' },
    color: colors,
    series: [
      {
        name: title,
        type: 'pie',
        radius: ['38%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%' },
        data: items.length ? items : [{ name: '暂无数据', value: 1, itemStyle: { color: '#eee' } }],
      },
    ],
  }
}

function lineOption(trend) {
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出', '结余'] },
    grid: { left: 60, right: 30, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: trend.map((t) => t.month) },
    yAxis: { type: 'value' },
    series: [
      { name: '收入', type: 'line', smooth: true, data: trend.map((t) => Number(t.income)) },
      { name: '支出', type: 'line', smooth: true, data: trend.map((t) => Number(t.expense)) },
      { name: '结余', type: 'line', smooth: true, data: trend.map((t) => Number(t.balance)) },
    ],
  }
}

async function loadData() {
  const [summaryData, breakdown, trend] = await Promise.all([
    statsApi.summary(month.value),
    statsApi.breakdown(month.value),
    statsApi.trend({ months: 6, endMonth: month.value }),
  ])
  Object.assign(summary, summaryData)

  const expenseItems = breakdown
    .filter((b) => b.type === 'EXPENSE')
    .map((b) => ({ name: `${b.categoryIcon || ''} ${b.categoryName}`, value: Number(b.total) }))
  const incomeItems = breakdown
    .filter((b) => b.type === 'INCOME')
    .map((b) => ({ name: `${b.categoryIcon || ''} ${b.categoryName}`, value: Number(b.total) }))

  pieExpenseChart?.setOption(pieOption('支出构成', expenseItems))
  pieIncomeChart?.setOption(pieOption('收入构成', incomeItems))
  lineChart?.setOption(lineOption(trend))
}

onMounted(() => {
  initCharts()
  loadData()
})

watch(month, loadData)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  pieExpenseChart?.dispose()
  pieIncomeChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}

.stat-cards {
  margin-bottom: 16px;
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.income {
  color: #67c23a;
}

.expense {
  color: #f56c6c;
}

.chart-card {
  margin-bottom: 16px;
}

.chart {
  height: 320px;
}

.chart-lg {
  height: 360px;
}
</style>
