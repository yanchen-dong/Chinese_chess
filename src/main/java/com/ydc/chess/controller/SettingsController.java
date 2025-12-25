package com.ydc.chess.controller;

import com.ydc.chess.model.GameSettings;
import com.ydc.chess.ui.UIManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * 设置界面控制器
 */
public class SettingsController {
    
    @FXML private CheckBox soundCheckBox;
    @FXML private CheckBox animationCheckBox;
    @FXML private Spinner<Integer> regretSpinner;
    @FXML private Spinner<Integer> timerSpinner;
    @FXML private TextField playerNameField;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private GameSettings settings;
    
    @FXML
    public void initialize() {
        settings = GameSettings.getInstance();
        
        // 初始化控件值
        soundCheckBox.setSelected(settings.isSoundEnabled());
        animationCheckBox.setSelected(settings.isAnimationEnabled());
        
        // 悔棋次数设置 (1-10)
        SpinnerValueFactory<Integer> regretFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, settings.getMaxRegretCount());
        regretSpinner.setValueFactory(regretFactory);
        
        // 计时器设置 (5-60分钟)
        SpinnerValueFactory<Integer> timerFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 60, settings.getTimerMinutes());
        timerSpinner.setValueFactory(timerFactory);
        
        playerNameField.setText(settings.getPlayerName());
        
        // 主题选择
        themeComboBox.getItems().addAll("经典", "现代", "简约", "传统");
        themeComboBox.setValue(settings.getTheme());
    }
    
    @FXML
    public void onSaveClicked() {
        // 保存设置
        settings.setSoundEnabled(soundCheckBox.isSelected());
        settings.setAnimationEnabled(animationCheckBox.isSelected());
        settings.setMaxRegretCount(regretSpinner.getValue());
        settings.setTimerMinutes(timerSpinner.getValue());
        settings.setPlayerName(playerNameField.getText().trim());
        settings.setTheme(themeComboBox.getValue());
        
        settings.saveSettings();
        
        // 显示保存成功提示
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("设置");
        alert.setHeaderText(null);
        alert.setContentText("设置已保存！");
        alert.showAndWait();
        
        // 返回主菜单
        onCancelClicked();
    }
    
    @FXML
    public void onCancelClicked() {
        UIManager.goTo("MainMenu.fxml", "主菜单");
    }
}

