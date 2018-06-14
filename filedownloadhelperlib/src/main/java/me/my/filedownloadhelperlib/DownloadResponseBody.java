package me.my.filedownloadhelperlib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import me.my.filedownloadhelperlib.db.DownloadCacheUtil;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * description:带进度返回体
 * Created by mingyue on 2018/5/17.
 */
public class DownloadResponseBody extends ResponseBody {

    private Context mContext;
    private ResponseBody responseBody;
    private DownloadInfo downloadInfo;
    private BufferedSource bufferedSource;
    private Handler mHandler;
    private int lastCallbackProgress = -1;

    public DownloadResponseBody(Context context, ResponseBody responseBody, DownloadInfo downloadInfo) {
        mContext = context;
        this.responseBody = responseBody;
        this.downloadInfo = downloadInfo;
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if (downloadInfo != null && downloadInfo.progressListener != null) {
                    if (bytesRead != -1) {
                        downloadInfo.readLength = totalBytesRead + downloadInfo.lastReadLength;
                        downloadInfo.totalLength = responseBody.contentLength() + downloadInfo.lastReadLength;
                        downloadInfo.progress = (int) (downloadInfo.readLength * 100 / downloadInfo.totalLength);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (lastCallbackProgress != downloadInfo.progress) {
                                    downloadInfo.progressListener.onProgress(downloadInfo, downloadInfo.progress, downloadInfo.totalLength);
                                    lastCallbackProgress = downloadInfo.progress;
                                    downloadInfo.downloadState = DownloadState.DOWNLOADING;
                                }

                                if (downloadInfo.progress == 100) {
                                    DownloadCacheUtil.updateFile(mContext, downloadInfo);
                                    downloadInfo.progressListener.onDownloadComplete(downloadInfo);
                                    downloadInfo.downloadState = DownloadState.FINISH;
                                }
                            }
                        });
                    }
                }
                return bytesRead;
            }
        };
    }

}
