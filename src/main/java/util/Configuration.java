package util;

import client.EnvClient;
import client.Gather;
import data.DataClient;
import server.DBStore;
import server.EnvSever;

/**
 * 获取各个配置模块的对象
 */
public interface Configuration {

    /**
     * 获取与树莓派交互的客户端的对象
     */
    DataClient getDataClient();

    /**
     * 获取采集模块的对象
     */
    Gather getGather();

    /**
     * 获取与服务器交互的客户端的对象
     */
    EnvClient getEnvClient();

    /**
     * 获取入库模块的对象
     */
    DBStore getDBStore();

    /**
     * 获取备份模块的对象
     */
    Backup getBackup();

    /**
     * 获取服务器的对象
     */
    EnvSever getEnvServer();

    /**
     * 获取日志模块的对象
     */
    Log getLog();
}
