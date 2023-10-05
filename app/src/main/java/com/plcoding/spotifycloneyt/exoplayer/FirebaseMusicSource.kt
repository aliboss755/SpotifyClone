package com.plcoding.spotifycloneyt.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat.MediaKeyAction
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.plcoding.spotifycloneyt.data.remote.MusicDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(
    private val musicDataBase: MusicDataBase

){

     var songs = emptyList<MediaMetadataCompat>()
    suspend fun fetchMediaData() = withContext(Dispatchers.IO){
    state =State.STATE_INITIALIZING
        val allSongs =musicDataBase.getAllSongs()
        songs =allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,song.subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,song.mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,song.imageUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,song.imageUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,song.imageUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,song.subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,song.subtitle)
                .build()


        }
        state =State.STATE_INITIALIZED
    }
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory):ConcatenatingMediaSource{
        val  coMediaSource =ConcatenatingMediaSource()
        songs.forEach{song ->
            val mediaSource =ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri())
            coMediaSource.addMediaSource(mediaSource)
        }
        return coMediaSource
    }
    fun asMediaItems() =songs.map { song ->
        val desc =MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc,FLAG_PLAYABLE)

    }

    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.STATE_CREATE
        set(value) {
            if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListener) {
                    field = value
                    onReadyListener.forEach { listener ->
                        listener(state == State.STATE_INITIALIZING)
                    }
                }
            } else {
                field = value

            }
        }
    fun whenReady(action: (Boolean) ->Unit):Boolean{
        if (state == State.STATE_CREATE || state == State.STATE_INITIALIZING) {
            onReadyListener ==action
            return false
        }else{
            action(state == State.STATE_INITIALIZED)
            return true
        }

    }

}

enum class State {
    STATE_CREATE,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR


}