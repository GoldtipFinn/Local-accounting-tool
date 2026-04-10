# VivoLedger

一个纯本地运行的 Android 记账应用，使用 Kotlin、Jetpack Compose 和 Room 构建。  
项目目标很直接：不接云端、不依赖服务器，把日常收支、统计分析和本地导出都放在手机端完成。

![应用封面](jizhang.png)

## 项目简介

VivoLedger 面向“本地、轻量、可直接安装使用”的个人记账场景：

- 所有账单数据仅保存在设备本地 Room 数据库
- 支持收入 / 支出两种类型
- 支持分类、备注、日期调整
- 内置图表统计与多维度收支分析
- 支持将数据导出为 `CSV`、`Excel XML`、`JSON`
- 适合直接在 Android 手机上安装使用

当前应用包名为 `com.goldtip.vivoledger`。

## 核心功能

### 1. 记账页

- 快速录入一笔收入或支出
- 输入金额与备注
- 按收入 / 支出动态切换分类
- 支持前一天 / 后一天调账日期
- 自动展示：
  - 累计结余
  - 今日收入
  - 今日支出
  - 本月收入
  - 本月支出
  - 本月日均支出
  - 本月重点支出分类
  - 最近账单

### 2. 明细页

- 按日期分组展示账单
- 每天显示当日收入 / 支出汇总
- 展示分类、备注、金额
- 支持单条删除账单

### 3. 图表页

- 支持按 `日 / 月 / 年` 三个粒度查看数据
- 支持输入时间关键字直接定位图表桶
  - 日：`2026-04-10` 或 `20260410`
  - 月：`202604`
  - 年：`2026`
- 查看指定时间范围内：
  - 收入 / 支出汇总
  - 收支分类占比
  - 分类条形图
  - 年度 12 个月收支差值混合图

### 4. 设置页

- 查看应用数据库所在目录
- 选择导出文件夹
- 导出全部记账数据
- 导出格式支持：
  - `CSV`
  - `Excel XML`
  - `JSON`
- 支持主题配色切换
- 支持自定义收入色、支出色、折线色、背景渐变色

## 技术栈

- `Kotlin`
- `Jetpack Compose`
- `Material 3`
- `Room`
- `KSP`
- `MVVM`
- `Coroutines`

## 开发环境

### 构建配置

- Android Gradle Plugin：`8.5.2`
- Kotlin：`1.9.24`
- KSP：`1.9.24-1.0.20`
- Compile SDK：`35`
- Target SDK：`35`
- Min SDK：`26`
- Java / JVM Target：`17`

### 运行要求

- Android 8.0 及以上设备
- Android Studio
- 本地已配置 Android SDK

## 项目结构

```text
tryAI/
├─ app/
│  ├─ src/main/java/com/goldtip/vivoledger/
│  │  ├─ MainActivity.kt
│  │  ├─ data/
│  │  │  ├─ Converters.kt
│  │  │  ├─ LedgerDao.kt
│  │  │  ├─ LedgerDatabase.kt
│  │  │  ├─ LedgerRepository.kt
│  │  │  ├─ TransactionEntity.kt
│  │  │  └─ TransactionType.kt
│  │  └─ ui/
│  │     ├─ LedgerAnalytics.kt
│  │     ├─ LedgerExport.kt
│  │     ├─ LedgerModels.kt
│  │     ├─ LedgerSampleData.kt
│  │     ├─ LedgerScreen.kt
│  │     ├─ LedgerViewModel.kt
│  │     └─ theme/
│  ├─ src/main/res/
│  └─ src/test/
├─ build.gradle.kts
├─ settings.gradle.kts
└─ README.md
```

### 目录说明

- `MainActivity.kt`
  - 应用入口，负责挂载 Compose UI
- `data/`
  - Room 数据库、DAO、仓储、实体与类型转换器
- `ui/LedgerViewModel.kt`
  - 页面状态、用户操作、导出流程、统计数据绑定
- `ui/LedgerScreen.kt`
  - 主要 Compose 页面与组件
- `ui/LedgerAnalytics.kt`
  - 收支统计、图表桶、分类聚合逻辑
- `ui/LedgerExport.kt`
  - 导出为 CSV / Excel XML / JSON 的实现
- `ui/LedgerSampleData.kt`
  - 样本数据生成器
- `src/test/`
  - 当前包含统计引擎与样本数据生成器的单元测试

## 数据存储说明

- 本地数据库使用 Room
- 数据库文件名：`local_accounting.db`
- 数据保存在应用私有目录
- 应用没有声明网络权限，不会把账单上传到服务器

## 导出说明

应用支持将全部账单导出到用户选择的本地目录。

### CSV

- 适合用 Excel 或表格工具直接打开
- 文件头包含：
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

- 更适合备份、迁移或后续再加工

## 页面与数据流

整体数据流比较清晰：

1. 用户在记账页录入数据
2. `LedgerViewModel` 调用 `LedgerRepository`
3. `LedgerRepository` 通过 `LedgerDao` 写入 Room
4. `observeTransactions()` 返回的 `Flow` 驱动 UI 自动刷新
5. `LedgerAnalyticsEngine` 基于当前日期生成统计结果
6. 图表页、明细页、首页卡片共享同一套状态与分析数据

## 如何运行

### 使用 Android Studio

1. 用 Android Studio 打开目录 `D:\Desktop\tryAI`
2. 等待 Gradle 同步完成
3. 连接 Android 手机或启动模拟器
4. 选择 `app` 模块并运行

### 命令行构建 Debug 包

在项目根目录执行：

```powershell
gradle.bat -p D:\Desktop\tryAI assembleDebug
```

生成的 APK 默认位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 安装到手机

如果已开启 USB 调试，可以使用：

```powershell
adb install -r "D:\Desktop\tryAI\app\build\outputs\apk\debug\app-debug.apk"
```

## 测试

当前项目包含两类本地单元测试：

- `LedgerAnalyticsEngineTest`
  - 验证统计结果、图表桶聚合与可见数据范围
- `SampleDataGeneratorTest`
  - 验证样本数据时间范围与月度收支约束

执行方式：

```powershell
gradle.bat -p D:\Desktop\tryAI testDebugUnitTest
```

## 当前实现特点

- 优先使用本地能力，逻辑简单直接
- Compose UI 集中在 `LedgerScreen.kt`
- ViewModel 负责状态聚合与用户行为分发
- 分析与导出逻辑已独立拆分，后续可继续扩展

## 已知情况

- 当前不是多模块项目，所有主要 UI 都集中在单个 `app` 模块
- `LedgerScreen.kt` 体量较大，后续可以继续拆分页面与组件
- 主题目前以亮色方案为主
- 导出功能已支持三种格式，但导入功能尚未接入

## 后续可扩展方向

- 预算提醒
- 多账本
- 搜索与筛选
- 数据导入
- 深色模式
- 更多图表维度
- 备份与恢复

## 许可证

当前仓库未单独声明许可证。  
如果准备公开发布到 GitHub，建议补充 `LICENSE` 文件后再开放分发。
