package com.contract;

import com.contract.util.DBUtil;
import com.contract.view.LoginFrame;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 合同管理系统主入口
 * 一键启动：自动检测数据库 → 自动初始化 → 启动GUI
 */
public class App {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 显示启动画面
        JWindow splash = showSplash();

        // 第一步：确保数据库就绪（自动检测/启动/轮询等待）
        if (!ensureDatabaseReady(splash)) {
            return; // 用户取消或超时，退出程序
        }

        // 第二步：检查并初始化表结构
        splashMsg(splash, "正在检查数据库结构...");
        if (!checkTablesExist()) {
            splashMsg(splash, "正在初始化数据库...");
            if (!initDatabase(splash)) {
                return;
            }
        }

        // 第三步：兼容性迁移（给已有表添加status列等）
        splashMsg(splash, "正在检查数据兼容性...");
        migrateDatabase();

        // 第四步：启动主界面
        splashMsg(splash, "正在启动系统...");
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        splash.dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    /**
     * 确保数据库就绪：先尝试直连 → 失败则询问启动 → 轮询等待直到真正连通
     */
    private static boolean ensureDatabaseReady(JWindow splash) {
        splashMsg(splash, "正在连接数据库...");

        // 1. 先尝试直接连接（数据库可能已经在运行）
        String result = tryConnect();
        if ("OK".equals(result)) {
            splashMsg(splash, "数据库已就绪");
            return true;
        }

        // 2. 连接失败，显示具体错误并询问用户
        splash.dispose();
        int option = JOptionPane.showConfirmDialog(null,
            "无法连接到Oracle数据库！\n\n" +
            "连接信息: scott/tiger@localhost:1521/freepdb1\n" +
            "错误原因: " + result + "\n\n" +
            "是否尝试启动数据库并自动等待就绪？",
            "数据库未连接",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (option != JOptionPane.YES_OPTION) {
            return false;
        }

        // 3. 执行start.bat启动数据库
        try {
            Process proc = Runtime.getRuntime().exec(
                "cmd /c start D:\\Java_IDEA\\HeTongGuanLiXitong\\doc\\start.bat");
            // 不等待进程结束（start.bat会打开一个新窗口）
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "启动脚本执行失败！\n" + ex.getMessage() +
                "\n\n请手动运行 doc/start.bat 启动数据库后重新打开程序。",
                "启动失败", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 4. 重新显示启动画面，开始轮询等待数据库真正就绪
        splash = showSplash();

        int maxRetries = 60;       // 最多重试60次
        int retryInterval = 5000;  // 每次间隔5秒，总计最多等5分钟

        for (int i = 1; i <= maxRetries; i++) {
            splashMsg(splash, "正在等待数据库启动完成... (" + i + "/" + maxRetries + ")");

            result = tryConnect();
            if ("OK".equals(result)) {
                splashMsg(splash, "数据库已就绪！");
                try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                return true;
            }

            // 每10次打印一次详细原因（避免刷屏）
            if (i % 10 == 0) {
                System.out.println("[第" + i + "次] 数据库仍未就绪: " + result);
            }

            try { Thread.sleep(retryInterval); } catch (InterruptedException ignored) {}
        }

        // 5. 超时
        splash.dispose();
        JOptionPane.showMessageDialog(null,
            "等待数据库启动超时（已等待约5分钟）！\n\n" +
            "最后一次尝试的错误: " + result + "\n\n" +
            "请手动确认Oracle服务正常运行后重新打开程序。",
            "数据库启动超时",
            JOptionPane.ERROR_MESSAGE);
        return false;
    }

    /**
     * 尝试一次完整的数据库连接测试
     * 返回 "OK" 表示成功，返回错误原因字符串表示失败
     * 注意：不仅获取Connection，还实际执行查询来确认DB完全可用
     */
    private static String tryConnect() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 不仅拿到连接，还要能执行SQL才算真正就绪
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT 1 FROM dual");
            rs.next();
            return "OK";
        } catch (java.sql.SQLRecoverableException e) {
            // 监听器没启动、实例不可用等可恢复异常
            return extractOracleError(e.getMessage());
        } catch (java.sql.SQLException e) {
            int code = e.getErrorCode();
            String msg = e.getMessage();
            if (code == 12505) return "ORA-12505: TNS监听器无法识别此连接描述符(服务名/SID不存在)";
            if (code == 12514) return "ORA-12514: TNS监听器无法解析指定的SERVICE_NAME";
            if (code == 1034)  return "ORA-01034: Oracle服务不可用";
            if (code == 17002) return "I/O错误: 无法建立网络连接(端口1521无响应)";
            if (code == 28000) return "ORA-28000: 账户被锁定";
            if (code == 1017)  return "ORA-1017: 用户名或密码无效(scott/tiger)";
            return "SQLException[" + code + "]: " + msg;
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    /**
     * 提取Oracle错误的核心信息（去掉冗余前缀）
     */
    private static String extractOracleError(String raw) {
        if (raw == null) return "未知错误(空异常)";
        // 去掉常见的驱动层包装信息
        String s = raw.replace("The Network Adapter could not establish the connection", "")
                     .replace("Io exception:", "").trim();
        if (s.isEmpty()) s = "网络无法连接(Oracle监听器可能未启动)";
        return s;
    }

    /**
     * 显示启动画面
     */
    private static JWindow showSplash() {
        JWindow splash = new JWindow();
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        panel.setBackground(new Color(44, 62, 80));

        JLabel lblTitle = new JLabel("合同管理系统", SwingConstants.CENTER);
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle, BorderLayout.NORTH);

        JLabel lblStatus = new JLabel("正在启动...", SwingConstants.CENTER);
        lblStatus.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblStatus.setForeground(new Color(189, 195, 199));
        lblStatus.setName("status");
        panel.add(lblStatus, BorderLayout.CENTER);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBackground(new Color(52, 73, 94));
        bar.setForeground(new Color(52, 152, 219));
        panel.add(bar, BorderLayout.SOUTH);

        splash.add(panel);
        splash.setSize(420, 180);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);
        return splash;
    }

