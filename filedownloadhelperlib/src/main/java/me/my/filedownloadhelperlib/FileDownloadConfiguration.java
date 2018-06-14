package me.my.filedownloadhelperlib;

/**
 * description:文件下载框架配置
 * Created by mingyue on 2018/5/29
 */
public class FileDownloadConfiguration {

    //同时下载任务数量，默认3
    public int maxTaskSize;
    //网络连接超时时间
    public int connectTimeout;
    //文件默认存储路径
    public String defaultSaveDir;

    public FileDownloadConfiguration() {}

    public FileDownloadConfiguration maxTaskSize(int maxTaskSize) {
        this.maxTaskSize = maxTaskSize;
        return this;
    }

    public FileDownloadConfiguration connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public FileDownloadConfiguration defaultSaveDir(String defaultSaveDir) {
        this.defaultSaveDir = defaultSaveDir;
        return this;
    }

}
