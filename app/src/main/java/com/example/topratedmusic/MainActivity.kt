package com.example.topratedmusic

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    lateinit var mediaPlayer: MediaPlayer
    lateinit var service: ApiService
    lateinit var handler: Handler
    val mainActivity : MainActivity = this;

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://uhoo4k3xsc.execute-api.us-east-2.amazonaws.com/prod/api/v1/songs/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create<ApiService>(ApiService::class.java)

        handler = Handler()

        mediaPlayer = MediaPlayer()
        playerSeekBar.max = 100

        imgRandom.setOnClickListener{
            reloadMediaPlayer()
            GetRandomSong();
        }

        imagePlayPause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                handler.removeCallbacks(updater)
                mediaPlayer.pause()
                imagePlayPause.setImageResource(R.drawable.ic_play)
            } else {
                mediaPlayer.start()
                imagePlayPause.setImageResource(R.drawable.ic_pause)
                updateSeekbar()
            }
        }
        GetRandomSong();
        //prepareMediaPlayer("https://canciones-piratas.s3.us-east-2.amazonaws.com/o-tu-o-yo-revisitado-cover-audio.mp3");

        playerSeekBar.setOnTouchListener { view, _ ->
            val seekBar = view as SeekBar
            val playPosition = (mediaPlayer.duration / 100) * seekBar.progress
            mediaPlayer.seekTo(playPosition)
            textCurrentTime.text = milliSecondsToTimer(mediaPlayer.currentPosition.toLong())
            false
        }

        mediaPlayer.setOnBufferingUpdateListener { _, i ->
            playerSeekBar.secondaryProgress = i
        }

        mediaPlayer.setOnCompletionListener {

            reloadMediaPlayer()
            GetRandomSong();
        }
    }

    private fun reloadMediaPlayer() {
        playerSeekBar.progress = 0
        imagePlayPause.setImageResource(R.drawable.ic_play)
        textCurrentTime.setText(R.string.zero)
        textTotalDuration.setText(R.string.zero)
        mediaPlayer.reset()
    }

    private fun prepareMediaPlayer(url: String) {
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()
            textTotalDuration.text = milliSecondsToTimer(mediaPlayer.duration.toLong())
        } catch (ex : Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
        }
    }

    private val updater = Runnable {
        updateSeekbar()
        val currentDuration: Long = mediaPlayer.currentPosition.toLong()
        textCurrentTime.text = milliSecondsToTimer(currentDuration)
    }

    private fun updateSeekbar() {
        if(mediaPlayer.isPlaying) {
            playerSeekBar.progress = (((mediaPlayer.currentPosition).toFloat() / mediaPlayer.duration) * 100).toInt()
            handler.postDelayed(updater, 1000)
        }
    }

    private fun milliSecondsToTimer(milliSeconds: Long): String{
        var timerString = ""
        var secondsString: String

        var hours: Int = (milliSeconds / (1000 * 60 * 60)).toInt()
        var minutes: Int = ((milliSeconds % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        var seconds: Int = (((milliSeconds % (1000 * 60 * 60)) % (1000 * 60)) / 1000).toInt()

        if(hours > 0) {
            timerString = "${hours}:"
        }

        if(seconds < 10) {
            secondsString = "0${seconds}"
        } else {
            secondsString= seconds.toString()
        }

        timerString = "${timerString}${minutes}:${secondsString}"
        return timerString
    }

    fun GetRandomSong(){
        var cancion : Cancion;
        //Recibimos todos los posts
        service.getRandom().enqueue(object: Callback<Cancion> {
            override fun onResponse(call: Call<Cancion>?, response: Response<Cancion>?) {
                cancion = response?.body() as Cancion
                Picasso.get().load(cancion.img).into(imgAlbum)
                txtAutor.text = cancion.artist
                txtNombreCancion.text = cancion.name
                prepareMediaPlayer(cancion.url);
            }
            override fun onFailure(call: Call<Cancion>?, t: Throwable?) {
                t?.printStackTrace()
                Toast.makeText(mainActivity, "ERROR INESPERADO", Toast.LENGTH_LONG).show()
            }
        })
    }
}