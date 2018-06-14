package me.my.filedownloadhelper;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.my.filedownloadhelperlib.DownloadInfo;
import me.my.filedownloadhelperlib.DownloadProgressListener;
import me.my.filedownloadhelperlib.FileDownloadConfiguration;
import me.my.filedownloadhelperlib.FileDownloadManager;
import me.my.filedownloadhelperlib.permission.PermissionUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "MainActivity";
    private Context mContext;
    private DownloadProgressListener progressListener;
    private LinearLayout llMainContent;
    private TextView[] tvFileName, tvProgress;
    private ProgressBar[] progressBar;
    private Button btnAllStart, btnAllPause, btnAllRestart, btnAllCancel;
    private List<DownloadInfo> downloadInfoList;

    private static final String[] downloadUrls = {
            "http://imtt.dd.qq.com/16891/87AA26FD9FDC950CB42A16220D784B19.apk?fsname=com.dewmobile.kuaiya_5.5.5(CN)_241.apk&csr=1bbd",
            "http://imtt.dd.qq.com/16891/98D4E535D169B460473D957F2DF0652F.apk?fsname=com.tencent.zebra_2.4.4.551_33.apk&csr=1bbd",
            "http://imtt.dd.qq.com/16891/1179E8825DDC63B8DE7761FED8560B8E.apk?fsname=com.chaozh.iReaderFree_7.6.0_761.apk&csr=1bbd",
            "http://imtt.dd.qq.com/16891/594B9228E8FF5D0AD4A8DA1ACFE31CDD.apk?fsname=cn.opda.a.phonoalbumshoushou_9.11.0_3950.apk&csr=1bbd",
            "http://imtt.dd.qq.com/16891/5B10613D646F2D251CD15E168D3BC2B4.apk?fsname=com.ss.android.article.news_6.7.3_673.apk&csr=1bbd"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        //检测是否有读写权限
        PermissionUtil.checkAndRequestPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE, 999, new PermissionUtil.PermissionRequestSuccessCallBack() {
            @Override
            public void onHasPermission() {
                init();
            }
        });

    }

    private void init() {
        FileDownloadConfiguration configuration = new FileDownloadConfiguration().maxTaskSize(4);
        FileDownloadManager.config(configuration);

        progressListener = new DownloadProgressListener() {
            @Override
            public void onStartDownload(DownloadInfo downloadInfo) {
                Log.d(TAG, "id=" + downloadInfo.id + "---onStartDownload");
                try {
                    if (!TextUtils.isEmpty(downloadInfo.filePath)) {
                        tvFileName[downloadInfo.id].setText(downloadInfo.filePath.substring(downloadInfo.filePath.lastIndexOf("/") + 1));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onProgress(DownloadInfo downloadInfo, int progress, long total) {
                Log.d(TAG, "id=" + downloadInfo.id + "---onProgress=" + progress);
                tvProgress[downloadInfo.id].setText(progress + "%");
                progressBar[downloadInfo.id].setProgress(progress);
            }

            @Override
            public void onDownloadComplete(DownloadInfo downloadInfo) {
                Log.d(TAG, "id=" + downloadInfo.id + "---onDownloadComplete");
            }

            @Override
            public void onPaused(DownloadInfo downloadInfo) {
                Log.d(TAG, "id=" + downloadInfo.id + "---onPaused");
            }

            @Override
            public void onCancel(DownloadInfo downloadInfo) {
                Log.d(TAG, "id=" + downloadInfo.id + "---onCancel");
                tvProgress[downloadInfo.id].setText("0%");
                progressBar[downloadInfo.id].setProgress(0);
            }

            @Override
            public void onFailed(DownloadInfo downloadInfo, String errorInfo) {
                Log.d(TAG, "id=" + downloadInfo.id + "---onFailed=" + errorInfo);
            }
        };

        initData();
        initView();
    }

    private void initView() {
        llMainContent = findViewById(R.id.ll_main_content);
        btnAllStart = findViewById(R.id.btn_all_start);
        btnAllPause = findViewById(R.id.btn_all_pause);
        btnAllRestart = findViewById(R.id.btn_all_restart);
        btnAllCancel = findViewById(R.id.btn_all_cancel);
        btnAllStart.setOnClickListener(this);
        btnAllPause.setOnClickListener(this);
        btnAllRestart.setOnClickListener(this);
        btnAllCancel.setOnClickListener(this);
        for (int i = 0; i < downloadUrls.length; i++) {
            final int position = i;
            View childView = LayoutInflater.from(this).inflate(R.layout.item_download, null);
            progressBar[i] = childView.findViewById(R.id.progressBar);
            tvFileName[i] = childView.findViewById(R.id.tv_filename);
            tvProgress[i] = childView.findViewById(R.id.tv_progress);
            Button btnStart = childView.findViewById(R.id.btn_start);
            Button btnPause = childView.findViewById(R.id.btn_pause);
            Button btnRetry = childView.findViewById(R.id.btn_retry);
            Button btnCancel = childView.findViewById(R.id.btn_cancel);

            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileDownloadManager.getInstance(mContext).startDownload(downloadInfoList.get(position));
                }
            });

            btnPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileDownloadManager.getInstance(mContext).pauseDownload(downloadInfoList.get(position));
                }
            });

            btnRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileDownloadManager.getInstance(mContext).reStartDownload(downloadInfoList.get(position));
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileDownloadManager.getInstance(mContext).cancelDownload(downloadInfoList.get(position));
                }
            });
            llMainContent.addView(childView);
        }

    }

    private void initData() {
        tvFileName = new TextView[downloadUrls.length];
        tvProgress = new TextView[downloadUrls.length];
        progressBar = new ProgressBar[downloadUrls.length];
        downloadInfoList = new ArrayList<>();
        for (int i = 0; i < downloadUrls.length; i++) {
            DownloadInfo downloadInfo = new DownloadInfo();
            downloadInfo.id = i;
            downloadInfo.downloadUrl = downloadUrls[i];
            downloadInfo.filePath = Environment.getExternalStorageDirectory() + "/test" + i + ".apk";
            downloadInfo.progressListener = progressListener;
            downloadInfoList.add(downloadInfo);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_all_start:
                FileDownloadManager.getInstance(this).startDownload(downloadInfoList);
                break;

            case R.id.btn_all_pause:
                FileDownloadManager.getInstance(this).pauseDownload(downloadInfoList);
                break;

            case R.id.btn_all_restart:
                FileDownloadManager.getInstance(this).reStartDownload(downloadInfoList);
                break;

            case R.id.btn_all_cancel:
                FileDownloadManager.getInstance(this).cancelDownload(downloadInfoList);
                break;
        }

    }
}
