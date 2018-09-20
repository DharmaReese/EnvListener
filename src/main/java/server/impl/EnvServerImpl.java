package server.impl;

import bean.Environment;
import server.DBStore;
import server.EnvSever;
import util.Configuration;
import util.ConfigurationAware;
import util.IOUtil;
import util.ThreadPoolFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public class EnvServerImpl implements EnvSever, ConfigurationAware {
    // 配置模块
    private Configuration config;
    // 服务器端口
    private int port;

    public EnvServerImpl() throws IOException {

    }

    @Override
    public void receive() {
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                // 获取套接字后交给线程处理
                ExecutorService pool = ThreadPoolFactory.newInstance().getPool();
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        InputStream is = null;
                        ObjectInputStream ois = null;
                        try {
                            is = socket.getInputStream();
                            ois = new ObjectInputStream(is);
                            // 接受客户端发送的信息的集合
                            Collection<Environment> collection = (Collection<Environment>) ois.readObject();
                            // 将信息写入数据库
                            DBStore dbStore = config.getDBStore();
                            if (collection.size() > 0) {
                                dbStore.saveEnvironmentToDB(collection);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                IOUtil.closeInputStream(ois, is);
                                if (socket != null) {
                                    socket.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    @Override
    public void init(Properties properties) {
        port = Integer.parseInt(properties.getProperty("port"));
    }

    // 测试
    public static void main(String[] args) {
        try {
            EnvSever envSever = new EnvServerImpl();
            envSever.receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
