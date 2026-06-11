package com.contract.util;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 纯Swing实现的日历选择器工具类
 * <p>提供一个弹出式月历视图供用户直观选择日期，无需外部依赖，完全兼容Java 1.8</p>
 *
 * <p>功能特点：</p>
 * <ul>
 *   <li>模态对话框形式的月历视图</li>
 *   <li>支持上月/下月导航和"今天"快捷按钮</li>
 *   <li>当前选中日期蓝色高亮，今天绿色边框标记</li>
 *   <li>非当月日期灰色显示</li>
 *   <li>单击或双击即可确认选择</li>
 * </ul>
 */
public class CalendarPickerUtil {

    /** 日期格式化器（yyyy-MM-dd） */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /** 星期标题（中文） */
    private static final String[] WEEK_HEADERS = {"日", "一", "二", "三", "四", "五", "六"};

    /** 选中日期背景色（蓝色） */
    private static final Color SELECTED_BG = new Color(66, 133, 244);

    /** 今天标记边框色（绿色） */
    private static final Color TODAY_BORDER = new Color(46, 204, 113);

    /** 非当月日期文字颜色（灰色） */
    private static final Color OTHER_MONTH_COLOR = new Color(180, 180, 180);

    /**
     * 显示日历选择对话框并返回选中的日期
     * <p>
     * 弹出一个模态JDialog，内含完整的月历网格供用户选择日期。
     * 用户点击日期格后关闭对话框并返回选中的Date对象。
     * </p>
     *
     * @param parent      父组件（用于定位对话框）
     * @param title       对话框标题
     * @param initialDate 初始选中的日期（可为null则使用今天）
     * @return 用户选中的Date对象；如果用户取消则返回null
     */
    public static Date showDatePicker(Component parent, String title, Date initialDate) {
        // 用数组包装结果，以便在匿名内部类中修改（Java 8兼容）
        final Date[] result = new Date[1];

        // 创建模态对话框（兼容Java 1.8：使用Frame或Dialog作为owner）
        Window ownerWindow = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog;
        if (ownerWindow instanceof Frame) {
            dialog = new JDialog((Frame) ownerWindow, title, true);
        } else if (ownerWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) ownerWindow, title, true);
        } else {
            dialog = new JDialog((Frame) null, title, true);
        }
        dialog.setResizable(false);
        dialog.setLayout(new BorderLayout(5, 5));

        // 当前显示的月份（可被导航按钮修改）
        final Calendar[] displayCal = {Calendar.getInstance()};
        // 当前选中的日期
        final Calendar[] selectedCal = {Calendar.getInstance()};

        // 设置初始选中日期
        if (initialDate != null) {
            selectedCal[0].setTime(initialDate);
            displayCal[0].setTime(initialDate);
        }

        // ===== 顶部导航区：[<上月] 年月标题 [下月>] [今天] =====
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3));

        JButton btnPrev = new JButton("<");
        btnPrev.setFont(new Font("微软雅黑", Font.BOLD, 12));
        btnPrev.setMargin(new Insets(2, 8, 2, 8));
        btnPrev.setFocusPainted(false);
        btnPrev.setToolTipText("上一月");
        navPanel.add(btnPrev);

        final JLabel lblMonthYear = new JLabel();
        lblMonthYear.setFont(new Font("微软雅黑", Font.BOLD, 14));
        navPanel.add(lblMonthYear);

        JButton btnNext = new JButton(">");
        btnNext.setFont(new Font("微软雅黑", Font.BOLD, 12));
        btnNext.setMargin(new Insets(2, 8, 2, 8));
        btnNext.setFocusPainted(false);
        btnNext.setToolTipText("下一月");
        navPanel.add(btnNext);

        JButton btnToday = new JButton("今天");
        btnToday.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        btnToday.setMargin(new Insets(2, 6, 2, 6));
        btnToday.setFocusPainted(false);
        btnToday.setForeground(new Color(46, 204, 113));
        navPanel.add(btnToday);

        dialog.add(navPanel, BorderLayout.NORTH);

        // ===== 中间日历网格区（7列×7行：星期标题+6行日期）=====
        JPanel calendarPanel = new JPanel(new GridLayout(7, 7, 2, 2));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 存储所有日期按钮的引用（用于更新高亮状态）
        final JButton[][] dayButtons = new JButton[6][7];

        // 第一行：中文星期标题
        for (String header : WEEK_HEADERS) {
            JLabel lblHeader = new JLabel(header, SwingConstants.CENTER);
            lblHeader.setFont(new Font("微软雅黑", Font.BOLD, 12));
            lblHeader.setForeground(new Color(100, 100, 100));
            lblHeader.setPreferredSize(new Dimension(36, 22));
            calendarPanel.add(lblHeader);
        }

        // 后面6行：日期格子按钮
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                final int r = row;
                final int c = col;
                JButton btnDay = new JButton();
                btnDay.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                btnDay.setMargin(new Insets(1, 1, 1, 1));
                btnDay.setFocusPainted(false);
                btnDay.setBorderPainted(true);
                btnDay.setContentAreaFilled(true);
                btnDay.setPreferredSize(new Dimension(36, 26));
                // 单击选中日期
                btnDay.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onDayButtonClicked(btnDay, r, c, displayCal, selectedCal, dayButtons, result, dialog);
                    }
                });
                // 双击确认并关闭
                btnDay.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            onDayButtonClicked(btnDay, r, c, displayCal, selectedCal, dayButtons, result, dialog);
                            dialog.dispose();
                        }
                    }
                });
                dayButtons[row][col] = btnDay;
                calendarPanel.add(btnDay);
            }
        }

        dialog.add(calendarPanel, BorderLayout.CENTER);

        // ===== 底部按钮区：确定 / 取消 =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnOK = new JButton("确定");
        btnOK.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnOK.setBackground(SELECTED_BG);
        btnOK.setOpaque(true);
        btnOK.setContentAreaFilled(true);
        btnOK.setForeground(Color.WHITE);
        btnOK.setFocusPainted(false);
        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result[0] = selectedCal[0].getTime();
                dialog.dispose();
            }
        });

        JButton btnCancel = new JButton("取消");
        btnCancel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result[0] = null;
                dialog.dispose();
            }
        });
        bottomPanel.add(btnOK);
        bottomPanel.add(btnCancel);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        // ===== 更新日历显示的方法（封装为可复用的操作）=====
        final Runnable updateCalendar = new Runnable() {
            @Override
            public void run() {
                refreshCalendarGrid(displayCal[0], selectedCal[0], lblMonthYear, dayButtons);
            }
        };

        // 导航按钮事件：上月
        btnPrev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayCal[0].add(Calendar.MONTH, -1);
                updateCalendar.run();
            }
        });

        // 导航按钮事件：下月
        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayCal[0].add(Calendar.MONTH, 1);
                updateCalendar.run();
            }
        });

        // 今天按钮：跳转到当前月份并选中今天
        btnToday.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Calendar today = Calendar.getInstance();
                displayCal[0].setTime(today.getTime());
                selectedCal[0].setTime(today.getTime());
                updateCalendar.run();
            }
        });

        // 初始渲染日历
        updateCalendar.run();

        // 设置对话框大小并居中显示
        dialog.setSize(320, 300);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    /**
     * 处理日期按钮点击事件
     * <p>更新选中日期、刷新UI高亮状态</p>
     *
     * @param btnDay      被点击的日期按钮
     * @param row         按钮所在行
     * @param col         按钮所在列
     * @param displayCal  当前显示月份的日历引用
     * @param selectedCal 当前选中日期的日历引用
     * @param dayButtons  所有日期按钮的二维数组
     * @param result      结果存储数组
     * @param dialog      对话框引用
     */
    private static void onDayButtonClicked(JButton btnDay, int row, int col,
                                           Calendar[] displayCal, Calendar[] selectedCal,
                                           JButton[][] dayButtons, Date[] result, JDialog dialog) {
        Object clientProp = btnDay.getClientProperty("dayValue");
        if (!(clientProp instanceof Integer)) return;

        int clickedDay = (Integer) clientProp;
        // 根据按钮上的日期值判断是否属于非当月日期
        Calendar clickedCal = (Calendar) displayCal[0].clone();
        clickedCal.set(Calendar.DAY_OF_MONTH, 1);  // 先设为当月1号

        // 如果点击的日期大于15但按钮文本小于15，说明是上月的日期
        int btnTextInt = Integer.parseInt(btnDay.getText());
        if (btnTextInt > 15 && clickedDay < 15) {
            // 属于下个月
            clickedCal.add(Calendar.MONTH, 1);
        } else if (btnTextInt < 15 && clickedDay > 20) {
            // 属于上个月
            clickedCal.add(Calendar.MONTH, -1);
        }

        clickedCal.set(Calendar.DAY_OF_MONTH, clickedDay);
        selectedCal[0].setTime(clickedCal.getTime());
        result[0] = clickedCal.getTime();
        // 刷新高亮状态
        refreshCalendarGrid(displayCal[0], selectedCal[0], null, dayButtons);
    }

    /**
     * 刷新日历网格的显示内容和高亮状态
     * <p>根据当前显示月份重新计算每个格子应显示的日期数字，
     * 并设置选中日期的高亮、今天的标记、非当月日期的灰色样式。</p>
     *
     * @param displayCal   当前显示的月份
     * @param selectedCal  当前选中的日期
     * @param lblMonthYear 月份标题标签（可为null表示不更新）
     * @param dayButtons   日期按钮二维数组
     */
    private static void refreshCalendarGrid(Calendar displayCal, Calendar selectedCal,
                                            JLabel lblMonthYear, JButton[][] dayButtons) {
        // 获取今天（用于"今天"标记）
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // 获取选中日期的年月日（用于高亮比对）
        int selYear = selectedCal.get(Calendar.YEAR);
        int selMonth = selectedCal.get(Calendar.MONTH);
        int selDay = selectedCal.get(Calendar.DAY_OF_MONTH);

        // 更新月份标题
        if (lblMonthYear != null) {
            lblMonthYear.setText(displayCal.get(Calendar.YEAR) + "年" + (displayCal.get(Calendar.MONTH) + 1) + "月");
        }

        // 计算当月1号是星期几（日=1, 一=2, ..., 六=7）
        Calendar cal = (Calendar) displayCal.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);  // 1=Sunday, 7=Saturday
        int startOffset = firstDayOfWeek - 1;  // 转换为0-based索引（周日=0）

        // 当月天数
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 填充日期格子（共42个格子 = 6行×7列）
        int displayDay = 1;  // 当月日期计数器
        int prevMonthDays = 0;  // 上月日期计数器
        int nextMonthDay = 1;   // 下月日期计数器

        // 计算上个月的天数（用于填充前面空格）
        Calendar prevCal = (Calendar) cal.clone();
        prevCal.add(Calendar.MONTH, -1);
        int daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 上月需要显示的天数 = startOffset
        int prevMonthStartDay = daysInPrevMonth - startOffset + 1;

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int index = row * 7 + col;
                JButton btn = dayButtons[row][col];
                btn.setText("");  // 先清空

                if (index < startOffset) {
                    // 上个月的日期（灰色显示）
                    int prevDay = prevMonthStartDay + prevMonthDays;
                    btn.setText(String.valueOf(prevDay));
                    btn.setForeground(OTHER_MONTH_COLOR);
                    btn.setBackground(Color.WHITE);
                    btn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                    btn.putClientProperty("dayValue", prevDay);
                    prevMonthDays++;
                } else if (displayDay <= daysInMonth) {
                    // 当月日期
                    btn.setText(String.valueOf(displayDay));
                    btn.putClientProperty("dayValue", displayDay);

                    // 判断是否为选中的日期
                    int dispYear = displayCal.get(Calendar.YEAR);
                    int dispMonth = displayCal.get(Calendar.MONTH);
                    boolean isSelected = (dispYear == selYear && dispMonth == selMonth && displayDay == selDay);

                    // 判断是否为今天
                    boolean isToday = (dispYear == today.get(Calendar.YEAR)
                            && dispMonth == today.get(Calendar.MONTH)
                            && displayDay == today.get(Calendar.DAY_OF_MONTH));

                    if (isSelected) {
                        // 选中日期：蓝色背景 + 白色文字
                        btn.setBackground(SELECTED_BG);
                        btn.setForeground(Color.WHITE);
                        if (isToday) {
                            // 选中且是今天：加绿色边框
                            btn.setBorder(new LineBorder(TODAY_BORDER, 2));
                        } else {
                            btn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                        }
                    } else if (isToday) {
                        // 今天但未选中：白色背景 + 绿色边框
                        btn.setBackground(Color.WHITE);
                        btn.setForeground(Color.BLACK);
                        btn.setBorder(new LineBorder(TODAY_BORDER, 2));
                    } else {
                        // 普通日期
                        btn.setBackground(Color.WHITE);
                        btn.setForeground(Color.BLACK);
                        btn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                    }

                    displayDay++;
                } else {
                    // 下个月的日期（灰色显示）
                    btn.setText(String.valueOf(nextMonthDay));
                    btn.setForeground(OTHER_MONTH_COLOR);
                    btn.setBackground(Color.WHITE);
                    btn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                    btn.putClientProperty("dayValue", nextMonthDay);
                    nextMonthDay++;
                }
            }
        }
    }

    /**
     * 简便方法：在指定文本框旁使用日历选择器
     * <p>通常配合按钮调用：点击按钮→弹出日历→选择后填入文本框（yyyy-MM-dd格式）</p>
     *
     * @param parent          父组件
     * @param targetTextField 目标文本框（填入yyyy-MM-dd格式日期）
     */
    public static void attachToButton(Component parent, final JTextField targetTextField) {
        // 此方法作为便捷入口，实际调用showDatePicker并将结果写入textField
        // 可通过按钮的ActionListener调用此方法
        Date selected = showDatePicker(parent, "选择日期", null);
        if (selected != null) {
            targetTextField.setText(DATE_FORMAT.format(selected));
        }
    }
}
