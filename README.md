# pvz-trainer-mod — 修改器模组

本生态的**修改器（trainer）模组**：关卡内一枚可拖动的原生石头按钮，点击展开 PvZ 原生对话框操作。**零 Mixin**——全部功能走事件 API、公开字段与游戏的按钮/对话框/复选框控件，是"API 之外不碰游戏内部"的活例。

## 形态

- **悬浮按钮**（折叠态）：`LawnStoneButton` 原生石钮贴图，按住可自由拖动（钳制在窗口内），位移小于阈值算点击——展开对话框；除加载画面/戴夫商店/图鉴/制作人员/信件页/三选关页/开场动画/试玩版推销板外显示；
- **原生对话框**（展开态）：`LawnDialog` 原生棕框 + 金色标题，modal 打开即自动暂停棋盘；顶部下拉式**十页标签**（关卡/卡槽/植物/僵尸/花园/场地/特效/状态/用户/其他），内容区为固定高度滚动视口，底部"收 起"收回折叠；
- 按钮只在关卡进行中且对话框未展开时显示，点击不穿透棋盘种植。

## 功能（按标签页）

- **关卡**：无限阳光 / 免费种植 / 自动收集 / 暂停出怪（含打僵尸关独立节奏）/ 立即下一波 / 推进关卡 / 倍速（1-10×）/ 不掉阳光 / 不掉金币 / 任意种植 / 直接过关；
- **其他**：杀死全场僵尸 / 杀死全场植物 / 小推车三连（清除/恢复/启动）/ 跳关器（F1 同款对话框）；
- **用户**（作用于当前存档）：解锁金向日葵 / 临时解锁所有模式 / 无限金币 / 设置金币；
- **花园**：无限花肥/杀虫剂/巧克力/树肥 / 设置智慧树高度 / 随机幼苗·金盏花·大花（带场景锁与已满判定）；
- **状态**：12 项棋盘实时状态展示（模式/界面/波次/血量/倒计时/数量）；
- **特效**：8 枚隐码效果开关（SUKHBIR/FUTURE/MUSTACHE/TRICKEDOUT/DAISIES/DANCE/PINATA/KONAMI，前三项有解锁门槛）；
- **卡槽**：卡槽格数 / 隐藏卡槽 / 显示铲子 / 卡片无冷却 / 卡槽内容与植物定义直改；
- **场地**：行列数滑块 / 一键种植·放置 / 背景音乐切换 / 清空墓碑 / 去除浓雾 / 花瓶透视。

## 配置

- 配置文件：启动目录 `config/pvz-trainer-mod.json`（保存时自动创建）；
- **展开修改器时自动暂停**（默认开）：修改器对话框 modal 打开即暂停棋盘；关闭则非模态、棋盘继续运行；
- **悬浮按钮始终置顶**（默认开）：关闭后按钮不再每帧恢复置顶；
- 配置入口：主菜单 X+C 组合键（800ms 防连发）或 ModMenu 的"配置"按钮。

## 构建

```bash
# 先在游戏仓库根跑一次 distZip 产出 build/distributions/（本模组的 pvz(...) 指向它）
cd ../../ && ./gradlew distZip && cd examples/pvz-trainer-mod

./gradlew build        # 产物 build/libs/pvz-trainer-mod-1.0.0.jar
./gradlew runClient    # 开发运行
```

对着正式发布版本构建时，把 `build.gradle.kts` 里的 `pvz(files("../../build/distributions"))` 换成 `pvz("io.github.pvzjre:pvz-portable:1.2.5")`。

## 安装（正式版本目录）

把 `build/libs/pvz-trainer-mod-1.0.0.jar` 放进版本目录的 `mods/`，同时确保 `fabric-api-<版本>.jar`（聚合 jar）也在里面。

## 实现要点（对照 [模组开发文档](../pvz-minimal-mod/模组开发文档.md) §5/§8）

- **原生控件复用**：悬浮按钮 = `LawnStoneButton` 子类（同款对话框 OK/Cancel 钮的绘制路径）；对话框 = `LawnDialog` 子类（`BUTTONS_FOOTER` 模式的 footer 按钮即"收 起"，id 1000 = `Dialog.ID_FOOTER`）；复选框 = `Checkbox` + `CheckboxListener`；模组自建控件 id 取 2001+ 高位段；
- **拖动**：`WidgetManager.mLastMouseX/Y` 是 public，TICK 里读全局指针；mouseDown 记抓取偏移，TICK 吸附跟随（阈值 5px 区分点击/拖动），mouseUp 结束并判定；
- **置顶与穿透**：命中测试从 widget 列表尾向前找（后加入者优先），TICK 检测按钮不在列表末位就摘下重加；`mVisible=false` 不参与命中测试；
- **对话框生命周期**：`mApp.addDialog(id, dialog)` 打开（modal 自动暂停），底部"收 起"在 `buttonDepress(1000)` 里 `mApp.killDialog(mId)` 关闭并复位折叠标记；
- **秒杀**：`Zombie.takeDamage(1800, DAMAGE_HITS_SHIELD_AND_BODY)`，与游戏 `Board.killAllZombiesInRadius` 的非燃烧路径同参，但去掉 `DAMAGE_DOESNT_LEAVE_BODY`——正常致死动画并留尸体。

MIT License.
