package com.contract.util;

import javax.sound.sampled.*;
import java.io.*;

/**
 * 提示音播放工具类
 * <p>
 * 根据系统设置中配置的提示音类型，播放相应的系统通知音效。
 * 支持多种提示音风格和静音模式。
 * </p>
 *
 * @author 合同管理系统
 * @version 1.0
 */
public class SoundUtil {

    /**
     * 播放系统提示音
     * <p>
     * 根据AppSettingsUtil中保存的soundEffect设置选择不同的提示音：
     * <ul>
     *   <li>提示音1 - 高音短促</li>
     *   <li>提示音2 - 低音较长</li>
     *   <li>系统默认 - 使用Toolkit蜂鸣</li>
     *   <li>静音 - 不播放任何声音</li>
     * </ul>
     * </p>
     */
    public static void playNotificationSound() {
        String soundType = AppSettingsUtil.loadSetting("soundEffect", "系统默认");
        if ("静音".equals(soundType)) return;

        try {
            switch (soundType) {
                case "提示音1":
                    playTone(800, 200);  // 高音短促
                    break;
                case "提示音2":
                    playTone(440, 300);  // 低音较长
                    break;
                default:
                    // 系统默认：使用Toolkit蜂鸣
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    break;
            }
            FileLogger.info("SoundUtil", "playNotificationSound", "播放提示音: " + soundType);
        } catch (Exception e) {
            FileLogger.warn("SoundUtil", "playNotificationSound", "播放提示音失败: " + e.getMessage());
        }
    }

    /**
     * 使用MIDI合成器播放简单音调
     * <p>
     * 通过javax.sound.sampled生成指定频率和时长的简单正弦波音调。
     * 如果生成失败则回退到系统蜂鸣。
     * </p>
     *
     * @param frequency 音调频率（Hz）
     * @param duration  持续时间（毫秒）
     */
    private static void playTone(int frequency, int duration) {
        try {
            // 生成正弦波音频数据
            float sampleRate = 8000f;
            int numSamples = (int) (sampleRate * duration / 1000);
            byte[] buffer = new byte[numSamples];
            for (int i = 0; i < numSamples; i++) {
                double angle = 2.0 * Math.PI * i / (sampleRate / frequency);
                buffer[i] = (byte) (Math.sin(angle) * 127.0 * 0.5);  // 50%音量
            }
            // 播放音频
            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (Exception e) {
            // 播放失败时回退到系统蜂鸣
            try {
                java.awt.Toolkit.getDefaultToolkit().beep();
            } catch (Exception ignored) {}
        }
    }
}
