package com.yhy.erouter.common;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 10:58
 * version: 1.0.0
 * desc   : 日志打印器
 */
public class Logger {
    private Messager msg;

    /**
     * 构造函数
     *
     * @param messager 消息
     */
    public Logger(Messager messager) {
        msg = messager;
    }

    /**
     * 打印普通信息
     *
     * @param info 信息
     */
    public void info(CharSequence info) {
        if (StringUtils.isNotEmpty(info)) {
            msg.printMessage(Diagnostic.Kind.NOTE, EConsts.PREFIX_OF_LOGGER + info);
        }
    }

    /**
     * 打印错误信息
     *
     * @param error 信息
     */
    public void error(CharSequence error) {
        if (StringUtils.isNotEmpty(error)) {
            msg.printMessage(Diagnostic.Kind.ERROR, EConsts.PREFIX_OF_LOGGER + "An exception is encountered, [" + error + "]");
        }
    }

    /**
     * 打印错误信息
     *
     * @param error 异常
     */
    public void error(Throwable error) {
        if (null != error) {
            msg.printMessage(Diagnostic.Kind.ERROR, EConsts.PREFIX_OF_LOGGER + "An exception is encountered, [" + error.getMessage() + "]" + "\n" + formatStackTrace(error.getStackTrace()));
        }
    }

    /**
     * 打印警告信息
     *
     * @param warning 信息
     */
    public void warning(CharSequence warning) {
        if (StringUtils.isNotEmpty(warning)) {
            msg.printMessage(Diagnostic.Kind.WARNING, EConsts.PREFIX_OF_LOGGER + warning);
        }
    }

    /**
     * 格式化
     *
     * @param stackTrace 输出栈
     * @return 结果
     */
    private String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            sb.append("    at ").append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
