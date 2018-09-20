package run;

import client.impl.EnvClientImpl;
import data.impl.DataClientImpl;
import data.DataServer;
import server.EnvSever;
import util.Configuration;
import util.ThreadPoolFactory;
import util.impl.ConfigurationImpl;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.ExecutorService;

public class RunAll {

    public static void main(String[] args) {
        // 获取线程池
        ExecutorService pool = ThreadPoolFactory.newInstance().getPool();
        // 加载配置模块
        Configuration config = new ConfigurationImpl();
        // 树莓派
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DataServer server = new DataServer();
                    server.receive();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // 服务器
        pool.execute(new Runnable() {
            @Override
            public void run() {
                EnvSever envSever = config.getEnvServer();
                envSever.receive();
            }
        });
        // 与服务器交互的客户端
        pool.execute(new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer();
                // 读取日志文件中的信息发送至服务器
                timer.schedule(new EnvClientImpl.Task(), 0, EnvClientImpl.time);
            }
        });
        // 与树莓派交互的客户端
        pool.execute(new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer();
                // 发送获取温度和湿度数据的请求
                timer.schedule(new DataClientImpl.Task(16), 0, DataClientImpl.time);
                // 发送获取光照强度数据的请求
                timer.schedule(new DataClientImpl.Task(256), 0, DataClientImpl.time);
                // 发送获取二氧化碳浓度数据的请求
                timer.schedule(new DataClientImpl.Task(1280), 0, DataClientImpl.time);
            }
        });
    }
}
