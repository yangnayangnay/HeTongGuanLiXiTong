const pptxgen = require("pptxgenjs");

let pres = new pptxgen();
pres.layout = "LAYOUT_16x9";
pres.author = "合同管理系统团队";
pres.title = "合同管理系统终期答辩";

const BG_DARK = "1A1A2E";
const BG_LIGHT = "F8F7F4";
const PRIMARY = "065A82";
const SECONDARY = "E0F2FE";
const ACCENT = "FFFFFF";
const TEXT_DARK = "1A1A2E";
const TEXT_LIGHT = "FFFFFF";
const SUCCESS = "059669";

function makeShadow() {
  return { type: "outer", blur: 6, offset: 2, angle: 135, color: "000000", opacity: 0.15 };
}

// Slide 1: Cover
let s1 = pres.addSlide();
s1.background = { color: BG_DARK };
s1.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 5.625, fill: { color: PRIMARY, transparency: 70 } });
s1.addText("合同管理系统", { x: 1, y: 1.2, w: 8, h: 1.5, fontSize: 44, fontFace: "Arial Black", color: TEXT_LIGHT, bold: true, align: "center" });
s1.addText("终期答辩汇报", { x: 1, y: 2.7, w: 8, h: 0.8, fontSize: 28, fontFace: "Arial", color: SECONDARY, align: "center" });
s1.addShape(pres.shapes.LINE, { x: 3, y: 3.7, w: 4, h: 0, line: { color: "4FC3F7", width: 2 } });
s1.addText("基于Spring Boot + Oracle的B/S架构合同全生命周期管理平台", { x: 1, y: 4.0, w: 8, h: 0.5, fontSize: 14, fontFace: "Calibri", color: "B0BEC5", align: "center" });
s1.addText("2026年6月", { x: 1, y: 4.7, w: 8, h: 0.4, fontSize: 14, fontFace: "Calibri", color: "78909C", align: "center" });

// Slide 2: Project Overview
let s2 = pres.addSlide();
s2.background = { color: BG_LIGHT };
s2.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.9, fill: { color: PRIMARY } });
s2.addText("项目概述", { x: 0.5, y: 0.15, w: 9, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: TEXT_LIGHT, bold: true });
s2.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 1.2, w: 4.2, h: 3.8, fill: { color: ACCENT }, shadow: makeShadow() });
s2.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 1.2, w: 0.08, h: 3.8, fill: { color: PRIMARY } });
s2.addText("项目背景", { x: 0.8, y: 1.35, w: 3.7, h: 0.5, fontSize: 18, fontFace: "Arial", color: PRIMARY, bold: true });
s2.addText([
  { text: "企业合同管理流程复杂，涉及起草、会签、定稿、审批、签订等多个环节", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "传统纸质管理效率低、易出错、难以追溯", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "需要信息化手段实现合同全生命周期管理", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } }
], { x: 0.8, y: 1.9, w: 3.7, h: 2.8 });

s2.addShape(pres.shapes.RECTANGLE, { x: 5.3, y: 1.2, w: 4.2, h: 3.8, fill: { color: ACCENT }, shadow: makeShadow() });
s2.addShape(pres.shapes.RECTANGLE, { x: 5.3, y: 1.2, w: 0.08, h: 3.8, fill: { color: SUCCESS } });
s2.addText("项目目标", { x: 5.6, y: 1.35, w: 3.7, h: 0.5, fontSize: 18, fontFace: "Arial", color: SUCCESS, bold: true });
s2.addText([
  { text: "实现合同从起草到签订的全流程电子化管理", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "支持多角色协同办公（起草人/会签人/审批人/签订人）", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "提供AI智能辅助、电子签名、邮件通知等创新功能", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "确保合同数据安全，支持权限控制和操作日志审计", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } }
], { x: 5.6, y: 1.9, w: 3.7, h: 2.8 });

// Slide 3: Tech Stack
let s3 = pres.addSlide();
s3.background = { color: BG_LIGHT };
s3.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.9, fill: { color: PRIMARY } });
s3.addText("技术架构", { x: 0.5, y: 0.15, w: 9, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: TEXT_LIGHT, bold: true });

