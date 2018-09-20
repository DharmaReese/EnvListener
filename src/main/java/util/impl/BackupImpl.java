package util.impl;

import bean.Environment;
import util.Backup;
import util.Configuration;
import util.ConfigurationAware;
import util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Properties;

public class BackupImpl implements Backup, ConfigurationAware {
    // 配置模块
    private Configuration config;
    // 备份文件路径
    String backupPath;

    public BackupImpl() {

    }

    @Override
    public void storeEnvironment(Collection<Environment> collection) {
        File file = new File(backupPath);
        // 在将本次信息备份之前，先读取并合并上一次的备份信息
        if (file.length() > 1) {
            Collection<Environment> backupColl = loadEnvironment();
            collection.addAll(backupColl);
        }

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);

            oos.writeObject(collection);
            oos.flush();
            config.getLog().info("写入备份文件信息" + collection.size() + "行成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                IOUtil.closeOutputStream(oos, fos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Collection<Environment> loadEnvironment() {
        File file = new File(backupPath);
        Collection<Environment> collection = null;
        if (file.length() > 1) {
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(file);
                ois = new ObjectInputStream(fis);
                
                collection = (Collection<Environment>) ois.readObject();
                config.getLog().info("读取备份文件信息" + collection.size() + "行成功");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                FileOutputStream fos = null;
                try {
                    IOUtil.closeInputStream(ois, fis);
                    // 读取结束后清空备份文件
                    fos = new FileOutputStream(file);
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
        }

        return collection;
    }

    @Override
    public void init(Properties properties) {
        backupPath = properties.getProperty("backupPath");
    }

    @Override
    public void setConfiguration(Configuration config) {
        this.config = config;
    }
}
