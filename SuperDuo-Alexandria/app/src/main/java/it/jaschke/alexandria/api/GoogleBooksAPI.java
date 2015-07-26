package it.jaschke.alexandria.api;

import java.util.Map;

import it.jaschke.alexandria.api.pojo.BookQueryResponse;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface GoogleBooksAPI {
    @GET("/volumes/")
    BookQueryResponse queryBooks(@QueryMap Map<String,String> queryArgs);
}
