package com.contract.view.panel;

import com.contract.entity.Customer;
import com.contract.service.CustomerService;
import com.contract.util.FileLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 客户信息管理面板
 * <p>
 * 该面板用于管理系统中的客户信息，提供完整的CRUD（增删改查）功能。
 * 客户信息是合同系统的基础数据，合同在起草时需要关联到具体的客户。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示所有客户信息的列表表格</li>
 *   <li>支持按客户名称进行模糊搜索</li>
 *   <li>添加新客户（弹出对话框录入详细信息）</li>
 *   <li>修改已有客户信息</li>
 *   <li>删除客户记录（需二次确认）</li>
 * </ul>
 *
 * <p>客户信息字段说明：</p>
 * <ul>
 *   <li>客户编号：唯一标识符，用于与合同关联</li>
 *   <li>客户名称：显示名称</li>
 *   <li>地址：客户联系地址</li>
 *   <li>电话：联系电话</li>
 *   <li>传真：传真号码</li>
 *   <li>邮编：邮政编码</li>
 *   <li>银行名称：开户银行</li>
 *   <li>银行账号：用于财务结算的账号</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class CustomerManagePanel extends JPanel {
    // 客户列表表格
    private JTable table;
    // 表格数据模型
    private DefaultTableModel tableModel;
    // 客户名称搜索输入框
    private JTextField txtSearchName;
    // 客户业务服务类
    private CustomerService customerService = new CustomerService();

    /**
     * 构造方法：初始化客户管理面板
     */
    public CustomerManagePanel() {
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
        // 加载所有客户数据
        loadAllData();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构：
     * - 北部(NORTH)：标题"客户信息管理"
     * - 中部(CENTER)：工具栏（搜索框 + 操作按钮组）
     * - 南部(SOUTH)：客户信息列表表格
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("客户信息管理");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 搜索和操作按钮面板 =====
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // 搜索区域
        toolPanel.add(new JLabel("客户名称:"));
        txtSearchName = new JTextField(15);
        toolPanel.add(txtSearchName);

        // 查询按钮
        JButton btnSearch = new JButton("查询");
        btnSearch.addActionListener(e -> doSearch());  // 点击后执行搜索
        toolPanel.add(btnSearch);

        // 显示全部按钮（重置搜索条件）
        JButton btnShowAll = new JButton("显示全部");
        btnShowAll.addActionListener(e -> loadAllData());  // 点击后加载全部数据
        toolPanel.add(btnShowAll);

        // 添加客户按钮（绿色背景）
        JButton btnAdd = new JButton("添加客户");
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

        add(toolPanel, BorderLayout.CENTER);

        // ===== 客户信息列表表格 =====
        // 定义表格列名：ID、客户编号、客户名称、地址、电话、传真、邮编、银行名称、银行账号
        String[] columns = {"ID", "客户编号", "客户名称", "地址", "电话", "传真", "邮编", "银行名称", "银行账号"};
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
     * 加载所有客户数据到表格中
     * <p>
     * 查询数据库中的所有客户记录，
     * 全部显示在列表中供用户浏览和管理。
     * </p>
     */
    private void loadAllData() {
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 查询所有客户
        List<Customer> customers = customerService.findAll();
        for (Customer c : customers) {
            // 将客户的所有字段信息添加到表格行中
            tableModel.addRow(new Object[]{c.getId(), c.getNum(), c.getName(), c.getAddress(), c.getTel(), c.getFax(), c.getCode(), c.getBank(), c.getAccount()});
        }
    }

    /**
     * 执行按名称搜索操作
     * <p>
     * 根据用户输入的客户名称进行模糊匹配查询。
     * 如果搜索框为空，则等同于显示全部客户。
     * </p>
     */
    private void doSearch() {
        // 获取搜索关键词
        String name = txtSearchName.getText().trim();
        FileLogger.info("CustomerManagePanel", "doSearch", "查询客户: name=" + name);
        // 清空表格现有数据
        tableModel.setRowCount(0);
        // 根据是否输入关键词选择不同的查询方式
        List<Customer> customers = name.isEmpty() ? customerService.findAll() : customerService.findByName(name);
        for (Customer c : customers) {
            // 将查询结果填充到表格
            tableModel.addRow(new Object[]{c.getId(), c.getNum(), c.getName(), c.getAddress(), c.getTel(), c.getFax(), c.getCode(), c.getBank(), c.getAccount()});
        }
    }

    /**
     * 显示添加客户对话框
     * <p>
     * 弹出一个模态对话框，包含客户信息的所有输入字段。
     * 用户填写完成后点击"保存"按钮将新客户信息保存到数据库。
     * 必填字段校验：编号、名称、电话、地址不能为空。
     * </p>
     */
    private void showAddDialog() {
        // 创建模态对话框（true参数表示模态）
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加客户", true);
        dialog.setSize(450, 400);  // 对话框尺寸
        dialog.setLocationRelativeTo(this);  // 居中显示

        // 使用GridBagLayout实现表单布局
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);  // 组件间距
        gbc.fill = GridBagConstraints.HORIZONTAL;  // 水平拉伸

        // 创建各字段的输入控件
        JTextField txtNum = new JTextField(20);       // 客户编号
        JTextField txtName = new JTextField(20);      // 客户名称
        JTextField txtAddress = new JTextField(20);   // 地址
        JTextField txtTel = new JTextField(20);       // 电话
        JTextField txtFax = new JTextField(20);       // 传真
        JTextField txtCode = new JTextField(20);      // 邮编
        JTextField txtBank = new JTextField(20);      // 银行名称
        JTextField txtAccount = new JTextField(20);   // 银行账号

        // 字段标签和对应输入控件的映射
        String[] labels = {"客户编号:", "客户名称:", "地址:", "电话:", "传真:", "邮编:", "银行名称:", "银行账号:"};
        JTextField[] fields = {txtNum, txtName, txtAddress, txtTel, txtFax, txtCode, txtBank, txtAccount};

        // 动态构建表单界面
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;  // 标签列不拉伸
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.gridy = i; gbc.weightx = 1;   // 输入框列拉伸
            panel.add(fields[i], gbc);
        }

        // 保存按钮及事件处理
        JButton btnSave = new JButton("保存");
        btnSave.addActionListener(e -> {
            // === 前端必填项校验 ===
            if (txtNum.getText().trim().isEmpty() || txtName.getText().trim().isEmpty() || txtTel.getText().trim().isEmpty() || txtAddress.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "编号、名称、电话、地址不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 构建客户实体对象
            Customer customer = new Customer();
            customer.setNum(txtNum.getText().trim());
            customer.setName(txtName.getText().trim());
            customer.setAddress(txtAddress.getText().trim());
            customer.setTel(txtTel.getText().trim());
            customer.setFax(txtFax.getText().trim());
            customer.setCode(txtCode.getText().trim());
            customer.setBank(txtBank.getText().trim());
            customer.setAccount(txtAccount.getText().trim());
            // 调用服务层保存客户
            if (customerService.addCustomer(customer)) {
                FileLogger.info("CustomerManagePanel", "showAddDialog", "添加客户成功: name=" + customer.getName());
                JOptionPane.showMessageDialog(dialog, "添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();  // 关闭对话框
                loadAllData();      // 刷新列表
            } else {
                JOptionPane.showMessageDialog(dialog, "添加失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        // 将保存按钮放在最后一行，跨两列显示
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        panel.add(btnSave, gbc);

        dialog.add(panel);
        dialog.setVisible(true);  // 显示对话框（模态阻塞）
    }

    /**
     * 显示修改客户对话框
     * <p>
     * 弹出一个模态对话框，预填充选中客户的现有信息。
     * 用户修改完成后点击"保存"按钮更新数据库中的记录。
     * 注意：客户编号字段不可修改（作为主键标识）。
     * </p>
     */
    private void showEditDialog() {
        // 获取选中的表格行索引
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要修改的客户！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 根据客户编号查询完整的客户实体对象
        Customer customer = customerService.findByNum((String) tableModel.getValueAt(row, 1));
        if (customer == null) return;

        // 创建模态对话框
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "修改客户", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        // 使用GridBagLayout实现表单布局
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 创建输入控件并预填充现有值（注意处理null值避免NPE）
        JTextField txtName = new JTextField(customer.getName(), 20);
        JTextField txtAddress = new JTextField(customer.getAddress() != null ? customer.getAddress() : "", 20);
        JTextField txtTel = new JTextField(customer.getTel() != null ? customer.getTel() : "", 20);
        JTextField txtFax = new JTextField(customer.getFax() != null ? customer.getFax() : "", 20);
        JTextField txtCode = new JTextField(customer.getCode() != null ? customer.getCode() : "", 20);
        JTextField txtBank = new JTextField(customer.getBank() != null ? customer.getBank() : "", 20);
        JTextField txtAccount = new JTextField(customer.getAccount() != null ? customer.getAccount() : "", 20);

        // 字段标签和对应输入控件（不含编号字段，因为不可修改）
        String[] labels = {"客户名称:", "地址:", "电话:", "传真:", "邮编:", "银行名称:", "银行账号:"};
        JTextField[] fields = {txtName, txtAddress, txtTel, txtFax, txtCode, txtBank, txtAccount};

        // 动态构建表单界面
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.gridy = i; gbc.weightx = 1;
            panel.add(fields[i], gbc);
        }

        // 保存按钮及事件处理
        JButton btnSave = new JButton("保存");
        btnSave.addActionListener(e -> {
            // 更新客户实体对象的各个字段
            customer.setName(txtName.getText().trim());
            customer.setAddress(txtAddress.getText().trim());
            customer.setTel(txtTel.getText().trim());
            customer.setFax(txtFax.getText().trim());
            customer.setCode(txtCode.getText().trim());
            customer.setBank(txtBank.getText().trim());
            customer.setAccount(txtAccount.getText().trim());
            // 调用服务层更新客户信息
            if (customerService.updateCustomer(customer)) {
                JOptionPane.showMessageDialog(dialog, "修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();  // 关闭对话框
                loadAllData();      // 刷新列表
            } else {
                JOptionPane.showMessageDialog(dialog, "修改失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        panel.add(btnSave, gbc);

        dialog.add(panel);
        dialog.setVisible(true);  // 显示对话框（模态阻塞）
    }

    /**
     * 执行删除客户操作
     * <p>
     * 处理流程：
     * <ol>
     *   <li>检查是否选中了要删除的客户</li>
     *   <li>弹出二次确认对话框，防止误删</li>
     *   <li>确认后调用服务层执行删除操作</li>
     *   <li>显示操作结果并刷新列表</li>
     * </ol>
     * </p>
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>必须先选择一个客户才能删除</li>
     *   <li>需要用户二次确认后才真正删除</li>
     *   <li>如果该客户已被合同引用，可能因外键约束导致删除失败</li>
     * </ul>
     */
    private void doDelete() {
        // 获取选中的表格行索引
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的客户！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 获取选中行的ID（第一列，数据库主键）
        int id = (int) tableModel.getValueAt(row, 0);
        // 弹出二次确认对话框
        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除该客户吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // 用户确认后执行删除
            if (customerService.deleteCustomer(id)) {
                FileLogger.info("CustomerManagePanel", "doDelete", "删除客户: id=" + id);
                JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadAllData();  // 刷新列表
            } else {
                JOptionPane.showMessageDialog(this, "删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
