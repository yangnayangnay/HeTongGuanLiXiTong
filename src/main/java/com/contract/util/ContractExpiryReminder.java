package com.contract.util;

import java.util.*;
import com.contract.dao.ContractDao;
import com.contract.entity.Contract;
import com.contract.dao.UserDao;
import com.contract.entity.User;

/**
 * 合同到期自动提醒服务
 * <p>定期扫描即将到期的合同，通过邮件和系统内通知提醒相关人员</p>
 *
 * <h3>提醒策略：</h3>
 * <ul>
 *   <li>到期前30天：首次提醒</li>
 *   <li>到期前15天：二次提醒</li>
 *   <li>到期前7天：紧急提醒</li>
 *   <li>到期前1天：最终提醒</li>
 *   <li>已过期：逾期警告</li>
 * </ul>
 */
public class ContractExpiryReminder {

    private static Timer timer;
    /** 检查间隔：默认每6小时检查一次 */
    private static long intervalMs = 6 * 60 * 60 * 1000;
    private static boolean running = false;
    /** 用户数据访问对象（用于发送邮件时查询admin用户） */
    private static UserDao userDao = new UserDao();

    /**
     * 启动到期提醒服务
     */
    public static void start() {
        if (running) return;
        timer = new Timer("ExpiryReminder", true);
        // 首次延迟5分钟后开始，之后每隔intervalMs执行一次
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { checkAndRemind(); }
        }, 5 * 60 * 1000, intervalMs);
        running = true;
        System.out.println("[到期提醒] 服务已启动，每" + (intervalMs/3600000) + "小时检查一次");
    }

    /** 停止服务 */
    public static void stop() {
        if (timer != null) { timer.cancel(); timer = null; }
        running = false;
    }

    /** 手动触发一次检查（供测试或手动调用） */
    public static List<String> checkNow() {
        return checkAndRemind();
    }

    /**
     * 执行到期检查逻辑
     * @return 发现的到期合同信息列表
     */
    private static List<String> checkAndRemind() {
        List<String> results = new ArrayList<>();
        try {
            ContractDao contractDao = new ContractDao();
            List<Contract> allContracts = contractDao.findAll();
            Calendar now = Calendar.getInstance();

            for (Contract c : allContracts) {
                if (c.getEndTime() == null) continue;

                Calendar endDate = Calendar.getInstance();
                endDate.setTime(c.getEndTime());
                long daysLeft = (endDate.getTimeInMillis() - now.getTimeInMillis()) / (24 * 60 * 60 * 1000);

                String level;
                if (daysLeft <= -1) {
                    level = "【已逾期】";
                } else if (daysLeft <= 1) {
                    level = "【紧急-明天到期】";
                } else if (daysLeft <= 7) {
                    level = "【警告-7天内】";
                } else if (daysLeft <= 15) {
                    level = "【注意-15天内】";
                } else if (daysLeft <= 30) {
                    level = "【提示-30天内】";
                } else {
                    continue;  // 超过30天的不提醒
                }

                String msg = level + c.getName() + "(" + c.getNum() + ") 剩余" + Math.max(0, daysLeft) + "天";
                results.add(msg);
                System.out.println("[到期提醒] " + msg);
            }

            // 如果有到期合同且邮件已配置，发送汇总邮件
            if (!results.isEmpty()) {
                sendSummaryEmail(results);
            }

        } catch (Exception e) {
            System.err.println("[到期提醒] 检查异常: " + e.getMessage());
        }
        return results;
    }

    /** 发送到期汇总邮件 */
    private static void sendSummaryEmail(List<String> items) {
        try {
            // 发送给admin用户
            User admin = userDao.findByName("admin");
            if (admin != null && admin.getEmail() != null && !"default@example.com".equals(admin.getEmail())) {
                StringBuilder sb = new StringBuilder();
                sb.append("尊敬的管理员：\n\n");
                sb.append("以下合同需要关注（共").append(items.size()).append("项）：\n\n");
                for (String item : items) sb.append("- ").append(item).append("\n");
                sb.append("\n请登录系统查看详情。\n-- 合同管理系统自动发送");
                EmailService.sendWithRetry(admin.getEmail(), "admin", "",
                    "合同到期提醒", "到期提醒", 3, 3000);
            }
        } catch (Exception ignored) {}
    }
}
