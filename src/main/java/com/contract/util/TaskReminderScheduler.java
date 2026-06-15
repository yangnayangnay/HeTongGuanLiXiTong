package com.contract.util;

import com.contract.dao.ContractDao;
import com.contract.dao.UserDao;
import com.contract.entity.Contract;
import com.contract.entity.User;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 定时任务提醒调度器
 * <p>
 * 每隔固定时间间隔检查用户的待办任务数量，如果发现未完成的待办任务，
 * 则通过邮件服务向用户发送提醒通知。采用Timer定时器实现后台周期性检查。
 * </p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>定期检查当前用户的待办任务数量</li>
 *   <li>当有待办任务时自动发送邮件提醒</li>
 *   <li>支持启动和停止调度服务</li>
 *   <li>使用守护线程运行，不阻塞主程序退出</li>
 * </ul>
 *
 * <h3>使用方式：</h3>
 * <pre>
 *   TaskReminderScheduler.start("userName");  // 启动定时器
 *   TaskReminderScheduler.stop();             // 停止定时器
 * </pre>
 *
 * @author 合同管理系统
 * @version 2.0
 * @since 2024-01-01
 */
public class TaskReminderScheduler {

    /** 定时器实例 */
    private static Timer timer;
    /** 当前登录用户名 */
    private static String currentUser;
    /** 默认检查间隔：30分钟（1800000毫秒） */
    private static long intervalMs = 30 * 60 * 1000;

    /**
     * 启动定时提醒服务
     * <p>
     * 先停止已有的定时器（避免重复启动），然后创建新的守护线程定时器，
     * 按照设定的间隔周期性地检查待办任务并发送邮件提醒。
     * </p>
     *
     * @param userName 当前登录用户名，用于查询待办任务和获取邮箱
     */
    public static void start(String userName) {
        FileLogger.info("TaskReminderScheduler", "start", "启动定时提醒服务, 用户: " + userName + ", 间隔: " + (intervalMs / 60000) + "分钟");
        stop();  // 先停止已有的定时器，防止重复启动
        currentUser = userName;
        timer = new Timer("TaskReminder", true);  // 守护线程，不阻止JVM退出
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndRemind();  // 执行检查和提醒逻辑
            }
        }, intervalMs, intervalMs);  // 首次延迟intervalMs后开始，之后每intervalMs执行一次
        System.out.println("[定时器] 任务提醒已启动，每" + (intervalMs / 60000) + "分钟检查一次");
        FileLogger.info("TaskReminderScheduler", "start", "定时提醒服务启动成功");
    }

    /**
     * 停止定时提醒服务
     * <p>取消当前的定时任务并释放资源</p>
     */
    public static void stop() {
        if (timer != null) {
            timer.cancel();  // 取消定时器及其所有已安排的任务
            timer = null;
            System.out.println("[定时器] 任务提醒已停止");
            FileLogger.info("TaskReminderScheduler", "stop", "定时提醒服务已停止");
        }
    }

    /**
     * 执行检查和提醒逻辑
     * <p>
     * 查询当前用户的待办任务数量，如果大于0则：
     * 1. 获取用户的邮箱地址
     * 2. 调用EmailService发送带重试机制的提醒邮件
     * </p>
     *
     * <p>异常处理：任何异常仅记录日志，不影响定时器的继续运行</p>
     */
    private static void checkAndRemind() {
        if (currentUser == null || currentUser.isEmpty()) return;
        try {
            FileLogger.info("TaskReminderScheduler", "checkAndRemind", "定时检查触发, 用户: " + currentUser);
            // 查询当前用户的待办任务数量
            int count = NotificationService.getPendingTaskCount(currentUser);
            if (count > 0) {
                // 有待办任务时，获取用户邮箱并发送提醒邮件
                UserDao userDao = new UserDao();
                User user = userDao.findByName(currentUser);
                if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                    // 获取用户的待办合同列表，尝试带附件发送邮件
                    List<NotificationService.PendingTaskInfo> pendingTasks = NotificationService.getPendingTaskDetails(currentUser);
                    boolean allSentWithAttachment = true;
                    for (NotificationService.PendingTaskInfo taskInfo : pendingTasks) {
                        // 查询合同附件数据
                        ContractDao contractDao = new ContractDao();
                        Contract contract = contractDao.findByNum(taskInfo.getConNum());
                        if (contract != null && contract.getFileData() != null && contract.getFileData().length > 0) {
                            // 有附件，使用带附件的邮件发送
                            EmailService.sendTaskNotificationWithAttachment(user.getEmail(), currentUser,
                                taskInfo.getConNum(), taskInfo.getContractName(), taskInfo.getTypeName(),
                                contract.getFileData(), contract.getFileName());
                        } else {
                            // 无附件，使用普通邮件发送
                            EmailService.sendWithRetry(user.getEmail(), currentUser,
                                taskInfo.getConNum(), taskInfo.getContractName(), taskInfo.getTypeName(), 5, 5000);
                        }
                    }
                    // 如果没有待办详情记录，回退到原来的发送方式
                    if (pendingTasks.isEmpty()) {
                        EmailService.sendWithRetry(user.getEmail(), currentUser,
                            "", "您有" + count + "个待办任务", "定时提醒", 5, 5000);
                    }
                }
                FileLogger.info("TaskReminderScheduler", "checkAndRemind", "发现" + count + "个待办任务，已发送提醒邮件");
                System.out.println("[定时器] 发现" + count + "个待办任务，已发送提醒邮件");
            } else {
                FileLogger.info("TaskReminderScheduler", "checkAndRemind", "无待办任务");
            }
        } catch (Exception e) {
            FileLogger.error("TaskReminderScheduler", "checkAndRemind", "定时检查异常: " + e.getMessage(), e);
        }
    }
}
