package data.impl;

import data.DataClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import util.Configuration;
import util.ConfigurationAware;
import util.IOUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataClientImpl implements DataClient, ConfigurationAware {
    // 配置模块
    private static Configuration config;
    // 树莓派地址
    private static String host;
    // 树莓派端口
    private static int port;
    // 计时器循环时间
    public static long time;

    private String logPath;

    public static class Task extends TimerTask {
        private Lock lock = new ReentrantLock();

        private int address;

        public Task(int address) {
            this.address = address;
        }

        @Override
        public void run() {
            // 占有同步锁，保证任务串行执行
            lock.lock();

            DataClient dataClient = config.getDataClient();
            try {
                Socket socket = new Socket(host, port);
                // 向服务器发送请求
                dataClient.send(socket, address);
                // 接收服务器回应
                String xml = dataClient.receive(socket);
                // 解析服务器回应
                String result = dataClient.parse(xml);
                // 将解析结果写入日志文件
                dataClient.save(result);
            } catch (IOException e) {
                e.printStackTrace();
            }

            lock.unlock();
        }
    }

    public DataClientImpl() {

    }

    public void send(Socket socket, int address) {
        // 生成请求xml
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Message>\n" +
                "<SrcID>100</SrcID>\n" +
                "<DstID>101</DstID>\n" +
                "<DevID>2</DevID>\n" +
                "<SensorAddress>" + address + "</SensorAddress>\n" +
                "<Counter>1</Counter>\n" +
                "<Cmd>3</Cmd>\n" +
                "<Status>1</Status>\n" +
                "</Message>\n";

        // 向树莓派发送请求
        OutputStream os = null;
        try {
            os = socket.getOutputStream();

            os.write(xml.getBytes());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String receive(Socket socket) {
        OutputStream os = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder builder = new StringBuilder();
        String str = null;
        try {
            os = socket.getOutputStream();
            is = socket.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            while (true) {
                // 读取一行树莓派回应
                str = br.readLine();
                // 拼接读取的一行树莓派回应
                builder.append(str);
                // 读取树莓派回应末尾时，跳出循环
                if (str.equals("</Message>")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                IOUtil.closeOutputStream(os);
                IOUtil.closeReader(br, isr);
                IOUtil.closeInputStream(is);
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return builder.toString();
    }

    public String parse(String xml) {
        String result = null;
        InputStream is = null;
        try {
            SAXReader sax = new SAXReader();
            is = new ByteArrayInputStream(xml.getBytes());
            Document document = sax.read(is);

            Element root = document.getRootElement();
            String srcId = root.element("SrcID").getText();
            String dstId = root.element("DstID").getText();
            String devId = root.element("DevID").getText();
            String counter = root.element("Counter").getText();
            String cmd = root.element("Cmd").getText();
            String data = root.element("Data").getText();
            String status = root.element("Status").getText();
            String date = System.currentTimeMillis() + "";

            if ((data != null) && (data.length() >= 2)) {
                // 获取数据中表示信息类型的最后两位
                String str = data.substring(data.length() - 2, data.length());
                //匹配信息类型
                switch (str) {
                    // 温度和湿度
                    case "02" :
                        result = srcId + "|" + dstId + "|" + devId + "|" + "16" + "|" + counter + "|" + cmd + "|" + data + "|" + status + "|" + date;
                        break;
                    // 光照强度
                    case "03" :
                        result = srcId + "|" + dstId + "|" + devId + "|" + "256" + "|" + counter + "|" + cmd + "|" + data + "|" + status + "|" + date;
                        break;
                    // 二氧化碳浓度
                    case "01" :
                        result = srcId + "|" + dstId + "|" + devId + "|" + "1280" + "|" + counter + "|" + cmd + "|" + data + "|" + status + "|" + date;
                        break;
                    default:
                        config.getLog().warn("来自树莓派的非法数据：" + data);
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                IOUtil.closeInputStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public void save(String str) {
        if (str == null) {
            return;
        }

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        PrintWriter pw = null;
        try {
            fos = new FileOutputStream(logPath, true);
            osw = new OutputStreamWriter(fos, "UTF-8");
            pw = new PrintWriter(osw);

            pw.println(str);
            pw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                IOUtil.closeWriter(pw, osw);
                IOUtil.closeOutputStream(fos);
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
        logPath = properties.getProperty("logPath");
    }

    // 测试
    public static void main(String[] args) {
        Timer timer = new Timer();
        // 发送获取温度和湿度的请求
        timer.schedule(new Task(16), 0, time);
        // 发送获取光照强度的请求
        timer.schedule(new Task(256), 0, time);
        // 发送获取二氧化碳浓度的请求
        timer.schedule(new Task(1280), 0, time);
    }

}
