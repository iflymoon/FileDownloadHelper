package me.my.filedownloadhelperlib;

/**
 * description:文件下载回调接口
 * Created by mingyue on 2018/5/17.
 */
public interface DownloadProgressListener {

    void onStartDownload(DownloadInfo downloadInfo);

    void onProgress(DownloadInfo downloadInfo, int progress, long total);

    void onDownloadComplete(DownloadInfo downloadInfo);

    void onPaused(DownloadInfo downloadInfo);

    void onCancel(DownloadInfo downloadInfo);

    void onFailed(DownloadInfo downloadInfo, String errorInfo);

}
