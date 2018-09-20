package util;

/**
 * 在其中某个模块中需要其他模块的对象时，让该模块实现ConfigurationAware接口，自动注入Configuration对象
 * 注意：与配置模块配合使用
 */
public interface ConfigurationAware {

    void setConfiguration(Configuration config);

}
