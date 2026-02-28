<template>
  <div>
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card class="stat-card">
          <div class="stat-title">{{ item.title }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="status" label="状态" />
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" size="small">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      statCards: [
        { title: '总用户数', value: '1500' },
        { title: '今日订单', value: '320' },
        { title: '待处理', value: '45' },
        { title: '总收入', value: '¥99,999' },
      ],
      tableData: [
        { name: '公司A', date: '2024/01/01', status: '正常' },
        { name: '公司B', date: '2024/01/02', status: '异常' },
        { name: '公司C', date: '2024/01/03', status: '正常' },
      ]
    }
  }
}
</script>
