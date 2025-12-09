package com.ydc.chess.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class TimerService {

    private Timeline timeline;
    private IntegerProperty seconds = new SimpleIntegerProperty(0);

    public TimerService() {
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> seconds.set(seconds.get() + 1))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    /** 启动一个新的计时（回合开始时调用） */
    public void startNewTimer() {
        seconds.set(0);
        timeline.playFromStart();
    }

    /** 暂停（比如游戏暂停） */
    public void pause() {
        timeline.pause();
    }

    /** 停止（比如游戏结束） */
    public void stop() {
        timeline.stop();
    }

    /** 获取当前秒数字段 */
    public IntegerProperty secondsProperty() {
        return seconds;
    }
}
