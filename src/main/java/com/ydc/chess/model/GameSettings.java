package com.ydc.chess.model;

import java.io.*;
import java.util.Properties;

/**
 * 游戏设置数据模型
 * 负责保存和加载游戏设置
 */
public class GameSettings {
    private static final String SETTINGS_FILE = "game_settings.properties";
    
    // 设置项
    private boolean soundEnabled = true;
    private boolean animationEnabled = true;
    private int maxRegretCount = 3;
    private int timerMinutes = 30;
    private String playerName = "玩家";
    private String theme = "经典";
    
    private static GameSettings instance;
    
    private GameSettings() {
        loadSettings();
    }
    
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }
    
    /**
     * 从文件加载设置
     */
    public void loadSettings() {
        Properties props = new Properties();
        File file = new File(SETTINGS_FILE);
        
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                soundEnabled = Boolean.parseBoolean(props.getProperty("soundEnabled", "true"));
                animationEnabled = Boolean.parseBoolean(props.getProperty("animationEnabled", "true"));
                maxRegretCount = Integer.parseInt(props.getProperty("maxRegretCount", "3"));
                timerMinutes = Integer.parseInt(props.getProperty("timerMinutes", "30"));
                playerName = props.getProperty("playerName", "玩家");
                theme = props.getProperty("theme", "经典");
            } catch (IOException e) {
                System.err.println("加载设置失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 保存设置到文件
     */
    public void saveSettings() {
        Properties props = new Properties();
        props.setProperty("soundEnabled", String.valueOf(soundEnabled));
        props.setProperty("animationEnabled", String.valueOf(animationEnabled));
        props.setProperty("maxRegretCount", String.valueOf(maxRegretCount));
        props.setProperty("timerMinutes", String.valueOf(timerMinutes));
        props.setProperty("playerName", playerName);
        props.setProperty("theme", theme);
        
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            props.store(fos, "游戏设置");
        } catch (IOException e) {
            System.err.println("保存设置失败: " + e.getMessage());
        }
    }
    
    // Getters and Setters
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
    
    public boolean isAnimationEnabled() {
        return animationEnabled;
    }
    
    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }
    
    public int getMaxRegretCount() {
        return maxRegretCount;
    }
    
    public void setMaxRegretCount(int maxRegretCount) {
        this.maxRegretCount = maxRegretCount;
    }
    
    public int getTimerMinutes() {
        return timerMinutes;
    }
    
    public void setTimerMinutes(int timerMinutes) {
        this.timerMinutes = timerMinutes;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
}

