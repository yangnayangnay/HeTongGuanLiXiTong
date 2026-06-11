package com.contract.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传下载工具类
 * <p>
 * 提供合同管理系统中文件上传、下载、分块传输等核心功能。
 * 支持PDF、Word文档等常见办公文件的读写操作。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>文件读取：将本地文件读取为字节数组，用于存入数据库BLOB字段</li>
 *   <li>文件保存：将字节数组写入本地文件，用于从数据库下载附件</li>
 *   <li>文件类型校验：检查上传的文件是否为允许的类型（PDF/DOCX/DOC）</li>
 *   <li>分块处理：支持大文件的分块上传和合并，实现断点续传</li>
 * </ul>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>合同起草时上传合同附件（PDF/Word文档）</li>
 *   <li>从数据库下载已存储的合同附件</li>
 *   <li>大文件分块上传以避免内存溢出</li>
 * </ul>
 *
 * @author 合同管理系统
 * @version 1.0
 * @since 2024-01-01
 */
public class FileUploadUtil {

    /** 允许上传的文件类型扩展名列表 */
    private static final String[] ALLOWED_EXTENSIONS = {"pdf", "docx", "doc"};

    /**
     * 默认分块大小：每块1MB（1024*1024字节）
     * <p>适用于大多数网络环境下的分块传输</p>
     */
    public static final int DEFAULT_CHUNK_SIZE = 1024 * 1024;