let techItems = [
  { title: "后端框架", desc: "Spring Boot 2.7.18\nSpring JDBC\nJava 8", color: "0D9488" },
  { title: "数据库", desc: "Oracle 26ai Free\n12张业务表\n12个序列", color: "7C3AED" },
  { title: "前端技术", desc: "Thymeleaf模板\nBootstrap 4.6.2\n原生JavaScript", color: "DC2626" },
  { title: "AI集成", desc: "Ollama本地大模型\nqwen2:7b\n数据不出本机", color: "EA580C" },
  { title: "邮件服务", desc: "JavaMail SSL\nSMTP加密传输\n自动重试机制", color: "2563EB" },
  { title: "文档处理", desc: "iText PDF生成\nOCR文字识别\n分块断点续传", color: "059669" }
];

techItems.forEach(function(item, i) {
  let col = i % 3;
  let row = Math.floor(i / 3);
  let x = 0.5 + col * 3.1;
  let y = 1.2 + row * 2.1;
  s3.addShape(pres.shapes.RECTANGLE, { x: x, y: y, w: 2.8, h: 1.8, fill: { color: ACCENT }, shadow: makeShadow() });
  s3.addShape(pres.shapes.RECTANGLE, { x: x, y: y, w: 2.8, h: 0.06, fill: { color: item.color } });
  s3.addText(item.title, { x: x + 0.15, y: y + 0.15, w: 2.5, h: 0.4, fontSize: 14, fontFace: "Arial", color: item.color, bold: true });
  s3.addText(item.desc, { x: x + 0.15, y: y + 0.6, w: 2.5, h: 1.0, fontSize: 11, fontFace: "Calibri", color: "475569" });
});

// Slide 4: Core Features - Contract Lifecycle
let s4 = pres.addSlide();
s4.background = { color: BG_LIGHT };
s4.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.9, fill: { color: PRIMARY } });
s4.addText("核心功能 — 合同全生命周期管理", { x: 0.5, y: 0.15, w: 9, h: 0.6, fontSize: 26, fontFace: "Arial Black", color: TEXT_LIGHT, bold: true });

let steps = [
  { num: "1", title: "起草合同", desc: "模板加载\n智能填入\n电子签名", color: "3B82F6" },
  { num: "2", title: "分配任务", desc: "指定人员\n邮件通知\n防重复提交", color: "8B5CF6" },
  { num: "3", title: "会签审核", desc: "多人会签\n意见汇总\n铃铛提醒", color: "EC4899" },
  { num: "4", title: "定稿确认", desc: "内容修改\n版本保存\n通知审批", color: "F59E0B" },
  { num: "5", title: "审批决策", desc: "通过/否决\n否决回退\n流程追踪", color: "10B981" },
  { num: "6", title: "签订归档", desc: "电子签名\n合同PDF\n到期提醒", color: "065A82" }
];

steps.forEach(function(step, i) {
  let x = 0.3 + i * 1.6;
  s4.addShape(pres.shapes.OVAL, { x: x + 0.45, y: 1.2, w: 0.7, h: 0.7, fill: { color: step.color } });
  s4.addText(step.num, { x: x + 0.45, y: 1.2, w: 0.7, h: 0.7, fontSize: 20, fontFace: "Arial", color: TEXT_LIGHT, bold: true, align: "center", valign: "middle" });
  if (i < steps.length - 1) {
    s4.addShape(pres.shapes.LINE, { x: x + 1.15, y: 1.55, w: 0.9, h: 0, line: { color: "94A3B8", width: 2, dashType: "dash" } });
  }
  s4.addText(step.title, { x: x, y: 2.1, w: 1.6, h: 0.4, fontSize: 13, fontFace: "Arial", color: step.color, bold: true, align: "center" });
  s4.addText(step.desc, { x: x, y: 2.5, w: 1.6, h: 1.2, fontSize: 10, fontFace: "Calibri", color: "64748B", align: "center" });
});

s4.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 3.9, w: 9, h: 1.2, fill: { color: ACCENT }, shadow: makeShadow() });
s4.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 3.9, w: 0.06, h: 1.2, fill: { color: "DC2626" } });
s4.addText("状态流转规则", { x: 0.8, y: 3.95, w: 8.5, h: 0.35, fontSize: 13, fontFace: "Arial", color: "DC2626", bold: true });
s4.addText("起草(1) → 会签完成(2) → 定稿完成(3) → 审批完成(4) → 签订完成(5)  |  审批否决时回退到会签完成(2)，需重新定稿", { x: 0.8, y: 4.35, w: 8.5, h: 0.5, fontSize: 11, fontFace: "Calibri", color: "475569" });

