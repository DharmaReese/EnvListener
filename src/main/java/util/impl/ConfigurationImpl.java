package util.impl;

import client.EnvClient;
import client.Gather;
import data.DataClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import server.DBStore;
import server.EnvSever;
import util.Backup;
import util.Configuration;
import util.ConfigurationAware;
import util.IOUtil;
import util.Log;
import util.WossModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigurationImpl implements Configuration {
    private Map<String, WossModel> map;

    public ConfigurationImpl() {
        map = new HashMap<>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("src/main/resources/config.xml");
            SAXReader sax = new SAXReader();
            Document document = sax.read(fis);

            Element root = document.getRootElement();
            List<Element> modules = root.elements();
            for (Element module : modules) {
                String className = module.attribute("class").getText();
                WossModel wossModel = (WossModel) Class.forName(className).newInstance();
                if (wossModel instanceof ConfigurationAware) {
                    ((ConfigurationAware) wossModel).setConfiguration(this);
                }

                Properties properties = new Properties();
                List<Element> attributes = module.elements();
                for (Element attribute : attributes) {
                    properties.setProperty(attribute.getName(), attribute.getText());
                }
                wossModel.init(properties);

                map.put(module.getName(), wossModel);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } finally {
            try {
                IOUtil.closeInputStream(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public DataClient getDataClient() {
        return (DataClient) map.get("DataClient");
    }

    @Override
    public Gather getGather() {
        return (Gather) map.get("Gather");
    }

    @Override
    public EnvClient getEnvClient() {
        return (EnvClient) map.get("EnvClient");
    }

    @Override
    public DBStore getDBStore() {
        return (DBStore) map.get("DBStore");
    }

    @Override
    public Backup getBackup() {
        return (Backup) map.get("Backup");
    }

    @Override
    public EnvSever getEnvServer() {
        return (EnvSever) map.get("EnvServer");
    }

    @Override
    public Log getLog() {
        return (Log) map.get("Log");
    }

}