    /**
     * 将文件读取为字节数组
     * <p>
     * 读取指定路径的完整文件内容到内存中的字节数组。
     * 适用于中小型文件（建议小于100MB），对于超大文件应使用分块方式。
     * 读取完成后自动关闭输入流，确保资源释放。
     * </p>
     *
     * @param file 要读取的文件对象（必须存在且可读）
     * @return 包含文件全部内容的字节数组；如果文件不存在或读取失败返回null
     */
    public static byte[] readFileToBytes(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            System.err.println("[文件工具] 文件无效或不存在: " + file);
            return null;
        }
        // 使用try-with-resources确保流自动关闭
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];  // 根据文件大小创建缓冲区
            fis.read(data);  // 将文件内容读入字节数组
            return data;
        } catch (IOException e) {
            System.err.println("[文件工具] 读取文件失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将字节数组保存为本地文件（用于下载）
     * <p>
     * 将内存中的字节数据写入指定的本地文件路径。
     * 如果目标文件的父目录不存在，会自动创建目录。
     * 写入完成后自动关闭输出流，确保数据完整性。
     * </p>
     *
     * @param data     要保存的字节数组（文件二进制内容）
     * @param fileName 目标文件的完整路径（包含文件名和扩展名）
     */
    public static void saveBytesToFile(byte[] data, String fileName) {
        if (data == null || data.length == 0) {
            System.err.println("[文件工具] 数据为空，无法保存文件");
            return;
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            System.err.println("[文件工具] 文件名为空");
            return;
        }
        try {
            // 确保父目录存在
            File targetFile = new File(fileName);
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();  // 递归创建所有不存在的父目录
            }
            // 使用try-with-resources确保流自动关闭
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(data);  // 将字节数组写入文件
                fos.flush();      // 强制刷新缓冲区，确保数据写入磁盘
            }
            System.out.println("[文件工具] 文件保存成功: " + fileName + " (" + data.length + " 字节)");
        } catch (IOException e) {
            System.err.println("[文件工具] 保存文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件的扩展名（小写）
     * <p>
     * 从文件名中提取扩展名部分，并统一转换为小写格式，
     * 便于进行文件类型比较和校验。例如："合同.pdf" 返回 "pdf"。
     * </p>
     *
     * @param fileName 完整文件名（可以包含路径）
     * @return 文件扩展名（小写）；如果没有扩展名则返回空字符串""
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "";
        }
        // 查找最后一个点号的位置
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            // 提取点号后的部分并转为小写
            return fileName.substring(lastDotIndex + 1).toLowerCase().trim();
        }
        return "";  // 没有找到扩展名
    }

    /**
     * 检查文件是否为允许上传的类型
     * <p>
     * 校验文件扩展名是否在系统允许的白名单中。
     * 当前支持的文件类型包括：
     * <ul>
     *   <li>PDF - 便携式文档格式</li>
     *   <li>DOCX - Microsoft Word文档（新版）</li>
     *   <li>DOC - Microsoft Word文档（旧版兼容）</li>
     * </ul>
     * 通过白名单机制防止上传恶意文件类型。
     * </p>
     *
     * @param fileName 要检查的文件名（含扩展名）
     * @return true-文件类型允许上传；false-不允许或文件名无效
     */
    public static boolean isAllowedFileType(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;  // 空文件名直接拒绝
        }
        String ext = getFileExtension(fileName);  // 获取小写扩展名
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(ext)) {
                return true;  // 在白名单中找到匹配项
            }
        }
        return false;  // 不在允许列表中
    }

    /**
     * 将完整的文件数据按指定大小分割成多个数据块
     * <p>
     * 用于大文件分块上传场景。将一个大的字节数组按照chunkSize参数
     * 切割成多个较小的块，每个块可以独立传输和存储。
     * 最后一块可能小于指定的块大小。
     * </p>
     *
     * <p>使用示例：</p>
     * <pre>
     * byte[] fullData = readFileToBytes(file);
     * byte[][] chunks = chunkFile(fullData, 1024*1024); // 每块1MB
     * // chunks[0], chunks[1], ... chunks[n-1] 分别是各分块数据
     * </pre>
     *
     * @param fullData  要分割的完整文件字节数组
     * @param chunkSize 每个分块的大小（字节），建议使用DEFAULT_CHUNK_SIZE（1MB）
     * @return 分割后的二维字节数组；如果数据为空返回null
     */
    public static byte[][] chunkFile(byte[] fullData, int chunkSize) {
        if (fullData == null || fullData.length == 0) {
            return null;  // 无效数据
        }
        if (chunkSize <= 0) {
            chunkSize = DEFAULT_CHUNK_SIZE;  // 使用默认分块大小
        }
        // 计算需要的总块数（向上取整）
        int totalChunks = (int) Math.ceil((double) fullData.length / chunkSize);
        byte[][] chunks = new byte[totalChunks][];
        // 循环切割数据块
        for (int i = 0; i < totalChunks; i++) {
            int startIndex = i * chunkSize;           // 当前块的起始位置
            // 计算当前块的结束位置（最后一块可能不足chunkSize）
            int endIndex = Math.min(startIndex + chunkSize, fullData.length);
            int currentChunkSize = endIndex - startIndex;  // 当前块的实际大小
            // 复制当前块的数据到新数组
            chunks[i] = new byte[currentChunkSize];
            System.arraycopy(fullData, startIndex, chunks[i], 0, currentChunkSize);
        }
        System.out.println("[文件工具] 文件分块完成: 共 " + totalChunks + " 块, 每块最大 " + chunkSize + " 字节");
        return chunks;
    }

    /**
     * 将多个数据块合并为完整的字节数组
     * <p>
     * 用于断点续传场景下，将接收到的多个分块数据按顺序合并还原为完整文件。
     * 要求传入的分块列表必须按正确顺序排列（即chunks.get(0)是第一块）。
     * </p>
     *
     * <p>使用示例：</p>
     * <pre>
     * List&lt;byte[]&gt; receivedChunks = getAllUploadedChunks(uploadId);
     * byte[] fullData = mergeChunks(receivedChunks);
     * saveBytesToFile(fullData, "restored_file.pdf");
     * </pre>
     *
     * @param chunks 按顺序排列的分块数据列表
     * @return 合并后的完整字节数组；如果列表为空返回null
     */
    public static byte[] mergeChunks(List<byte[]> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return null;  // 没有数据块
        }
        // 第一步：计算总长度
        int totalLength = 0;
        for (byte[] chunk : chunks) {
            if (chunk != null) {
                totalLength += chunk.length;
            }
        }
        // 第二步：创建目标数组并逐块复制
        byte[] mergedData = new byte[totalLength];
        int currentPosition = 0;  // 当前写入位置偏移量
        for (byte[] chunk : chunks) {
            if (chunk != null && chunk.length > 0) {
                System.arraycopy(chunk, 0, mergedData, currentPosition, chunk.length);
                currentPosition += chunk.length;
            }
        }
        System.out.println("[文件工具] 分块合并完成: 共 " + chunks.size() + " 块, 总计 " + totalLength + " 字节");
        return mergedData;
    }
}
