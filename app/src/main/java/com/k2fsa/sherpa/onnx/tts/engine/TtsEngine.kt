package com.k2fsa.sherpa.onnx.tts.engine

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.getOfflineTtsConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

const val MIN_TTS_SPEED = 0.1f
const val MAX_TTS_SPEED = 3.0f

object TtsEngine {
    private const val TAG = "sherpa-onnx-tts-engine"
    var tts: OfflineTts? = null

    private var modelDir: String? = null
    private var modelName: String? = null
    private var acousticModelName: String? = null
    private var vocoder: String? = null
    private var voices: String? = null
    private var lexicon: String? = null
    private var tokens: String? = null
    private var dataDir: String? = null
    private var ruleFsts: String? = null
    private var ruleFars: String? = null
    private var isKitten = false
    private var isStyleBertVits2 = false

    var lang: String? = null
    var lang2: String? = null

    var speed: Float
        get() = speedState.value
        set(value) {
            speedState.value = value
        }

    var speakerId: Int
        get() = speakerIdState.value
        set(value) {
            speakerIdState.value = value
        }

    val speedState: MutableFloatState = mutableFloatStateOf(1.0f)
    val speakerIdState: MutableIntState = mutableIntStateOf(0)

    init {
        // Style-Bert-VITS2 Japanese
        modelDir = "style-bert-vits2-ja"
        modelName = "model.onnx"
        lexicon = "lexicon.txt"  // Use the generated lexicon.txt that maps characters to themselves
        tokens = "tokens.txt"
        dataDir = "style-bert-vits2-ja/espeak-ng-data"
        lang = "jpn"
        lang2 = "eng"  // Accept English requests (will synthesize with Japanese model)
        isStyleBertVits2 = true
    }

    fun createTts(context: Context) {
        if (tts == null) {
            initTts(context)
        }
    }

    private fun initTts(context: Context) {
        if (modelDir == null) {
            Log.i(TAG, "Please select a model")
            return
        }

        try {
            val config = getOfflineTtsConfig(
                modelDir = modelDir!!,
                modelName = modelName ?: "",
                acousticModelName = acousticModelName ?: "",
                vocoder = vocoder ?: "",
                voices = voices ?: "",
                lexicon = lexicon ?: "",
                tokens = tokens ?: "",
                dataDir = dataDir ?: "",
                dictDir = "",
                ruleFsts = ruleFsts ?: "",
                ruleFars = ruleFars ?: "",
                isKitten = isKitten,
                isStyleBertVits2 = isStyleBertVits2,
                numThreads = 4,  // Optimize for Dimensity 9500
            )

            Log.i(TAG, "TTS config: model=${config.model.vits.model}, tokens=${config.model.vits.tokens}, lexicon=${config.model.vits.lexicon}, dataDir=${config.model.vits.dataDir}")

            val preferenceHelper = PreferencesHelper(context)
            speed = preferenceHelper.getSpeed()
            speakerId = preferenceHelper.getSid()

            tts = OfflineTts(assetManager = context.assets, config = config)
            Log.i(TAG, "TTS initialized successfully. SampleRate=${tts?.sampleRate()}, NumSpeakers=${tts?.numSpeakers()}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TTS", e)
            tts = null
        }
    }
}
