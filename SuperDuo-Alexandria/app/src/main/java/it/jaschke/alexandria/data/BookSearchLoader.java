package it.jaschke.alexandria.data;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.jaschke.alexandria.AlexandriaApplication;
import it.jaschke.alexandria.api.pojo.BookQueryResponse;
import it.jaschke.alexandria.api.pojo.ImageLinks;
import it.jaschke.alexandria.api.pojo.Item;
import it.jaschke.alexandria.api.pojo.VolumeInfo;

public class BookSearchLoader extends AsyncTaskLoader<LoaderResult<List<Book>>> {

    private String mEanQuery;

    public BookSearchLoader(Context context, String eanQuery) {
        super(context);

        if (TextUtils.isEmpty(eanQuery) || eanQuery.length() != 13) {
            throw new IllegalArgumentException("Invalid or empy EAN number parameter.");
        }

        this.mEanQuery = eanQuery;
    }

    @Override
    public LoaderResult<List<Book>> loadInBackground() {
        return executeBookQuery(mEanQuery);
    }

    private LoaderResult<List<Book>> executeBookQuery(String ean) {
        BookQueryResponse response;
        try {
            Map<String, String> args = new ArrayMap<>(10);
            args.put("q", "isbn:" + ean);
            args.put("orderBy", "newest");
            args.put("projection", "full");
            response = AlexandriaApplication.getInstance().getGoogleBooksAPI().queryBooks(args);

            List<Book> results = new ArrayList<>(response.getItems().size());
            for(Item item : response.getItems()){
                results.add(createBookFromVolume(mEanQuery, item));
            }
            return new LoaderResult<>(results);

        } catch (Throwable error) {
            return new LoaderResult<>(error);
        }
    }

    private Book createBookFromVolume(String ean, Item bookItem){
        VolumeInfo info = bookItem.getVolumeInfo();
        return new Book(ean,
                info.getTitle(),
                info.getSubtitle(),
                info.getDescription(),
                extractImageUrl(info.getImageLinks()),
                info.getAuthors(),
                info.getCategories());
    }

    private String extractImageUrl(ImageLinks images){
        if(images == null){
            return null;
        }

        if(images.getLarge()!= null){
            return images.getLarge();
        }else if(images.getMedium()!= null){
            return images.getMedium();
        } else if(images.getSmall()!= null){
            return images.getSmall();
        } else if(images.getThumbnail()!= null){
            return images.getThumbnail();
        } else if(images.getSmallThumbnail()!= null){
            return images.getSmallThumbnail();
        }else {
            return null;
        }
    }
}
