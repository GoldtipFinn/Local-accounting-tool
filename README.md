# Local-accounting-tool

一个纯本地运行的 Android 记账应用，使用 `Kotlin`、`Jetpack Compose` 和 `Room` 构建。  
它面向“轻量、直接、本地优先”的个人记账场景：不接云端、不依赖服务器，把记账、统计和导出都放在手机端完成。

![应用封面](jizhang.png)

## 项目定位

Local-accounting-tool 适合以下使用方式：

- 想要一款纯本地记账 App
- 不希望账单上传到云端
- 需要基础统计和图表分析
- 需要把数据导出到本地文件夹
- 希望可以直接安装到 Android 手机使用

当前应用包名：`com.goldtip.localaccountingtool`

## 功能总览

### 记账

- 快速录入收入或支出
- 支持金额、备注、日期调整
- 收入 / 支出切换后自动切换分类
- 支持按天前后调整账单日期

### 明细

- 按日期分组展示账单
- 显示每天收入 / 支出汇总
- 支持单条删除账单

### 统计与图表

- 按 `日 / 月 / 年` 粒度查看数据
- 支持输入日期关键字快速定位图表桶
- 支持查看：
  - 收入 / 支出汇总
  - 收支分类占比
  - 分类条形图
  - 年度 12 个月收支差值混合图

### 导出与设置

- 查看应用数据库目录
- 选择导出文件夹
- 导出全部账单数据
- 导出格式支持：
  - `CSV`
  - `Excel XML`
  - `JSON`
- 支持预设主题配色
- 支持自定义收入色、支出色、折线色和背景渐变色

## 技术栈

- `Kotlin`
- `Jetpack Compose`
- `Material 3`
- `Room`
- `KSP`
- `MVVM`
- `Coroutines`

## 构建配置

- Android Gradle Plugin：`8.5.2`
- Kotlin：`1.9.24`
- KSP：`1.9.24-1.0.20`
- Compile SDK：`35`
- Target SDK：`35`
- Min SDK：`26`
- Java / JVM Target：`17`

## 项目结构

```text
app/
├─ src/main/java/com/goldtip/localaccountingtool/
│  ├─ MainActivity.kt
│  ├─ data/
│  │  ├─ Converters.kt
│  │  ├─ LedgerDao.kt
│  │  ├─ LedgerDatabase.kt
│  │  ├─ LedgerRepository.kt
│  │  ├─ TransactionEntity.kt
│  │  └─ TransactionType.kt
│  └─ ui/
│     ├─ LedgerAnalytics.kt
│     ├─ LedgerExport.kt
│     ├─ LedgerModels.kt
│     ├─ LedgerSampleData.kt
│     ├─ LedgerScreen.kt
│     ├─ LedgerViewModel.kt
│     └─ theme/
├─ src/main/res/
└─ src/test/
```

### 关键模块说明

- `MainActivity.kt`
  - 应用入口，负责挂载 Compose UI
- `data/`
  - Room 数据库、DAO、仓储、实体与类型转换器
- `ui/LedgerViewModel.kt`
  - 页面状态、用户行为、导出流程、统计数据绑定
- `ui/LedgerScreen.kt`
  - 主要 Compose 页面与组件
- `ui/LedgerAnalytics.kt`
  - 收支统计、图表桶、分类聚合逻辑
- `ui/LedgerExport.kt`
  - CSV / Excel XML / JSON 导出实现
- `ui/LedgerSampleData.kt`
  - 样本数据生成器
- `src/test/`
  - 统计引擎与样本数据生成器的单元测试

## 数据与隐私

- 使用 Room 本地数据库存储账单
- 数据库文件名：`local_accounting.db`
- 数据保存在应用私有目录
- 当前应用未声明网络权限
- 账单不会上传到服务器

## 导出格式说明

### CSV

- 适合用 Excel 或表格工具直接打开
- 字段包含：
  - `ID`
  - `日期`
  - `类型`
  - `类别`
  - `金额`
  - `说明`

### Excel XML

- 兼容 Excel
- 保留表格结构

### JSON

- 适合备份、迁移和二次加工

## 数据流

整体流程如下：

1. 用户在记账页录入账单
2. `LedgerViewModel` 调用 `LedgerRepository`
3. `LedgerRepository` 通过 `LedgerDao` 写入 Room
4. `observeTransactions()` 返回的 `Flow` 驱动 UI 自动刷新
5. `LedgerAnalyticsEngine` 基于当前日期生成统计结果
6. 首页、明细页、图表页共享同一套账单与分析数据

## 运行方式

### 使用 Android Studio

1. 用 Android Studio 打开当前项目目录
2. 等待 Gradle 同步完成
3. 连接 Android 手机或启动模拟器
4. 运行 `app` 模块

### 命令行构建 Debug APK

如果你的环境已经安装并配置好了 Gradle，可以在项目根目录执行：

```powershell
gradle.bat assembleDebug
```

生成产物默认位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 安装到手机

如果设备已开启 USB 调试，可执行：

```powershell
adb install -r "app/build/outputs/apk/debug/app-debug.apk"
```

## 测试

当前项目包含两类本地单元测试：

- `LedgerAnalyticsEngineTest`
  - 验证统计结果、图表桶聚合与可见数据范围
- `SampleDataGeneratorTest`
  - 验证样本数据时间范围与月度收支约束

执行方式：

```powershell
gradle.bat testDebugUnitTest
```

## 当前实现特点

- 纯本地记账，不依赖后端
- Compose UI 直接驱动页面表现
- ViewModel 统一管理状态和用户行为
- 分析与导出逻辑已独立拆分，便于后续扩展

## 已知情况

- 当前是单模块 Android 项目
- `LedgerScreen.kt` 体量较大，后续可以继续拆分页面和组件
- 当前主题以亮色方案为主
- 已支持导出，但暂未实现导入
- 当前仓库未提交 Gradle Wrapper，如果打算给他人直接克隆运行，建议补上 `gradlew` / `gradlew.bat`

## 后续可扩展方向

- 预算提醒
- 多账本
- 搜索与筛选
- 数据导入
- 深色模式
- 更多图表维度
- 备份与恢复

