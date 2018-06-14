package me.my.filedownloadhelperlib;

import io.reactivex.Flowable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface DownloadService {

    @Streaming
    @GET
    Flowable<ResponseBody> download(@Header("range") String start, @Url String url);

}
