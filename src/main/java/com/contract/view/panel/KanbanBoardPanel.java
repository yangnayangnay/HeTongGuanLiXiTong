package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.service.ContractService;
import com.contract.service.CustomerService;
import com.contract.entity.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 合同流程可视化看板面板（Kanban Board）
 * <p>
 * 采用类似Trello的看板布局，将合同按流程状态分为5个列进行展示：
 * 起草 → 会签完成 → 定稿完成 → 审批完成 → 签订完成
 * </p>
 *
 * <h3>功能特点：</h3>
 * <ul>
 *   <li>直观展示各阶段合同数量和分布情况</li>
 *   <li>卡片式设计，显示合同关键信息</li>
 *   <li>颜色标记：快到期(红色)、正常(灰色)、已完成(绿色)</li>
 *   <li>双击卡片查看合同详情</li>
 *   <li>支持客户筛选和刷新功能</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 2.0
 * @since 2026-01-01
 */
public class KanbanBoardPanel extends JPanel {

    /** 合同业务服务类 */
    private ContractService contractService = new ContractService();
    /** 客户业务服务类 */
    private CustomerService customerService = new CustomerService();
    /** 客户筛选下拉框 */
    private JComboBox<String> cmbCustomerFilter;
    /** 看板内容容器 */
    private JPanel boardContainer;

    /** 状态列定义：名称、类型码、颜色 */
    private static final String[][] STATUS_COLUMNS = {
        {"📝 起草", "1", new Color(52, 152, 219).toString()},   // 蓝色
        {"✍️ 会签完成", "2", new Color(155, 89, 182).toString()},   // 紫色
        {"📋 定稿完成", "3", new Color(241, 196, 15).toString()},   // 黄色
        {"✅ 审批完成", "4", new Color(230, 126, 34).toString()},   // 橙色
        {"📝 签订完成", "5", new Color(46, 204, 113).toString()}    // 绿色
    };

