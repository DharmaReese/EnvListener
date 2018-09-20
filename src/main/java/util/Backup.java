package util;

import bean.Environment;

import java.util.Collection;

/**
 * 备份模块
 * 客户端发送集合没有发送出去，对集合对象备份
 * 客户端连接不上服务器，对集合对象备份
 * 服务器接收了集合对象，写入数据库出错，对集合对象备份
 * 注意：备份都是存储在本地
 */
public interface Backup extends WossModel {
    /**
     * 将集合对象保存在文件中
     */
    void storeEnvironment(Collection<Environment> collection);

    /**
     * 将集合对象从文件中读取
     */
    Collection<Environment> loadEnvironment();

}
