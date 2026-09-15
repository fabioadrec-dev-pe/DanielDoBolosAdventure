package com.danieldobolo.adventure;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;

/** Minimal host Activity for the real-time trailer scene. */
public final class DanielTrailerActivity extends Activity {
    private DanielTrailerView trailerView;
    private MediaPlayer music;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        trailerView = new DanielTrailerView(this);
        setContentView(trailerView);
        prepareMusic();
        trailerView.start();
        if (music != null) {
            music.start();
        }
    }

    private void prepareMusic() {
        try {
            AssetFileDescriptor afd = getAssets().openFd("music.ogg");
            music = new MediaPlayer();
            music.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            music.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            music.prepare();
        } catch (IOException | RuntimeException ignored) {
            // The animation still runs silently if audio is unavailable.
            music = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        trailerView.pause();
        if (music != null && music.isPlaying()) {
            music.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (trailerView != null) {
            trailerView.resume();
        }
        if (music != null && !music.isPlaying()) {
            music.start();
        }
    }

    @Override
    protected void onDestroy() {
        if (trailerView != null) {
            trailerView.stop();
        }
        if (music != null) {
            music.release();
            music = null;
        }
        super.onDestroy();
    }
}
