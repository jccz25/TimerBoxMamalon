package com.jccz25.timerboxmamalon

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jccz25.timerboxmamalon.databinding.ActivityMainBinding
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.media.AudioManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var timer: CountDownTimer? = null
    private var tiempoRestante: Long = 0
    private var estaPausado = false
    private var mediaPlayer: MediaPlayer? = null

    // Variables del workout

    private var tiempoPreparacion = 15000L // 15 segundos antes de iniciar rutina

    private var toneGen: ToneGenerator? = null

    private var setsTotales = 0
    private var ejerciciosTotales = 0
    private var tiempoTrabajo = 0L
    private var tiempoDescansoEj = 0L
    private var tiempoDescansoSet = 0L

    private var setActual = 1
    private var ejercicioActual = 1
    private var esTrabajo = true // true = verde, false = rojo
    private var esDescansoSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPlay.setOnClickListener { iniciarWorkout() }
        binding.btnPause.setOnClickListener { pausarTimer() }
        binding.btnStop.setOnClickListener { detenerTodo() }
        toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100) // 100 = volumen
    }

    private fun sonarCampana() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.campana)
        mediaPlayer?.start()
    }

    private fun iniciarWorkout() {
        if (estaPausado) {
            reanudarTimer()
            return
        }

        // Leer config
        setsTotales = binding.etSets.text.toString().toIntOrNull() ?: 5
        ejerciciosTotales = binding.etEjercicios.text.toString().toIntOrNull() ?: 5
        tiempoTrabajo = (binding.etTiempoEjercicio.text.toString().toIntOrNull() ?: 60) * 1000L
        tiempoDescansoEj = (binding.etDescansoEjercicio.text.toString().toIntOrNull() ?: 15) * 1000L
        tiempoDescansoSet = (binding.etDescansoSet.text.toString().toIntOrNull() ?: 60) * 1000L

        setActual = 1
        ejercicioActual = 1
        esTrabajo = false  //aqui estaba true
        esDescansoSet = false

        binding.configLayout.visibility = View.GONE
        binding.timerLayout.visibility = View.VISIBLE

        //iniciarSiguienteIntervalo()
        iniciarPreparacion()
    }

    private fun iniciarPreparacion() {
        binding.root.setBackgroundColor(Color.parseColor("#E53935")) // Rojo
        binding.tvEstado.text = "PREPÁRATE"
        //binding.tvContadorSets.text = "Set 1/${setsTotales}"
        //binding.tvContadorEjercicios.text = "Ejercicio 1/${ejerciciosTotales}"

        sonarCampana() // Suena la campana de inicio

        timer = object : CountDownTimer(tiempoPreparacion, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvReloj.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                esTrabajo = true // Ahora sí empezamos con TRABAJO
                iniciarSiguienteIntervalo()
            }
        }.start()
    }

    private fun iniciarSiguienteIntervalo() {
        var duracion: Long
        var color: Int
        var textoEstado: String

        sonarCampana()

        when {
            esDescansoSet -> {
                duracion = tiempoDescansoSet
                color = Color.RED
                textoEstado = "DESCANSO SET"
            }
            esTrabajo -> {
                duracion = tiempoTrabajo
                color = Color.parseColor("#4CAF50") // Verde
                textoEstado = "TRABAJO"
            }
            else -> {
                duracion = tiempoDescansoEj
                color = Color.RED
                textoEstado = "DESCANSO"
            }
        }

        binding.rootLayout.setBackgroundColor(color)
        binding.tvEstado.text = textoEstado
        binding.tvProgreso.text = "Set $setActual/$setsTotales  Ejercicio $ejercicioActual/$ejerciciosTotales"

        timer = object : CountDownTimer(duracion, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tiempoRestante = millisUntilFinished
                val segundos = millisUntilFinished / 1000
                binding.tvReloj.text = (millisUntilFinished / 1000).toString()

                // NUEVO: Beep en 3, 2, 1
                if (segundos <= 5 && segundos > 0) {
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 150) // 150ms de beep
                }

            }

            override fun onFinish() {
                avanzarWorkout()
            }
        }.start()
    }

    private fun avanzarWorkout() {
        if (esDescansoSet) {
            // Terminó el descanso largo entre sets
            esDescansoSet = false
            setActual++
            ejercicioActual = 1
            esTrabajo = true
            iniciarSiguienteIntervalo()
            return
        }

        if (esTrabajo) {
            // Terminó un periodo de trabajo
            if (ejercicioActual < ejerciciosTotales) {
                // Aún quedan ejercicios, descanso corto entre ejercicios
                esTrabajo = false
                iniciarSiguienteIntervalo()
            } else {
                // Era el último ejercicio del set
                if (setActual < setsTotales) {
                    // Hay más sets, pasamos directamente al descanso largo (sin el corto)
                    esDescansoSet = true
                    iniciarSiguienteIntervalo()
                } else {
                    // Era el último set y último ejercicio
                    terminarWorkout()
                }
            }
        } else {
            // Terminó el descanso corto entre ejercicios, siguiente ejercicio del mismo set
            esTrabajo = true
            ejercicioActual++
            iniciarSiguienteIntervalo()
        }
    }

    private fun pausarTimer() {
        timer?.cancel()
        estaPausado = true
        binding.btnPlay.text = "RESUME"
    }

    private fun reanudarTimer() {
        estaPausado = false
        binding.btnPlay.text = "PLAY"
        timer = object : CountDownTimer(tiempoRestante, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tiempoRestante = millisUntilFinished
                binding.tvReloj.text = (millisUntilFinished / 1000).toString()
            }
            override fun onFinish() {
                avanzarWorkout()
            }
        }.start()
    }

    private fun detenerTodo() {
        timer?.cancel()
        estaPausado = false
        binding.rootLayout.setBackgroundColor(Color.parseColor("#4CAF50"))
        binding.tvCopyright.setTextColor(Color.parseColor("#8A000000"))
        binding.configLayout.visibility = View.VISIBLE
        binding.timerLayout.visibility = View.GONE
        binding.btnPlay.text = "PLAY"
    }

    private fun terminarWorkout() {
        binding.tvEstado.text = "TERMINADO"
        binding.tvReloj.text = "✓"
        binding.rootLayout.setBackgroundColor(Color.BLUE)
        binding.tvCopyright.setTextColor(Color.WHITE)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        toneGen?.release()
        timer?.cancel()
    }

}