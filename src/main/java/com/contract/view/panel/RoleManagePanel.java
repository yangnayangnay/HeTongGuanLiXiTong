package com.contract.view.panel;

import com.contract.entity.Function;
import com.contract.entity.Role;
import com.contract.service.RoleService;
import com.contract.dao.FunctionDao;
import com.contract.util.FileLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 角色管理面板
 * <p>
 * 该面板用于管理系统中的角色信息，提供完整的CRUD功能。
 * 角色是RBAC权限模型的核心组件，用于将一组功能权限打包，
 * 然后通过分配角色来批量授予用户相应的操作权限。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示所有角色的列表（ID、名称、描述、拥有的功能权限）</li>
 *   <li>添加新角色（同时选择该角色包含的功能权限）</li>
 *   <li>修改已有角色（可调整功能权限组合）</li>
 *   <li>删除角色（需二次确认）</li>
 * </ul>
 *
 * <p>业务说明：</p>
 * <ul>
 *   <li>角色的"功能权限"字段存储的是功能编号的逗号分隔字符串（如"F01,F02,F03"）</li>
 *   <li>功能权限来自Function表，定义了系统中所有可用的操作权限点</li>
 *   <li>删除角色前应确保没有用户正在使用该角色</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class RoleManagePanel extends JPanel {
    // 角色列表表格
    private JTable table;
    // 表格数据模型
    private DefaultTableModel tableModel;
    // 角色业务服务类
    private RoleService roleService = new RoleService();
    // 功能数据访问对象（用于获取系统中的所有功能列表）
    private FunctionDao functionDao = new FunctionDao();

    /**
     * 构造方法：初始化角色管理面板
     */
    public RoleManagePanel() {
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
        // 加载所有角色数据
        loadData();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构：
     * - 北部(NORTH)：标题"角色管理"
     * - 中部(CENTER)：工具栏（添加/修改/删除/刷新按钮）
     * - 南部(SOUTH)：角色列表表格
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("角色管理");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 操作按钮面板 =====
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // 添加角色按钮（绿色背景）
        JButton btnAdd = new JButton("添加角色");
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
        btnDelete.setContentAreaFilled(true);
        btnDelete.setForeground(Color.BLACK);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> doDelete());  // 点击后执行删除
        toolPanel.add(btnDelete);

        // 刷新按钮
        JButton btnRefresh = new JButton("刷新");
        btnRefresh.addActionListener(e -> loadData());  // 点击后刷新角色列表
        toolPanel.add(btnRefresh);

        add(toolPanel, BorderLayout.CENTER);

        // ===== 角色列表表格 =====
        // 定义表格列名：ID、角色名称、描述、功能权限
        String[] columns = {"ID", "角色名称", "描述", "功能权限"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }  // 表格不可编辑
        };
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(28);  // 设置行高以提升可读性
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));  // 表头加粗
        add(new JScrollPane(table), BorderLayout.SOUTH);
    }

    /**
     * 加载所有角色数据到表格中
     * <p>
     * 查询数据库中的所有角色记录，包括每个角色所拥有的功能权限。
     * 功能权限以逗号分隔的功能编号形式显示在表格中。
     * </p>
     */
    private void loadData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询所有角色
        List<Role> roles = roleService.findAll();
        for (Role r : roles) {
            // 将角色信息添加到表格行（包括功能权限字段）
            tableModel.addRow(new Object[]{r.getId(), r.getName(), r.getDescription(), r.getFunctions()});
        }
    }

    /**
     * 显示添加角色对话框
     * <p>
     * 弹出一个模态对话框，包含：
     * - 角色基本信息输入（角色名称、角色描述）
     * - 功能权限多选列表（勾选该角色应包含的功能权限）
     * </p>
     * <p>保存时：</p>
     * <ul>
     *   <li>校验角色名称不能为空</li>
     *   <li>将选中的功能编号拼接为逗号分隔字符串存储</li>
     * </ul>
     */
    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加角色", true);
        dialog.setSize(450, 400);  // 对话框尺寸
        dialog.setLocationRelativeTo(this);  // 居中显示

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---- 上半部分：角色基本信息表单 ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = new JTextField(20);   // 角色名称输入框
        JTextField txtDesc = new JTextField(20);   // 角色描述输入框

        // 第1行：角色名称
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("角色名称:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtName, gbc);

        // 第2行：角色描述
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("角色描述:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtDesc, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // ---- 下半部分：功能权限选择 ----
        List<Function> functions = functionDao.findAll();  // 获取系统中所有可用功能
        JCheckBox[] checkBoxes = new JCheckBox[functions.size()];
        JPanel funcPanel = new JPanel(new GridLayout(0, 2, 5, 5));  // 两列网格布局
        funcPanel.setBorder(BorderFactory.createTitledBorder("功能权限"));
        for (int i = 0; i < functions.size(); i++) {
            checkBoxes[i] = new JCheckBox(functions.get(i).getName());  // 为每个功能创建复选框
            funcPanel.add(checkBoxes[i]);
        }
        panel.add(new JScrollPane(funcPanel), BorderLayout.CENTER);

        // 保存按钮及事件处理
        JButton btnSave = new JButton("保存");
        btnSave.addActionListener(e -> {
            // 校验角色名称不能为空
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "角色名称不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 收集选中的功能编号，用逗号连接成字符串
            StringBuilder funcStr = new StringBuilder();
            for (int i = 0; i < functions.size(); i++) {
                if (checkBoxes[i].isSelected()) {
                    if (funcStr.length() > 0) funcStr.append(",");  // 非首项前加逗号
                    funcStr.append(functions.get(i).getNum());      // 添加功能编号
                }
            }
            // 构建角色实体对象并保存
            Role role = new Role();
            role.setName(name);
            role.setDescription(txtDesc.getText().trim());
            role.setFunctions(funcStr.toString());
            if (roleService.addRole(role)) {
                FileLogger.info("RoleManagePanel", "showAddDialog", "添加角色成功: name=" + name);
                JOptionPane.showMessageDialog(dialog, "添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();  // 关闭对话框
                loadData();        // 刷新角色列表
            } else {
                JOptionPane.showMessageDialog(dialog, "添加失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnSave, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);  // 显示模态对话框
    }

    /**
     * 显示修改角色对话框
     * <p>
     * 弹出一个模态对话框，预填充选中角色的现有信息：
     * - 角色名称和描述可直接编辑
     * - 功能权限复选框根据现有权限自动勾选
     * </p>
     */
    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要修改的角色！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        // 查找完整的角色对象
        Role role = roleService.findAll().stream().filter(r -> r.getId() == id).findFirst().orElse(null);
        if (role == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "修改角色", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---- 上半部分：角色基本信息表单 ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 预填充现有值（注意处理null值避免NPE）
        JTextField txtName = new JTextField(role.getName(), 20);
        JTextField txtDesc = new JTextField(role.getDescription() != null ? role.getDescription() : "", 20);

        // 第1行：角色名称
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("角色名称:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtName, gbc);

        // 第2行：角色描述
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("角色描述:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtDesc, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // ---- 下半部分：功能权限选择（预选中已有权限）----
        List<Function> functions = functionDao.findAll();
        // 解析现有的功能权限字符串为集合，用于判断哪些复选框应该被选中
        String[] existingFuncs = role.getFunctions() != null ? role.getFunctions().split(",") : new String[0];
        java.util.Set<String> existingSet = new java.util.HashSet<>();
        for (String f : existingFuncs) existingSet.add(f.trim());

        JCheckBox[] checkBoxes = new JCheckBox[functions.size()];
        JPanel funcPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        funcPanel.setBorder(BorderFactory.createTitledBorder("功能权限"));
        for (int i = 0; i < functions.size(); i++) {
            checkBoxes[i] = new JCheckBox(functions.get(i).getName());
            // 如果该功能已在角色权限中，则默认勾选
            checkBoxes[i].setSelected(existingSet.contains(functions.get(i).getNum()));
            funcPanel.add(checkBoxes[i]);
        }
        panel.add(new JScrollPane(funcPanel), BorderLayout.CENTER);

        // 保存按钮及事件处理
        JButton btnSave = new JButton("保存");
        btnSave.addActionListener(e -> {
            // 重新收集选中的功能编号
            StringBuilder funcStr = new StringBuilder();
            for (int i = 0; i < functions.size(); i++) {
                if (checkBoxes[i].isSelected()) {
                    if (funcStr.length() > 0) funcStr.append(",");
                    funcStr.append(functions.get(i).getNum());
                }
            }
            // 更新角色对象并保存
            role.setName(txtName.getText().trim());
            role.setDescription(txtDesc.getText().trim());
            role.setFunctions(funcStr.toString());
            if (roleService.updateRole(role)) {
                JOptionPane.showMessageDialog(dialog, "修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "修改失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnSave, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * 执行删除角色操作
     * <p>
     * 处理流程：
     * <ol>
     *   <li>检查是否选中了要删除的角色</li>
     *   <li>弹出二次确认对话框</li>
     *   <li>确认后调用服务层执行删除</li>
     *   <li>显示操作结果并刷新列表</li>
     * </ol>
     * </p>
     *
     * <p>注意事项：</p>
     * <ul>
     *   <li>如果该角色已被分配给用户，可能因外键约束导致删除失败</li>
     *   <li>删除角色会同时清除相关的用户-角色关联记录</li>
     * </ul>
     */
    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的角色！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        // 弹出二次确认对话框
        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除该角色吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (roleService.deleteRole(id)) {
                FileLogger.info("RoleManagePanel", "doDelete", "删除角色: id=" + id);
                JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadData();  // 刷新角色列表
            } else {
                JOptionPane.showMessageDialog(this, "删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
