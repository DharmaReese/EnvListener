package client;

import bean.Environment;
import util.WossModel;

import java.util.Collection;

/**
 * 客户端模块
 */
public interface EnvClient extends WossModel{
    /**
     * 发送采集模块采集到的集合对象
     */
    void send(Collection<Environment> collection);

}
