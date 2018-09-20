package client.impl;

import bean.Environment;
import client.Gather;
import util.Configuration;
import util.ConfigurationAware;
import util.IOUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class GatherImpl implements Gather, ConfigurationAware {
    // 配置模块
    private Configuration config;
    // 日志文件的相对路径
    private String logPath;
    // 配置文件的相对路径
    private String pointPath;
    // 读取点的配置文件
    private Properties properties;
    // 上一次日志文件读取结束的位置
    private long point;

    public GatherImpl() {

    }

    @Override
    public synchronized Collection<Environment> gather() {
        List<Environment> list = new ArrayList<>();
        RandomAccessFile in = null;
        try {
            in = new RandomAccessFile(logPath, "r");
            // 从上一次读取结束的位置继续读取
            point = getPoint();
            in.seek(point);
            int rs = 0;
            List<Byte> bytes = new ArrayList<>(50);
            while ((rs = in.read()) != -1) {
                bytes.add((byte) rs);
                // 读取至有效行的行末时，获取整行信息
                if ((rs == 10) && (bytes.size() > 3)) {
                    // 更新内存中的读取点
                    point = in.getFilePointer();
                    // 去掉\n
                    bytes.remove(bytes.size() - 1);
                    // 去掉\r
                    bytes.remove(bytes.size() - 1);
                    byte[] array = new byte[bytes.size()];
                    for (int i = 0; i < bytes.size(); i++) {
                        array[i] = bytes.get(i);
                    }
                    String str = new String(array, "UTF-8");
                    // 整合信息并放入集合
                    addEnvironment(list, str);
                    // 更新缓存集合
                    bytes = new ArrayList<>(50);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 保存读取点
        savePoint();
        if (list.size() > 0) {
            config.getLog().info("读取日志文件信息" + list.size() + "行成功");
        }

        return list;
    }

    // 拆解并整合信息为Envirionment对象，将对象放入集合
    private void addEnvironment(List<Environment> list, String str) {
        // 拆解信息
        String[] split = str.split("\\|");
        // 整合信息
        if (split.length == 9) {
            // 获取并设置相同的信息
            Environment env = new Environment();
            int srcId = Integer.parseInt(split[0]);
            env.setSrcId(srcId);
            int dstId = Integer.parseInt(split[1]);
            env.setDstId(dstId);
            int devId = Integer.parseInt(split[2]);
            env.setDevId(devId);
            int sensorAddress = Integer.parseInt(split[3]);
            env.setSensorAddress(sensorAddress);
            int counter = Integer.parseInt(split[4]);
            env.setCounter(counter);
            int cmd = Integer.parseInt(split[5]);
            env.setCmd(cmd);
            String data = split[6];
            int status = Integer.parseInt(split[7]);
            env.setStatus(status);
            long date = Long.parseLong(split[8]);
            env.setGather_date(new Timestamp(date));

            // 匹配传感器地址
            switch (sensorAddress) {
                case 16 :
                    // 温度和湿度
                    if (data.length() == 10) {
                        // 创建湿度信息
                        Environment another = new Environment(env);
                        another.setName("湿度");
                        double dataNum = Integer.parseInt(data.substring(4, 8), 16) * 0.00190735 - 6;
                        another.setData(dataNum);

                        env.setName("温度");
                        dataNum = Integer.parseInt(data.substring(0, 4), 16) * 0.00268127 - 46.85;
                        env.setData(dataNum);

                        list.add(env);
                        list.add(another);
                    } else {
                        // 非法数据长度
                        config.getLog().warn("日志文件非法数据长度：" + "温度和湿度  " + data);
                    }
                    break;
                case 256 :
                    // 光照强度
                    if (data.length() == 6) {
                        env.setName("光照强度");
                        double dataNum = Integer.parseInt(data.substring(0, 4), 16);
                        env.setData(dataNum);

                        list.add(env);
                    } else {
                        // 非法数据长度
                        config.getLog().warn("日志文件非法数据长度：" + "光照强度  " + data);
                    }
                    break;
                case 1280 :
                    // 二氧化碳浓度
                    if (data.length() == 6) {
                        env.setName("二氧化碳浓度");
                        double dataNum = Integer.parseInt(data.substring(0, 4), 16);
                        env.setData(dataNum);

                        list.add(env);
                    } else {
                        // 非法数据长度
                        config.getLog().warn("日志文件非法数据长度：" + "二氧化碳浓度  " + data);
                    }
                    break;
                default :
                    // 未知信息类型
                    config.getLog().warn("日志文件未知信息类型：" + sensorAddress);
            }
        } else {
            // 非法信息长度
            config.getLog().warn("日志文件非法信息长度：" + str);
        }
    }

    // 从本地文件中获取读取点
    private long getPoint() {
        long point = 0;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(pointPath);
            properties = new Properties();
            properties.load(fis);

            String str = properties.getProperty("point");
            point = Long.parseLong(str);
            //System.out.println(point);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                IOUtil.closeInputStream(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return point;
    }

    // 保存读取点至本地文件
    private void savePoint() {
        properties.setProperty("point", point + "");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pointPath);
            properties.store(fos, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                IOUtil.closeOutputStream(fos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 将16进制数的字符串转换为10进制数
    private int toDecimalNumber(String str) {
        char[] chars = reverseChars(str.toCharArray());
        int sum = 0;
        for (int i = 0; i < chars.length; i++) {
            int num = 0;
            if ((chars[i] >= '0') && (chars[i] <= '9')) {
                num = chars[i] - 48;
            } else {
                num = chars[i] - 87;
            }
            sum = sum + num * (int) Math.pow(16, i);
        }

        return sum;
    }

    // 反转字符数组
    private char[] reverseChars(char[] chars) {
        char[] newChars = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            newChars[i] = chars[chars.length - i - 1];
        }

        return newChars;
    }

    @Override
    public void init(Properties properties) {
        logPath = properties.getProperty("logPath");
        pointPath = properties.getProperty("pointPath");
    }

    @Override
    public void setConfiguration(Configuration config) {
        this.config = config;
    }
}
