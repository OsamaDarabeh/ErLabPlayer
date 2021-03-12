package com.osdar.erlabplayer.erLabPlayer;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.github.vkay94.dtpv.DoubleTapPlayerView;
import com.github.vkay94.dtpv.youtube.YouTubeOverlay;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.osdar.erlabplayer.R;
import com.osdar.erlabplayer.main.MainFragment;
import com.osdar.erlabplayer.utils.Const;

import java.util.ArrayList;

import static com.osdar.erlabplayer.utils.Const.NORMAL;


public class ExoPlayerActivity extends AppCompatActivity {

    private static final String TAG = ExoPlayerActivity.class.getSimpleName();
    private DoubleTapPlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private PlaybackStateListener playbackStateListener;
    private YouTubeOverlay youtubeOverlay;
    private Const mVideoType;
    private ProgressBar progressBar;
    private ExtractorMediaSource mediaSource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player);

        mVideoType = (Const) getIntent().getSerializableExtra(MainFragment.VIDEO_TYPE);

        progressBar = findViewById(R.id.progress_bar);
        playerView = findViewById(R.id.video_view);
        youtubeOverlay = findViewById(R.id.youtube_overlay1);

        playbackStateListener = new PlaybackStateListener();

        youtubeOverlay.performListener(new YouTubeOverlay.PerformListener() {
            @Override
            public void onAnimationStart() {
                youtubeOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd() {
                youtubeOverlay.setVisibility(View.GONE);
            }
        });

    }

    private void initializePlayer() {
        MediaItem mediaItem = null;

        switch (mVideoType) {
            case NORMAL:
//                // subtitle
//                MediaItem.Subtitle subtitle =
//                        new MediaItem.Subtitle(Uri.parse(getString(R.string.media_url_mp4_subtitle)),
//                                MimeTypes.APPLICATION_MP4VTT, // The correct MIME type.
//                                "en"// The subtitle language. May be null.
//                        ); // Selection flags for the track.
//                ArrayList<MediaItem.Subtitle> x = new ArrayList<>();
//                x.add(subtitle);
//                // streaming
                mediaItem = new MediaItem.Builder()
                        .setUri(getString(R.string.media_url_mp4))
                        .build();

            case DASH:
                // streaming
                mediaItem = new MediaItem.Builder()
                        .setUri(getString(R.string.media_url_dash))
                        .setMimeType(MimeTypes.APPLICATION_MPD)
                        .build();

                break;
            case HLS:
                // streaming
                mediaItem = new MediaItem.Builder()
                        .setUri(getString(R.string.media_url_hls2))
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build();
                break;
            default:
                break;
        }

        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();
        }


        playerView.setPlayer(player);
        youtubeOverlay.player(player);

        player.setMediaItem(mediaItem);
        //
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.addListener(playbackStateListener);
        player.prepare();

    }


    private void playWithCaption() {
        player = ExoPlayerFactory.newSimpleInstance(this,
                new DefaultTrackSelector());


        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "erLabPlayer"));

        MediaSource contentMediaSource = buildMediaSource(Uri.parse(getString(R.string.media_url_mp4_with_subtitle)));
        MediaSource[] mediaSources = new MediaSource[2]; //The Size must change depending on the Uris
        mediaSources[0] = contentMediaSource; // uri

        //Add subtitles
        Format en = Format.createTextSampleFormat(null,
                MimeTypes.APPLICATION_MP4VTT,
                Format.NO_VALUE,
                "en");

        SingleSampleMediaSource subtitleSource = new SingleSampleMediaSource(
                Uri.parse(getString(R.string.media_url_mp4_subtitle)),
                dataSourceFactory,
                en,
                C.TIME_UNSET);

        mediaSources[1] = subtitleSource;

        MediaSource mediaSource = new MergingMediaSource(mediaSources);


        playerView.setPlayer(player);
        youtubeOverlay.player(player);

        // Prepare the player with the source.
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.addListener(playbackStateListener);
        player.prepare(mediaSource);

    }

    private MediaSource buildMediaSource(Uri parse) {
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "exo-demo"));

        mediaSource = new ExtractorMediaSource(parse, dataSourceFactory, new DefaultExtractorsFactory(), new Handler(), null);

        return mediaSource;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24)
            initializePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.removeListener(playbackStateListener);
            player.release();
            player = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    private class PlaybackStateListener implements Player.EventListener {

        @Override
        public void onPlaybackStateChanged(int playbackState) {

            if (playbackState == Player.STATE_BUFFERING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString);
        }
    }
}
