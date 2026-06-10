package com.contract.view.panel;

import com.contract.entity.Log;
import com.contract.service.LogService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 日志管理面板
 * <p>
 * 该面板用于查看系统操作日志记录。系统会自动记录用户的关键操作，
 * 如登录、合同起草/会签/审批/签订、用户管理等行为。
 * 管理员可以通过此面板追踪系统的使用情况和操作历史。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示所有系统日志的列表表格</li>
 *   <li>每条日志包含：ID、操作人、操作内容、操作时间</li>
 *   <li>支持刷新获取最新日志数据</li>
 * </ul>
 *
 * <p>业务说明：</p>
 * <ul>
 *   <li>日志由系统自动生成，用户无法手动添加或修改</li>
 *   <li>日志按时间倒序排列（最新的在最前面）</li>
 *   <li>日志用于审计追踪和安全监控</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class LogPanel extends JPanel {
    // 日志列表表格
    private JTable table;
    // 表格数据模型
    private DefaultTableModel tableModel;
    // 日志业务服务类
    private LogService logService = new LogService();

    /**
     * 构造方法：初始化日志管理面板
     */
    public LogPanel() {
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
        // 加载所有日志数据
        loadData();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构：
     * - 北部(NORTH)：标题"日志管理"
     * - 中部(CENTER)：工具栏（刷新按钮）
     * - 南部(SOUTH)：日志列表表格
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("日志管理");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 工具栏（刷新按钮）=====
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("刷新");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnRefresh.addActionListener(e -> loadData());  // 点击后重新加载日志数据
        toolPanel.add(btnRefresh);
        add(toolPanel, BorderLayout.CENTER);

        // ===== 日志列表表格 =====
        // 定义表格列名：ID、操作人、操作内容、操作时间
        String[] columns = {"ID", "操作人", "操作内容", "操作时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }  // 表格不可编辑（只读）
        };
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));  // 设置表格字体
        table.setRowHeight(28);  // 设置行高以提升可读性
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));  // 表头加粗
        add(new JScrollPane(table), BorderLayout.SOUTH);
    }

    /**
     * 加载所有日志数据到表格中
     * <p>
     * 查询数据库中的所有日志记录，按时间顺序显示在表格中。
     * 每条日志记录包含：
     * <ul>
     *   <li>ID：日志记录的唯一标识符（数据库主键）</li>
     *   <li>操作人：执行该操作的登录用户名</li>
     *   <li>操作内容：对所执行操作的文字描述</li>
     *   <li>操作时间：操作发生的精确时间（格式：yyyy-MM-dd HH:mm:ss）</li>
     * </ul>
     * </p>
     */
    private void loadData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询所有日志记录
        List<Log> logs = logService.findAll();
        for (Log l : logs) {
            // 将每条日志信息添加到表格行中
            // 操作时间格式化为"yyyy-MM-dd HH:mm:ss"格式
            tableModel.addRow(new Object[]{
                l.getId(),                                    // 日志ID
                l.getUserName(),                               // 操作人姓名
                l.getContent(),                                // 操作内容描述
                l.getTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(l.getTime()) : ""  // 格式化后的操作时间
            });
        }
    }
}
