package com.mobisys.assignment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mobisys.assignment.Interface.GetMovies;
import com.mobisys.assignment.Movie.MovieContent;

import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MovieListActivity extends AppCompatActivity {


    public static final String ENDPOINT_URL = "https://api.themoviedb.org/3/";
    public static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w92/";
    public static final String IMAGE_HIGH_BASE_URL = "https://image.tmdb.org/t/p/w300/";
    private boolean mTwoPane;
    private ProgressDialog progressDialog;
    List<MovieContent.ResultsBean> movieList;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Upcoming Movies");


        if (findViewById(R.id.movie_detail_container) != null) {

            mTwoPane = true;
        }
        context = this;
        Retrofit retrofit = new Retrofit.Builder().baseUrl(ENDPOINT_URL).
                addConverterFactory(GsonConverterFactory.create()).build();
        GetMovies getMovies = retrofit.create(GetMovies.class);
        retrofit2.Call<MovieContent> call = getMovies.all();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        call.enqueue(new Callback<MovieContent>() {
            @Override
            public void onResponse(retrofit2.Call<MovieContent> call, Response<MovieContent> response) {
                MovieContent movieContent = response.body();
                displayResult(movieContent);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(retrofit2.Call<MovieContent> call, Throwable t) {
                Toast.makeText(MovieListActivity.this, "Error in connecting to network", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id;
        id = item.getItemId();
        if (id == R.id.myInfo) {
            startActivity(new Intent(this, Information.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayResult(MovieContent movieContent) {
        if (movieContent != null) {
            System.out.println(movieContent.getTotal_results());
            movieList = movieContent.getResults();
            View recyclerView = findViewById(R.id.movie_list);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView);

        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(movieList));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<MovieContent.ResultsBean> mValues;
        private MovieContent.ResultsBean mItemResult;

        public SimpleItemRecyclerViewAdapter(List<MovieContent.ResultsBean> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mTitle.setText(holder.mItem.getTitle());
            holder.mReleaseDate.setText(holder.mItem.getRelease_date());
            if (holder.mItem.isAdult()) holder.mAdult.setText("(A)");
            else holder.mAdult.setText("(U/A)");
            Glide.with(context).load(IMAGE_BASE_URL + holder.mItem.getPoster_path()).into(holder.mThumbImage);
            holder.mCardItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putInt(MovieDetailFragment.ARG_ITEM_ID, holder.mItem.getId());
                        MovieDetailFragment fragment = new MovieDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.movie_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, MovieDetailActivity.class);
                        intent.putExtra(MovieDetailFragment.ARG_ITEM_ID, holder.mItem.getId());
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mTitle;
            public final TextView mReleaseDate;
            public final TextView mAdult;
            public final ImageView mThumbImage;
            public final CardView mCardItem;
            public MovieContent.ResultsBean mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitle = (TextView) view.findViewById(R.id.title);
                mReleaseDate = (TextView) view.findViewById(R.id.dateRelease);
                mAdult = (TextView) view.findViewById(R.id.adult);
                mThumbImage = (ImageView) view.findViewById(R.id.thumbnail);
                mCardItem = (CardView) view.findViewById(R.id.cardItem);
            }


        }
    }
}