// Slide 5: Innovation Features
let s5 = pres.addSlide();
s5.background = { color: BG_LIGHT };
s5.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.9, fill: { color: PRIMARY } });
s5.addText("创新功能", { x: 0.5, y: 0.15, w: 9, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: TEXT_LIGHT, bold: true });

let innovations = [
  { title: "AI智能辅助", desc: "基于Ollama本地大模型，提供合同审查和智能起草功能，确保合同数据不出本机，兼顾智能化与安全性", icon: "AI", color: "7C3AED" },
  { title: "电子签名系统", desc: "支持手写签名板和签名图片上传，签名自动插入合同标记位置，可配置是否预放签名", icon: "ES", color: "DC2626" },
  { title: "全流程邮件通知", desc: "分配/定稿/审批/签订各环节自动发送邮件通知，支持附件发送和无文件纯文本模式，带重试机制", icon: "EM", color: "2563EB" },
  { title: "合同到期提醒", desc: "30/15/7/1天多级到期提醒，定时检查待办任务并发送邮件，确保合同不遗漏", icon: "RM", color: "EA580C" }
];

innovations.forEach(function(item, i) {
  let col = i % 2;
  let row = Math.floor(i / 2);
  let x = 0.5 + col * 4.8;
  let y = 1.2 + row * 2.1;
  s5.addShape(pres.shapes.RECTANGLE, { x: x, y: y, w: 4.5, h: 1.8, fill: { color: ACCENT }, shadow: makeShadow() });
  s5.addShape(pres.shapes.OVAL, { x: x + 0.2, y: y + 0.3, w: 0.6, h: 0.6, fill: { color: item.color } });
  s5.addText(item.icon, { x: x + 0.2, y: y + 0.3, w: 0.6, h: 0.6, fontSize: 12, fontFace: "Arial", color: TEXT_LIGHT, bold: true, align: "center", valign: "middle" });
  s5.addText(item.title, { x: x + 1.0, y: y + 0.15, w: 3.2, h: 0.4, fontSize: 16, fontFace: "Arial", color: item.color, bold: true });
  s5.addText(item.desc, { x: x + 1.0, y: y + 0.6, w: 3.2, h: 1.0, fontSize: 11, fontFace: "Calibri", color: "475569" });
});

// Slide 6: System Security & Permissions
let s6 = pres.addSlide();
s6.background = { color: BG_LIGHT };
s6.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.9, fill: { color: PRIMARY } });
s6.addText("安全与权限体系", { x: 0.5, y: 0.15, w: 9, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: TEXT_LIGHT, bold: true });

s6.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 1.2, w: 4.2, h: 3.8, fill: { color: ACCENT }, shadow: makeShadow() });
s6.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 1.2, w: 0.06, h: 3.8, fill: { color: "DC2626" } });
s6.addText("RBAC权限模型", { x: 0.8, y: 1.35, w: 3.7, h: 0.4, fontSize: 16, fontFace: "Arial", color: "DC2626", bold: true });
s6.addText([
  { text: "用户 → 角色 → 功能权限 三级模型", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "12项功能权限(F01-F12)精细控制", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "用户注册需管理员审核", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "菜单按权限动态显示", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "合同详情按权限控制可见性", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } }
], { x: 0.8, y: 1.85, w: 3.7, h: 2.8 });

s6.addShape(pres.shapes.RECTANGLE, { x: 5.3, y: 1.2, w: 4.2, h: 3.8, fill: { color: ACCENT }, shadow: makeShadow() });
s6.addShape(pres.shapes.RECTANGLE, { x: 5.3, y: 1.2, w: 0.06, h: 3.8, fill: { color: "2563EB" } });
s6.addText("安全保障措施", { x: 5.6, y: 1.35, w: 3.7, h: 0.4, fontSize: 16, fontFace: "Arial", color: "2563EB", bold: true });
s6.addText([
  { text: "操作日志完整记录（含IP地址）", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "合同版本历史自动保存", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "SSL加密邮件传输", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "AI数据不出本机（Ollama本地部署）", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } },
  { text: "分配任务防重复提交（前后端双重校验）", options: { bullet: true, breakLine: true, fontSize: 12, color: TEXT_DARK } }
], { x: 5.6, y: 1.85, w: 3.7, h: 2.8 });

