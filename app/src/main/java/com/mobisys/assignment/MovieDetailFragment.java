package com.mobisys.assignment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mobisys.assignment.Interface.GetMovies;
import com.mobisys.assignment.Movie.MovieDetails;
import com.mobisys.assignment.Movie.MovieImages;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MovieDetailFragment extends Fragment implements ViewPager.OnPageChangeListener {

    public static final String ARG_ITEM_ID = "item_id";

    private ViewPager mImagePager;
    private TextView mTitle;
    private TextView mOverview;
    private RatingBar mRatingBar;
    private ProgressDialog progressDialog;
    private TabLayout mImageIndicator;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            System.out.println(getArguments().getInt(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        mImagePager = (ViewPager) rootView.findViewById(R.id.viewPager);
        mTitle = (TextView) rootView.findViewById(R.id.movieTitle);
        mOverview = (TextView) rootView.findViewById(R.id.overviewText);
        mRatingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        mRatingBar.setNumStars(5);
        mImageIndicator = (TabLayout) rootView.findViewById(R.id.tabLayout);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(MovieListActivity.ENDPOINT_URL).
                addConverterFactory(GsonConverterFactory.create()).build();
        GetMovies getMovies = retrofit.create(GetMovies.class);
        retrofit2.Call<MovieDetails> call = getMovies.select(getArguments().getInt(ARG_ITEM_ID));
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Connecting...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        call.enqueue(new Callback<MovieDetails>() {
            @Override
            public void onResponse(retrofit2.Call<MovieDetails> call, Response<MovieDetails> response) {
                MovieDetails movieContent = response.body();
                displayResult(movieContent);
                System.out.println(movieContent.getOriginal_title());
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(retrofit2.Call<MovieDetails> call, Throwable t) {
                Toast.makeText(getActivity(), "Error in connecting to network", Toast.LENGTH_SHORT).show();

            }
        });
        retrofit2.Call<MovieImages> call1 = getMovies.image(getArguments().getInt(ARG_ITEM_ID));
        call1.enqueue(new Callback<MovieImages>() {
            @Override
            public void onResponse(Call<MovieImages> call, Response<MovieImages> response) {
                MovieImages movieImages = response.body();
                displayImages(movieImages);
                System.out.println("From Image:" + movieImages.getId());
            }

            @Override
            public void onFailure(Call<MovieImages> call, Throwable t) {
                Toast.makeText(getActivity(), "Error in connecting to network", Toast.LENGTH_SHORT).show();
            }
        });


        mImagePager.addOnPageChangeListener(this);
        mImageIndicator.setupWithViewPager(mImagePager);
        return rootView;
    }

    private void displayImages(MovieImages movieImages) {
        CustomPagerAdapter customPagerAdapter = new CustomPagerAdapter(getActivity(), movieImages.getBackdrops());
        mImagePager.setAdapter(customPagerAdapter);
    }

    private void displayResult(MovieDetails movieContent) {
        if (movieContent != null) {
            mTitle.setText(movieContent.getTitle());
            mOverview.setText(movieContent.getOverview());
            mRatingBar.setRating((float) (movieContent.getVote_average() / 2));
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(movieContent.getTitle());
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        System.out.println(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    class CustomPagerAdapter extends PagerAdapter {
        Context mContext;
        LayoutInflater mLayoutInflater;
        List<MovieImages.BackdropsBean> imageList;

        public CustomPagerAdapter(Context context, List<MovieImages.BackdropsBean> imageList) {
            this.mContext = context;
            this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.imageList = imageList;
        }

        @Override
        public int getCount() {
            if (imageList.size() < 5) return imageList.size();
            else return 5;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.img_pager_item);
            Glide.with(mContext).load(MovieListActivity.IMAGE_HIGH_BASE_URL + imageList.get(position).getFile_path()).into(imageView);
            System.out.println(MovieListActivity.IMAGE_BASE_URL + imageList.get(position).getFile_path());
            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }

}
