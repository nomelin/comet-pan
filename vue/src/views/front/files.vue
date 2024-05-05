<template>
  <div class="main-container">
    <div class="blank"></div>
    <div class="operation">
      <el-input class="search-input" placeholder="搜索全部文件" style="width:12rem" v-model="name"
                @keyup.enter.native="selectAll(1)">
        <template #suffix>
          <i class="el-icon-search" @click="selectAll(1)"
             style="cursor: pointer; font-size: 2rem; color:#909399; transform: translateY(0.4rem);"></i>
        </template>
      </el-input>


      <el-button class="normal-button" plain style="margin-left: 1rem" @click="reset">
        <i class="el-icon-refresh"></i> 重置
      </el-button>
      <!--      <img class="little-icon" src="@/assets/imgs/folder-add.svg" alt="">-->
      <el-button class="primary-button" type="text" plain style="margin-left: 1rem" @click="addFolder">
        <i class="el-icon-plus"></i> 新建文件夹
      </el-button>
      <el-button class="normal-button" type="danger" plain @click="delBatch">
        <i class="el-icon-delete-solid"></i> 批量删除
      </el-button>
      <el-button class="primary-button" type="primary" plain style="margin-left: 1rem" @click="uploadFile">
        <i class="el-icon-upload"></i> 上传 / 秒传
      </el-button>
      <el-button class="normal-button" type="info" plain style="margin-left: 1rem"
                 @click="shareFile">
        <i class="el-icon-share"></i> 分享
      </el-button>

      <span style="color: #909399;margin-left: 1rem ;font-size: 0.8rem ; font-weight: bold">功能仍在开发中</span>
    </div>
    <div class="blank"></div>
    <div class="backAndForward">
      <el-button type="primary" plain @click="backNavigation" icon="el-icon-back" class="navigation-button"
                 circle :disabled="cacheIndex <= 0"></el-button>
      <el-button type="primary" plain @click="forwardNavigation" icon="el-icon-right" class="navigation-button"
                 circle :disabled="cacheIndex >= requestCache.length - 1"></el-button>
      <div class="path">
        <span v-if="!isSearch">全部文件</span>
        <span v-else>搜索结果</span>
        <span>{{ this.path }} 共{{ this.total }}</span>
      </div>
    </div>

    <div class="table">
      <!-- 使用 v-if 控制 el-skeleton 的显示与隐藏 -->
      <el-skeleton class="table-skeleton" :rows="10" animated v-if="loading"/>
      <el-table v-else :data="filteredData" strip @selection-change="handleSelectionChange"
                height="68vh" class="table-style" empty-text="" @row-contextmenu="rightClick"
                ref="table" :default-sort="{prop: 'name', order: 'ascending'}"
                @cell-mouse-enter="mouseEnter"
                @cell-mouse-leave="mouseLeave"
                :row-style="{height: '4rem'}"
                :cell-style="{padding: '0'}"
      >
        <template v-if="!isSearch" slot="empty">
          <el-empty description=" ">
            <p class="emptyText"><span style='font-size: 1.2rem;font-weight: bold'>这里还没有文件哦, 赶快上传吧</span></p>
          </el-empty>
          <el-button type="primary" @click="uploadFile" class="primary-button">上传 / 秒传</el-button>
        </template>
        <template v-else slot="empty">
          <el-empty description=" ">
            <p class="emptyText"><span style='font-size: 1.1rem;font-weight: bold'>没有找到相关文件</span></p>
          </el-empty>
        </template>

        <el-table-column type="selection" min-width="30" align="center"></el-table-column>
        <!--        <el-table-column prop="id" label="序号" width="70" align="center"></el-table-column>-->
        <el-table-column prop="folder" label="" width="60">
          <template v-slot="scope">
            <span @click="handleFolderClick(scope.row)" style="cursor: pointer;">
              <FileIcon :file-type="scope.row.type" :isFolder="scope.row.folder"></FileIcon>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名称" min-width="180" show-overflow-tooltip
                         sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']"
        >
          <template v-slot="scope">
            <template v-if="!scope.row.isEditing">
              <div class="name-container" @click="handleFolderClick(scope.row)">
                <!-- 如果type为空，则只显示name -->
                <span v-if="!scope.row.type" v-html="highlightText(scope.row.name)"
                      style="cursor: pointer;"></span>
                <!-- 如果type不为空，则显示完整名称 -->
                <span v-else v-html="highlightText(scope.row.name + '.' + scope.row.type)"
                      style="cursor: pointer;"></span>
              </div>

            </template>

            <template v-else>
              <div style="display: flex; align-items: center;">
                <el-input class="rename-input" v-if="!scope.row.type" v-model="editedName"
                          @keyup.enter.native="saveRename(scope.row)"
                          @blur="cancelRename(scope.row)"></el-input>
                <el-input class="rename-input" v-else v-model="editedName" @keyup.enter.native="saveRename(scope.row)"
                          @blur="cancelRename(scope.row)"></el-input>
                <!-- 取消按钮 -->
                <el-button @click="cancelRename(scope.row)" size="mini" style="margin-left: 5px;">取消</el-button>
              </div>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="">
          <template v-slot="scope">
            <template v-if="!scope.row.isEditing">
              <div>
                <div class="opt-container" v-if="scope.row.optShow">
                  <el-tooltip content="分享" effect="dark" :open-delay="100">
                    <i class="el-icon-share" style="margin-right: 10px; cursor: pointer"
                       @click="shareFile"></i>
                  </el-tooltip>
                  <el-tooltip content="删除" effect="dark" :open-delay="100">
                    <i class="el-icon-delete-solid" style="margin-right: 10px; cursor: pointer"
                       @click="del(scope.row.id)"></i>
                  </el-tooltip>
                  <el-tooltip content="重命名" effect="dark" :open-delay="100">
                    <i class="el-icon-document" style="margin-right: 10px; cursor: pointer"
                       @click="rename(scope.row)"></i>
                  </el-tooltip>
                  <el-tooltip content="移动到" effect="dark" :open-delay="100">
                    <i class="el-icon-s-unfold" style="cursor: pointer"
                       @click="move(scope.row.id)"></i>
                  </el-tooltip>
                </div>
              </div>
            </template>
          </template>

        </el-table-column>

        <!--        <el-table-column prop="path" label="文件路径" show-overflow-tooltip></el-table-column>-->
        <!--        <el-table-column prop="type" label="文件类型"></el-table-column>-->
        <el-table-column prop="size" label="文件大小" :formatter="formatSize"
                         sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']">
        </el-table-column>
        <!--        <el-table-column prop="createTime" :formatter="formatTime" label="创建时间"></el-table-column>-->
        <el-table-column prop="updateTime" :formatter="formatTime" label="修改时间"
                         sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']">
          <!--                    <template slot-scope="scope">
                                <span v-if="scope.row.updateTime != null">
                                  {{ scope.row.updateTime | formatTime }}
                                </span>
                              </template>-->
        </el-table-column>
      </el-table>
    </div>
    <div id="contextmenu"
         v-show="menuVisible"
         class="menu">
      <div class="contextmenu__item"
           @click="handleFolderClick(CurrentRow)">打开
      </div>
      <!--      <div class="contextmenu__item"-->
      <!--           @click="download(CurrentRow)">下载(小文件)-->
      <!--      </div>-->
      <div class="contextmenu__item"
           @click="downloadByBrowser(CurrentRow)">下载
      </div>
      <div class="contextmenu__item"
           @click="shareFile">分享
      </div>
      <div class="contextmenu__item"
           @click="rename(CurrentRow)">重命名
      </div>
      <div class="contextmenu__item"
           @click="copy(CurrentRow.id)">复制
      </div>
      <div class="contextmenu__item"
           @click="move(CurrentRow.id)">移动到
      </div>
      <div class="contextmenu__item"
           @click="details(CurrentRow)">详细信息
      </div>
      <div class="divider">
        <el-divider></el-divider>
      </div>

      <div class="contextmenu__item"
           @click="del(CurrentRow.id)">删除
      </div>

    </div>

    <el-dialog title="分享" :visible.sync="shareDialogVisible" width="40%"
               center>
      <el-form ref="form" :model="shareForm" label-width="25%" :rules="rules">
        <el-form-item label="分享名称">
          <el-input v-model="shareForm.name"></el-input>
        </el-form-item>
        <el-form-item label="过期时间">
          <el-select v-model="shareForm.leftDays">
            <el-option label="一天" value="1"></el-option>
            <el-option label="三天" value="3"></el-option>
            <el-option label="七天" value="7"></el-option>
            <!-- <el-option label="十四天" value="14"></el-option> -->
            <el-option label="三十天" value="30"></el-option>
            <el-option label="永久" value="-1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否需要密码">
          <el-radio-group v-model="shareForm.needCode">
            <el-radio label="0">无密码</el-radio>
            <el-radio label="1">有密码</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="shareForm.needCode === '1'" label="分享密码" prop="code">
          <el-input v-model="shareForm.code" placeholder="请输入分享密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button class="normal-button" @click="shareDialogVisible = false">取消</el-button>
          <el-button class="primary-button" type="primary" @click="onSubmit" :disabled="!isCodeValid">创建分享
          </el-button>
        </el-form-item>
      </el-form>

    </el-dialog>

    <template>
      <div class="dialog-files">
        <file-table-dialog :dialog-files-visible.sync="dialogFilesVisible" :src-id.sync="srcId" :type.sync="type"/>
        <!-- .sync 是 Vue.js 中的一种特殊语法，用于实现子组件和父组件之间双向绑定数据的功能。
        它可以简化父子组件之间的通信，特别是用于修改父组件中的 prop 数据。-->
      </div>
    </template>

    <template>
      <div class="uploader">
        <el-drawer
            title="上传文件"
            :visible.sync="uploaderVisible"
            :close-on-press-escape="false"
            size="50%"
            @close="uploaderClose"
        >
          <span><uploader :src-id.sync="uploaderSrcId"/></span>
        </el-drawer>

      </div>
    </template>

  </div>
