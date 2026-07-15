package com.yorkyang2333.claudwecho.data.api

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

    @GET("/captcha/sent")
    suspend fun sendCaptcha(
        @Query("phone") phone: String,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): BaseResponse

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

    @GET("/song/detail")
    suspend fun getSongDetail(
        @Query("ids") ids: Long,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): SongDetailResponse

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
    @GET("/album")
    suspend fun getAlbumDetail(
        @Query("id") id: Long,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): AlbumDetailResponse

    @GET("/dj/program")
    suspend fun getDjPrograms(
        @Query("rid") rid: Long,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): DjProgramResponse
    @GET("/dj/detail")
    suspend fun getDjRadioDetail(
        @Query("rid") rid: Long,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): DjRadioDetailResponse
    @GET("/record/recent/song")
    suspend fun getRecentSongs(
        @Query("limit") limit: Int = 50,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): RecentSongsResponse
    @GET("/like")
    suspend fun likeSong(
        @Query("id") id: Long,
        @Query("like") like: Boolean = true,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): LikeResponse

    @GET("/cloudsearch")
    suspend fun search(
        @Query("keywords") keywords: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("type") type: Int = 1,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): SearchResponse

    @GET("/likelist")
    suspend fun getLikeList(
        @Query("uid") uid: Long,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): LikeListResponse

    @GET("/personal_fm")
    suspend fun getPersonalFm(
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): PersonalFmResponse

    @GET("/fm_trash")
    suspend fun fmTrash(
        @Query("id") id: Long,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): BaseResponse

    @GET("/logout")
    suspend fun logout(
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): BaseResponse
    @GET("/vip/info")
    suspend fun getVipInfo(
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): VipInfoResponse

    @GET("/playlist/tracks")
    suspend fun updatePlaylistTracks(
        @Query("op") op: String, // "add" or "del"
        @Query("pid") pid: Long,
        @Query("tracks") tracks: String, // comma separated track ids
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): PlaylistTracksResponse
}

@Serializable
data class PlaylistTracksResponse(
    val code: Int? = null,
    val status: Int? = null,
    val message: String? = null,
    val msg: String? = null,
    val body: PlaylistTracksBody? = null
) {
    val isSuccess: Boolean
        get() = code == 200 || status == 200 || body?.code == 200 || body?.status == 200 || code == 502 || body?.code == 502
}

@Serializable
data class PlaylistTracksBody(
    val code: Int? = null,
    val status: Int? = null,
    val message: String? = null,
    val msg: String? = null,
    val count: Int? = null
)

@Serializable
data class VipInfoResponse(val data: VipInfoData? = null, val code: Int)

@Serializable
data class VipInfoData(
    val redVipLevel: Int? = null,
    val redVipAnnualCount: Int? = null,
    val musicPackage: VipMusicPackage? = null
)

@Serializable
data class VipMusicPackage(
    val expireTime: Long? = null,
    val vipLevel: Int? = null
)

@Serializable
data class LikeListResponse(val ids: List<Long> = emptyList(), val code: Int)

@Serializable
data class PersonalFmResponse(val data: List<Song> = emptyList(), val code: Int)

@Serializable
data class BaseResponse(val code: Int? = null, val status: Int? = null, val message: String? = null)

@Serializable
data class LikeResponse(val code: Int, val message: String? = null)

@Serializable
data class RecentSongsResponse(val code: Int, val data: RecentSongsData? = null)

@Serializable
data class RecentSongsData(val list: List<RecentSongItem> = emptyList())

@Serializable
data class RecentSongItem(val resourceId: String, val data: Song)

@Serializable
data class AlbumDetailResponse(val code: Int, val album: Album? = null, val songs: List<Song>? = null)

@Serializable
data class DjProgramResponse(val code: Int, val programs: List<DjProgram>? = null)

@Serializable
data class DjProgram(val id: Long, val mainSong: DjSong)

@Serializable
data class DjSong(val id: Long, val name: String, val artists: List<Artist>? = null, val album: Album? = null)

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
data class UserProfile(val userId: Long, val nickname: String, val avatarUrl: String?, val vipType: Int? = null)

@Serializable
data class UserAccount(val id: Long)

@Serializable
data class SearchResponse(
    val code: Int,
    val result: SearchResult? = null
)

@Serializable
data class SearchResult(
    val songs: List<Song>? = null,
    val songCount: Int = 0
)

@Serializable
data class RecommendSongsResponse(val data: RecommendSongsData, val code: Int)

@Serializable
data class RecommendSongsData(val dailySongs: List<Song>)

@Serializable
data class Song(
    val id: Long, 
    val name: String, 
    val ar: List<Artist> = emptyList(), 
    val al: Album? = null, 
    val fee: Int = 0,
    val artists: List<Artist>? = null,
    val album: Album? = null,
    val isPodcast: Boolean = false
) {
    val displayArtists: List<Artist> get() = ar.ifEmpty { artists ?: emptyList() }
    val displayAlbum: Album? get() = al ?: album
}

@Serializable
data class Artist(val id: Long, val name: String)

@Serializable
data class Album(val id: Long, val name: String? = null, val picUrl: String? = null)

@Serializable
data class UserPlaylistResponse(val playlist: List<Playlist>, val code: Int)

@Serializable
data class PlaylistCreator(val userId: Long? = null, val nickname: String? = null)

@Serializable
data class Playlist(
    val id: Long,
    val name: String,
    val coverImgUrl: String,
    val trackCount: Int,
    val userId: Long? = null,
    val creator: PlaylistCreator? = null,
    val subscribed: Boolean? = null
) {
    fun isCreatedBy(currentUserId: Long?): Boolean {
        val ownerId = userId ?: creator?.userId
        if (ownerId != null && currentUserId != null) {
            return ownerId == currentUserId
        }
        if (subscribed != null) {
            return !subscribed
        }
        return true
    }
}

@Serializable
data class PlaylistDetailResponse(val code: Int, val playlist: PlaylistDetail)

@Serializable
data class PlaylistDetail(val name: String? = null, val tracks: List<Song>)

@Serializable
data class LyricResponse(val code: Int, val lrc: LrcData? = null, val tlyric: LrcData? = null)

@Serializable
data class LrcData(val lyric: String?)

@Serializable
data class SongDetailResponse(val code: Int, val songs: List<SongDetail> = emptyList())

@Serializable
data class SongDetail(
    val id: Long,
    val name: String,
    val alia: List<String> = emptyList(),
    val ar: List<Artist> = emptyList(),
    val al: Album? = null,
    val dt: Long? = null,
    val cd: String? = null,
    val no: Int? = null,
    val publishTime: Long? = null,
    val mv: Long? = null,
    val fee: Int? = null
)

@Serializable
data class SubscribedAlbumsResponse(val data: List<Album>, val code: Int)

@Serializable
data class SubscribedDjRadiosResponse(val djRadios: List<DjRadio>, val code: Int)

@Serializable
data class DjRadio(val id: Long, val name: String, val picUrl: String)

@Serializable
data class DjRadioDetailResponse(val code: Int, val data: DjRadio? = null, val djRadio: DjRadio? = null) {
    val radio: DjRadio?
        get() = data ?: djRadio
}
