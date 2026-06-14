package com.contract.view.panel;

import com.contract.entity.Contract;
import com.contract.service.ContractService;
import com.contract.util.DataExportUtil;
import com.contract.util.NotificationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 数据统计面板
 * <p>
 * 提供合同数据的统计分析功能，包括：
 * - 概览卡片（总合同数、总金额、本月新增、待处理任务）
 * - 按状态分布统计
 * - 客户排名TOP10
 * - 最近6个月合同趋势
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 */
public class StatisticsPanel extends JPanel {

    /** 合同业务服务类 */
    private ContractService contractService = new ContractService();
    /** 当前登录用户（用于查询待办任务） */
    private String currentUserName;

    /**
     * 构造方法：初始化数据统计面板
     */
    public StatisticsPanel(com.contract.entity.User user) {
        this.currentUserName = user.getName();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
        loadData();
    }

    /**
     * 初始化界面组件
     */
    private void initUI() {
        // 标题区域
        JLabel lblTitle = new JLabel("📊 数据统计");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // 主内容区（使用滚动面板支持大量数据展示）
        JScrollPane scrollPane = new JScrollPane(createMainContent());
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 创建主内容面板
     */
    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 区域1：概览卡片
        mainPanel.add(createOverviewCards());

        // 区域2：按状态分布
        mainPanel.add(createStatusDistributionPanel());

        // 区域3：客户排名TOP10
        mainPanel.add(createCustomerRankingPanel());

        // 区域4：时间趋势
        mainPanel.add(createTimeTrendPanel());

        // 导出按钮区域
        mainPanel.add(createExportButtonPanel());

        return mainPanel;
    }

