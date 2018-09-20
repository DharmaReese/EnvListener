package server;

import util.ConfigurationAware;
import util.WossModel;

/**
 * 服务器模块
 */
public interface EnvSever extends WossModel, ConfigurationAware {

    /**
     * 接收客户端发送过来的集合对象
     */
    void receive();

}
