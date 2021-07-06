package kst.app.fcaudiorecorder

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kst.app.fcaudiorecorder.databinding.ActivityMainBinding
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var recordState = State.BEFORE_RECORDING
    set(value) {
        field = value
        binding.resetBt.isEnabled = (
                value == State.AFTER_RECORDING ||
                value == State.ON_PLYING
        )
        binding.recordBt.updateIconWithState(value)
    }

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val recordingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestAudioPermission()
        initView()
        bindViews()
        intiVariables()
    }

    private fun initView() {
        binding.recordBt.updateIconWithState(recordState)
    }

    private fun bindViews(){
        binding.soundVisualizerView.onRequestCurrentAmplitude = {
            recorder?.maxAmplitude ?: 0
        }

        binding.recordBt.setOnClickListener {
            when(recordState){
                State.BEFORE_RECORDING -> {
                    startRecorder()
                }
                State.ON_RECORDING -> {
                    stopRecording()
                }
                State.AFTER_RECORDING -> {
                    startPlay()
                }
                State.ON_PLYING -> {
                    stopPlay()
                }
            }
        }
        binding.resetBt.setOnClickListener {
            stopPlay()
            binding.soundVisualizerView.clearVisualizing()
            binding.recordTimeTextView.clearCounttime()
            recordState = State.BEFORE_RECORDING
        }
    }

    private fun intiVariables(){
        recordState = State.BEFORE_RECORDING
    }

    private fun requestAudioPermission() {
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                grantResults.firstOrNull() ==PackageManager.PERMISSION_GRANTED

        if (!audioRecordPermissionGranted){
            finish()
        }
    }

    companion object{
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }
    private fun startRecorder(){
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(recordingFilePath)
            Log.d("gwan2103", "recordingFilePath ====> $recordingFilePath")
            prepare()
        }

        recorder?.start()
        binding.soundVisualizerView.startVisualizing(false)
        binding.recordTimeTextView.startCountUp()
        recordState = State.ON_RECORDING
    }

    private fun stopRecording(){
        recorder?.run {
            stop()
            release()
        }
        recorder = null
        binding.soundVisualizerView.stopVisualizing()
        binding.recordTimeTextView.stopCountUp()
        recordState = State.AFTER_RECORDING
    }

    private fun startPlay(){
        player = MediaPlayer().apply {
            setDataSource(recordingFilePath)
            prepare()
        }
        player?.setOnCompletionListener {
            stopPlay()
            recordState = State.AFTER_RECORDING
        }
        player?.start()
        binding.soundVisualizerView.startVisualizing(true)
        binding.recordTimeTextView.startCountUp()
        recordState = State.ON_PLYING
    }

    private fun stopPlay(){
        player?.release()
        player = null
        recordState = State.AFTER_RECORDING
    }
}