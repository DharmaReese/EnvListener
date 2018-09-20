package data;

import util.WossModel;

import java.net.Socket;

/**
 * 客户端
 * 发送指令给树莓派系统
 * 接收树莓派写回的指令
 * 把发送和接收的指令整合成一条日志文件
 */
public interface DataClient extends WossModel {

    /**
     * 向指定的树莓派传感器发送请求
     */
    void send(Socket socket, int address);

    /**
     * 接收并返回树莓派的回应xml
     */
    String receive(Socket socket);

    /**
     * 解析树莓派回应，并将结果写入日志文件
     */
    String parse(String xml);

    /**
     * 将信息写入日志文件
     */
    void save(String str);

}
