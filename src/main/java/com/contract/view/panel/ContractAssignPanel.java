package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.entity.User;
import com.contract.service.ContractService;
import com.contract.service.UserService;
import com.contract.util.EmailService;
import com.contract.util.FileLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 分配合同面板（按权限过滤备选人员，复选框多选）
 * <p>
 * 该面板用于将起草完成的合同分配给相应的会签、审批、签订人员。
 * 使用JCheckBox复选框实现直观的多选功能。
 * </p>
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
    // 会签人员复选框列表
    private java.util.List<JCheckBox> countersignCheckBoxes = new ArrayList<>();
    // 审批人员复选框列表
    private java.util.List<JCheckBox> approveCheckBoxes = new ArrayList<>();
    // 签订人员复选框列表
    private java.util.List<JCheckBox> signCheckBoxes = new ArrayList<>();
    // 合同业务服务类
    private ContractService contractService = new ContractService();
    // 用户业务服务类
    private UserService userService = new UserService();

    /**
     * 构造方法：初始化分配合同面板
     */
    public ContractAssignPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
        loadData();
    }

    /**
     * 初始化用户界面组件
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("分配合同");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 上半部分：待分配合同列表 =====
        JPanel topPanel = new JPanel(new BorderLayout());
        String[] columns = {"合同编号", "合同名称", "客户", "起草人", "状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        topPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("刷新列表");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnRefresh.addActionListener(e -> loadData());
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshPanel.add(btnRefresh);
        topPanel.add(refreshPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.CENTER);

        // ===== 下半部分：分配操作区（按权限过滤人员，复选框多选）=====
        JPanel assignPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        assignPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        List<User> allUsers = userService.findAll();

        // ---- 第1列：会签人员选择（复选框）----
        List<String> countersignNames = filterUsersByFunction(allUsers, "F02");
        assignPanel.add(createCheckBoxColumn("会签人员 (会签权限):", countersignNames, countersignCheckBoxes));

        // ---- 第2列：审批人员选择（复选框）----
        List<String> approveNames = filterUsersByFunction(allUsers, "F04");
        assignPanel.add(createCheckBoxColumn("审批人员 (审批权限):", approveNames, approveCheckBoxes));

        // ---- 第3列：签订人员选择（复选框）----
        List<String> signNames = filterUsersByFunction(allUsers, "F05");
        assignPanel.add(createCheckBoxColumn("签订人员 (签订权限):", signNames, signCheckBoxes));

        add(assignPanel, BorderLayout.EAST);

        // ===== 确认分配按钮 =====
        JButton btnAssign = new JButton("确认分配");
        btnAssign.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnAssign.setBackground(new Color(66, 133, 244));
        btnAssign.setOpaque(true);
        btnAssign.setContentAreaFilled(true);
        btnAssign.setForeground(Color.WHITE);
        btnAssign.setFocusPainted(false);
        btnAssign.addActionListener(e -> doAssign());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(btnAssign);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * 创建带复选框的人员选择列
     */
    private JPanel createCheckBoxColumn(String title, List<String> names, List<JCheckBox> checkBoxList) {
        JPanel column = new JPanel(new BorderLayout(5, 5));
        column.add(new JLabel(title), BorderLayout.NORTH);

        // 全选/取消全选按钮
        JPanel selectAllPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        JButton btnSelectAll = new JButton("全选");
        btnSelectAll.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        JButton btnDeselectAll = new JButton("取消全选");
        btnDeselectAll.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        selectAllPanel.add(btnSelectAll);
        selectAllPanel.add(btnDeselectAll);
        column.add(selectAllPanel, BorderLayout.SOUTH);

        // 复选框面板
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        checkBoxList.clear();
        for (String name : names) {
            JCheckBox cb = new JCheckBox(name);
            cb.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            checkBoxPanel.add(cb);
            checkBoxList.add(cb);
        }
        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setPreferredSize(new Dimension(160, 200));
        column.add(scrollPane, BorderLayout.CENTER);

        // 全选/取消全选事件
        btnSelectAll.addActionListener(e -> {
            for (JCheckBox cb : checkBoxList) cb.setSelected(true);
        });
        btnDeselectAll.addActionListener(e -> {
            for (JCheckBox cb : checkBoxList) cb.setSelected(false);
        });

        return column;
    }

    /**
     * 获取复选框列表中选中的用户名
     */
    private List<String> getSelectedNames(List<JCheckBox> checkBoxes) {
        List<String> selected = new ArrayList<>();
        for (JCheckBox cb : checkBoxes) {
            if (cb.isSelected()) {
                selected.add(cb.getText());
            }
        }
        return selected;
    }

    /**
     * 根据功能编号过滤用户列表
     */
    private List<String> filterUsersByFunction(List<User> allUsers, String functionNum) {
        List<String> result = new ArrayList<>();
        for (User u : allUsers) {
            Set<String> funcs = userService.getUserFunctions(u.getName());
            if (funcs.contains(functionNum)) {
                result.add(u.getName());
            }
        }
        return result;
    }

    /**
     * 加载待分配的合同列表
     */
    private void loadData() {
        tableModel.setRowCount(0);
        List<Contract> contracts = contractService.getUnassignedContracts();
        for (Contract c : contracts) {
            tableModel.addRow(new Object[]{c.getNum(), c.getName(), c.getCustomer(), c.getUserName(), "起草"});
        }
    }

    /**
     * 执行合同分配操作
     */
    private void doAssign() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要分配的合同！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String conNum = (String) tableModel.getValueAt(row, 0);

        // 从复选框获取选中人员
        List<String> countersignUsers = getSelectedNames(countersignCheckBoxes);
        List<String> approveUsers = getSelectedNames(approveCheckBoxes);
        List<String> signUsers = getSelectedNames(signCheckBoxes);

        if (countersignUsers.isEmpty() || approveUsers.isEmpty() || signUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "会签、审批、签订人员必须全部指定！\n请勾选对应人员（支持勾选多人）。",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (contractService.assignContract(conNum, countersignUsers, approveUsers, signUsers)) {
            FileLogger.info("ContractAssignPanel", "doAssign", "分配合同成功: contract=" + conNum + ", 会签=" + countersignUsers.size() + "人, 审批=" + approveUsers.size() + "人, 签订=" + signUsers.size() + "人");
            JOptionPane.showMessageDialog(this, "分配成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

            // 发送任务通知邮件
            Contract contract = contractService.findByNum(conNum);
            byte[] fileData = contract != null ? contract.getFileData() : null;
            String fileName = contract != null ? contract.getFileName() : null;
            String contractName = tableModel.getValueAt(row, 1).toString();

            for (String countersignUser : countersignUsers) {
                User u = userService.findByName(countersignUser);
                if (u != null && u.getEmail() != null) {
                    if (fileData != null && fileData.length > 0) {
                        EmailService.sendTaskNotificationWithAttachment(u.getEmail(), countersignUser, conNum,
                            contractName, "会签", fileData, fileName);
                    } else {
                        EmailService.sendTaskNotification(u.getEmail(), countersignUser, conNum,
                            contractName, "会签");
                    }
                }
            }
            for (String approveUser : approveUsers) {
                User u = userService.findByName(approveUser);
                if (u != null && u.getEmail() != null) {
                    if (fileData != null && fileData.length > 0) {
                        EmailService.sendTaskNotificationWithAttachment(u.getEmail(), approveUser, conNum,
                            contractName, "审批", fileData, fileName);
                    } else {
                        EmailService.sendTaskNotification(u.getEmail(), approveUser, conNum,
                            contractName, "审批");
                    }
                }
            }
            for (String signUser : signUsers) {
                User u = userService.findByName(signUser);
                if (u != null && u.getEmail() != null) {
                    if (fileData != null && fileData.length > 0) {
                        EmailService.sendTaskNotificationWithAttachment(u.getEmail(), signUser, conNum,
                            contractName, "签订", fileData, fileName);
                    } else {
                        EmailService.sendTaskNotification(u.getEmail(), signUser, conNum,
                            contractName, "签订");
                    }
                }
            }

            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "分配失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
