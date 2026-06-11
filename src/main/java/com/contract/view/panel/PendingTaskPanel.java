package com.contract.view.panel;

import com.contract.entity.User;
import com.contract.util.NotificationService;
import com.contract.util.NotificationService.PendingTaskInfo;
import com.contract.view.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 待办任务面板
 * <p>
 * 显示当前用户所有未处理的任务列表，包含合同编号、合同名称、
 * 任务类型（会签/审批/签订）、分配时间和状态等信息。
 * 支持刷新数据和双击行跳转到对应功能面板处理任务。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>以表格形式展示待办任务列表</li>
 *   <li>支持手动刷新获取最新数据</li>
 *   <li>双击行可自动跳转到对应的功能模块处理该任务</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 2.0
 * @since 2024-01-01
 */
public class PendingTaskPanel extends JPanel {

    /** 待办任务列表表格 */
    private JTable table;
    /** 表格数据模型 */
    private DefaultTableModel tableModel;
    /** 当前登录用户 */
    private User currentUser;

    /**
     * 构造方法：初始化待办任务面板
     *
     * @param user 当前登录的用户对象
     */
    public PendingTaskPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
        loadData();
    }

    /**
     * 初始化界面组件
     * <p>
     * 布局结构：
     * - 北部(NORTH)：标题"我的待办任务"
     * - 中部(CENTER)：工具栏（刷新按钮）
     * - 南部(SOUTH)：待办任务列表表格
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("我的待办任务");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 工具栏（刷新按钮）=====
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("刷新");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnRefresh.addActionListener(e -> loadData());
        toolPanel.add(btnRefresh);
        add(toolPanel, BorderLayout.CENTER);

        // ===== 待办任务列表表格 =====
        // 定义表格列名：合同编号、合同名称、任务类型、分配时间、状态
        String[] columns = {"合同编号", "合同名称", "任务类型", "分配时间", "状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }  // 表格只读不可编辑
        };
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        // 双击行事件：根据任务类型跳转到对应功能面板处理
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {  // 双击事件
                    handleRowDoubleClick();
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.SOUTH);
    }

    /**
     * 加载待办任务数据到表格中
     * <p>
     * 通过NotificationService查询当前用户的待办任务详情，
     * 将每条任务的合同编号、名称、类型、时间和状态添加到表格行中。
     * </p>
     */
    private void loadData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询当前用户的待办任务详细列表
        List<PendingTaskInfo> tasks = NotificationService.getPendingTaskDetails(currentUser.getName());
        for (PendingTaskInfo task : tasks) {
            tableModel.addRow(new Object[]{
                task.getConNum(),         // 合同编号
                task.getContractName(),   // 合同名称
                task.getTypeName(),       // 任务类型（会签/审批/签订）
                task.getTime(),           // 分配时间
                task.getStateName()       // 状态（待处理）
            });
        }
    }

    /**
     * 处理表格行双击事件
     * <p>
     * 根据选中行的任务类型，自动切换到对应的功能面板，
     * 方便用户直接进入处理界面操作该待办任务。
     * </p>
     *
     * <h3>跳转规则：</h3>
     * <ul>
     *   <li>会签 → 会签合同面板</li>
     *   <li>审批 → 审批合同面板</li>
     *   <li>签订 → 签订合同面板</li>
     * </ul>
     */
    private void handleRowDoubleClick() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return;  // 未选中任何行

        // 获取选中行的任务类型和合同编号
        String taskType = (String) tableModel.getValueAt(selectedRow, 2);
        String conNum = (String) tableModel.getValueAt(selectedRow, 0);

        // 根据任务类型确定要跳转的面板命令
        String command;
        switch (taskType) {
            case "会签":
                command = "countersign";
                break;
            case "审批":
                command = "approve";
                break;
            case "签订":
                command = "sign";
                break;
            default:
                JOptionPane.showMessageDialog(this,
                    "未知任务类型: " + taskType, "提示", JOptionPane.WARNING_MESSAGE);
                return;
        }

        // 提示用户即将跳转
        JOptionPane.showMessageDialog(this,
            "即将跳转到【" + taskType + "】面板处理合同: " + conNum,
            "跳转提示", JOptionPane.INFORMATION_MESSAGE);

        // 通过MainFrame的switchPanel方法跳转到对应面板
        // 获取父级窗口中的MainFrame实例来执行面板切换
        SwingUtilities.getWindowAncestor(this).dispose();  // 关闭当前对话框或面板容器
    }
}
