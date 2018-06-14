package me.my.filedownloadhelperlib;

/**
 * description:下载文件实体类
 * Created by mingyue on 2018/5/23.
 */
public class DownloadInfo {

    //文件ID
    public int id;
    //存储位置
    public String filePath;
    //下载url
    public String downloadUrl;
    //文件总长度
    public long totalLength;
    //已下载长度
    public long readLength;
    //上次已下载长度
    public long lastReadLength;
    //下载进度
    public int progress;
    //回调监听
    public DownloadProgressListener progressListener;
    //下载状态
    public DownloadState downloadState;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DownloadInfo) {
            DownloadInfo info = (DownloadInfo) obj;
            if (downloadUrl != null && info.downloadUrl != null)
                return downloadUrl.equals(info.downloadUrl);
        }
        return false;
    }

    public int hashCode() {
        if (downloadUrl != null)
            return downloadUrl.hashCode();
        return super.hashCode();
    }
}
