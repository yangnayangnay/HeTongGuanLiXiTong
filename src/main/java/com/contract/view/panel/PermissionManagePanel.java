package com.contract.view.panel;

import com.contract.entity.Right;
import com.contract.entity.Role;
import com.contract.service.RightService;
import com.contract.service.RoleService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限管理面板（分配角色给用户）
 * <p>
 * 该面板用于为指定用户分配或调整角色，是RBAC权限模型中
 * "用户-角色"关联关系的管理界面。通过分配不同的角色组合，
 * 可以灵活控制用户在系统中的操作权限。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>显示系统中所有可用角色的列表（含描述信息）</li>
 *   <li>预填充该用户当前已拥有的角色（自动勾选）</li>
 *   <li>支持多选/取消选择角色</li>
 *   <li>保存后批量更新用户的角色分配</li>
 *   <li>安全保护：防止管理员取消自己的管理员角色</li>
 * </ul>
 *
 * <p>使用方式：</p>
 * <ul>
 *   <li>通常以对话框形式嵌入到UserManagePanel中使用</li>
 *   <li>调用showDialog()方法以模态对话框展示</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class PermissionManagePanel extends JPanel {
    // 目标用户：正在被分配角色的用户
    private String targetUser;
    // 操作者：当前执行分配操作的管理员用户
    // 用于安全检查，防止管理员误操作取消自己的权限
    private String operatorUser;
    // 权限业务服务类（用于查询和更新用户-角色关联）
    private RightService rightService = new RightService();
    // 角色业务服务类（用于获取所有可用角色列表）
    private RoleService roleService = new RoleService();

    /**
     * 构造方法：初始化权限管理面板
     *
     * @param userName 要分配角色的目标用户名
     * @param operatorName 执行操作的管理员用户名（用于安全校验）
     */
    public PermissionManagePanel(String userName, String operatorName) {
        this.targetUser = userName;       // 记录目标用户
        this.operatorUser = operatorName;  // 记录操作者
        // 使用BorderLayout作为主布局管理器
        setLayout(new BorderLayout());
        // 设置面板边距为15像素
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 初始化界面组件
        initUI();
    }

    /**
     * 初始化用户界面组件
     * <p>
     * 布局结构：
     * - 北部(NORTH)：标题（显示目标用户名）
     * - 中部(CENTER)：角色复选框列表（单列垂直排列）
     * - 南部(SOUTH)：保存权限按钮
     * </p>
     */
    private void initUI() {
        // ===== 标题区域 =====
        JLabel lblTitle = new JLabel("权限管理 - 为用户 " + targetUser + " 分配角色");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== 角色选择区域 =====
        List<Role> roles = roleService.findAll();  // 获取系统中所有可用角色
        // 查询目标用户当前已拥有的角色
        List<Right> existingRights = rightService.findByUserName(targetUser);
        Set<String> existingRoles = new HashSet<>();
        for (Right right : existingRights) existingRoles.add(right.getRoleName());  // 构建已有角色名称集合

        JPanel rolePanel = new JPanel(new GridLayout(0, 1, 5, 5));  // 单列网格布局
        rolePanel.setBorder(BorderFactory.createTitledBorder("选择角色"));
        JCheckBox[] checkBoxes = new JCheckBox[roles.size()];
        for (int i = 0; i < roles.size(); i++) {
            // 复选框文本格式："角色名称 - 角色描述"
            checkBoxes[i] = new JCheckBox(roles.get(i).getName() + " - " + (roles.get(i).getDescription() != null ? roles.get(i).getDescription() : ""));
            // 如果该角色已属于用户，则默认勾选
            checkBoxes[i].setSelected(existingRoles.contains(roles.get(i).getName()));
            rolePanel.add(checkBoxes[i]);
        }
        add(new JScrollPane(rolePanel), BorderLayout.CENTER);

        // ===== 保存按钮 =====
        JButton btnSave = new JButton("保存权限");
        btnSave.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnSave.setBackground(new Color(66, 133, 244));  // 蓝色背景
        btnSave.setOpaque(true);
        btnSave.setContentAreaFilled(true);
        btnSave.setForeground(Color.BLACK);
        btnSave.setFocusPainted(false);

        // 保存按钮事件处理（包含安全校验逻辑）
        btnSave.addActionListener(e -> {
            List<String> selectedRoles = new ArrayList<>();
            int adminRoleIndex = -1;

            // 收集所有被选中的角色
            for (int i = 0; i < roles.size(); i++) {
                if (checkBoxes[i].isSelected()) {
                    selectedRoles.add(roles.get(i).getName());
                }
                // 记录"管理员"角色在列表中的位置索引
                if ("管理员".equals(roles.get(i).getName())) {
                    adminRoleIndex = i;
                }
            }

            // === 安全保护：防止管理员取消自己的管理员角色 ===
            // 如果操作者和目标是同一人，且尝试取消管理员角色，则阻止操作
            if (targetUser.equals(operatorUser) && adminRoleIndex >= 0 && !checkBoxes[adminRoleIndex].isSelected()) {
                JOptionPane.showMessageDialog(this,
                    "不能取消自己的管理员权限！",
                    "提示", JOptionPane.WARNING_MESSAGE);
                // 强制重新勾选管理员角色复选框
                checkBoxes[adminRoleIndex].setSelected(true);
                return;  // 终止保存操作
            }

            // 调用服务层执行角色重新分配（先删除旧关联，再创建新关联）
            if (rightService.reassignRoles(targetUser, selectedRoles)) {
                JOptionPane.showMessageDialog(this, "权限分配成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "权限分配失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        add(btnSave, BorderLayout.SOUTH);
    }

    /**
     * 以对话框形式显示此面板
     * <p>
     * 创建一个模态JDialog，将当前面板作为内容添加进去。
     * 模态对话框会阻塞父窗口的操作，直到用户完成角色分配并关闭对话框。
     * 此方法是供外部调用的公共接口。
     * </p>
     *
     * @param parent 父组件（用于定位对话框位置和确定所属窗口）
     * @param operatorName 操作者用户名（传递给构造函数用于安全校验）
     */
    public void showDialog(Component parent, String operatorName) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "分配权限 - " + targetUser, true);
        dialog.setSize(400, 350);           // 对话框尺寸
        dialog.setLocationRelativeTo(parent);  // 相对于父组件居中显示
        dialog.add(this);                     // 将当前面板添加到对话框中
        dialog.setVisible(true);              // 显示模态对话框（阻塞直到关闭）
    }
}
