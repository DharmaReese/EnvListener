package client.impl;

import bean.Environment;
import client.EnvClient;
import util.Configuration;
import util.ConfigurationAware;
import util.IOUtil;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class EnvClientImpl implements EnvClient, ConfigurationAware {
    // 配置模块
    private static Configuration config;
    // 计时器循环时间
    public static long time;
    // 服务器地址
    private String host;
    // 服务器端口
    private int port;

    public static class Task extends TimerTask {

        @Override
        public void run() {
            EnvClient envClient = config.getEnvClient();
            // 读取日志文件中的信息
            Collection<Environment> collection = config.getGather().gather();
            // 将信息的集合发送至服务器
            envClient.send(collection);
        }
    }

    public EnvClientImpl() {

    }

    @Override
    public void send(Collection<Environment> collection) {
        Socket socket = null;
        OutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            // 获取上一次备份的信息，并与本次信息合并
            Collection<Environment> backupColl = config.getBackup().loadEnvironment();
            if (backupColl != null) {
                collection.addAll(backupColl);
            }

            socket = new Socket(host, port);
            os = socket.getOutputStream();
            oos = new ObjectOutputStream(os);

            oos.writeObject(collection);
            oos.flush();
            if (collection.size() > 0) {
                config.getLog().info("向服务器发送信息" + collection.size() + "行成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (collection.size() > 0) {
                // 发送信息至服务器失败，将信息备份至本地文件
                config.getLog().warn("向服务器发送信息" + collection.size() + "行失败");
                config.getBackup().storeEnvironment(collection);
            }
        } finally {
            // 关闭资源
            try {
                IOUtil.closeOutputStream(oos, os);
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    @Override
    public void init(Properties properties) {
        host = properties.getProperty("host");
        port = Integer.parseInt(properties.getProperty("port"));
        time = Long.parseLong(properties.getProperty("time"));
    }

    // 测试
    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.schedule(new Task(), 0, time);
    }

}