    /**
     * 构造方法：初始化看板面板
     */
    public KanbanBoardPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initUI();
        loadData();  // 初始加载数据
    }

    /**
     * 初始化用户界面组件
     */
    private void initUI() {
        // ===== 顶部工具栏 =====
        JPanel toolbarPanel = new JPanel(new BorderLayout(10, 5));
        toolbarPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // 左侧：标题和刷新按钮
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel lblTitle = new JLabel("📋 合同流程看板");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 20));
        lblTitle.setForeground(new Color(44, 62, 80));
        leftPanel.add(lblTitle);

        // 刷新按钮
        JButton btnRefresh = new JButton("🔄 刷新");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setOpaque(true);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadData());
        leftPanel.add(btnRefresh);

        toolbarPanel.add(leftPanel, BorderLayout.WEST);

        // 右侧：筛选条件
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JLabel lblFilter = new JLabel("客户筛选:");
        lblFilter.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        rightPanel.add(lblFilter);

        cmbCustomerFilter = new JComboBox<>();
        cmbCustomerFilter.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cmbCustomerFilter.setPreferredSize(new Dimension(150, 28));
        cmbCustomerFilter.addItem("全部客户");  // 默认选项

        // 加载客户列表到下拉框
        List<Customer> customers = customerService.findAll();
        for (Customer c : customers) {
            cmbCustomerFilter.addItem(c.getName());
        }

        // 选择变化时自动刷新数据
        cmbCustomerFilter.addActionListener(e -> loadData());
        rightPanel.add(cmbCustomerFilter);

        toolbarPanel.add(rightPanel, BorderLayout.EAST);
        add(toolbarPanel, BorderLayout.NORTH);

        // ===== 看板内容区域（水平滚动）=====
        JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(null);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);  // 设置滚动速度

        boardContainer = new JPanel();
        boardContainer.setLayout(new BoxLayout(boardContainer, BoxLayout.X_AXIS));
        boardContainer.setBackground(new Color(236, 240, 241));   // 浅灰背景
        scrollPane.setViewportView(boardContainer);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 加载并渲染看板数据
     * <p>
     * 从数据库查询所有合同，根据状态分发到对应的列中，
     * 每个合同生成一张卡片显示在对应的状态列下。
     * </p>
     */
    private void loadData() {
        // 清空现有卡片
        boardContainer.removeAll();

        // 获取选中的客户筛选条件
        String selectedCustomer = (String) cmbCustomerFilter.getSelectedItem();
        boolean filterByCustomer = selectedCustomer != null && !"全部客户".equals(selectedCustomer);

        // 获取所有合同列表
        List<Contract> allContracts = contractService.findAll();

        // 为每个状态列创建容器
        for (int i = 0; i < STATUS_COLUMNS.length; i++) {
            String statusName = STATUS_COLUMNS[i][0];
            String statusCode = STATUS_COLUMNS[i][1];

            // 创建状态列面板
            JPanel columnPanel = createStatusColumn(statusName, parseColor(STATUS_COLUMNS[i][2]));

            // 统计该状态的合同数量
            int count = 0;

            // 遍历所有合同，找出属于当前状态的合同
            for (Contract contract : allContracts) {
                // 应用客户筛选
                if (filterByCustomer && !selectedCustomer.equals(contract.getCustomer())) {
                    continue;  // 不匹配筛选条件，跳过
                }

                // 获取合同的当前状态类型
                int currentStateType = contractService.getContractStateType(contract.getNum());

                // 如果合同属于当前列的状态，则添加卡片
                if (currentStateType == Integer.parseInt(statusCode)) {
                    KanbanCard card = createKanbanCard(contract, currentStateType);
                    columnPanel.add(card);
                    count++;
                }
            }

            // 更新列标题中的数量标签
            updateColumnCount(columnPanel, count);

            // 将状态列添加到看板容器
            boardContainer.add(columnPanel);

            // 列之间添加间距
            if (i < STATUS_COLUMNS.length - 1) {
                JPanel spacer = new JPanel();
                spacer.setPreferredSize(new Dimension(10, 0));
                spacer.setOpaque(false);
                boardContainer.add(spacer);
            }
        }

        // 重新布局和绘制
        boardContainer.revalidate();
        boardContainer.repaint();
    }

    /**
     * 创建状态列面板
     *
     * @param statusName 状态名称（如"起草"）
     * @param headerColor 列标题背景色
     * @return 配置好的状态列JPanel
     */
    private JPanel createStatusColumn(String statusName, Color headerColor) {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBackground(Color.WHITE);
        column.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(0, 0, 0, 0)
        ));
        column.setPreferredSize(new Dimension(260, 0));  // 固定宽度260px
        column.setMaximumSize(new Dimension(260, Short.MAX_VALUE));

        // 列标题
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(headerColor);
        headerPanel.setPreferredSize(new Dimension(260, 40));
        headerPanel.setMaximumSize(new Dimension(260, 40));

        JLabel lblHeader = new JLabel("  " + statusName + " (0)");
        lblHeader.setFont(new Font("微软雅黑", Font.BOLD, 13));
        lblHeader.setForeground(Color.WHITE);
        headerPanel.add(lblHeader, BorderLayout.WEST);

        // 存储标题标签引用，用于后续更新数量
        column.putClientProperty("headerLabel", lblHeader);

        column.add(headerPanel);

        // 卡片容器（垂直排列）
        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(Color.WHITE);
        cardsContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        // 使用滚动条包裹卡片容器（当卡片过多时可以滚动）
        JScrollPane cardScroll = new JScrollPane(cardsContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cardScroll.setBorder(null);
        cardScroll.getVerticalScrollBar().setUnitIncrement(16);

        column.putClientProperty("cardsContainer", cardsContainer);  // 存储引用
        column.add(cardScroll, BorderLayout.CENTER);

        return column;
    }

    /**
     * 更新状态列的合同数量显示
     *
     * @param column 状态列面板
     * @param count  合同数量
     */
    private void updateColumnCount(JPanel column, int count) {
        JLabel headerLabel = (JLabel) column.getClientProperty("headerLabel");
        if (headerLabel != null) {
            // 提取原始状态名称（去掉后面的数量部分）
            String originalText = headerLabel.getText().split("\\(")[0].trim();
            headerLabel.setText(originalText + " (" + count + ")");
        }
    }

    /**
     * 创建看板卡片
     * <p>
     * 每张卡片代表一个合同，显示关键信息：
     * - 合同编号、名称
     * - 客户名称、负责人
     * - 剩余天数（如果未到期）
     * 根据到期时间使用不同颜色的边框标识紧急程度。
     * </p>
     *
     * @param contract      合同对象
     * @param stateType     当前状态类型码
     * @return 配置好的卡片JPanel
     */
    private KanbanCard createKanbanCard(Contract contract, int stateType) {
        return new KanbanCard(contract, stateType);
    }

    /**
     * 解析颜色字符串为Color对象
     *
     * @param colorStr 颜色字符串（RGB格式）
     * @return Color对象
     */
    private Color parseColor(String colorStr) {
        try {
            // 解析 java.awt.Color[r=...,g=...,b=...] 格式
            if (colorStr.contains("Color[")) {
                String[] parts = colorStr.replaceAll("[^\\d,]", "").split(",");
                return new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
        } catch (Exception e) {
            // 解析失败使用默认颜色
        }
        return new Color(100, 100, 100);
    }

    /**
     * 看板卡片内部类
     * <p>
     * 自定义JPanel子类，用于展示单个合同的关键信息。
     * 支持鼠标悬停效果和双击打开详情。
     * </p>
     */
    private class KanbanCard extends JPanel {

        /** 关联的合同对象 */
        private Contract contract;
        /** 卡片边框颜色（默认） */
        private final Color defaultBorderColor = new Color(220, 220, 220);
        /** 悬停时的边框颜色 */
        private final Color hoverBorderColor = new Color(52, 152, 219);
        /** 快到期的边框颜色（红色警告） */
        private final Color urgentBorderColor = new Color(231, 76, 60);
        /** 已完成的边框颜色（绿色） */
        private final Color completedBorderColor = new Color(46, 204, 113);

        /**
         * 构造方法：创建合同卡片
         *
         * @param contract  合同对象
         * @param stateType 状态类型码
         */
        public KanbanCard(Contract contract, int stateType) {
            this.contract = contract;
            initCard(stateType);
        }

        /**
         * 初始化卡片UI
         *
         * @param stateType 状态类型码（用于判断是否已完成）
         */
        private void initCard(int stateType) {
            setLayout(new BorderLayout(8, 8));
            setBackground(Color.WHITE);
            setMaximumSize(new Dimension(240, 160));  // 最大尺寸限制
            setAlignmentY(Component.TOP_ALIGNMENT);

            // 根据合同状态确定边框颜色
            Color borderColor = determineBorderColor(stateType);
            setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 2, true),  // 圆角边框
                new EmptyBorder(10, 10, 10, 10)
            ));

            // ===== 卡片内容区 =====
            JPanel contentPanel = new JPanel(new GridLayout(4, 1, 4, 4));
            contentPanel.setOpaque(false);

            // 第1行：合同编号（粗体）
            JLabel lblNum = new JLabel("<html><b>" + truncate(contract.getNum(), 18) + "</b></html>");
            lblNum.setFont(new Font("微软雅黑", Font.BOLD, 11));
            lblNum.setForeground(new Color(44, 62, 80));
            contentPanel.add(lblNum);

            // 第2行：合同名称
            JLabel lblName = new JLabel(truncate(contract.getName(), 20));
            lblName.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            lblName.setForeground(new Color(52, 73, 94));
            contentPanel.add(lblName);

            // 第3行：客户和负责人
            StringBuilder infoBuilder = new StringBuilder();
            if (contract.getCustomer() != null && !contract.getCustomer().isEmpty()) {
                infoBuilder.append("👤 ").append(truncate(contract.getCustomer(), 10));
            }
            if (contract.getUserName() != null && !contract.getUserName().isEmpty()) {
                if (infoBuilder.length() > 0) infoBuilder.append(" | ");
                infoBuilder.append("👤 ").append(truncate(contract.getUserName(), 8));
            }
            JLabel lblInfo = new JLabel(infoBuilder.toString());
            lblInfo.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            lblInfo.setForeground(new Color(127, 140, 141));
            contentPanel.add(lblInfo);

            // 第4行：剩余天数或状态提示
            JLabel lblDays = new JLabel(getDaysLabel(contract.getEndTime(), stateType));
            lblDays.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            contentPanel.add(lblDays);

            add(contentPanel, BorderLayout.CENTER);

            // ===== 鼠标悬停效果 =====
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent evt) {
                    // 悬停时改变边框颜色
                    setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(hoverBorderColor, 2, true),
                        new EmptyBorder(10, 10, 10, 10)
                    ));
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent evt) {
                    // 离开时恢复原边框颜色
                    setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(determineBorderColor(stateType), 2, true),
                        new EmptyBorder(10, 10, 10, 10)
                    ));
                    setCursor(Cursor.getDefaultCursor());
                }

                @Override
                public void mouseClicked(MouseEvent evt) {
                    // 双击打开详情对话框
                    if (evt.getClickCount() == 2) {
                        showContractDetailDialog();
                    }
                }
            });
        }

        /**
         * 根据合同状态和到期时间确定卡片边框颜色
         *
         * @param stateType 状态类型码
         * @return 边框颜色
         */
        private Color determineBorderColor(int stateType) {
            // 已完成的合同用绿色边框
            if (stateType == 5) {  // 签订完成
                return completedBorderColor;
            }

            // 未完成的合同检查是否快到期
            if (contract.getEndTime() != null) {
                long daysRemaining = calculateDaysRemaining(contract.getEndTime());
                if (daysRemaining >= 0 && daysRemaining <= 7) {
                    return urgentBorderColor;  // 7天内到期用红色边框
                }
            }

            return defaultBorderColor;  // 默认灰色边框
        }

        /**
         * 计算距离到期日的剩余天数
         *
         * @param endTime 到期日期
         * @return 剩余天数；已过期返回负数
         */
        private long calculateDaysRemaining(Date endTime) {
            if (endTime == null) return Long.MAX_VALUE;  // 无截止日期
            long diff = endTime.getTime() - System.currentTimeMillis();
            return diff / (1000 * 60 * 60 * 24);  // 转换为天数
        }

        /**
         * 生成剩余天数/状态提示文本
         *
         * @param endTime   到期日期
         * @param stateType 状态类型码
         * @return 提示文本
         */
        private String getDaysLabel(Date endTime, int stateType) {
            if (stateType == 5) {
                return "✅ 已签订完成";
            }

            if (endTime == null) {
                return "⏳ 进行中";
            }

            long days = calculateDaysRemaining(endTime);
            if (days < 0) {
                return "⚠️ 已过期 " + Math.abs(days) + " 天";
            } else if (days <= 7) {
                return "🔴 剩余 " + days + " 天";
            } else if (days <= 30) {
                return "🟡 剩余 " + days + " 天";
            } else {
                return "🟢 剩余 " + days + " 天";
            }
        }

        /**
         * 截断过长文本
         *
         * @param text       原始文本
         * @param maxLength  最大长度
         * @return 截断后的文本（超出部分用...代替）
         */
        private String truncate(String text, int maxLength) {
            if (text == null) return "";
            if (text.length() <= maxLength) return text;
            return text.substring(0, maxLength - 3) + "...";
        }

        /**
         * 显示合同详情对话框
         * <p>
         * 双击卡片后弹出模态对话框，展示合同的完整信息。
         * </p>
         */
        private void showContractDetailDialog() {
            JDialog detailDlg = new JDialog(
                (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(KanbanBoardPanel.this),
                "📄 合同详情: " + contract.getNum(),
                true  // 模态对话框
            );
            detailDlg.setSize(500, 450);
            detailDlg.setLocationRelativeTo(KanbanBoardPanel.this);
            detailDlg.setLayout(new BorderLayout(10, 10));

            // 详情内容面板
            JPanel detailPanel = new JPanel(new GridBagLayout());
            detailPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 5, 8, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            int row = 0;
            // 显示各项合同信息
            addDetailRow(detailPanel, gbc, row++, "合同编号:", contract.getNum());
            addDetailRow(detailPanel, gbc, row++, "合同名称:", contract.getName());
            addDetailRow(detailPanel, gbc, row++, "签约客户:", contract.getCustomer());
            addDetailRow(detailPanel, gbc, row++, "负责人:", contract.getUserName());
            addDetailRow(detailPanel, gbc, row++, "开始时间:",
                contract.getBeginTime() != null ? sdf.format(contract.getBeginTime()) : "-");
            addDetailRow(detailPanel, gbc, row++, "结束时间:",
                contract.getEndTime() != null ? sdf.format(contract.getEndTime()) : "-");
            addDetailRow(detailPanel, gbc, row++, "当前状态:",
                contractService.getContractStateName(contract.getNum()));

            detailDlg.add(detailPanel, BorderLayout.CENTER);

            // 关闭按钮
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton btnClose = new JButton("关闭");
            btnClose.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            btnClose.setFocusPainted(false);
            btnClose.addActionListener(e -> detailDlg.dispose());
            btnPanel.add(btnClose);
            detailDlg.add(btnPanel, BorderLayout.SOUTH);

            detailDlg.setVisible(true);
        }

        /**
         * 在详情面板中添加一行信息
         */
        private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            JLabel lblLabel = new JLabel(label);
            lblLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
            panel.add(lblLabel, gbc);

            gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
            JLabel lblValue = new JLabel(value != null ? value : "-");
            lblValue.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            panel.add(lblValue, gbc);
        }
    }
}