    /**
     * 更新启动画面文字
     */
    private static void splashMsg(JWindow splash, String msg) {
        if (splash == null) return;
        for (Component c : splash.getContentPane().getComponents()) {
            if (c instanceof JPanel) {
                for (Component cc : ((JPanel) c).getComponents()) {
                    if ("status".equals(cc.getName()) && cc instanceof JLabel) {
                        ((JLabel) cc).setText(msg);
                        splash.repaint(); // 强制刷新UI
                    }
                }
            }
        }
    }

    /**
     * 数据库兼容性迁移：给已有t_user表添加status列（如果不存在）
     */
    private static void migrateDatabase() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();

            // 检查t_user表是否有status列
            boolean hasStatusColumn = false;
            try {
                rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM user_tab_columns WHERE table_name='T_USER' AND column_name='STATUS'");
                if (rs.next() && rs.getInt(1) > 0) {
                    hasStatusColumn = true;
                }
            } catch (Exception ignored) {}

            if (!hasStatusColumn) {
                // 添加status列，默认值为1（已通过），兼容已有用户
                stmt.execute("ALTER TABLE t_user ADD status NUMBER(1) DEFAULT 1");
                stmt.executeUpdate("UPDATE t_user SET status = 1 WHERE status IS NULL");
                System.out.println("[迁移] 已为t_user表添加status列，现有用户状态设为已通过");
            }
        } catch (Exception e) {
            // 迁移失败不影响启动，只是打印警告
            System.err.println("[迁移警告] 数据库兼容性检查异常: " + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    /**
     * 检查核心表是否已存在且有数据
     */
    private static boolean checkTablesExist() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM t_user");
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return false;
    }

    /**
     * 自动初始化数据库（从init.sql读取并逐条执行）
     */
    private static boolean initDatabase(JWindow splash) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    App.class.getClassLoader().getResourceAsStream("sql/init.sql"),
                    "UTF-8"));

            StringBuilder sqlBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) continue;
                sqlBuffer.append(line).append(" ");
                if (line.endsWith(";")) {
                    String sql = sqlBuffer.toString().trim();
                    sql = sql.substring(0, sql.length() - 1).trim();
                    sqlBuffer.setLength(0);
                    if (!sql.isEmpty()) {
                        try { stmt.execute(sql); }
                        catch (Exception e) {
                            if (!sql.toUpperCase().startsWith("DROP")) {
                                System.err.println("[SQL警告] " + e.getMessage());
                            }
                        }
                    }
                }
            }
            reader.close();
            conn.commit();

            splashMsg(splash, "数据库初始化完成！");
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            splash.dispose();
            JOptionPane.showMessageDialog(null,
                "数据库初始化失败！\n" + e.getMessage() +
                "\n\n请手动在SQL*Plus中执行:\n" +
                "@src/main/resources/sql/init.sql",
                "初始化错误", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            DBUtil.close(conn, stmt);
        }
    }
}
