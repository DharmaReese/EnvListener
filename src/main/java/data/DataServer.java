package data;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import util.IOUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 树莓派系统
 * 接收客户端发出的指令，基于指令调用传感器采集数据
 * 把采集的数据封装成指令后发给客户端
 *
 */
public class DataServer {
    // 树莓派端口
    private static final int PORT = 10000;
    // 服务器监听
    private ServerSocket server;

    public DataServer() throws IOException {
        server = new ServerSocket(PORT);
    }

    // 接收客户端请求
    public void receive(){
        while (true) {
            InputStream is = null;
            try {
                Socket socket = server.accept();
                is = socket.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder builder = new StringBuilder();
                String str = null;
                while (true) {
                    // 读取一行客户端请求
                    str = br.readLine();
                    // 拼接读取的一行客户端请求
                    builder.append(str);
                    // 读取客户端至请求末尾时，跳出循环
                    if (str.equals("</Message>")) {
                        break;
                    }
                }
                // 解析客户端请求
                String xml = parse(builder.toString());
                // 回应客户端请求
                send(socket, xml);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 关闭资源
                try {
                    IOUtil.closeInputStream(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 解析客户端请求，返回请求的数据
    private String parse(String xml) {
        InputStream is = null;
        String data = null;
        try {
            SAXReader sax = new SAXReader();
            is = new ByteArrayInputStream(xml.getBytes());
            Document document = sax.read(is);

            Element root = document.getRootElement();
            // 获取传感器地址
            String sensorAddress = root.element("SensorAddress").getText();
            // 获取对应请求的数据
            switch (sensorAddress) {
                // 温度和湿度的请求
                case "16" :
                    data = getHygrothermo();
                    break;
                // 光照强度的请求
                case "256" :
                    data = getLight();
                    break;
                // 二氧化碳浓度的请求
                case "1280" :
                    data = getCo2();
                    break;
                // 未知请求
                default :
                    System.err.println("来自客户端的未知请求类型" + sensorAddress);
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

        return data;
    }

    // 获取获取温度和湿度的数据
    private String getHygrothermo() {
        String data = random(4) + random(4) + "02";

        return getXml(data);
    }

    // 获取获取光照强度的数据
    private String getLight() {
        String data = random(4) + "03";

        return getXml(data);
    }

    // 获取获取二氧化碳浓度的数据
    private String getCo2() {
        String data = random(4) + "01";

        return getXml(data);
    }

    // 获取指定位数的随机16进制数的字符串
    private String random(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int num = (int) (Math.random() * 15 + 1);
            builder.append(Integer.toHexString(num));
        }

        return builder.toString();
    }

    // 生成回应xml
    private String getXml(String data) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Message>\n" +
                "<SrcID>100</SrcID>\n" +
                "<DstID>101</DstID>\n" +
                "<DevID>2</DevID>\n" +
                "<SensorAddress>0</SensorAddress>\n" +
                "<Counter>0</Counter>\n" +
                "<Cmd>3</Cmd>\n" +
                "<Data>" + data + "</Data>\n" +
                "<Status>1</Status>\n" +
                "</Message>\n";

        return xml;
    }

    // 以xml回应客户端的请求
    private void send(Socket socket, String xml) {
        if (xml == null) {
            return;
        }

        OutputStream os = null;
        try {
            os = socket.getOutputStream();

            os.write(xml.getBytes());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                IOUtil.closeOutputStream(os);
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // 测试
    public static void main(String[] args) {
        try {
            DataServer server = new DataServer();
            server.receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
