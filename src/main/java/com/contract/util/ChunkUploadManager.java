package com.contract.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 分块上传管理器
 * <p>
 * 管理大文件分块上传的完整生命周期，包括会话创建、进度追踪、
 * 分块状态标记和完整性校验。用于实现断点续传功能，
 * 当网络中断后可以从中断位置继续上传剩余分块。
 * </p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>会话管理：为每次上传创建唯一标识，管理上传状态</li>
 *   <li>进度追踪：实时记录已上传/总块数，计算上传百分比</li>
 *   <li>断点续传：记录每个分块的完成状态，支持跳过已完成分块</li>
 *   <li>完整性校验：判断所有分块是否全部上传完毕</li>
 * </ul>
 *
 * <h3>工作流程：</h3>
 * <pre>
 * 1. createUploadSession() - 创建上传会话，初始化分块信息
 * 2. 循环上传每个分块 - 每上传完一块调用 markChunkUploaded()
 * 3. getProgress() - 随时查询当前上传进度
 * 4. isComplete() - 判断是否所有分块都已上传完成
 * 5. getAllChunks() - 获取所有分块的状态列表
 * </pre>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class ChunkUploadManager {

    /**
     * 上传会话唯一标识符
     * <p>每次分块上传操作都会生成一个唯一的uploadId，
     * 用于在客户端和服务端之间关联同一个上传任务</p>
     */
    private String uploadId;
    /**
     * 上传的原始文件名
     * <p>保留用户选择的文件名，合并完成后用于还原文件</p>
     */
    private String fileName;
    /**
     * 总分块数量
     * <p>根据文件大小和设定的分块大小自动计算得出</p>
     */
    private int totalChunks;
    /**
     * 已成功上传的分块数量
     * <p>每调用一次markChunkUploaded()此值加1</p>
     */
    private int uploadedChunks;
    /**
     * 各分块的上传状态列表
     * <p>true表示该分块已上传完成，false表示未上传或上传失败</p>
     * <p>索引从0开始，对应第1块、第2块...第N块</p>
     */
    private List<Boolean> chunkStatus;
    /**
     * 原始文件的字节大小
     * <p>用于计算分块数和在UI上显示文件大小信息</p>
     */
    private long fileSize;

    /**
     * 创建一个新的分块上传会话
     * <p>
     * 初始化上传管理器的所有状态信息，包括生成唯一上传ID、
     * 计算总分块数、初始化各分块状态为"未上传"。
     * </p>
     *
     * @param fileName    要上传的文件名
     * @param totalChunks 该文件被分割成的总块数
     * @param fileSize    原始文件的总字节数
     * @return 当前ChunkUploadManager实例（支持链式调用）
     */
    public ChunkUploadManager createUploadSession(String fileName, int totalChunks, long fileSize) {
        FileLogger.info("ChunkUploadManager", "createUploadSession", "创建分块上传会话, 文件: " + fileName + ", 总块数: " + totalChunks + ", 大小: " + formatFileSize(fileSize));
        this.uploadId = "UPLOAD_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);  // 生成唯一ID
        this.fileName = fileName;           // 记录文件名
        this.totalChunks = totalChunks;     // 设置总块数
        this.fileSize = fileSize;           // 记录文件大小
        this.uploadedChunks = 0;            // 初始化已上传数为0
        // 初始化所有分块状态为false（未上传）
        this.chunkStatus = new ArrayList<>();
        for (int i = 0; i < totalChunks; i++) {
            this.chunkStatus.add(false);
        }
        System.out.println("[分块上传] 会话创建成功: " + this.uploadId +
            ", 文件: " + fileName + ", 总块数: " + totalChunks +
            ", 大小: " + formatFileSize(fileSize));
        FileLogger.info("ChunkUploadManager", "createUploadSession", "会话创建成功: " + this.uploadId);
        return this;  // 返回当前实例以支持链式调用
    }

    /**
     * 获取当前上传进度（百分比）
     * <p>
     * 计算已完成的分块占总分块数的比例，返回0~100之间的整数。
     * 如果总块数为0则返回0以避免除零错误。
     * </p>
     *
     * @return 上传进度的百分比值（0~100的整数）
     */
    public int getProgress() {
        if (totalChunks <= 0) {
            return 0;  // 无分块时返回0%
        }
        // 使用浮点数计算以确保精度，最后四舍五入取整
        return (int) Math.round((double) uploadedChunks / totalChunks * 100);
    }

    /**
     * 标记指定分块已上传完成
     * <p>
     * 当某个分块成功传输到服务端后调用此方法更新状态。
     * 如果该分块已被标记过（重复上传），不会重复计数。
     * </p>
     *
     * @param chunkIndex 分块索引（从0开始，0表示第一块）
     * @return true-标记成功；false-索引越界或已标记过
     */
    public boolean markChunkUploaded(int chunkIndex) {
        if (chunkIndex < 0 || chunkIndex >= totalChunks) {
            FileLogger.warn("ChunkUploadManager", "markChunkUploaded", "无效的分块索引: " + chunkIndex + ", 有效范围: 0-" + (totalChunks - 1));
            return false;  // 索引越界
        }
        if (chunkStatus.get(chunkIndex)) {
            FileLogger.warn("ChunkUploadManager", "markChunkUploaded", "分块 " + chunkIndex + " 已标记过，跳过重复标记");
            return false;  // 已经标记过了
        }
        // 更新该分块状态为已完成
        chunkStatus.set(chunkIndex, true);
        uploadedChunks++;  // 递增已上传计数
        System.out.println("[分块上传] 分块 " + chunkIndex + "/" + (totalChunks - 1) +
            " 上传完成, 进度: " + getProgress() + "%");
        FileLogger.info("ChunkUploadManager", "markChunkUploaded", "分块 " + chunkIndex + "/" + (totalChunks - 1) + " 上传完成, 进度: " + getProgress() + "%");
        return true;
    }

    /**
     * 判断所有分块是否已全部上传完成
     * <p>
     * 当uploadedChunks等于totalChunks时表示所有分块都已完成上传，
     * 此时可以进行分块合并操作来还原完整文件。
     * </p>
     *
     * @return true-所有分块上传完毕；false-仍有分块未完成
     */
    public boolean isComplete() {
        return uploadedChunks >= totalChunks && totalChunks > 0;
    }

    /**
     * 获取所有分块的上传状态列表
     * <p>
     * 返回分块状态的副本，外部修改不影响内部状态。
     * 可用于：
     * <ul>
     *   <li>UI显示哪些分块已完成、哪些还在传输中</li>
     *   <li>断点续传时确定需要重新上传哪些分块</li>
     * </ul>
     * </p>
     *
     * @return 各分块上传状态的列表副本（true=已完成，false=未完成）
     */
    public List<Boolean> getAllChunks() {
        return new ArrayList<>(chunkStatus);  // 返回保护性拷贝
    }

    // ==================== Getter方法 ====================

    /** 获取上传会话的唯一标识 */
    public String getUploadId() { return uploadId; }

    /** 获取上传的文件名 */
    public String getFileName() { return fileName; }

    /** 获取总分块数 */
    public int getTotalChunks() { return totalChunks; }

    /** 获取已上传的分块数 */
    public int getUploadedChunks() { return uploadedChunks; }

    /** 获取原始文件大小（字节） */
    public long getFileSize() { return fileSize; }

    // ==================== 工具方法 ====================

    /**
     * 格式化文件大小为可读字符串
     * <p>
     * 将字节数转换为人类可读的格式（B/KB/MB/GB），
     * 用于在界面上显示文件大小信息。
     * </p>
     *
     * @param size 文件大小（字节数）
     * @return 格式化后的字符串，如 "1.5 MB"、"320 KB"
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }

    /**
     * 获取尚未上传完成的分块索引列表
     * <p>
     * 用于断点续传场景：当上传中断后恢复时，
     * 通过此方法获取还需要重新上传的分块编号。
     * </p>
     *
     * @return 未完成上传的分块索引列表
     */
    public List<Integer> getPendingChunks() {
        List<Integer> pending = new ArrayList<>();
        for (int i = 0; i < totalChunks; i++) {
            if (!chunkStatus.get(i)) {
                pending.add(i);  // 收集所有未完成的分块索引
            }
        }
        return pending;
    }

    /**
     * 重置指定分块的状态为未上传
     * <p>
     * 当某分块上传失败需要重试时调用此方法。
     * 同时递减已上传计数以保持一致性。
     * </p>
     *
     * @param chunkIndex 需要重置的分块索引
     * @return true-重置成功；false-索引越界或该分块本身未完成
     */
    public boolean resetChunk(int chunkIndex) {
        if (chunkIndex < 0 || chunkIndex >= totalChunks) {
            return false;
        }
        if (!chunkStatus.get(chunkIndex)) {
            return false;  // 本身就是未完成状态
        }
        chunkStatus.set(chunkIndex, false);
        uploadedChunks--;
        FileLogger.info("ChunkUploadManager", "resetChunk", "重置分块 " + chunkIndex + " 状态为未上传");
        return true;
    }
}
