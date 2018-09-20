package server.impl;

import bean.Environment;
import server.DBStore;
import util.Configuration;
import util.ConfigurationAware;
import util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class DBStoreImpl implements DBStore, ConfigurationAware {
    // 配置模块
    private Configuration config;
    // 批处理上限
    private int batch;

    @Override
    public void saveEnvironmentToDB(Collection<Environment> collection) {
        ConnectionPool pool = ConnectionPool.getInstance();
        // 已执行的sql语句的数量
        int countOfExecute = 0;
        try {
            Connection conn = pool.getConnection();

            SimpleDateFormat formatter = new SimpleDateFormat("dd");
            PreparedStatement pstmt = null;
            // 上一次天数
            String lastDay = "0";
            // 缓存中的sql语句的数量
            int countOfBatch = 0;
            for (Environment env : collection) {
                String day = formatter.format(env.getGather_date());
                String sql = "insert into DETAIL_" + day + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                // 上一次天数为初值，或与本次天数不同时，编译sql语句
                if (!day.equals(lastDay)) {
                    pstmt = conn.prepareStatement(sql);
                    // 上一次天数不为初值，且与本次天数不同时，执行缓存中的sql语句
                    if (!lastDay.equals("0")) {
                        pstmt.executeBatch();
                        // 更新已执行的sql语句的数量
                        countOfExecute = countOfExecute + countOfBatch;
                        // 更新缓存中sql语句的数量
                        countOfBatch = 0;
                    }
                }
                // 更新上一次天数
                lastDay = day;

                pstmt.setObject(1, env.getName());
                pstmt.setObject(2, env.getSrcId());
                pstmt.setObject(3, env.getDstId());
                pstmt.setObject(4, env.getDevId());
                pstmt.setObject(5, env.getSensorAddress());
                pstmt.setObject(6, env.getCounter());
                pstmt.setObject(7, env.getCmd());
                pstmt.setObject(8, env.getData());
                pstmt.setObject(9, env.getStatus());
                pstmt.setObject(10, env.getGather_date());

                // 添加sql语句到缓存
                pstmt.addBatch();
                // 更新缓存中sql语句的数量
                countOfBatch++;
                // 缓存中每有10条sql语句时将其全部执行
                if (countOfBatch % batch == 0) {
                    pstmt.executeBatch();
                    // 更新已执行的sql语句的数量
                    countOfExecute = countOfExecute + countOfBatch;
                    // 更新缓存中sql语句的数量
                    countOfBatch = 0;
                }
            }
            // 执行缓存中剩余的sql语句
            pstmt.executeBatch();
            countOfExecute = collection.size();
            config.getLog().info("向数据库插入信息" + collection.size() + "行成功");
        } catch (SQLException e) {
            // 插入信息至数据库部分失败，将未插入的信息备份至本地文件

            // 获取还未执行的sql语句
            List<Environment> list = new ArrayList<>();
            for (int i = countOfExecute; i < collection.size(); i++) {
                list.add(((List<Environment>) collection).get(i));
            }

            config.getBackup().storeEnvironment(list);
            config.getLog().warn("向数据库插入信息" + collection.size() + "行异常：插入失败" + (collection.size() - countOfExecute) + "行");
        }
    }

    @Override
    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    @Override
    public void init(Properties properties) {
        batch = Integer.parseInt(properties.getProperty("batch"));
    }
}
