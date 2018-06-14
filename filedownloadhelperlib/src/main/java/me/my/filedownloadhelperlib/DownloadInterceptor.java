package me.my.filedownloadhelperlib;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * description:带进度文件下载拦截器
 * Created by mingyue on 2018/5/17.
 */
public class DownloadInterceptor implements Interceptor {

    private Context mContext;
    private DownloadInfo downloadInfo;

    public DownloadInterceptor(Context context, DownloadInfo downloadInfo) {
        mContext = context;
        this.downloadInfo = downloadInfo;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder().body(
                new DownloadResponseBody(mContext, response.body(), downloadInfo)).build();
    }

}