    /**
     * 创建概览卡片区域（4个横向排列的统计卡片）
     */
    private JPanel createOverviewCards() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 10));
        cardsPanel.setBorder(BorderFactory.createTitledBorder("📈 数据概览"));
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // 创建4个统计卡片
        JPanel cardTotal = createStatCard("总合同数", "0", new Color(66, 133, 244));
        JPanel cardAmount = createStatCard("总金额", "¥0", new Color(46, 204, 113));
        JPanel cardMonth = createStatCard("本月新增", "0", new Color(241, 196, 15));
        JPanel cardPending = createStatCard("待处理任务", "0", new Color(231, 76, 60));

        cardTotal.setName("cardTotal");
        cardAmount.setName("cardAmount");
        cardMonth.setName("cardMonth");
        cardPending.setName("cardPending");

        cardsPanel.add(cardTotal);
        cardsPanel.add(cardAmount);
        cardsPanel.add(cardMonth);
        cardsPanel.add(cardPending);

        return cardsPanel;
    }

    /**
     * 创建单个统计卡片
     * @param title 卡片标题
     * @param value 初始显示值
     * @param color 卡片主题色
     * @return 配置好的卡片面板
     */
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createLineBorder(color, 2, true));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(200, 90));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblTitle.setForeground(Color.GRAY);
        card.add(lblTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("微软雅黑", Font.BOLD, 28));
        lblValue.setForeground(color);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    /**
     * 创建按状态分布面板
     */
    private JPanel createStatusDistributionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📊 按状态分布"));

        String[] columns = {"状态", "合同数量", "金额占比"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.setName("statusTable");

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        return panel;
    }

    /**
     * 创建客户排名TOP10面板
     */
    private JPanel createCustomerRankingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("🏆 客户排名 TOP10"));

        String[] columns = {"排名", "客户名称", "合同数量", "总金额"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.setName("customerRankTable");

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        return panel;
    }

    /**
     * 创建时间趋势面板（最近6个月）
     */
    private JPanel createTimeTrendPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📅 最近6个月合同趋势"));

        String[] columns = {"月份", "新增合同数", "签约总额"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.setName("trendTable");

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        return panel;
    }

    /**
     * 创建导出按钮面板
     */
    private JPanel createExportButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBorder(new EmptyBorder(15, 0, 5, 0));

        JButton btnExportCSV = new JButton("📥 导出CSV");
        btnExportCSV.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnExportCSV.setBackground(new Color(66, 133, 244));
        btnExportCSV.setOpaque(true);
        btnExportCSV.setContentAreaFilled(true);
        btnExportCSV.setForeground(Color.WHITE);
        btnExportCSV.setFocusPainted(false);
        btnExportCSV.addActionListener(e -> exportToCSV());

        JButton btnExportHTML = new JButton("📄 导出报表");
        btnExportHTML.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnExportHTML.setBackground(new Color(46, 204, 113));
        btnExportHTML.setOpaque(true);
        btnExportHTML.setContentAreaFilled(true);
        btnExportHTML.setForeground(Color.BLACK);
        btnExportHTML.setFocusPainted(false);
        btnExportHTML.addActionListener(e -> exportToHTML());

        panel.add(btnExportCSV);
        panel.add(btnExportHTML);

        return panel;
    }

    /**
     * 加载并显示统计数据
     */
    private void loadData() {
        try {
            List<Contract> allContracts = contractService.findAll();

            // === 更新概览卡片 ===
            updateCardValue("cardTotal", String.valueOf(allContracts.size()));

            double totalAmount = 0;
            for (Contract c : allContracts) {
                totalAmount += c.getAmount();
            }
            updateCardValue("cardAmount", formatCurrency(totalAmount));

            // 本月新增数量
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);  // 本月第一天
            Date monthStart = cal.getTime();
            int monthCount = 0;
            double monthAmount = 0;
            for (Contract c : allContracts) {
                if (c.getBeginTime() != null && !c.getBeginTime().before(monthStart)) {
                    monthCount++;
                    monthAmount += c.getAmount();
                }
            }
            updateCardValue("cardMonth", String.valueOf(monthCount));

            // 待处理任务数
            int pendingCount = NotificationService.getPendingTaskCount(currentUserName);
            updateCardValue("cardPending", String.valueOf(pendingCount));

            // === 更新状态分布表 ===
            updateStatusDistribution(allContracts, totalAmount);

            // === 更新客户排名TOP10 ===
            updateCustomerRanking(allContracts);

            // === 更新时间趋势表 ===
            updateTimeTrend(allContracts);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 更新指定名称卡片的显示值
     */
    private void updateCardValue(String cardName, String value) {
        for (Component comp : getComponents()) {
            if (comp instanceof JScrollPane) {
                JPanel view = (JPanel) ((JScrollPane) comp).getViewport().getView();
                findAndUpdateCard(view, cardName, value);
            }
        }
    }

    /**
     * 递归查找并更新卡片值
     */
    private void findAndUpdateCard(Container parent, String cardName, String value) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JPanel && cardName.equals(comp.getName())) {
                JPanel card = (JPanel) comp;
                // 找到中间的大号数值标签
                for (Component child : card.getComponents()) {
                    if (child instanceof JLabel && ((JLabel) child).getFont().getSize() > 20) {
                        ((JLabel) child).setText(value);
                        break;
                    }
                }
            } else if (comp instanceof Container) {
                findAndUpdateCard((Container) comp, cardName, value);
            }
        }
    }

    /**
     * 更新状态分布表格数据
     */
    private void updateStatusDistribution(List<Contract> contracts, double totalAmount) {
        Map<String, Integer> statusCount = new LinkedHashMap<>();
        Map<String, Double> statusAmount = new LinkedHashMap<>();
        statusCount.put("起草", 0); statusAmount.put("起草", 0.0);
        statusCount.put("会签完成", 0); statusAmount.put("会签完成", 0.0);
        statusCount.put("定稿完成", 0); statusAmount.put("定稿完成", 0.0);
        statusCount.put("审批完成", 0); statusAmount.put("审批完成", 0.0);
        statusCount.put("签订完成", 0); statusAmount.put("签订完成", 0.0);

        for (Contract c : contracts) {
            String stateName = contractService.getContractStateName(c.getNum());
            if (stateName != null && statusCount.containsKey(stateName)) {
                statusCount.put(stateName, statusCount.get(stateName) + 1);
                statusAmount.put(stateName, statusAmount.get(stateName) + c.getAmount());
            }
        }

        DefaultTableModel model = getStatusTableModel();
        if (model != null) {
            model.setRowCount(0);
            for (String state : statusCount.keySet()) {
                int count = statusCount.get(state);
                double amount = statusAmount.get(state);
                String percentage = totalAmount > 0 ? String.format("%.1f%%", amount / totalAmount * 100) : "0.0%";
                model.addRow(new Object[]{state, count + "份", formatCurrency(amount) + " (" + percentage + ")"});
            }
        }
    }

    /**
     * 更新客户排名表格数据（按总金额降序排列）
     */
    private void updateCustomerRanking(List<Contract> contracts) {
        // 按客户分组统计
        Map<String, Integer> customerCount = new HashMap<>();
        Map<String, Double> customerAmount = new HashMap<>();
        for (Contract c : contracts) {
            String customer = c.getCustomer();
            if (customer == null || customer.isEmpty()) continue;
            customerCount.merge(customer, 1, Integer::sum);
            customerAmount.merge(customer, c.getAmount(), Double::sum);
        }

        // 按金额排序取前10
        List<Map.Entry<String, Double>> sortedList = new ArrayList<>(customerAmount.entrySet());
        sortedList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        if (sortedList.size() > 10) sortedList = sortedList.subList(0, 10);

        DefaultTableModel model = getCustomerRankTableModel();
        if (model != null) {
            model.setRowCount(0);
            int rank = 1;
            for (Map.Entry<String, Double> entry : sortedList) {
                String customer = entry.getKey();
                model.addRow(new Object[]{rank++, customer,
                    customerCount.getOrDefault(customer, 0) + "份",
                    formatCurrency(entry.getValue())});
            }
        }
    }

    /**
     * 更新最近6个月的趋势数据
     */
    private void updateTimeTrend(List<Contract> contracts) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();

        DefaultTableModel model = getTrendTableModel();
        if (model != null) {
            model.setRowCount(0);
            // 从6个月前开始，逐月统计
            for (int i = 5; i >= 0; i--) {
                Calendar tempCal = (Calendar) cal.clone();
                tempCal.add(Calendar.MONTH, -i);
                tempCal.set(Calendar.DAY_OF_MONTH, 1);
                Date monthStart = tempCal.getTime();
                tempCal.set(Calendar.DAY_OF_MONTH, tempCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                Date monthEnd = tempCal.getTime();

                String monthLabel = sdf.format(monthStart);
                int count = 0;
                double amount = 0;
                for (Contract c : contracts) {
                    if (c.getBeginTime() != null &&
                        !c.getBeginTime().before(monthStart) && !c.getBeginTime().after(monthEnd)) {
                        count++;
                        amount += c.getAmount();
                    }
                }
                model.addRow(new Object[]{monthLabel, count + "份", formatCurrency(amount)});
            }
        }
    }

    /**
     * 获取状态分布表的模型
     */
    private DefaultTableModel getStatusTableModel() {
        return findTableModelByName("statusTable");
    }

    /**
     * 获取客户排名表的模型
     */
    private DefaultTableModel getCustomerRankTableModel() {
        return findTableModelByName("customerRankTable");
    }

    /**
     * 获取趋势表的模型
     */
    private DefaultTableModel getTrendTableModel() {
        return findTableModelByName("trendTable");
    }

    /**
     * 根据面板名称查找对应的表格模型
     */
    private DefaultTableModel findTableModelByName(String name) {
        for (Component comp : getComponents()) {
            if (comp instanceof JScrollPane) {
                JPanel view = (JPanel) ((JScrollPane) comp).getViewport().getView();
                DefaultTableModel model = findTableModelRecursive(view, name);
                if (model != null) return model;
            }
        }
        return null;
    }

    /**
     * 递归查找指定名称的面板中的表格模型
     */
    private DefaultTableModel findTableModelRecursive(Container parent, String name) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JPanel && name.equals(comp.getName())) {
                JPanel panel = (JPanel) comp;
                for (Component child : panel.getComponents()) {
                    if (child instanceof JScrollPane) {
                        JViewport vp = ((JScrollPane) child).getViewport();
                        if (vp.getView() instanceof JTable) {
                            return (DefaultTableModel) ((JTable) vp.getView()).getModel();
                        }
                    }
                }
            } else if (comp instanceof Container) {
                DefaultTableModel result = findTableModelRecursive((Container) comp, name);
                if (result != null) return result;
            }
        }
        return null;
    }

    /**
     * 格式化货币显示
     */
    private String formatCurrency(double value) {
        if (value >= 100000000) {
            return String.format("%.2f亿", value / 100000000);
        } else if (value >= 10000) {
            return String.format("%.2f万", value / 10000);
        } else {
            return String.format("%.2f", value);
        }
    }

    /**
     * 导出为CSV文件
     */
    private void exportToCSV() {
        try {
            List<Contract> contracts = contractService.findAll();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出CSV文件");
            fileChooser.setSelectedFile(new java.io.File("合同统计数据.csv"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                DataExportUtil.exportToCSV(contracts, fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "CSV导出成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "导出失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 导出为HTML报表
     */
    private void exportToHTML() {
        try {
            List<Contract> contracts = contractService.findAll();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出HTML报表");
            fileChooser.setSelectedFile(new java.io.File("合同统计报表.html"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                DataExportUtil.exportToHTML(contracts, fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "HTML报表导出成功！\n可用浏览器打开后Ctrl+P打印为PDF。", "成功", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "导出失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
