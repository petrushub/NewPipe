package org.schabi.newpipe.radio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;

import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;

/**
 * Foreground service that plays one live radio station at a time.
 */
public final class RadioService extends Service {
    private static final String ACTION_PLAY = "org.schabi.newpipe.radio.PLAY";
    private static final String ACTION_STOP = "org.schabi.newpipe.radio.STOP";
    private static final String EXTRA_STATION_ID = "station_id";
    private static final String CHANNEL_ID = "radio_playback";
    private static final int NOTIFICATION_ID = 7001;

    private static final MutableLiveData<String> PLAYING_STATION_ID = new MutableLiveData<>(null);

    private ExoPlayer player;

    /**
     * @return the id of the station currently playing, or {@code null} if none
     */
    @NonNull
    public static LiveData<String> getPlayingStationId() {
        return PLAYING_STATION_ID;
    }

    public static void play(@NonNull final Context context, @NonNull final RadioStation station) {
        ContextCompat.startForegroundService(context,
                new Intent(context, RadioService.class)
                        .setAction(ACTION_PLAY)
                        .putExtra(EXTRA_STATION_ID, station.id));
    }

    public static void stop(@NonNull final Context context) {
        context.startService(new Intent(context, RadioService.class).setAction(ACTION_STOP));
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null || ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        final RadioStation station =
                RadioStation.findById(intent.getStringExtra(EXTRA_STATION_ID));
        if (station == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        startInForeground(station);
        startPlayback(station);
        return START_NOT_STICKY;
    }

    private void startPlayback(@NonNull final RadioStation station) {
        if (player == null) {
            player = new ExoPlayer.Builder(this)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(C.USAGE_MEDIA)
                            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                            .build(), true)
                    .setHandleAudioBecomingNoisy(true)
                    .build();
            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(@NonNull final PlaybackException error) {
                    Toast.makeText(RadioService.this, R.string.radio_error, Toast.LENGTH_LONG)
                            .show();
                    stopSelf();
                }
            });
        }
        player.setMediaItem(MediaItem.fromUri(station.streamUrl));
        player.prepare();
        player.play();
        PLAYING_STATION_ID.postValue(station.id);
    }

    private void startInForeground(@NonNull final RadioStation station) {
        createNotificationChannel();

        final PendingIntent openApp = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        final PendingIntent stopIntent = PendingIntent.getService(this, 0,
                new Intent(this, RadioService.class).setAction(ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE);

        final Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_radio)
                .setContentTitle(station.name)
                .setContentText(getString(R.string.radio_playing))
                .setContentIntent(openApp)
                .addAction(R.drawable.ic_pause, getString(R.string.radio_stop), stopIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK : 0);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager.class).createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, getString(R.string.radio),
                            NotificationManager.IMPORTANCE_LOW));
        }
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        PLAYING_STATION_ID.postValue(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }
}
