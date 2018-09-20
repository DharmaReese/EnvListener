package client;

import bean.Environment;
import util.WossModel;

import java.util.Collection;

/**
 * 采集模块
 */
public interface Gather extends WossModel {
    /**
     * 对日志文件进行处理
     * 一行日志封装成1个Environment对象或2个Environment对象
     * 所有采集到的对象都封装到集合中
     */
    Collection<Environment> gather();

}
