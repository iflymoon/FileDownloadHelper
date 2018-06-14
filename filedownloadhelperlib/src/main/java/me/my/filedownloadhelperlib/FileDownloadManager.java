package me.my.filedownloadhelperlib;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.MemoryHandler;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.ResourceSubscriber;
import me.my.filedownloadhelperlib.db.DownloadCacheUtil;
import me.my.filedownloadhelperlib.permission.PermissionUtil;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * description:文件下载工具类
 * Created by mingyue on 2018/5/22.
 */
public class FileDownloadManager {

    private final String TAG = "FileDownloadManager";
    private Context mContext;
    //同时下载任务数量，默认3
    private static int maxTaskSize = 3;
    //网络连接超时时间
    private static int connectTimeout = 15;
    //文件默认存储路径
    private static String defaultSaveDir;
    //下载数据队列
    private List<DownloadInfo> downloadingList;
    //待下载数据队列
    private List<DownloadInfo> prepareDownloadList;
    //回调队列
    private HashMap<String, ResourceSubscriber> subscriberMap;
    //保存每一个文件下载对应的下载类，以便于记录下载的状态
    private HashMap<String, DownloadInfo> downloadInfoMap;
    private static FileDownloadManager mInstance;

    private FileDownloadManager(Context context) {
        mContext = context;
        downloadingList = new ArrayList<>();
        prepareDownloadList = new ArrayList<>();
        subscriberMap = new HashMap<>();
        downloadInfoMap = new HashMap<>();
    }

