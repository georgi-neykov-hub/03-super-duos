
package it.jaschke.alexandria.api.pojo;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;

public class BookQueryResponse {

    @Expose
    private long totalItems;
    @Expose
    private List<Item> items = new ArrayList<>();

    /**
     * 
     * @return
     *     The totalItems
     */
    public long getTotalItems() {
        return totalItems;
    }

    /**
     * 
     * @return
     *     The items
     */
    public List<Item> getItems() {
        return items;
    }
}
