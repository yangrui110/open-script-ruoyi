<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="设备ID" prop="androidId">
        <el-input
          v-model="queryParams.androidId"
          placeholder="请输入设备ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="日志级别" prop="level">
        <el-select v-model="queryParams.level" placeholder="请选择日志级别" clearable>
          <el-option label="信息" value="INFO" />
          <el-option label="警告" value="WARN" />
          <el-option label="错误" value="ERROR" />
        </el-select>
      </el-form-item>
      <el-form-item label="标签类型" prop="tag">
        <el-input
          v-model="queryParams.tag"
          placeholder="请输入标签类型"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="日志信息" prop="message">
        <el-input
          v-model="queryParams.message"
          placeholder="请输入日志信息"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['script:log:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['script:log:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['script:log:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['script:log:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-delete"
          size="mini"
          @click="handleClearLogs"
          v-hasPermi="['script:log:remove']"
        >清空日志</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="设备ID" align="center" prop="androidId" width="200" :show-overflow-tooltip="true" />
      <el-table-column label="日志级别" align="center" prop="level" width="100">
        <template slot-scope="scope">
          <el-tag :type="getLevelTagType(scope.row.level)">
            {{ getLevelText(scope.row.level) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标签类型" align="center" prop="tag" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="日志信息" align="center" prop="message" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['script:log:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['script:log:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改脚本日志对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="设备ID" prop="androidId">
              <el-input v-model="form.androidId" placeholder="请输入设备ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="日志级别" prop="level">
              <el-select v-model="form.level" placeholder="请选择日志级别" style="width: 100%">
                <el-option label="信息" value="INFO" />
                <el-option label="警告" value="WARN" />
                <el-option label="错误" value="ERROR" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="标签类型" prop="tag">
              <el-input v-model="form.tag" placeholder="请输入标签类型" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="日志信息" prop="message">
              <el-input v-model="form.message" placeholder="请输入日志信息" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="额外信息" prop="extra">
              <el-input v-model="form.extra" type="textarea" :rows="4" placeholder="请输入额外信息" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="日志详情" :visible.sync="detailOpen" width="800px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备ID">{{ detailData.androidId }}</el-descriptions-item>
        <el-descriptions-item label="日志级别">
          <el-tag :type="getLevelTagType(detailData.level)">
            {{ getLevelText(detailData.level) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标签类型">{{ detailData.tag }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="日志信息" :span="2">{{ detailData.message }}</el-descriptions-item>
        <el-descriptions-item label="额外信息" :span="2">
          <div style="max-height: 200px; overflow-y: auto; white-space: pre-wrap;">{{ detailData.extra }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2" v-if="detailData.remark">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 清空日志对话框 -->
    <el-dialog title="清空日志" :visible.sync="clearOpen" width="500px" append-to-body>
      <el-form ref="clearForm" :model="clearForm" label-width="120px">
        <el-form-item label="清空方式">
          <el-radio-group v-model="clearType">
            <el-radio label="byDevice">按设备清空</el-radio>
            <el-radio label="byDate">按日期清空</el-radio>
            <el-radio label="all">清空全部</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设备ID" v-if="clearType === 'byDevice'">
          <el-input v-model="clearForm.androidId" placeholder="请输入设备ID" />
        </el-form-item>
        <el-form-item label="清空日期" v-if="clearType === 'byDate'">
          <el-date-picker
            v-model="clearForm.beforeDate"
            type="date"
            placeholder="清空此日期之前的日志"
            value-format="yyyy-MM-dd"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="confirmClearLogs">确 定</el-button>
        <el-button @click="clearOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLog, getLog, delLog, addLog, updateLog, clearLogsByAndroidId, clearLogsBeforeDate } from "@/api/script/log"

export default {
  name: "Log",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 脚本日志表格数据
      logList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示详情弹出层
      detailOpen: false,
      // 是否显示清空日志弹出层
      clearOpen: false,
      // 详情数据
      detailData: {},
      // 日期范围
      dateRange: [],
      // 清空类型
      clearType: 'all',
      // 清空表单
      clearForm: {
        androidId: '',
        beforeDate: ''
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        androidId: undefined,
        level: undefined,
        tag: undefined,
        message: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        androidId: [
          { required: true, message: "设备ID不能为空", trigger: "blur" }
        ],
        level: [
          { required: true, message: "日志级别不能为空", trigger: "change" }
        ],
        tag: [
          { required: true, message: "标签类型不能为空", trigger: "blur" }
        ],
        message: [
          { required: true, message: "日志信息不能为空", trigger: "blur" }
        ],
        extra: [
          { required: true, message: "额外信息不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询脚本日志列表 */
    getList() {
      this.loading = true
      this.queryParams.params = {}
      if (null != this.dateRange && '' != this.dateRange) {
        this.queryParams.params["beginTime"] = this.dateRange[0]
        this.queryParams.params["endTime"] = this.dateRange[1]
      }
      listLog(this.queryParams).then(response => {
        this.logList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        androidId: undefined,
        level: undefined,
        tag: undefined,
        message: undefined,
        extra: undefined,
        remark: undefined
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加脚本日志"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getLog(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改脚本日志"
      })
    },
    /** 查看详情 */
    handleView(row) {
      this.detailData = { ...row }
      this.detailOpen = true
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateLog(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addLog(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除脚本日志编号为"' + ids + '"的数据项？').then(function() {
        return delLog(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 清空日志按钮操作 */
    handleClearLogs() {
      this.clearType = 'all'
      this.clearForm = {
        androidId: '',
        beforeDate: ''
      }
      this.clearOpen = true
    },
    /** 确认清空日志 */
    confirmClearLogs() {
      if (this.clearType === 'byDevice') {
        if (!this.clearForm.androidId) {
          this.$modal.msgError("请输入设备ID")
          return
        }
        this.$modal.confirm('是否确认清空设备"' + this.clearForm.androidId + '"的所有日志？').then(() => {
          return clearLogsByAndroidId(this.clearForm.androidId)
        }).then(() => {
          this.$modal.msgSuccess("清空成功")
          this.clearOpen = false
          this.getList()
        }).catch(() => {})
      } else if (this.clearType === 'byDate') {
        if (!this.clearForm.beforeDate) {
          this.$modal.msgError("请选择清空日期")
          return
        }
        this.$modal.confirm('是否确认清空' + this.clearForm.beforeDate + '之前的所有日志？').then(() => {
          return clearLogsBeforeDate(this.clearForm.beforeDate)
        }).then(() => {
          this.$modal.msgSuccess("清空成功")
          this.clearOpen = false
          this.getList()
        }).catch(() => {})
      } else {
        this.$modal.confirm('是否确认清空所有日志？此操作不可恢复！').then(() => {
          return clearLogsBeforeDate('2099-12-31')
        }).then(() => {
          this.$modal.msgSuccess("清空成功")
          this.clearOpen = false
          this.getList()
        }).catch(() => {})
      }
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('script/log/export', {
        ...this.queryParams
      }, `log_${new Date().getTime()}.xlsx`)
    },
    /** 获取日志级别对应的标签类型 */
    getLevelTagType(level) {
      switch (level) {
        case 'INFO':
          return 'success'
        case 'WARN':
          return 'warning'
        case 'ERROR':
          return 'danger'
        default:
          return 'info'
      }
    },
    /** 获取日志级别对应的文本 */
    getLevelText(level) {
      switch (level) {
        case 'INFO':
          return '信息'
        case 'WARN':
          return '警告'
        case 'ERROR':
          return '错误'
        default:
          return level
      }
    }
  }
}
</script>