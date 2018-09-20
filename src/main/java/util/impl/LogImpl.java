package util.impl;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import util.Log;

import java.util.Properties;

public class LogImpl implements Log {
    // 日志记录器
    private static final Logger logger = Logger.getRootLogger();
    // 配置文件路径
    private String proPath;

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void init(Properties properties) {
        proPath = properties.getProperty("proPath");
        PropertyConfigurator.configure(proPath);
    }
}
