package com.contract.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 手写签名板组件
 * <p>提供鼠标手写签名功能，支持清除和确认操作</p>
 *
 * 功能特点：
 * - 鼠标拖动绘制签名笔迹
 * - 支持笔触颜色和粗细配置
 * - 清除重签功能
 * - 导出为PNG图片字节数组
 * - 签名区域白色背景，黑色笔迹
 */
public class SignaturePad extends JPanel {

    /** 签名图像缓冲区 */
    private BufferedImage signatureImage;
    /** 图形绘制上下文 */
    private Graphics2D g2d;
    /** 笔触颜色 */
    private Color penColor = Color.BLACK;
    /** 笔触粗细 */
    private float penWidth = 2.0f;
    /** 上一个绘制的点坐标 */
    private Point lastPoint = null;
    /** 是否正在绘制 */
    private boolean isDrawing = false;
    /** 签名画布宽度 */
    private static final int CANVAS_WIDTH = 400;
    /** 签名画布高度 */
    private static final int CANVAS_HEIGHT = 200;

    /**
     * 构造方法：初始化签名板
     */
    public SignaturePad() {
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // 初始化签名画布
        resetCanvas();

        // 鼠标事件监听
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isDrawing = true;
                lastPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDrawing = false;
                lastPoint = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDrawing && g2d != null && lastPoint != null) {
                    g2d.setColor(penColor);
                    g2d.setStroke(new BasicStroke(penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawLine(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                    lastPoint = e.getPoint();
                    repaint();  // 重绘显示笔画
                }
            }
        });
    }

    /** 初始化/重置画布 */
    public void resetCanvas() {
        signatureImage = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g2d = signatureImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        g2d.setColor(penColor);
        g2d.setStroke(new BasicStroke(penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        repaint();
        FileLogger.info("SignaturePad", "resetCanvas", "签名画布已重置");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (signatureImage != null) {
            g.drawImage(signatureImage, 0, 0, this);
        }
    }

    /**
     * 获取签名图片的字节数组（PNG格式）
     * @return PNG编码的字节数组；如果未签名返回null
     */
    public byte[] getSignatureBytes() {
        if (signatureImage == null) return null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(signatureImage, "PNG", baos);
            byte[] result = baos.toByteArray();
            FileLogger.info("SignaturePad", "getSignatureBytes", "获取签名图片成功, 大小: " + result.length + " 字节");
            return result;
        } catch (IOException e) {
            FileLogger.error("SignaturePad", "getSignatureBytes", "获取签名图片失败: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检查是否有签名内容
     * @return true表示已有签名笔迹；false表示画布为空白
     */
    public boolean hasSignature() {
        // 简单判断：检查图像是否全白
        if (signatureImage == null) return false;
        for (int y = 0; y < signatureImage.getHeight(); y++) {
            for (int x = 0; x < signatureImage.getWidth(); x++) {
                int rgb = signatureImage.getRGB(x, y);
                Color c = new Color(rgb, true);
                if (c.getRed() < 240 || c.getGreen() < 240 || c.getBlue() < 240) {
                    return true;  // 发现非白色像素
                }
            }
        }
        return false;
    }

    // ===== getter/setter方法 =====

    /**
     * 获取当前笔触颜色
     * @return 笔触颜色
     */
    public Color getPenColor() {
        return penColor;
    }

    /**
     * 设置笔触颜色
     * @param penColor 笔触颜色
     */
    public void setPenColor(Color penColor) {
        this.penColor = penColor;
        if (g2d != null) {
            g2d.setColor(penColor);
        }
    }

    /**
     * 获取当前笔触粗细
     * @return 笔触粗细（浮点数）
     */
    public float getPenWidth() {
        return penWidth;
    }

    /**
     * 设置笔触粗细
     * @param penWidth 笔触粗细（浮点数）
     */
    public void setPenWidth(float penWidth) {
        this.penWidth = penWidth;
        if (g2d != null) {
            g2d.setStroke(new BasicStroke(penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
    }

    /**
     * 获取签名画布宽度
     * @return 画布宽度（像素）
     */
    public static int getCanvasWidth() {
        return CANVAS_WIDTH;
    }

    /**
     * 获取签名画布高度
     * @return 画布高度（像素）
     */
    public static int getCanvasHeight() {
        return CANVAS_HEIGHT;
    }
}
