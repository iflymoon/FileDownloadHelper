## 最新版本号：1.0.0

# 介绍
这是一个基于RxJava和Retrofit的文件下载框架。

# 效果图展示
![](https://i.imgur.com/E2Csblx.png)

# 引入方法

- gradle引入方式：

		compile 'me.my.library:FileDownloadHelper:1.0.0'
- maven引入方式：

		<dependency>
  			<groupId>me.my.library</groupId>
  			<artifactId>FileDownloadHelper</artifactId>
  			<version>1.0.0</version>
  			<type>pom</type>
		</dependency>

- aar包下载：

    [点击下载](https://jcenter.bintray.com/me/my/library/FileDownloadHelper/1.0.0/FileDownloadHelper-1.0.0.aar)

#使用方法

- 框架配置：

    - maxTaskSize：同时下载任务数量，默认3

    - connectTimeout：网络连接超时时间

    - defaultSaveDir：文件默认存储路径
    
    代码示例：

		FileDownloadConfiguration configuration = new FileDownloadConfiguration().maxTaskSize(4).connectTimeout(15).defaultSaveDir("...");
        FileDownloadManager.config(configuration);

- 进度监听：

		DownloadProgressListener progressListener = new DownloadProgressListener() {
            @Override
            public void onStartDownload(DownloadInfo downloadInfo) {

            }

            @Override
            public void onProgress(DownloadInfo downloadInfo, int progress, long total) {

            }

            @Override
            public void onDownloadComplete(DownloadInfo downloadInfo) {

            }

            @Override
            public void onPaused(DownloadInfo downloadInfo) {

            }

            @Override
            public void onCancel(DownloadInfo downloadInfo) {

            }

            @Override
            public void onFailed(DownloadInfo downloadInfo, String errorInfo) {

            }
        };

- 单个文件下载用法：

    创建下载类：


       - id：下载文件id，用户自己定义

	   - downloadUrl：文件下载地址
	   
	   - filePath：文件存储路径
	   
	   - progressListener：下载进度监听
	   
	  代码示例：

		DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.id = "123";
        downloadInfo.downloadUrl = "http://...";
        downloadInfo.filePath = Environment.getExternalStorageDirectory() + "/test.apk";
        downloadInfo.progressListener = progressListener;
   开启下载：

		FileDownloadManager.getInstance(context).startDownload(downloadInfo);
   暂停下载：

		FileDownloadManager.getInstance(context).pauseDownload(downloadInfo);
   取消下载：

		FileDownloadManager.getInstance(context).cancelDownload(downloadInfo);
   重新下载：

		FileDownloadManager.getInstance(context).reStartDownload(downloadInfo);

- 批量文件下载用法：

    创建下载集合：

		List<DownloadInfo> downloadInfoList = new ArrayList<>();
		DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.id = "123";
        downloadInfo.downloadUrl = "http://...";
        downloadInfo.filePath = Environment.getExternalStorageDirectory() + "/test.apk";
        downloadInfo.progressListener = progressListener;
		downloadInfoList.add(downloadInfo);
   开启下载：

		FileDownloadManager.getInstance(context).startDownload(downloadInfoList);
   暂停下载：

		FileDownloadManager.getInstance(context).pauseDownload(downloadInfoList);
   取消下载：

		FileDownloadManager.getInstance(context).cancelDownload(downloadInfoList);
   重新下载：

		FileDownloadManager.getInstance(context).reStartDownload(downloadInfoList);

- 查询文件是否已下载：

		//如果本地存在该下载文件则返回文件路径，否则返回null
		FileDownloadManager.getInstance(context).getFilePath(downloadUrl);

       
