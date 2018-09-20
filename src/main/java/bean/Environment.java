package bean;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 每一行日志封装的对象
 * sensorAddress为16时封装两个对象：温度和湿度
 * sensorAddress为256时封装一个对象：光照强度
 * sensorAddress为1280时封装一个对象：二氧化碳浓度
 */
public class Environment implements Serializable {
    /**
     * srcId|destId|devId|sersorAddress|count|cmd|Data|status|gather_date
     * 发送端的Id|树莓派系统的Id|实验箱区域模块的Id|模块上传感器的地址|传感器的个数|发送指令的标号|服务器端发送回来的二氧化碳的值|状态码|生成这行指令的时间
     */
    private static final long serialVersionUID = 2065792256515406453L;

    // 信息类型的名称（是温度、湿度、光照强度还是二氧化碳浓度）
    private String name;
    // 客户端的ID
    private int srcId;
    // 树莓派系统的Id
    private int dstId;
    // 实验箱区域模块的Id
    private int devId;
    // 模块上传感器的地址
    private int sensorAddress;
    // 调用传感器的个数
    private int counter;
    // 发送指令的标号
    private int cmd;
    // 服务器端发送回来的信息类型的值
    private double data;
    // 状态码
    private int status;
    // 日志生成的时间
    private Timestamp gather_date;

    public Environment() {

    }

    public Environment(Environment env) {
        name = env.name;
        srcId = env.srcId;
        dstId = env.dstId;
        devId = env.devId;
        sensorAddress = env.sensorAddress;
        counter = env.counter;
        cmd = env.cmd;
        data = env.data;
        status = env.status;
        gather_date = env.gather_date;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSrcId() {
        return srcId;
    }

    public void setSrcId(int srcId) {
        this.srcId = srcId;
    }

    public int getDstId() {
        return dstId;
    }

    public void setDstId(int destId) {
        this.dstId = destId;
    }

    public int getDevId() {
        return devId;
    }

    public void setDevId(int devId) {
        this.devId = devId;
    }

    public int getSensorAddress() {
        return sensorAddress;
    }

    public void setSensorAddress(int sersorAddress) {
        this.sensorAddress = sersorAddress;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Timestamp getGather_date() {
        return gather_date;
    }

    public void setGather_date(Timestamp gather_date) {
        this.gather_date = gather_date;
    }

    @Override
    public String toString() {
        return "Environment{" +
                "name=" + name +
                ", srcId=" + srcId +
                ", dstId=" + dstId +
                ", devId=" + devId +
                ", sensorAddress=" + sensorAddress +
                ", counter=" + counter +
                ", cmd=" + cmd +
                ", data=" + data +
                ", status=" + status +
                ", gather_date=" + gather_date +
                '}';
    }
}