// Slide 7: Database Design
let s7 = pres.addSlide();
s7.background = { color: BG_LIGHT };
s7.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.9, fill: { color: PRIMARY } });
s7.addText("数据库设计", { x: 0.5, y: 0.15, w: 9, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: TEXT_LIGHT, bold: true });

let tables = [
  ["表名", "说明", "关键字段"],
  ["t_contract", "合同表", "num, name, customer, content, file_data"],
  ["t_contract_process", "流程操作表", "conNum, type, state, userName"],
  ["t_contract_state", "状态变更表", "conNum, type(1-5), time"],
  ["t_user", "用户表", "name, password, email, status"],
  ["t_role", "角色表", "name, functions(F01-F12)"],
  ["t_right", "权限关联表", "userName, roleName"],
  ["t_customer", "客户表", "num, name, address, tel"],
  ["t_contract_attachment", "附件表", "conNum, fileName, file_data"],
  ["t_contract_version", "版本表", "contract_num, version_no, content"],
  ["t_log", "日志表", "userName, content, ip_address"],
  ["t_settings", "设置表", "key_name, key_value"]
];

s7.addTable(tables, {
  x: 0.5, y: 1.1, w: 9,
  border: { pt: 0.5, color: "CBD5E1" },
  colW: [2.2, 2.0, 4.8],
  rowH: [0.35, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3],
  fontSize: 10,
  fontFace: "Calibri",
  autoPage: false
});

let headerOpts = { fill: { color: PRIMARY }, color: TEXT_LIGHT, bold: true, fontSize: 11 };
for (let c = 0; c < 3; c++) {
  // Header row styling is handled by addTable options
}

// Slide 8: Key Improvements
let s8 = pres.addSlide();
s8.background = { color: BG_LIGHT };
s8.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.9, fill: { color: PRIMARY } });
s8.addText("迭代优化与问题修复", { x: 0.5, y: 0.15, w: 9, h: 0.6, fontSize: 26, fontFace: "Arial Black", color: TEXT_LIGHT, bold: true });

let fixes = [
  { title: "铃铛通知实时刷新", desc: "所有操作页面完成后立即通知父窗口刷新待办数量，不再依赖60秒轮询", color: "3B82F6" },
  { title: "智能起草优化", desc: "改为模板加载+自动填入，不再调用AI模型，速度从分钟级提升至秒级", color: "8B5CF6" },
  { title: "电子签名设置", desc: "支持配置是否预放签名，不预放时提交后自动从内容中移除签名标签", color: "EC4899" },
  { title: "防重复分配", desc: "后端增加幂等性校验，同一合同无论点击多少次只分配一次", color: "10B981" },
  { title: "查询功能增强", desc: "合同查询支持起止时间+模糊搜索（名称+编号），流程查询支持多维度筛选", color: "F59E0B" },
  { title: "流程逻辑严谨", desc: "各操作页面按合同当前状态严格过滤，确保流程顺序正确不可跳跃", color: "065A82" }
];

fixes.forEach(function(item, i) {
  let col = i % 2;
  let row = Math.floor(i / 2);
  let x = 0.5 + col * 4.8;
  let y = 1.15 + row * 1.45;
  s8.addShape(pres.shapes.RECTANGLE, { x: x, y: y, w: 4.5, h: 1.2, fill: { color: ACCENT }, shadow: makeShadow() });
  s8.addShape(pres.shapes.RECTANGLE, { x: x, y: y, w: 0.06, h: 1.2, fill: { color: item.color } });
  s8.addText(item.title, { x: x + 0.2, y: y + 0.1, w: 4.1, h: 0.35, fontSize: 14, fontFace: "Arial", color: item.color, bold: true });
  s8.addText(item.desc, { x: x + 0.2, y: y + 0.5, w: 4.1, h: 0.6, fontSize: 11, fontFace: "Calibri", color: "475569" });
});

