package com.mobisys.assignment.Interface;

import com.mobisys.assignment.Movie.MovieContent;
import com.mobisys.assignment.Movie.MovieDetails;
import com.mobisys.assignment.Movie.MovieImages;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Subhodh on 8/1/2017.
 */

public interface GetMovies {
    @GET("movie/upcoming?api_key=b7cd3340a794e5a2f35e3abb820b497f")
    Call<MovieContent> all();

    @GET("movie/{id}?api_key=b7cd3340a794e5a2f35e3abb820b497f")
    Call<MovieDetails> select(@Path("id") int id);

    @GET("movie/{id}/images?api_key=b7cd3340a794e5a2f35e3abb820b497f")
    Call<MovieImages> image(@Path("id") int id);
}
