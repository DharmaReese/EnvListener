package util;

/**
 * 日志模块
 * 记录整个程序的运行流程
 * 四个级别：debug info warn error
 */
public interface Log extends WossModel {

    /**
     * 输出debug级别的日志
     */
    void debug(String msg);

    /**
     * 输出info级别的日志
     */
    void info(String msg);

    /**
     * 输出warn级别的日志
     */
    void warn(String msg);

    /**
     * 输出error级别的日志
     */
    void error(String msg);
}
