package com.contract.view.panel;

import com.contract.entity.User;
import com.contract.entity.Role;
import com.contract.entity.Right;
import com.contract.service.UserService;
import com.contract.service.RoleService;
import com.contract.service.RightService;
import com.contract.util.FileLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理面板（含待审核用户审核功能）
 * <p>
 * 该面板用于管理系统中的用户账号，提供完整的用户管理功能，
 * 包括待审核用户的审批/拒绝操作、已有用户的增删改查、以及角色分配功能。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li><b>待审核区域</b>：显示注册申请待审核的用户列表，支持通过/拒绝操作</li>
 *   <li><b>已有用户区域</b>：显示所有已存在的用户列表
 *     · 添加新用户（可同时分配角色）</li>
 *     · 修改用户信息（用户名、密码）</li>
 *     · 删除用户（保护admin账户不被删除）</li>
 *     · 分配角色（调用权限管理面板）</li>
 * </ul>
 *
 * <p>业务说明：</p>
 * <ul>
 *   <li>新用户注册后状态为"待审核"，需管理员审批后才能登录</li>
 *   <li>用户状态包括：待审核(0)、已通过(1)、已拒绝(2)</li>
 *   <li>管理员账户(admin)受保护，不允许删除</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class UserManagePanel extends JPanel {
    // 已有用户列表表格
    private JTable table;
    // 已有用户表格数据模型
    private DefaultTableModel tableModel;
    // 待审核用户列表表格
    private JTable pendingTable;
    // 待审核用户表格数据模型
    private DefaultTableModel pendingModel;
    // 用户业务服务类
    private UserService userService = new UserService();
    // 角色业务服务类（用于获取角色列表和分配角色）
    private RoleService roleService = new RoleService();
    // 权限业务服务类（用于管理用户-角色关联关系）
    private RightService rightService = new RightService();
    // 当前登录用户信息（用于权限控制）
    private com.contract.entity.User currentUser;

    /**
     * 构造方法：初始化用户管理面板
     *
     * @param user 当前登录的用户对象
     */
    public UserManagePanel(com.contract.entity.User user) {
        this.currentUser = user;
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
        // 加载已有用户数据
        loadData();
        // 加载待审核用户数据
        loadPendingData();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构采用上下分区设计：
     * - 上半部分：待审核用户区域（橙色边框突出显示）
     * - 下半部分：已有用户列表区域（蓝色边框）
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("用户管理");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        // 使用BoxLayout实现垂直排列的两个区域
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // ===== 待审核用户区域（上半部分）=====
        JPanel pendingSection = createPendingSection();
        mainPanel.add(pendingSection);

        // ===== 已有用户列表区域（下半部分）=====
        JPanel userSection = createUserSection();
        mainPanel.add(userSection);

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * 创建待审核用户区域
     * <p>
     * 构建一个带橙色边框的面板，包含：
     * - 操作按钮栏：通过（绿色）、拒绝（红色）、刷新
     * - 待审核用户表格：显示ID、用户名、状态
     * </p>
     *
     * @return 配置好的待审核用户区域面板
     */
    private JPanel createPendingSection() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        // 设置橙色边框和标题，突出显示待处理事项
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 152, 0), 1),
            " 待审核用户 ", 0, 0,
            new Font("微软雅黑", Font.BOLD, 13), new Color(255, 152, 0)));

        // 操作按钮行
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // 通过按钮（绿色背景）
        JButton btnApprove = new JButton("通过");
        btnApprove.setBackground(new Color(52, 168, 83));  // 绿色表示正向操作
        btnApprove.setOpaque(true);
        btnApprove.setContentAreaFilled(true);
        btnApprove.setForeground(Color.BLACK);
        btnApprove.setFocusPainted(false);
        btnApprove.addActionListener(e -> doApprove());  // 点击后执行通过操作
        toolPanel.add(btnApprove);

        // 拒绝按钮（红色背景）
        JButton btnReject = new JButton("拒绝");
        btnReject.setBackground(new Color(234, 67, 53));  // 红色表示负向操作
        btnReject.setOpaque(true);
        btnReject.setForeground(Color.BLACK);
        btnReject.setFocusPainted(false);
        btnReject.addActionListener(e -> doReject());  // 点击后执行拒绝操作
        toolPanel.add(btnReject);

        // 刷新按钮
        JButton btnRefreshPending = new JButton("刷新");
        btnRefreshPending.addActionListener(e -> loadPendingData());  // 点击后刷新待审核列表
        toolPanel.add(btnRefreshPending);

        panel.add(toolPanel, BorderLayout.NORTH);

        // 待审核用户表格
        String[] pendingCols = {"ID", "用户名", "状态"};
        pendingModel = new DefaultTableModel(pendingCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }  // 表格不可编辑
        };
        pendingTable = new JTable(pendingModel);
        pendingTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        pendingTable.setRowHeight(28);  // 设置行高以提升可读性
        pendingTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));  // 表头加粗
        panel.add(new JScrollPane(pendingTable), BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建已有用户列表区域
     * <p>
     * 构建一个带蓝色边框的面板，包含：
     * - 操作按钮栏：添加用户、修改、删除、分配角色、刷新
     * - 已有用户表格：显示ID、用户名、密码、状态
     * </p>
     *
     * @return 配置好的已有用户列表区域面板
     */
    private JPanel createUserSection() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        // 设置蓝色边框和标题
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
            " 已有用户列表 ", 0, 0,
            new Font("微软雅黑", Font.BOLD, 13), new Color(70, 130, 180)));

        // 操作按钮面板
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // 添加用户按钮（绿色背景）
        JButton btnAdd = new JButton("添加用户");
        btnAdd.setBackground(new Color(52, 168, 83));  // 绿色表示新增操作
        btnAdd.setOpaque(true);
        btnAdd.setContentAreaFilled(true);
        btnAdd.setForeground(Color.BLACK);
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> showAddDialog());  // 点击后弹出添加对话框
        toolPanel.add(btnAdd);

        // 修改按钮
        JButton btnEdit = new JButton("修改");
        btnEdit.addActionListener(e -> showEditDialog());  // 点击后弹出修改对话框
        toolPanel.add(btnEdit);

        // 删除按钮（红色背景）
        JButton btnDelete = new JButton("删除");
        btnDelete.setBackground(new Color(234, 67, 53));  // 红色表示危险操作
        btnDelete.setOpaque(true);
        btnDelete.setForeground(Color.BLACK);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> doDelete());  // 点击后执行删除
        toolPanel.add(btnDelete);

        // 分配角色按钮
        JButton btnAssignRole = new JButton("分配角色");
        btnAssignRole.addActionListener(e -> showAssignRoleDialog());  // 点击后打开权限管理面板
        toolPanel.add(btnAssignRole);

        // 刷新按钮
        JButton btnRefresh = new JButton("刷新");
        btnRefresh.addActionListener(e -> loadData());  // 点击后刷新用户列表
        toolPanel.add(btnRefresh);

        panel.add(toolPanel, BorderLayout.NORTH);

        // 已有用户表格（含角色列）
        String[] columns = {"ID", "用户名", "密码", "状态", "角色"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }  // 表格不可编辑
        };
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    /**
     * 加载待审核用户数据到表格中
     * <p>
     * 查询所有状态为"待审核"(STATUS_PENDING)的用户记录。
     * 这些用户已经完成了注册流程，但尚未获得管理员批准，
     * 因此暂时无法登录系统。
     * </p>
     */
    private void loadPendingData() {
        // 清空表格现有数据
        pendingModel.setRowCount(0);
        // 查询所有待审核用户
        List<User> pendingUsers = userService.findPendingUsers();
        for (User u : pendingUsers) {
            // 将待审核用户信息添加到表格（状态固定显示"待审核"）
            pendingModel.addRow(new Object[]{u.getId(), u.getName(), "待审核"});
        }
    }

    /**
     * 加载已有用户数据到表格中
     * <p>
     * 查询数据库中的所有用户记录（不分状态），
     * 并将状态码转换为中文描述显示在表格中。
     * </p>
     */
    private void loadData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询所有用户
        List<User> users = userService.findAll();
        for (User u : users) {
            // 将状态码转换为中文描述
            String statusText;
            switch (u.getStatus()) {
                case UserService.STATUS_PENDING:   statusText = "待审核"; break;   // 0-待审核
                case UserService.STATUS_APPROVED:  statusText = "已通过"; break;   // 1-已通过
                case UserService.STATUS_REJECTED:  statusText = "已拒绝"; break;   // 2-已拒绝
                default: statusText = "未知";  // 未知状态
            }
            // 查询该用户拥有的角色名称列表
            List<Right> rights = rightService.findByUserName(u.getName());
            StringBuilder roleNames = new StringBuilder();
            for (int i = 0; i < rights.size(); i++) {
                if (i > 0) roleNames.append(", ");
                roleNames.append(rights.get(i).getRoleName());
            }
            if (roleNames.length() == 0) {
                roleNames.append("未分配");
            }
            // 将用户信息添加到表格行（含角色列）
            tableModel.addRow(new Object[]{u.getId(), u.getName(), u.getPassword(), statusText, roleNames.toString()});
        }
    }

    /**
     * 审核通过操作
     * <p>
     * 将选中的待审核用户状态更新为"已通过"(STATUS_APPROVED)，
     * 使该用户可以正常登录系统。
     * 操作前会弹出确认对话框，防止误操作。
     * </p>
     */
    private void doApprove() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要通过的用户！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) pendingModel.getValueAt(row, 0);
        String name = (String) pendingModel.getValueAt(row, 1);
        // 弹出二次确认对话框
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要通过用户 " + name + " 的注册申请吗？\n通过后该用户即可正常登录。",
            "确认审核", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // 调用服务层执行通过操作
            if (userService.approveUser(id)) {
                FileLogger.info("UserManagePanel", "doApprove", "审核通过用户: name=" + name);
                JOptionPane.showMessageDialog(this, "已通过用户 " + name + " 的注册申请！", "成功",
                    JOptionPane.INFORMATION_MESSAGE);
                // 同时刷新两个列表（待审核列表和已有用户列表）
                loadPendingData();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 拒绝注册操作
     * <p>
     * 将选中的待审核用户状态更新为"已拒绝"(STATUS_REJECTED)，
     * 该用户将无法使用此账号登录系统。
     * 操作前会弹出确认对话框，防止误操作。
     * </p>
     */
    private void doReject() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要拒绝的用户！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) pendingModel.getValueAt(row, 0);
        String name = (String) pendingModel.getValueAt(row, 1);
        // 弹出二次确认对话框
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要拒绝用户 " + name + " 的注册申请吗？\n拒绝后该用户将无法登录。",
            "确认审核", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // 调用服务层执行拒绝操作
            if (userService.rejectUser(id)) {
                FileLogger.info("UserManagePanel", "doReject", "审核拒绝用户: name=" + name);
                JOptionPane.showMessageDialog(this, "已拒绝用户 " + name + " 的注册申请！", "成功",
                    JOptionPane.INFORMATION_MESSAGE);
                // 同时刷新两个列表
                loadPendingData();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 显示添加用户对话框
     * <p>
     * 弹出一个模态对话框，包含：
     * - 用户基本信息输入（用户名、密码、确认密码）
     * - 角色多选列表（可选，勾选后自动分配对应角色）
     * </p>
     * <p>校验规则：</p>
     * <ul>
     *   <li>用户名和密码不能为空</li>
     *   <li>两次密码必须一致</li>
     * </ul>
     */
    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加用户", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---- 上半部分：用户基本信息表单 ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = new JTextField(15);           // 用户名输入框
        JPasswordField txtPwd = new JPasswordField(15);      // 密码输入框
        JPasswordField txtPwd2 = new JPasswordField(15);     // 确认密码输入框

        // 第1行：用户名
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtName, gbc);

        // 第2行：密码
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("密码:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPwd, gbc);

        // 第3行：确认密码
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("确认密码:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPwd2, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // ---- 下半部分：角色选择（可选）----
        List<Role> roles = roleService.findAll();  // 获取所有可用角色
        JCheckBox[] checkBoxes = new JCheckBox[roles.size()];
        JPanel rolePanel = new JPanel(new GridLayout(0, 2, 5, 5));  // 两列网格布局
        rolePanel.setBorder(BorderFactory.createTitledBorder("分配角色（可不选）"));
        for (int i = 0; i < roles.size(); i++) {
            checkBoxes[i] = new JCheckBox(roles.get(i).getName());  // 为每个角色创建复选框
            rolePanel.add(checkBoxes[i]);
        }
        panel.add(new JScrollPane(rolePanel), BorderLayout.CENTER);

        // 保存按钮
        JButton btnSave = new JButton("保存");
        btnSave.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnSave.setBackground(new Color(52, 168, 83));  // 绿色背景
        btnSave.setOpaque(true);
        btnSave.setContentAreaFilled(true);
        btnSave.setForeground(Color.BLACK);
        btnSave.setFocusPainted(false);

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnSave);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // 保存按钮事件处理
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String pwd = new String(txtPwd.getPassword()).trim();
            String pwd2 = new String(txtPwd2.getPassword()).trim();

            // === 前端校验 ===
            if (name.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "用户名和密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pwd.equals(pwd2)) {
                JOptionPane.showMessageDialog(dialog, "两次密码不一致！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 构建用户实体对象并保存
            User user = new User();
            user.setName(name);
            user.setPassword(pwd);
            if (userService.addUser(user)) {
                FileLogger.info("UserManagePanel", "showAddDialog", "添加用户成功: name=" + name);
                // 如果选择了角色，自动为新用户分配
                List<String> selectedRoles = new ArrayList<>();
                for (int i = 0; i < roles.size(); i++) {
                    if (checkBoxes[i].isSelected()) {
                        selectedRoles.add(roles.get(i).getName());
                    }
                }
                if (!selectedRoles.isEmpty()) {
                    rightService.reassignRoles(name, selectedRoles);  // 批量分配角色
                }
                JOptionPane.showMessageDialog(dialog, "添加成功！" + (selectedRoles.isEmpty() ? "" : "\n已分配角色: " + selectedRoles),
                    "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();  // 刷新用户列表
            } else {
                JOptionPane.showMessageDialog(dialog, "添加失败，用户名可能已存在！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);  // 显示模态对话框
    }

    /**
     * 显示修改用户对话框
     * <p>
     * 弹出一个简单的模态对话框，允许修改用户名和密码。
     * 预填充选中用户的当前信息。
     * </p>
     */
    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要修改的用户！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        // 根据ID查找完整的用户对象
        User user = userService.findAll().stream().filter(u -> u.getId() == id).findFirst().orElse(null);
        if (user == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "修改用户", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 预填充当前值
        JTextField txtName = new JTextField(user.getName(), 15);
        JPasswordField txtPwd = new JPasswordField(user.getPassword(), 15);

        // 用户名输入行
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        panel.add(txtName, gbc);

        // 新密码输入行
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("新密码:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPwd, gbc);

        // 保存按钮
        JButton btnSave = new JButton("保存");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnSave, gbc);

        btnSave.addActionListener(e -> {
            user.setName(txtName.getText().trim());
            user.setPassword(new String(txtPwd.getPassword()).trim());
            if (userService.updateUser(user)) {
                JOptionPane.showMessageDialog(dialog, "修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();  // 刷新用户列表
            } else {
                JOptionPane.showMessageDialog(dialog, "修改失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * 执行删除用户操作
     * <p>
     * 处理流程：
     * <ol>
     *   <li>检查是否选中了要删除的用户</li>
     *   <li>特殊保护：禁止删除admin管理员账户</li>
     *   <li>弹出二次确认对话框</li>
     *   <li>确认后调用服务层执行删除</li>
     * </ol>
     * </p>
     */
    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的用户！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        // 保护管理员账户不被删除
        if ("admin".equals(name)) {
            JOptionPane.showMessageDialog(this, "不能删除管理员账户！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 弹出二次确认对话框
        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除用户 " + name + " 吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userService.deleteUser(id)) {
                FileLogger.info("UserManagePanel", "doDelete", "删除用户: name=" + name);
                JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadData();  // 刷新用户列表
            } else {
                JOptionPane.showMessageDialog(this, "删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 显示角色分配对话框
     * <p>
     * 打开权限管理面板(PermissionManagePanel)，以对话框形式展示，
     * 允许为选中的用户分配或调整角色。
     * </p>
     */
    private void showAssignRoleDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要分配角色的用户！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String userName = (String) tableModel.getValueAt(row, 1);
        // 创建权限管理面板并以对话框形式显示
        new com.contract.view.panel.PermissionManagePanel(userName, currentUser.getName()).showDialog(this, currentUser.getName());
        loadData();  // 关闭后刷新用户列表
    }
}
