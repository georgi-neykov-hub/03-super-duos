package it.jaschke.alexandria.data;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.jaschke.alexandria.R;

public class BookSearchAdapter extends RecyclerView.Adapter<BookSearchAdapter.BookCardViewHolder> {

    public interface OnAddItemClickListener{
        void onAddClick(int position, Book book);
    }

    private List<Book> mItems;
    private OnAddItemClickListener mAddClickListener;

    public BookSearchAdapter() {
        this.mItems = new ArrayList<>();
    }

    public void setAddItemClickListener(OnAddItemClickListener listener) {
        mAddClickListener = listener;
    }

    @Override
    public BookCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_book_card, parent, false);
        return new BookCardViewHolder(itemView, mItemClickListener);
    }

    @Override
    public void onBindViewHolder(BookCardViewHolder holder, int position) {
        holder.bindItem(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addItems(Collection<Book> newItems){
        if(!newItems.isEmpty()) {
            int startPos = mItems.isEmpty() ? 0 : mItems.size() - 1;
            mItems.addAll(newItems);
            notifyItemRangeInserted(startPos, newItems.size());
        }
    }

    public void clearItems(){
        int itemCount = getItemCount();
        if(itemCount > 0){
            mItems.clear();
            this.notifyDataSetChanged();
        }
    }

    private final BookCardViewHolder.OnItemViewClickListener mItemClickListener = new BookCardViewHolder.OnItemViewClickListener() {
        @Override
        public void onAddClick(RecyclerView.ViewHolder holder, View view) {
            if(view.getId() == R.id.addBookAction && mAddClickListener != null){
                    int position = holder.getAdapterPosition();
                    mAddClickListener.onAddClick(position, mItems.get(position));
            }
        }
    };

    protected static class BookCardViewHolder extends RecyclerView.ViewHolder{

        public interface OnItemViewClickListener{
            void onAddClick(RecyclerView.ViewHolder holder, View view);
        }

        private static final String AUTHORS_DIVIDER = " ";
        private static final String CATEGORY_DIVIDER = " ";

        private ImageView mBookCoverView;
        private TextView mTitleView;
        private TextView mSubtitleView;
        private TextView mAuthorsView;
        private TextView mCategoriesView;
        private View mAddActionButton;

        public BookCardViewHolder(final View itemView, final OnItemViewClickListener listener) {
            super(itemView);
            mBookCoverView = (ImageView) itemView.findViewById(R.id.bookCover);
            mTitleView = (TextView) itemView.findViewById(R.id.bookTitle);
            mSubtitleView = (TextView) itemView.findViewById(R.id.bookSubTitle);
            mAuthorsView = (TextView) itemView.findViewById(R.id.authors);
            mCategoriesView = (TextView) itemView.findViewById(R.id.categories);
            mAddActionButton = itemView.findViewById(R.id.addBookAction);
            mAddActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!= null){
                        listener.onAddClick(BookCardViewHolder.this, v);
                    }
                }
            });
        }

        protected void bindItem(Book item){
            loadBookCover(item.getCoverImageUrl());
            mTitleView.setText(item.getTitle());
            mSubtitleView.setText(item.getSubtitle());
            loadAuthors(item);
            loadCategories(item);
        }

        private void loadAuthors(Book item) {
            String[] authors = item.getAuthors();
            if(authors != null && authors.length>0){
                mAuthorsView.setText(authors[0]);
                for (int index = 1; index < item.getAuthors().length; index++){
                    mCategoriesView.append(AUTHORS_DIVIDER);
                    mAuthorsView.append(authors[index]);
                }
            }else {
                mAuthorsView.setText(null);
            }
        }

        private void loadCategories(Book item) {
            String[] categories = item.getCategories();
            if(categories != null && categories.length>0){
                mCategoriesView.setText(categories[0]);

                for (int index = 1; index < item.getCategories().length; index++){
                    mCategoriesView.append(CATEGORY_DIVIDER);
                    mCategoriesView.append(categories[index]);
                }
            }else {
                mCategoriesView.setText(null);
                mCategoriesView.setVisibility(View.GONE);
            }
        }

        private void loadBookCover(String imageUrl){
            Picasso instance = Picasso.with(mBookCoverView.getContext());
            instance.cancelRequest(mBookCoverView);
            if (imageUrl != null) {
                instance.load(imageUrl)
                        .fit()
                        .centerCrop()
                        .noPlaceholder()
                        .into(mBookCoverView);
            }
        }
    }
}
