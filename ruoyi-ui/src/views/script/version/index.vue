<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="游戏" prop="gameId">
        <el-select v-model="queryParams.gameId" placeholder="请选择游戏" clearable>
          <el-option
            v-for="game in gameOptions"
            :key="game.id"
            :label="game.title"
            :value="game.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="文件类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择文件类型" clearable>
          <el-option label="单js文件" value="0" />
          <el-option label="zip文件" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="版本号" prop="version">
        <el-input
          v-model="queryParams.version"
          placeholder="请输入版本号"
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
          v-hasPermi="['script:version:add']"
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
          v-hasPermi="['script:version:edit']"
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
          v-hasPermi="['script:version:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['script:version:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="versionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="游戏名称" align="center" prop="gameTitle" />
      <el-table-column label="文件地址" align="center" prop="fileUrl" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-link :href="getFileUrl(scope.row.fileUrl)" :underline="false" target="_blank" type="primary">
            {{ getFileName(scope.row.fileUrl) }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="文件类型" align="center" prop="type">
        <template slot-scope="scope">
          <el-tag :type="scope.row.type === 0 ? 'success' : 'info'">
            {{ scope.row.type === 0 ? '单js文件' : 'zip文件' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="版本号" align="center" prop="version">
        <template slot-scope="scope">
          <el-tag type="primary">v{{ scope.row.version }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['script:version:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['script:version:remove']"
          >删除</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleDownload(scope.row)"
          >下载</el-button>
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

    <!-- 添加或修改脚本版本对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="游戏" prop="gameId">
              <el-select v-model="form.gameId" placeholder="请选择游戏" style="width: 100%">
                <el-option
                  v-for="game in gameOptions"
                  :key="game.id"
                  :label="game.title"
                  :value="game.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="脚本文件" prop="fileUrl">
              <file-upload 
                v-model="form.fileUrl"
                :limit="1"
                :fileType="getAllowedFileTypes()"
                :fileSize="50"
                :drag="false"
                :isShowTip="true"
              />
              <div class="el-upload__tip" style="margin-top: 5px; color: #909399; font-size: 12px;">
                {{ getUploadTip() }}
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="文件类型" prop="type">
              <el-radio-group v-model="form.type" @change="handleFileTypeChange">
                <el-radio :label="0">单js文件</el-radio>
                <el-radio :label="1">zip文件</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="form.version" placeholder="保存时自动生成" :disabled="true" />
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
  </div>
</template>

<script>
import { listVersion, getVersion, delVersion, addVersion, updateVersion } from "@/api/script/version"
import { getGameOptions } from "@/api/script/game"
import FileUpload from "@/components/FileUpload"

export default {
  name: "Version",
  components: {
    FileUpload
  },
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
      // 脚本版本表格数据
      versionList: [],
      // 游戏数据
      gameOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        gameId: undefined,
        type: undefined,
        version: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        gameId: [
          { required: true, message: "游戏不能为空", trigger: "change" }
        ],
        fileUrl: [
          { required: true, message: "脚本文件不能为空", trigger: "change" }
        ],
        type: [
          { required: true, message: "文件类型不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getGameOptionsList()
  },
  methods: {
    /** 查询脚本版本列表 */
    getList() {
      this.loading = true
      this.queryParams.params = {}
      if (null != this.dateRange && '' != this.dateRange) {
        this.queryParams.params["beginTime"] = this.dateRange[0]
        this.queryParams.params["endTime"] = this.dateRange[1]
      }
      listVersion(this.queryParams).then(response => {
        this.versionList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询游戏选项 */
    getGameOptionsList() {
      getGameOptions().then(response => {
        this.gameOptions = response.data || []
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
        gameId: undefined,
        fileUrl: undefined,
        type: 0,
        version: undefined,
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
      this.title = "添加脚本版本"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getVersion(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改脚本版本"
      })
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateVersion(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addVersion(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除脚本版本编号为"' + ids + '"的数据项？').then(function() {
        return delVersion(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 下载按钮操作 */
    handleDownload(row) {
      if (row.fileUrl) {
        // 如果是相对路径，则拼接基础URL
        let downloadUrl = row.fileUrl
        if (!downloadUrl.startsWith('http')) {
          downloadUrl = process.env.VUE_APP_BASE_API + row.fileUrl
        }
        window.open(downloadUrl, '_blank')
      } else {
        this.$modal.msgError("文件地址为空")
      }
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('script/version/export', {
        ...this.queryParams
      }, `version_${new Date().getTime()}.xlsx`)
    },
    /** 获取完整文件URL */
    getFileUrl(fileUrl) {
      if (!fileUrl) return '#'
      if (fileUrl.startsWith('http')) {
        return fileUrl
      }
      return process.env.VUE_APP_BASE_API + fileUrl
    },
    /** 获取文件名 */
    getFileName(fileUrl) {
      if (!fileUrl) return '未知文件'
      // 如果是完整路径，提取文件名
      const fileName = fileUrl.split('/').pop()
      // 如果文件名包含时间戳前缀，去掉前缀
      const parts = fileName.split('_')
      if (parts.length > 1 && /^\d+$/.test(parts[0])) {
        return parts.slice(1).join('_')
      }
      return fileName
    },
    /** 根据文件类型获取允许的文件扩展名 */
    getAllowedFileTypes() {
      if (this.form.type === 0) {
        // 单js文件，只允许js格式
        return ['js']
      } else if (this.form.type === 1) {
        // zip文件，只允许zip格式
        return ['zip']
      } else {
        // 默认情况（新增时type可能为undefined）
        return ['js', 'zip']
      }
    },
    /** 文件类型变化时的处理 */
    handleFileTypeChange(newType) {
      // 当文件类型改变时，清空已上传的文件
      if (this.form.fileUrl) {
        this.$modal.confirm('切换文件类型将清空已上传的文件，是否继续？').then(() => {
          this.form.fileUrl = ''
        }).catch(() => {
          // 用户取消，恢复原来的类型
          this.$nextTick(() => {
            this.form.type = newType === 0 ? 1 : 0
          })
        })
      }
    },
    /** 获取上传提示文本 */
    getUploadTip() {
      if (this.form.type === 0) {
        return '请选择"单js文件"类型，只能上传 .js 格式的文件，大小不超过50MB'
      } else if (this.form.type === 1) {
        return '请选择"zip文件"类型，只能上传 .zip 格式的压缩文件，大小不超过50MB'
      } else {
        return '请先选择文件类型，然后上传对应格式的文件'
      }
    }
  }
}
</script>