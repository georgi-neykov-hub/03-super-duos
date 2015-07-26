package it.jaschke.alexandria;

import android.app.Application;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.api.Constants;
import it.jaschke.alexandria.api.GoogleBooksAPI;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class AlexandriaApplication extends Application {
    private static AlexandriaApplication sInstance;

    public static AlexandriaApplication getInstance() {
        return sInstance;
    }

    private GoogleBooksAPI mGoogleBooksAPI;
    private OkHttpClient mHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        setupPicasso(getOkHttpClient());
        setupEventBus();

        sInstance = this;
    }

    private void setupEventBus() {
        EventBus.builder()
                .sendNoSubscriberEvent(false)
                .throwSubscriberException(false)
                .sendSubscriberExceptionEvent(false)
                .installDefaultEventBus();
    }

    public GoogleBooksAPI getGoogleBooksAPI(){
        if(mGoogleBooksAPI == null){
            mGoogleBooksAPI = createGoogleBooksApiObject(getOkHttpClient());
        }
        return mGoogleBooksAPI;
    }

    public OkHttpClient getOkHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = createHttpClient();
        }

        return mHttpClient;
    }

    private OkHttpClient createHttpClient() {
        OkHttpClient client = new OkHttpClient();

        //Set Cache size and Timeout limits
        Cache cache = new Cache(getCacheDir(), Constants.OKHTTP_CACHE_SIZE_BYTES);
        client.setCache(cache);

        client.setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(Constants.HTTP_READOUT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        client.setWriteTimeout(Constants.HTTP_WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        return client;
    }

    private void setupPicasso(OkHttpClient client) {
        Picasso instance = new Picasso.Builder(this)
                .downloader(new OkHttpDownloader(client))
                .build();

        Picasso.setSingletonInstance(instance);
    }

    private GoogleBooksAPI createGoogleBooksApiObject(OkHttpClient okHttpClient) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.GOOGLE_BOOKS_API_ENDPOINT)
                .setClient(new OkClient(okHttpClient))
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build();
        return restAdapter.create(GoogleBooksAPI.class);
    }
}
