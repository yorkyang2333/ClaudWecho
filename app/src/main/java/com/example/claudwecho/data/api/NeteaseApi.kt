package com.example.claudwecho.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface NeteaseApi {
    @GET("/login/qr/key")
    suspend fun getQrKey(@Query("timestamp") timestamp: Long = System.currentTimeMillis()): QrKeyResponse

    @GET("/login/qr/create")
    suspend fun createQr(
        @Query("key") key: String,
        @Query("qrimg") qrimg: Boolean = true,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): QrCreateResponse

    @GET("/login/qr/check")
    suspend fun checkQr(
        @Query("key") key: String,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): QrCheckResponse

    @GET("/login/cellphone")
    suspend fun loginCellphonePassword(
        @Query("phone") phone: String,
        @Query("password") password: String,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): LoginResponse

    @GET("/login/cellphone")
    suspend fun loginCellphoneCaptcha(
        @Query("phone") phone: String,
        @Query("captcha") captcha: String,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): LoginResponse

    @GET("/login/status")
    suspend fun getLoginStatus(@Query("timestamp") timestamp: Long = System.currentTimeMillis()): LoginStatusResponse

    @GET("/recommend/songs")
    suspend fun getRecommendSongs(@Query("timestamp") timestamp: Long = System.currentTimeMillis()): RecommendSongsResponse

    @GET("/user/playlist")
    suspend fun getUserPlaylists(
        @Query("uid") uid: Long,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): UserPlaylistResponse

    @GET("/playlist/detail")
    suspend fun getPlaylistDetail(
        @Query("id") id: Long,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): PlaylistDetailResponse

    @GET("/lyric")
    suspend fun getLyric(@Query("id") id: Long): LyricResponse
    @GET("/song/url/v1")
    suspend fun getSongUrl(
        @Query("id") id: Long,
        @Query("level") level: String = "standard",
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): SongUrlResponse

    @GET("/album/sublist")
    suspend fun getSubscribedAlbums(
        @Query("limit") limit: Int = 50,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): SubscribedAlbumsResponse

    @GET("/dj/sublist")
    suspend fun getSubscribedDjRadios(
        @Query("limit") limit: Int = 50,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): SubscribedDjRadiosResponse
}

@Serializable
data class SongUrlResponse(val data: List<SongUrlData>, val code: Int)

@Serializable
data class SongUrlData(val id: Long, val url: String?)

@Serializable
data class QrKeyResponse(val data: QrKeyData, val code: Int)

@Serializable
data class QrKeyData(val unikey: String, val code: Int)

@Serializable
data class QrCreateResponse(val data: QrCreateData, val code: Int)

@Serializable
data class QrCreateData(val qrurl: String, val qrimg: String)

@Serializable
data class QrCheckResponse(val code: Int, val message: String? = null, val cookie: String? = null)

@Serializable
data class LoginResponse(val code: Int, val message: String? = null, val cookie: String? = null)

@Serializable
data class LoginStatusResponse(val data: LoginStatusData)

@Serializable
data class LoginStatusData(val profile: UserProfile?, val account: UserAccount?)

@Serializable
data class UserProfile(val userId: Long, val nickname: String, val avatarUrl: String?)

@Serializable
data class UserAccount(val id: Long)

@Serializable
data class RecommendSongsResponse(val data: RecommendSongsData, val code: Int)

@Serializable
data class RecommendSongsData(val dailySongs: List<Song>)

@Serializable
data class Song(val id: Long, val name: String, val ar: List<Artist>, val al: Album, val fee: Int = 0)

@Serializable
data class Artist(val id: Long, val name: String)

@Serializable
data class Album(val id: Long, val name: String, val picUrl: String)

@Serializable
data class UserPlaylistResponse(val playlist: List<Playlist>, val code: Int)

@Serializable
data class Playlist(val id: Long, val name: String, val coverImgUrl: String, val trackCount: Int)

@Serializable
data class PlaylistDetailResponse(val code: Int, val playlist: PlaylistDetail)

@Serializable
data class PlaylistDetail(val tracks: List<Song>)

@Serializable
data class LyricResponse(val code: Int, val lrc: LrcData? = null, val tlyric: LrcData? = null)

@Serializable
data class LrcData(val lyric: String?)

@Serializable
data class SubscribedAlbumsResponse(val data: List<Album>, val code: Int)

@Serializable
data class SubscribedDjRadiosResponse(val djRadios: List<DjRadio>, val code: Int)

@Serializable
data class DjRadio(val id: Long, val name: String, val picUrl: String)
