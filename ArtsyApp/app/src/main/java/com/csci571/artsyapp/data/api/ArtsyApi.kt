package com.csci571.artsyapp.data.api

import com.csci571.artsyapp.data.model.ArtistDetailResponse
import com.csci571.artsyapp.data.model.ArtworkCategoriesResponse
import com.csci571.artsyapp.data.model.ArtworksResponse
import com.csci571.artsyapp.data.model.AuthResponse
import com.csci571.artsyapp.data.model.FavoriteRequest
import com.csci571.artsyapp.data.model.FavoriteResponse
import com.csci571.artsyapp.data.model.FavoritesResponse
import com.csci571.artsyapp.data.model.LoginRequest
import com.csci571.artsyapp.data.model.RegisterRequest
import com.csci571.artsyapp.data.model.SearchResponse
import com.csci571.artsyapp.data.model.SimilarArtistsResponse
import com.csci571.artsyapp.data.model.UserResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for the Artsy application.
 *
 * This interface defines the endpoints for user authentication, favorites management,
 * and common operations related to artists and artworks.
 */
interface ArtsyApi {
    // Authentication endpoints
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<ResponseBody>

    @DELETE("api/auth/delete")
    suspend fun deleteAccount(): Response<ResponseBody>

    // Favorites endpoints
    @GET("api/favorites")
    suspend fun getFavorites(): Response<FavoritesResponse>

    @POST("api/favorites")
    suspend fun addFavorite(@Body request: FavoriteRequest): Response<FavoriteResponse>

    @DELETE("api/favorites/{artistId}")
    suspend fun removeFavorite(@Path("artistId") artistId: String): Response<ResponseBody>

    @GET("api/favorites/check/{artistId}")
    suspend fun checkFavorite(@Path("artistId") artistId: String): Response<Map<String, Boolean>>

    // Common endpoints
    @GET("api/artsy/search")
    suspend fun searchArtists(@Query("q") query: String): Response<SearchResponse>

    @GET("api/artsy/artists/{id}")
    suspend fun getArtistDetails(@Path("id") artistId: String): Response<ArtistDetailResponse>

    @GET("api/artsy/artists/{id}/similar")
    suspend fun getSimilarArtists(@Path("id") artistId: String): Response<SimilarArtistsResponse>

    @GET("api/artsy/artists/{id}/artworks")
    suspend fun getArtworks(@Path("id") artistId: String): Response<ArtworksResponse>

    @GET("api/artsy/artworks/{id}/categories")
    suspend fun getArtworkCategories(@Path("id") artworkId: String): Response<ArtworkCategoriesResponse>
}