    public static FileDownloadManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (FileDownloadManager.class) {
                if (mInstance == null) {
                    mInstance = new FileDownloadManager(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 框架配置
     */
    public static void config(FileDownloadConfiguration configuration) {
        if (configuration != null) {
            if (configuration.maxTaskSize > 0)
                maxTaskSize = configuration.maxTaskSize;
            if (configuration.connectTimeout > 5)
                connectTimeout = configuration.connectTimeout;
            if (!TextUtils.isEmpty(configuration.defaultSaveDir))
                defaultSaveDir = configuration.defaultSaveDir;
        }
    }

    /**
     * 根据下载地址获取文件本地路径
     *
     * @param downloadUrl 文件下载地址
     * @return 如果本地存在该下载文件则返回文件路径，否则返回null
     */
    public String getFilePath(String downloadUrl) {
        if (!TextUtils.isEmpty(downloadUrl)) {
            DownloadInfo tempInfo = DownloadCacheUtil.queryFile(mContext, downloadUrl);
            if (tempInfo != null
                    && tempInfo.readLength > 0
                    && tempInfo.readLength == tempInfo.totalLength
                    && !TextUtils.isEmpty(tempInfo.filePath)
                    && new File(tempInfo.filePath).exists())
                return tempInfo.filePath;
        }
        return null;
    }

    /**
     * 开始下载某地址的文件
     *
     * @param downloadUrl 文件下载地址
     * @param fileName 文件名
     * @param progressListener 回调
     */
    public void startDownload(String downloadUrl, String fileName, DownloadProgressListener progressListener) {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.downloadUrl = downloadUrl;
        //检测SD卡是否可用
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "sd卡未挂载！");
            return;
        } else {
            if (TextUtils.isEmpty(defaultSaveDir)) {
                defaultSaveDir = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/FileDownloadManager/";
            }
        }
        downloadInfo.filePath = defaultSaveDir + fileName;
        downloadInfo.progressListener = progressListener;
        download(downloadInfo);
    }

    /**
     * 开始下载某地址的文件
     *
     * @param downloadUrl 文件下载地址
     * @param fileName 文件名
     */
    public void startDownload(String downloadUrl, String fileName) {
        startDownload(downloadUrl, fileName, null);
    }

    /**
     * 批量下载
     *
     * @param downloadList 文件下载集合
     */
    public void startDownload(List<DownloadInfo> downloadList) {
        if (downloadList != null && downloadList.size() > 0) {
            Observable
                    .fromIterable(downloadList)
                    .subscribe(new Consumer<DownloadInfo>() {
                        @Override
                        public void accept(DownloadInfo downloadInfo) throws Exception {
                            download(downloadInfo);
                        }
                    });
        }
    }

    /**
     * 批量重新下载文件
     *
     * @param downloadList 文件下载集合
     */
    public void reStartDownload(List<DownloadInfo> downloadList) {
        if (downloadList != null && downloadList.size() > 0) {
            Observable
                    .fromIterable(downloadList)
                    .subscribe(new Consumer<DownloadInfo>() {
                        @Override
                        public void accept(DownloadInfo downloadInfo) throws Exception {
                            reStartDownload(downloadInfo);
                        }
                    });
        }
    }

    /**
     * 批量暂停下载
     *
     * @param downloadList 文件下载集合
     */
    public void pauseDownload(List<DownloadInfo> downloadList) {
        if (downloadList != null && downloadList.size() > 0) {
            Observable
                    .fromIterable(downloadList)
                    .subscribe(new Consumer<DownloadInfo>() {
                        @Override
                        public void accept(DownloadInfo downloadInfo) throws Exception {
                            pauseDownload(downloadInfo);
                        }
                    });
        }
    }

    /**
     * 批量取消下载
     *
     * @param downloadList 文件下载集合
     */
    public void cancelDownload(List<DownloadInfo> downloadList) {
        if (downloadList != null && downloadList.size() > 0) {
            Observable
                    .fromIterable(downloadList)
                    .subscribe(new Consumer<DownloadInfo>() {
                        @Override
                        public void accept(DownloadInfo downloadInfo) throws Exception {
                            cancelDownload(downloadInfo);
                        }
                    });
        }
    }

    /**
     * 开始下载文件
     *
     * @param downloadInfo 下载文件信息
     */
    public synchronized void startDownload(DownloadInfo downloadInfo) {
        download(downloadInfo);
    }

    /**
     * 重新下载文件
     *
     * @param downloadInfo 下载文件信息
     */
    public synchronized void reStartDownload(DownloadInfo downloadInfo) {
        cancelDownload(downloadInfo);
        download(downloadInfo);
    }

    /**
     * 暂停下载
     *
     * @param downloadInfo 下载文件信息
     */
    public synchronized void pauseDownload(DownloadInfo downloadInfo) {
        if (downloadInfo != null && !TextUtils.isEmpty(downloadInfo.downloadUrl) && subscriberMap.containsKey(downloadInfo.downloadUrl)) {
            ResourceSubscriber subscriber = subscriberMap.get(downloadInfo.downloadUrl);
            //取消网络请求
            subscriber.dispose();
            subscriberMap.remove(downloadInfo.downloadUrl);
            if (downloadInfoMap.containsKey(downloadInfo.downloadUrl)) {
                DownloadInfo tempDownloadInfo = downloadInfoMap.get(downloadInfo.downloadUrl);
                if (downloadingList.contains(tempDownloadInfo))
                    downloadingList.remove(tempDownloadInfo);
                //暂停下载则更新数据库中文件下载状态
                DownloadCacheUtil.updateFile(mContext, tempDownloadInfo);
                tempDownloadInfo.downloadState = DownloadState.PAUSE;
                if (tempDownloadInfo.progressListener != null)
                    tempDownloadInfo.progressListener.onPaused(tempDownloadInfo);
            }
        }
    }

    /**
     * 取消下载（删除已下载文件）
     *
     * @param downloadInfo 下载文件信息
     */
    public synchronized void cancelDownload(DownloadInfo downloadInfo) {
        //如果还在下载中就取消下载的网络请求
        if (downloadInfo != null && !TextUtils.isEmpty(downloadInfo.downloadUrl) && subscriberMap.containsKey(downloadInfo.downloadUrl)) {
            ResourceSubscriber subscriber = subscriberMap.get(downloadInfo.downloadUrl);
            subscriber.dispose();
            subscriberMap.remove(downloadInfo.downloadUrl);
        }

        if (downloadInfo != null && !TextUtils.isEmpty(downloadInfo.downloadUrl)) {
            DownloadInfo tempInfo = DownloadCacheUtil.queryFile(mContext, downloadInfo.downloadUrl);
            if (tempInfo != null) {
                //删除、取消下载则删除数据库中文件下载记录，同时删除本地文件
                DownloadCacheUtil.deleteFile(mContext, tempInfo);
                File file = new File(tempInfo.filePath);
                if (file.exists())
                    file.delete();
            }
            DownloadInfo tempDownloadInfo = downloadInfoMap.get(downloadInfo.downloadUrl);
            if (tempDownloadInfo != null && tempDownloadInfo.progressListener != null)
                tempDownloadInfo.progressListener.onCancel(tempDownloadInfo);
            downloadInfoMap.remove(downloadInfo.downloadUrl);
            if (downloadingList.contains(tempDownloadInfo))
                downloadingList.remove(tempDownloadInfo);
        }

    }

    /**
     * 开始下载
     *
     * @param downloadInfo 下载实体
     */
    private synchronized void download(final DownloadInfo downloadInfo) {
        //检测是否有读写权限
        if (!PermissionUtil.checkPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.e(TAG, "Manifest.permission.WRITE_EXTERNAL_STORAGE is denyed!");
            PermissionUtil.requestPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE, 999);
            return;
        }

        //检测SD卡是否可用
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "sd卡未挂载！");
            return;
        }

        //判断下载地址是否合法
        if (downloadInfo == null
                || TextUtils.isEmpty(downloadInfo.downloadUrl)
                || getBasUrl(downloadInfo.downloadUrl) == null
                || TextUtils.isEmpty(downloadInfo.filePath)) {
            Log.e(TAG, "downloadInfo is null or downloadUrl is null or baseUrl is error or filePath is null!");
            return;
        }

        if (downloadInfoMap.containsKey(downloadInfo.downloadUrl)) {
            DownloadInfo info = downloadInfoMap.get(downloadInfo.downloadUrl);
            if (info.downloadState != null && info.downloadState == DownloadState.START || info.downloadState == DownloadState.DOWNLOADING) {
                Log.e(TAG, "downloadInfo is downloading!");
                return;
            }
        }

        //超过最大下载数不执行下载操作
        if (downloadingList.size() >= maxTaskSize) {
            //待下载队列不包含该下载则添加入待下载队列
            if (!prepareDownloadList.contains(downloadInfo))
                prepareDownloadList.add(downloadInfo);
            //待下载队列包含该下载则将该下载置于待下载末位
            else {
                prepareDownloadList.remove(downloadInfo);
                prepareDownloadList.add(downloadInfo);
            }
            return;
        }

        downloadInfo.lastReadLength = 0;
        downloadInfo.readLength = 0;
        downloadInfo.totalLength = 0;
        downloadInfo.progress = 0;
        //读取上一次已经下载的长度
        DownloadInfo tempInfo = DownloadCacheUtil.queryFile(mContext, downloadInfo.downloadUrl);
        //文件已下载完成
        if (tempInfo != null && tempInfo.readLength != 0 && tempInfo.totalLength == tempInfo.readLength && new File(tempInfo.filePath).exists()) {
            downloadInfo.downloadState = DownloadState.FINISH;
            if (downloadInfo.progressListener != null) {
                downloadInfo.progress = 100;
                downloadInfo.totalLength = tempInfo.totalLength;
                downloadInfo.readLength = tempInfo.readLength;
                downloadInfo.progressListener.onProgress(downloadInfo, downloadInfo.progress, downloadInfo.totalLength);
                downloadInfo.progressListener.onDownloadComplete(downloadInfo);
                return;
            }
        }
        //文件下载过，但是没有下载完
        else if (tempInfo != null && tempInfo.readLength != 0 && new File(tempInfo.filePath).exists())
            downloadInfo.lastReadLength = tempInfo.readLength;
        //文件从头开始下载
        else
            DownloadCacheUtil.insertFile(mContext, downloadInfo);
        downloadInfoMap.put(downloadInfo.downloadUrl, downloadInfo);

        ResourceSubscriber subscriber = new ResourceSubscriber<DownloadInfo>() {

            @Override
            protected void onStart() {
                downloadingList.add(downloadInfo);
                if (downloadInfo.progressListener != null) {
                    downloadInfo.progressListener.onStartDownload(downloadInfo);
                    downloadInfo.downloadState = DownloadState.START;
                }
            }

            @Override
            public void onNext(DownloadInfo downloadInfo) {
            }

            @Override
            public void onError(Throwable t) {
                errorDeal(downloadInfo, t.getMessage());
            }

            @Override
            public void onComplete() {
            }
        };
        subscriberMap.put(downloadInfo.downloadUrl, subscriber);

        String baseUrl = getBasUrl(downloadInfo.downloadUrl);
        DownloadInterceptor mInterceptor = new DownloadInterceptor(mContext, downloadInfo);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(mInterceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        retrofit.create(DownloadService.class)
                .download("bytes=" + downloadInfo.lastReadLength + "-", downloadInfo.downloadUrl.replace(baseUrl, ""))
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (!TextUtils.isEmpty(downloadInfo.downloadUrl))
                            subscriberMap.remove(downloadInfo.downloadUrl);

                        downloadingList.remove(downloadInfo);
                        if (prepareDownloadList.size() > 0) {
                            download(prepareDownloadList.get(0));
                            prepareDownloadList.remove(0);
                        }
                        Log.d(TAG, "剩余下载数=" + prepareDownloadList.size());
                    }
                })
                .map(new Function<ResponseBody, DownloadInfo>() {
                    @Override
                    public DownloadInfo apply(ResponseBody responseBody) {
                        writeFile(responseBody, downloadInfo);
                        return downloadInfo;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

    }

    /**
     * 下载出错处理
     */
    private void errorDeal(DownloadInfo downloadInfo, String errorMsg) {
        if (downloadInfo.progressListener != null) {
            downloadInfo.progressListener.onFailed(downloadInfo, errorMsg);
            downloadInfo.downloadState = DownloadState.ERROR;
            DownloadCacheUtil.updateFile(mContext, downloadInfo);

            downloadingList.remove(downloadInfo);
            if (prepareDownloadList.size() > 0) {
                download(prepareDownloadList.get(0));
                prepareDownloadList.remove(0);
            }
        }
    }

    /**
     * 写入文件
     *
     * @param responseBody  返回体
     * @param downloadInfo 下载实体
     */
    public void writeFile(ResponseBody responseBody, DownloadInfo downloadInfo) {
        try {
            File file = new File(downloadInfo.filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (downloadInfo.lastReadLength == 0 && file.exists()) {
                file.delete();
            }

            RandomAccessFile randomAccessFile;
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(randomAccessFile.length());
            byte[] buffer = new byte[1024 * 8];
            int len;
            while ((len = responseBody.byteStream().read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, len);
            }
            responseBody.byteStream().close();
            randomAccessFile.close();
        } catch (IOException e) {
            errorDeal(downloadInfo, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取BaseUrl
     */
    private String getBasUrl(String url) {
        if (url != null) {
            String head;
            int index = url.indexOf("://");
            if (index != -1) {
                head = url.substring(0, index + 3);
                url = url.substring(index + 3);
            } else {
                return null;
            }
            index = url.indexOf("/");
            if (index != -1) {
                url = url.substring(0, index + 1);
            } else {
                return null;
            }
            return head + url;
        }
        return null;
    }

}
