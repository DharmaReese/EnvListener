package server;

import bean.Environment;
import util.WossModel;

import java.util.Collection;

/**
 * 入库模块
 */
public interface DBStore extends WossModel {

    /**
     * 把服务器接收到的数据写入到数据库持久保存
     * 注意：数据的录入是按天插入的
     */
    void saveEnvironmentToDB(Collection<Environment> collection);

}
