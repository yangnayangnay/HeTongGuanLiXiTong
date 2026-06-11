package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.User;
import com.contract.service.ContractService;
import com.contract.service.UserService;
import com.contract.util.EmailService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 分配合同面板（按权限过滤备选人员）
 * <p>
 * 该面板用于将起草完成的合同分配给相应的会签、审批、签订人员。
 * 分配是合同从"起草"状态进入后续流程的关键步骤，只有分配后
 * 相关人员才能在各自的待办列表中看到该合同。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示所有待分配的合同列表（状态为"起草"的合同）</li>
 *   <li>按权限过滤显示可选的人员列表：
 *     · 会签人员列表：只显示拥有F02(会签)权限的用户
 *     · 审批人员列表：只显示拥有F04(审批)权限的用户
 *     · 签订人员列表：只显示拥有F05(签订)权限的用户</li>
 *   <li>支持多选人员（一个合同可分配多个会签/审批/签订人）</li>
 *   <li>执行分配操作，创建对应的流程节点记录</li>
 * </ul>
 *
 * <p>业务说明：</p>
 * <ul>
 *   <li>只有管理员或具有分配权限的用户才能访问此面板</li>
 *   <li>会签、审批、签订三类人员必须全部指定才能完成分配</li>
 *   <li>人员列表按功能权限自动过滤，避免分配给无权限的人员</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ContractAssignPanel extends JPanel {
    // 待分配合同列表表格
    private JTable table;
    // 表格数据模型
    private DefaultTableModel tableModel;
    // 会签人员多选列表
    private JList<String> listCountersign;
    // 审批人员多选列表
    private JList<String> listApprove;
    // 签订人员多选列表
    private JList<String> listSign;
    // 合同业务服务类
    private ContractService contractService = new ContractService();
    // 用户业务服务类（用于查询用户和权限信息）
    private UserService userService = new UserService();

    /**
     * 构造方法：初始化分配合同面板
     */
    public ContractAssignPanel() {
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
        // 加载待分配合同数据
        loadData();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构采用左右分区设计：
     * - 中部(CENTER)：左侧 - 待分配合同列表表格
     * - 东部(EAST)：右侧 - 三列人员选择区（会签/审批/签订）
     * - 南部(SOUTH)：底部 - 确认分配按钮
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("分配合同");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 上半部分：待分配合同列表 =====
        JPanel topPanel = new JPanel(new BorderLayout());
        // 定义表格列名：合同编号、合同名称、客户、起草人、状态
        String[] columns = {"合同编号", "合同名称", "客户", "起草人", "状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }  // 表格不可编辑
        };
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        topPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // 刷新按钮
        JButton btnRefresh = new JButton("刷新列表");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnRefresh.addActionListener(e -> loadData());
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshPanel.add(btnRefresh);
        topPanel.add(refreshPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.CENTER);

        // ===== 下半部分：分配操作区（按权限过滤人员）=====
        // 使用GridLayout实现三列等宽布局
        JPanel assignPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        assignPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // 获取系统中所有用户列表
        List<User> allUsers = userService.findAll();

        // ---- 第1列：会签人员选择 ----
        // 只显示拥有F02(会签权限)功能的用户
        List<String> countersignNames = filterUsersByFunction(allUsers, "F02");
        JPanel p1 = new JPanel(new BorderLayout(5, 5));
        p1.add(new JLabel("会签人员 (会签权限):"), BorderLayout.NORTH);
        listCountersign = new JList<>(countersignNames.toArray(new String[0]));
        listCountersign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);  // 支持多选
        p1.add(new JScrollPane(listCountersign), BorderLayout.CENTER);
        assignPanel.add(p1);

        // ---- 第2列：审批人员选择 ----
        // 只显示拥有F04(审批权限)功能的用户
        List<String> approveNames = filterUsersByFunction(allUsers, "F04");
        JPanel p2 = new JPanel(new BorderLayout(5, 5));
        p2.add(new JLabel("审批人员 (审批权限):"), BorderLayout.NORTH);
        listApprove = new JList<>(approveNames.toArray(new String[0]));
        listApprove.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);  // 支持多选
        p2.add(new JScrollPane(listApprove), BorderLayout.CENTER);
        assignPanel.add(p2);

        // ---- 第3列：签订人员选择 ----
        // 只显示拥有F05(签订权限)功能的用户
        List<String> signNames = filterUsersByFunction(allUsers, "F05");
        JPanel p3 = new JPanel(new BorderLayout(5, 5));
        p3.add(new JLabel("签订人员 (签订权限):"), BorderLayout.NORTH);
        listSign = new JList<>(signNames.toArray(new String[0]));
        listSign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);  // 支持多选
        p3.add(new JScrollPane(listSign), BorderLayout.CENTER);
        assignPanel.add(p3);

        add(assignPanel, BorderLayout.EAST);

        // ===== 确认分配按钮 =====
        JButton btnAssign = new JButton("确认分配");
        btnAssign.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnAssign.setBackground(new Color(66, 133, 244));  // 蓝色背景
        btnAssign.setOpaque(true);
        btnAssign.setContentAreaFilled(true);
        btnAssign.setForeground(Color.BLACK);
        btnAssign.setFocusPainted(false);
        btnAssign.addActionListener(e -> doAssign());  // 点击后执行分配逻辑
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(btnAssign);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * 根据功能编号过滤用户列表
     * <p>
     * 遍历所有用户，检查每个用户是否拥有指定的功能权限，
     * 只返回拥有该功能的用户名称列表。此方法用于确保
     * 只有具备相应权限的用户才会出现在备选人员列表中。
     * </p>
     *
     * @param allUsers 系统中的所有用户列表
     * @param functionNum 功能编号（如"F02"表示会签权限、"F04"表示审批权限、"F05"表示签订权限）
     * @return 拥有指定功能的用户名称列表
     */
    private List<String> filterUsersByFunction(List<User> allUsers, String functionNum) {
        List<String> result = new ArrayList<>();
        for (User u : allUsers) {
            // 查询该用户拥有的所有功能权限集合
            Set<String> funcs = userService.getUserFunctions(u.getName());
            // 如果用户拥有目标功能权限，则将其加入结果列表
            if (funcs.contains(functionNum)) {
                result.add(u.getName());
            }
        }
        return result;
    }

    /**
     * 加载待分配的合同列表
     * <p>
     * 查询所有处于"起草"状态且尚未分配流程人员的合同。
     * 这些合同已经完成了起草步骤，但还没有被分配给
     * 会签、审批、签订人员，因此无法进入后续流程。
     * </p>
     */
    private void loadData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询所有未分配的合同（状态为"起草"）
        List<Contract> contracts = contractService.getUnassignedContracts();
        for (Contract c : contracts) {
            // 将合同信息添加到表格行中
            tableModel.addRow(new Object[]{c.getNum(), c.getName(), c.getCustomer(), c.getUserName(), "起草"});
        }
    }

    /**
     * 执行合同分配操作
     * <p>
     * 处理流程：
     * <ol>
     *   <li>检查是否选中了要分配的合同</li>
     *   <li>获取三组人员选择（会签/审批/签订）</li>
     *   <li>校验每组人员都必须至少选择一人</li>
     *   <li>调用服务层执行分配操作，为合同创建对应的流程节点</li>
     *   <li>显示操作结果并刷新列表</li>
     * </ol>
     * </p>
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>必须先选择一个待分配的合同</li>
     *   <li>会签、审批、签订人员都必须至少指定一人</li>
     *   <li>分配成功后合同将从待分配列表消失</li>
     * </ul>
     */
    private void doAssign() {
        // 获取选中的表格行索引
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要分配的合同！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 获取选中行的合同编号（第一列）
        String conNum = (String) tableModel.getValueAt(row, 0);

        // 获取三组已选中的人员列表
        List<String> countersignUsers = listCountersign.getSelectedValuesList();  // 会签人员
        List<String> approveUsers = listApprove.getSelectedValuesList();          // 审批人员
        List<String> signUsers = listSign.getSelectedValuesList();               // 签订人员

        // 校验：三类人员必须全部指定，不能有空缺
        if (countersignUsers.isEmpty() || approveUsers.isEmpty() || signUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "会签、审批、签订人员必须全部指定！\n注意：列表中只显示了具有对应权限的人员。",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 调用服务层执行分配操作（传入合同编号和三组人员名单）
        if (contractService.assignContract(conNum, countersignUsers, approveUsers, signUsers)) {
            // 分配成功提示
            JOptionPane.showMessageDialog(this, "分配成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

            // 发送任务通知邮件给被分配的人员
            // 遍历会签人员列表，为每个会签人员发送"会签"任务通知邮件
            for (String countersignUser : countersignUsers) {
                User u = userService.findByName(countersignUser);
                if (u != null && u.getEmail() != null) {
                    EmailService.sendTaskNotification(u.getEmail(), countersignUser, conNum,
                        tableModel.getValueAt(row, 1).toString(), "会签");
                }
            }
            // 遍历审批人员列表，为每个审批人员发送"审批"任务通知邮件
            for (String approveUser : approveUsers) {
                User u = userService.findByName(approveUser);
                if (u != null && u.getEmail() != null) {
                    EmailService.sendTaskNotification(u.getEmail(), approveUser, conNum,
                        tableModel.getValueAt(row, 1).toString(), "审批");
                }
            }
            // 遍历签订人员列表，为每个签订人员发送"签订"任务通知邮件
            for (String signUser : signUsers) {
                User u = userService.findByName(signUser);
                if (u != null && u.getEmail() != null) {
                    EmailService.sendTaskNotification(u.getEmail(), signUser, conNum,
                        tableModel.getValueAt(row, 1).toString(), "签订");
                }
            }

            // 刷新列表，移除已分配的合同
            loadData();
        } else {
            // 分配失败提示
            JOptionPane.showMessageDialog(this, "分配失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