</template>
<script>
import FileTableDialog from "@/views/front/fileTableDialog";
import Uploader from "@/views/front/uploader";
import FileIcon from "@/views/FileIcon";
import {downloadFile} from "@/App";
// 在需要使用的地方引入 Base64
import {Base64} from 'js-base64';

export default {
  name: "DiskFiles",
  components: {FileTableDialog, Uploader, FileIcon},
  data() {
    return {
      // extension: fileExtensions,

      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      total: 0,
      name: null,
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      ids: [],

      requestCache: [], // 存储发送的请求 URL
      cacheIndex: -1,  // 缓存数组的当前索引
      maxCacheSize: 50, // 最大缓存大小

      isSearch: false,//当前是否处于查询模式
      searchText: "", // 搜索的关键字

      loading: true, // 控制加载动画的显示与隐藏
      folderId: -1, // 当前的文件夹的id
      path: "", // 当前路径

      menuVisible: false, // 右键菜单是否显示

      editedName: '',// 编辑文件的名称

      dialogFilesVisible: false, // 打开文件夹选择框的Dialog
      srcId: -1, // 移动的源文件夹id

      uploaderVisible: false, // 上传文件对话框是否显示
      uploaderSrcId: -1, // 上传文件的目标文件夹id

      shareDialogVisible: false, // 分享对话框是否显示

      shareForm: {
        name: '未命名',
        needCode: "0",
        code: '',
        leftDays: '7',
      },
      rules: {
        code: [
          {required: true, message: '请输入分享密码', trigger: 'blur'},
          {min: 4, max: 4, message: '密码长度必须为4个字符', trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z]+$/, message: '密码只能包含英文字母或数字', trigger: 'blur'}
        ],
      },

      type: "move", // 移动类型，move 移动，copy 复制


    }
  },
  mounted() {
  },
  created() {
    this.load(1)
    this.folderId = this.user.rootId
  },
  computed: {
    // 计算属性，过滤出属性 delete 为 false 的数据,data变化以后自动更新
    filteredData() {
      // console.log(this.folderId)
      // let data = this.tableData.filter(item => !item.delete)
      let data = this.tableData
      this.total = data.length
      this.getPath(this.folderId)
      return data;
    },
    // 计算属性，根据 isSearch 属性和 searchText 动态生成高亮文本
    highlightText() {
      // console.log(this.isSearch, this.searchText)
      return (name) => {
        if (this.isSearch && this.searchText) {
          // 使用正则表达式替换匹配的文本为带有样式的高亮文本
          return name.replace(new RegExp(this.searchText, 'gi'), match => {
            return `<span class="highlight">${match}</span>`;
          });
        } else {
          // 如果不是搜索状态，直接返回原始文本
          return name;
        }
      };
    },
    isCodeValid() {
      if (this.shareForm.needCode === '1') {
        // 根据需要密码的状态检查密码是否符合规则
        // 这里假设密码必须是4个字符的英文字母或数字
        const code = this.shareForm.code;
        return /^[a-zA-Z0-9]{4}$/.test(code);
      } else {
        // 不需要密码时，直接返回 true
        return true;
      }
    },
  },
  watch: {
    dialogFilesVisible(newVal, oldVal) {
      if (oldVal && !newVal) {
        // 当 dialogFilesVisible 从 true 变为 false 时执行的逻辑
        // console.log("dialogFilesVisible 从 true 变为 false")
        // 刷新当前页面
        setTimeout(() => {
          location.reload();
        }, 200); // 延迟200毫秒
      }
    }
  },
  methods: {
    customSortMethod(a, b) {
      let table = this.$refs.table
      // 获取排序状态对象
      let sortState = table.store.states;
      // 获取当前的排序字段
      let currentSortProp = sortState.sortProp;
      // 获取当前的排序顺序
      let currentSortOrder = sortState.sortOrder;
      // console.log(currentSortProp, currentSortOrder)
      if (a.folder === b.folder) {
        // 如果两个值相等，按照要排序的字段排序
        if (currentSortProp === 'name') {
          return a.name.localeCompare(b.name);
        } else if (currentSortProp === 'updateTime') {
          return a.updateTime - b.updateTime
        } else if (currentSortProp === 'size') {
          return a.size - b.size
        }
      } else if (a.folder) {
        if (currentSortOrder === 'ascending') {
          return -1;
        } else if (currentSortOrder === 'descending') {
          return 1;
        }
      } else if (b.folder) {
        if (currentSortOrder === 'ascending') {
          return 1;
        } else if (currentSortOrder === 'descending') {
          return -1;
        }
      }
    },
    del(id) {   // 单个删除
      this.$confirm('确定要删除选中的文件吗？', '确认删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(response => {
        this.$request.delete('/files/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('删除成功')
            this.reload()
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)   //  [1,2]
    },
    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('请选择')
        return
      }
      this.$confirm('确定要删除选中的文件吗？', '确认删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(response => {
        this.$request.delete('/files', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('删除成功')
            this.reload()
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.handleCacheAndGetFileRequest('/files')
      this.folderId = this.user.rootId
    },
    selectAll(pageNum) {
      this.folderId = this.user.rootId

      this.isSearch = true;
      this.searchText = this.name;
      //根据条件查询所有数据
      if (pageNum) this.pageNum = pageNum
      this.loading = true;
      this.requestCache = [];
      this.cacheIndex = 0;
      //如果搜素条件为空，则相当于重置
      if (this.name === null || this.name === "") {
        this.isSearch = false;
        this.reset()
        return;
      }
      // this.requestCache = this.requestCache.slice(0, this.cacheIndex + 1)
      this.$request.get("/files/all", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.loading = false;
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.tableData = res.data
        }
      })
    },
    getPath(id) {
      this.$request.get('/files/file/' + id).then(res => {
        if (res.code === '200') {
          this.path = res.data.path.replace(/\//g, '>'); // 使用正则表达式替换所有匹配到的 /
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
      })
    },
    reset() {
      this.name = null
      this.isSearch = false;
      this.searchText = "";
      // this.load(1);
      this.folderId = this.user.rootId
      this.handleCacheAndGetFileRequest("/files")
    },
    reload() {
      this.getFileRequest(this.requestCache[this.cacheIndex])
    },
    handleCacheAndGetFileRequest(url) {
      // 用于封装getFileRequest和addToCache方法，统一处理
      // 丢弃索引之后的缓存(因为用户已经重新点击了新的路径)
      this.requestCache = this.requestCache.slice(0, this.cacheIndex + 1)
      this.isSearch = false;
      this.addToCache(url)
      this.getFileRequest(url)
    },
    getFileRequest(url) {// 发送请求,获取数据。不要直接调用此方法，调用handleCacheAndGetFileRequest方法
      //前进和后退可以直接调用此方法。
      this.loading = true;
      this.$request.get(url, {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        }
      }).then(res => {
        this.loading = false;
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.tableData = res.data
          this.total = res.data.length
        }
      })
    },
    addToCache(url) {
      // 将新的 URL 添加到缓存中
      this.requestCache.push(url)
      // 如果缓存大小超过限制，则丢弃最老的请求 URL
      if (this.requestCache.length > this.maxCacheSize) {
        this.requestCache.shift()
      }
      // 更新缓存索引
      this.cacheIndex = this.requestCache.length - 1
    },
    handleFolderClick(row) {
      if (row.folder) {
        this.handleCacheAndGetFileRequest('/files/folder/' + row.id)
        // console.log(this.requestCache + ":" + this.cacheIndex)
        this.folderId = row.id
      } else {
        //let originUrl = 'http://localhost:12345/download?diskId=' + row.diskId + "&fileId=" + row.id;
        // console.log("total:"+row.size)
        if (row.size > 30 * 1024 * 1024) {
          this.$message.error("文件过大，暂不支持在线预览");
          return;
        }
        let originUrl = process.env.VUE_APP_BASEURL + '/download?diskId=' + row.diskId + "&fileId=" + row.id;
        // console.log(originUrl)
        originUrl = originUrl + '&fullfilename=' + row.name + "." + row.type;
        // window.open(process.env.VUE_APP_FILE_VIEW_URL+'?url=' + encodeURIComponent(Base64.encode(originUrl)));
        let url = process.env.VUE_APP_FILE_VIEW_URL + '?url=' + encodeURIComponent(Base64.encode(originUrl))
        console.log(url)
        window.open(url);
      }
    },
    backNavigation() {
      // 判断是否有可后退的请求
      if (this.cacheIndex > 0) {
        this.cacheIndex--
        let folderId = this.requestCache[this.cacheIndex].split('/')[3]
        if (!isNaN(folderId)) {
          this.folderId = folderId//如果是数字，则更新文件夹id
        } else {
          this.folderId = this.user.rootId//如果不是数字，则说明回到根目录
        }
        this.getFileRequest(this.requestCache[this.cacheIndex])

      }
    },
    forwardNavigation() {
      // 判断是否有可前进的请求
      if (this.cacheIndex < this.requestCache.length - 1) {
        this.cacheIndex++
        let folderId = this.requestCache[this.cacheIndex].split('/')[3]
        if (!isNaN(folderId)) {
          this.folderId = folderId//如果是数字，则更新文件夹id
        } else {
          this.folderId = this.user.rootId//如果不是数字，则说明回到根目录
        }
        this.getFileRequest(this.requestCache[this.cacheIndex])
      }
    },
    addFolder() {
      this.$request({
        url: '/files/folder',
        method: 'POST',
        data: {name: '新建文件夹', folderId: this.folderId}
      }).then(res => {
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.reload()
        }
      })
    },
    // 右键菜单
    rightClick(row, column, event) {
      // 判断当前行是否已经被选中
      //let isSelected = this.$refs.table.selection.includes(row);
      // 如果当前行未被选中，则添加到选中行列表中
      this.$refs.table.clearSelection();//清空选中行列表
      //if (!isSelected) {
      this.$refs.table.toggleRowSelection(row);
      //}
      this.testModeCode = row.testModeCode
      this.menuVisible = false // 先把模态框关死，目的是 第二次或者第n次右键鼠标的时候 它默认的是true
      this.menuVisible = true // 显示模态窗口，跳出自定义菜单栏
      event.preventDefault() //关闭浏览器右键默认事件
      this.CurrentRow = row
      var menu = document.querySelector('.menu')
      this.styleMenu(menu)
    },
    foo() {
      // 取消鼠标监听事件 菜单栏
      this.menuVisible = false
      document.removeEventListener('click', this.foo) // 关掉监听，
    },
    styleMenu(menu) {
      if (event.clientX > 1800) {
        menu.style.left = event.clientX - 100 + 'px'
      } else {
        menu.style.left = event.clientX + 1 + 'px'
      }
      document.addEventListener('click', this.foo) // 给整个document新增监听鼠标事件，点击任何位置执行foo方法
      if (event.clientY > 700) {
        menu.style.top = event.clientY - 30 + 'px'
      } else {
        menu.style.top = event.clientY - 10 + 'px'
      }
    },
    formatSize(row, column) {
      // row 是当前行的数据对象
      // column 是当前列的属性配置对象
      // 调用过滤器来格式化文件大小
      return this.$options.filters.sizeFormat(row[column.property], 1);
    },
    formatTime(row, column) {
      return this.$options.filters.formatTime(row[column.property]);
    },
    // 重命名操作
    rename(row) {
      // 遍历数据源中的每一行，将其 isEditing 属性设为 false
      this.tableData.forEach(item => {
        this.$set(item, 'isEditing', false);
      });
      // 将当前行设置为正在编辑状态
      this.$set(row, 'isEditing', true);
      this.editedName = row.name + (row.type ? '.' + row.type : '');
    },
    // 保存重命名后的名称
    saveRename(row) {
      console.log(row.name, row.type)
      console.log(this.editedName)
      let newName = this.editedName;
      if (row.folder) {
        row.name = newName;
        row.type = "";
      } else {
        const lastIndex = this.editedName.lastIndexOf('.');
        let name = this.editedName.substring(0, lastIndex);
        let type = this.editedName.substring(lastIndex + 1);
        if (lastIndex === -1) {
          name = this.editedName;
          type = "";
        }
        row.name = name;
        row.type = type;
        newName = name + (type === "" ? "" : "." + type);
      }
      console.log("newName:" + newName)
      this.$request.put('/files/rename', {id: row.id, name: newName}).then(res => {
        if (res.code === '200') {
          this.$message.success('重命名成功')
          this.reload()
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
      })
      this.$set(row, 'isEditing', false);
      this.reload();
    },
    // 取消重命名操作
    cancelRename(row) {
      if (row.isEditing) {
        // 取消编辑状态，恢复原始名称
        this.$set(row, 'isEditing', false);
        this.$message.info('取消重命名')
        this.reload();
      }
    },
    move(id) {
      this.srcId = id
      this.type = 'move'
      this.dialogFilesVisible = true
    },
    copy(id) {
      this.srcId = id
      this.type = 'copy'
      this.dialogFilesVisible = true
    },
    uploadFile() {
      this.uploaderVisible = true
      this.uploaderSrcId = parseInt(this.folderId)
    },
    uploaderClose() {
      this.reload()
    },
    mouseEnter(row) {
      this.$set(row, 'optShow', true)
    },
    mouseLeave(row) {
      this.$set(row, 'optShow', false)
    },
    async downloadByBrowser(row) {
      if (row.folder === true) {
        this.$message.info('暂不支持下载文件夹')
        return
      }
      // console.log(row)
      downloadFile(row.diskId, row.id)
    },
    download(row) {
      if (row.folder === true) {
        this.$message.info('暂不支持下载文件夹')
        console.log("是文件夹：" + row.name)
        return
      }
      this.XHRLoadFile(process.env.VUE_APP_BASEURL + '/download/' + row.id + '/0', {})
    },

    //通过XMLHttpRequest发送post请求下载文件
    //url : 请求的Url
    //data ： 请求的数据
    XHRLoadFile: (url, data) => {
      let xhr = new XMLHttpRequest()
      xhr.open('post', url)
      //如果需要请求头中这是token信息可以在这设置
      // xhr.setRequestHeader('Content-Type','application/json;charset=UTF-8')
      let user = JSON.parse(localStorage.getItem('user') || '{}')
      console.log(user.token)
      let token = user.token
      xhr.setRequestHeader('token', token)
      xhr.responseType = 'blob'
      xhr.send(JSON.stringify(data))
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
          const blob = new Blob([xhr.response])
          let url = window.URL.createObjectURL(blob)

          //创建一个a标签元素，设置下载属性，点击下载，最后移除该元素
          let link = document.createElement('a')
          link.href = url
          link.style.display = 'none'
          //取出下载文件名
          const disposition = xhr.getResponseHeader('content-disposition')
          let fileName = disposition.substring(disposition.indexOf("=") + 1);
          const downloadFileName = decodeURIComponent(fileName)
          link.setAttribute('download', downloadFileName)
          link.click()
          window.URL.revokeObjectURL(url)
        }
      }
    },
    shareFile() {
      if (!this.ids.length) {
        this.$message.warning('请选择数据')
        return
      }
      this.shareDialogVisible = true
    },
    onSubmit() {
      if (!this.ids.length) {
        this.$message.warning('请选择')
        return
      }
      console.log(this.ids)
      console.log(this.ids.join(','))
      this.$request.post('/share', {
        fileIds: this.ids.join(','),
        name: this.shareForm.name,
        code: this.shareForm.code,
        leftDays: this.shareForm.leftDays,
      }).then(res => {
        if (res.code === '200') {   // 表示操作成功
          this.$message.success('分享成功')
          // this.$alert('链接' + res.data.path, '分享成功', {
          //   confirmButtonText: '确定',
          // });
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
        this.shareDialogVisible = false
      })
    },
    details(row) {
      let message =
          "<div style='width:300px; margin: 0 auto; text-align: left;'>" +
          "<strong style='color:black'>文件名：</strong>" +
          "<span style='color:gray'>" + row.name + "</span><br>" +
          "<strong style='color:black'>文件类型：</strong>" +
          "<span style='color:gray'>" + (row.folder ? "文件夹" : row.type || "其它") + "</span><br>" +
          "<strong style='color:black'>文件大小：</strong>" +
          "<span style='color:gray'>" + this.$options.filters.sizeFormat(row.size, 3) + "</span><br>" +
          "<strong style='color:black'>创建时间：</strong>" +
          "<span style='color:gray'>" + this.$options.filters.formatTime(row.createTime) + "</span><br>" +
          "<strong style='color:black'>修改时间：</strong>" +
          "<span style='color:gray'>" + this.$options.filters.formatTime(row.updateTime) + "</span><br>" +
          "<strong style='color:black'>文件路径：</strong>" +
          "<span style='color:gray'>" + row.path + "</span>";

      this.$confirm(message, '详细信息', {
        showCancelButton: false,
        showConfirmButton: false,
        center: true,
        dangerouslyUseHTMLString: true // 使用 HTML 字符串
      }).then(() => {
      }).catch(() => {
      })
    },

  }
}
</script>

<style scoped>

.blank {
  height: 3%
}

.operation {
  /*position: absolute;*/
  margin-left: 3%;
}

.backAndForward {
  margin-left: 5%;
  width: 80%;
  display: flex;
  justify-content: flex-start; /* 靠左对齐 */
}


::v-deep .search-input .el-input__inner {
  width: 12rem;
  height: 3rem;
  background-color: #EBEEF5;
  text-align: center;
  border: 0 !important;
  outline: none;
  font-weight: bold;
  font-size: 1rem;
  border-radius: 1rem;
}

::v-deep .highlight {
  /*background-color: yellow;*/
  color: #0d53ff;
  font-weight: bold;
  font-size: 1rem;
}


::v-deep .rename-input .el-input__inner {
  overflow: visible;
  width: 100%;
  text-align: left;
  border: 0 !important;
  outline: none;
  font-size: 0.9rem;
  font-weight: bold;
}

.dialog-files {
  z-index: 999;
}

.name-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.opt-container {
  color: #606266;
  font-size: 1rem;
  font-weight: bold;
}

.divider {
  height: 0;
}

.navigation-button {
  border: 0.2rem solid;
}
</style>