// Slide 9: Demo / System Screenshots
let s9 = pres.addSlide();
s9.background = { color: BG_LIGHT };
s9.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.9, fill: { color: PRIMARY } });
s9.addText("系统功能展示", { x: 0.5, y: 0.15, w: 9, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: TEXT_LIGHT, bold: true });

let features = [
  { title: "起草合同", desc: "模板加载、智能填入\n起止日期、电子签名", color: "3B82F6" },
  { title: "会签/审批/签订", desc: "多角色协同\n意见填写、状态流转", color: "8B5CF6" },
  { title: "待办任务中心", desc: "铃铛实时提醒\n待办分类展示", color: "EC4899" },
  { title: "流程看板", desc: "合同状态可视化\n进度一目了然", color: "10B981" },
  { title: "查询统计", desc: "多维度筛选\n合同/流程双查询", color: "F59E0B" },
  { title: "系统设置", desc: "邮件/AI/签名配置\n主题切换", color: "065A82" }
];

features.forEach(function(item, i) {
  let col = i % 3;
  let row = Math.floor(i / 3);
  let x = 0.5 + col * 3.1;
  let y = 1.2 + row * 2.1;
  s9.addShape(pres.shapes.RECTANGLE, { x: x, y: y, w: 2.8, h: 1.8, fill: { color: ACCENT }, shadow: makeShadow() });
  s9.addShape(pres.shapes.RECTANGLE, { x: x, y: y, w: 2.8, h: 0.06, fill: { color: item.color } });
  s9.addText(item.title, { x: x + 0.15, y: y + 0.15, w: 2.5, h: 0.4, fontSize: 15, fontFace: "Arial", color: item.color, bold: true });
  s9.addText(item.desc, { x: x + 0.15, y: y + 0.6, w: 2.5, h: 1.0, fontSize: 11, fontFace: "Calibri", color: "475569" });
});

// Slide 10: Summary & Thanks
let s10 = pres.addSlide();
s10.background = { color: BG_DARK };
s10.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 5.625, fill: { color: PRIMARY, transparency: 70 } });
s10.addText("总结与展望", { x: 1, y: 0.8, w: 8, h: 0.8, fontSize: 36, fontFace: "Arial Black", color: TEXT_LIGHT, bold: true, align: "center" });
s10.addShape(pres.shapes.LINE, { x: 3, y: 1.7, w: 4, h: 0, line: { color: "4FC3F7", width: 2 } });

s10.addText([
  { text: "已实现", options: { bold: true, fontSize: 16, color: "4FC3F7", breakLine: true } },
  { text: "合同全生命周期管理（起草→分配→会签→定稿→审批→签订）", options: { fontSize: 13, color: "B0BEC5", breakLine: true } },
  { text: "AI智能辅助、电子签名、邮件通知、到期提醒等创新功能", options: { fontSize: 13, color: "B0BEC5", breakLine: true } },
  { text: "RBAC权限模型、操作日志审计、版本控制等安全机制", options: { fontSize: 13, color: "B0BEC5", breakLine: true } },
  { text: "", options: { fontSize: 8, breakLine: true } },
  { text: "未来展望", options: { bold: true, fontSize: 16, color: "4FC3F7", breakLine: true } },
  { text: "引入更强大的AI模型提升智能化水平", options: { fontSize: 13, color: "B0BEC5", breakLine: true } },
  { text: "支持电子签章CA认证，满足法律效力要求", options: { fontSize: 13, color: "B0BEC5", breakLine: true } },
  { text: "移动端适配，支持手机端审批签订", options: { fontSize: 13, color: "B0BEC5", breakLine: true } }
], { x: 1.5, y: 2.0, w: 7, h: 3.0 });

s10.addText("感谢聆听", { x: 1, y: 4.8, w: 8, h: 0.5, fontSize: 20, fontFace: "Arial", color: "78909C", align: "center" });

const fs = require('fs');
pres.write('nodebuffer').then(function(buf) {
  fs.writeFileSync('D:/Java_IDEA/HeTongGuanLiXitong/doc/final_presentation.pptx', buf);
  console.log('PPT generated successfully! Size: ' + buf.length + ' bytes');
}).catch(function(err) { console.error('Error:', err); });