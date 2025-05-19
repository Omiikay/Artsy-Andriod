package com.csci571.artsyapp.data.model

import java.util.Date

// Authentication Models
data class RegisterRequest(
    val fullname: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val user: User
)

data class UserResponse(
    val user: User
)

data class User(
    val id: String,
    val fullname: String,
    val email: String,
    val profileImageUrl: String
)

// Favorites Models
data class FavoriteRequest(
    val artistId: String
)

data class FavoriteResponse(
    val favorite: Favorite
)

data class FavoritesResponse(
    val favorites: List<Favorite>
)

data class Favorite(
    val _id: String,
    val user: String,
    val artistId: String,
    val artistName: String,
    val imageUrl: String,
    val nationality: String,
    val birthday: String,
    val deathday: String,
    val addedAt: Date
)

// Search Models
data class SearchResponse(
    val results: List<ArtistResult>
)

data class ArtistResult(
    val id: String,
    val title: String,
    val imageUrl: String
)

// Artist Detail Models
data class ArtistDetailResponse(
    val artist: ArtistDetail
)

data class ArtistDetail(
    val id: String,
    val name: String,
    val birthday: String?,
    val deathday: String?,
    val nationality: String?,
    val biography: String?,
    val imageUrl: String
)

// Similar Artists Models
data class SimilarArtistsResponse(
    val similarArtists: List<ArtistDetail>
)

// Artworks Models
data class ArtworksResponse(
    val artworks: List<Artwork>
)

data class Artwork(
    val id: String,
    val title: String,
    val date: String?,
    val imageUrl: String
)

// Artwork Categories Models
data class ArtworkCategoriesResponse(
    val categories: List<Category>
)

data class Category(
    val id: String,
    val name: String,
    val imageUrl: String,
    val description: String
)