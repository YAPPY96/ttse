// Copyright (c)  2023  Xiaomi Corporation
package com.k2fsa.sherpa.onnx

import android.content.res.AssetManager

data class OfflineTtsVitsModelConfig(
    var model: String = "",
    var lexicon: String = "",
    var tokens: String = "",
    var dataDir: String = "",
    var dictDir: String = "",
    var noiseScale: Float = 0.667f,
    var noiseScaleW: Float = 0.8f,
    var lengthScale: Float = 1.0f,
)

data class OfflineTtsMatchaModelConfig(
    var acousticModel: String = "",
    var vocoder: String = "",
    var lexicon: String = "",
    var tokens: String = "",
    var dataDir: String = "",
    var dictDir: String = "",
    var noiseScale: Float = 1.0f,
    var lengthScale: Float = 1.0f,
)

data class OfflineTtsKokoroModelConfig(
    var model: String = "",
    var voices: String = "",
    var tokens: String = "",
    var dataDir: String = "",
    var lexicon: String = "",
    var lang: String = "",
    var dictDir: String = "",
    var lengthScale: Float = 1.0f,
)

data class OfflineTtsZipVoiceModelConfig(
    var tokens: String = "",
    var encoder: String = "",
    var decoder: String = "",
    var vocoder: String = "",
    var dataDir: String = "",
    var lexicon: String = "",
    var featScale: Float = 0.1f,
    var tShift: Float = 0.5f,
    var targetRms: Float = 0.1f,
    var guidanceScale: Float = 1.0f,
)

data class OfflineTtsKittenModelConfig(
    var model: String = "",
    var voices: String = "",
    var tokens: String = "",
    var dataDir: String = "",
    var lengthScale: Float = 1.0f,
)

data class OfflineTtsPocketModelConfig(
    var lmFlow: String = "",
    var lmMain: String = "",
    var encoder: String = "",
    var decoder: String = "",
    var textConditioner: String = "",
    var vocabJson: String = "",
    var tokenScoresJson: String = "",
    var voiceEmbeddingCacheCapacity: Int = 50,
)

data class OfflineTtsSupertonicModelConfig(
    var durationPredictor: String = "",
    var textEncoder: String = "",
    var vectorEstimator: String = "",
    var vocoder: String = "",
    var ttsJson: String = "",
    var unicodeIndexer: String = "",
    var voiceStyle: String = "",
)

data class OfflineTtsModelConfig(
    var vits: OfflineTtsVitsModelConfig = OfflineTtsVitsModelConfig(),
    var matcha: OfflineTtsMatchaModelConfig = OfflineTtsMatchaModelConfig(),
    var kokoro: OfflineTtsKokoroModelConfig = OfflineTtsKokoroModelConfig(),
    var zipvoice: OfflineTtsZipVoiceModelConfig = OfflineTtsZipVoiceModelConfig(),
    var kitten: OfflineTtsKittenModelConfig = OfflineTtsKittenModelConfig(),
    var pocket: OfflineTtsPocketModelConfig = OfflineTtsPocketModelConfig(),
    var supertonic: OfflineTtsSupertonicModelConfig = OfflineTtsSupertonicModelConfig(),
    var numThreads: Int = 1,
    var debug: Boolean = false,
    var provider: String = "cpu",
)

data class OfflineTtsConfig(
    var model: OfflineTtsModelConfig = OfflineTtsModelConfig(),
    var ruleFsts: String = "",
    var ruleFars: String = "",
    var maxNumSentences: Int = 1,
    var silenceScale: Float = 0.2f,
)

data class GenerationConfig(
    var silenceScale: Float = 0.2f,
    var speed: Float = 1.0f,
    var sid: Int = 0,
    var referenceAudio: FloatArray? = null,
    var referenceSampleRate: Int = 0,
    var referenceText: String? = null,
    var numSteps: Int = 5,
    var extra: Map<String, String>? = null
)

data class GeneratedAudio(
    val samples: FloatArray,
    val sampleRate: Int,
) {
    fun save(filename: String): Boolean = saveImpl(filename = filename, samples = samples, sampleRate = sampleRate)

    private external fun saveImpl(
        filename: String,
        samples: FloatArray,
        sampleRate: Int
    ): Boolean

    companion object {
        init {
            System.loadLibrary("sherpa-onnx-jni")
        }
    }
}

class OfflineTts(
    var ptr: Long,
    val assetManager: AssetManager? = null,
    val config: OfflineTtsConfig,
) {
    constructor(config: OfflineTtsConfig) : this(
        ptr = 0,
        assetManager = null,
        config = config
    ) {
        ptr = newFromFile(config)
    }

    constructor(assetManager: AssetManager, config: OfflineTtsConfig) : this(
        ptr = 0,
        assetManager = assetManager,
        config = config
    ) {
        ptr = newFromAsset(assetManager, config)
    }

    protected fun finalize() {
        if (ptr != 0L) {
            delete(ptr)
            ptr = 0
        }
    }

    fun generate(
        text: String,
        sid: Int = 0,
        speed: Float = 1.0f,
    ): GeneratedAudio {
        return generateImpl(ptr, text, sid, speed)
    }

    fun generateWithCallback(
        text: String,
        sid: Int = 0,
        speed: Float = 1.0f,
        callback: (samples: FloatArray) -> Int,
    ): GeneratedAudio {
        return generateWithCallbackImpl(ptr, text, sid, speed, callback)
    }

    fun generateWithConfig(
        text: String,
        config: GenerationConfig,
    ): GeneratedAudio {
        return generateWithConfigImpl(ptr, text, config, null)
    }

    fun generateWithConfigAndCallback(
        text: String,
        config: GenerationConfig,
        callback: ((samples: FloatArray) -> Int)? = null,
    ): GeneratedAudio {
        return generateWithConfigImpl(ptr, text, config, callback)
    }

    fun getSampleRate(): Int = getSampleRate(ptr)
    fun sampleRate(): Int = getSampleRate(ptr)
    fun numSpeakers(): Int = getNumSpeakers(ptr)

    private external fun newFromFile(config: OfflineTtsConfig): Long
    private external fun newFromAsset(
        assetManager: AssetManager,
        config: OfflineTtsConfig,
    ): Long

    private external fun delete(ptr: Long)

    private external fun generateImpl(
        ptr: Long,
        text: String,
        sid: Int,
        speed: Float,
    ): GeneratedAudio

    private external fun generateWithCallbackImpl(
        ptr: Long,
        text: String,
        sid: Int,
        speed: Float,
        callback: (samples: FloatArray) -> Int
    ): GeneratedAudio

    private external fun generateWithConfigImpl(
        ptr: Long,
        text: String,
        config: GenerationConfig,
        callback: ((samples: FloatArray) -> Int)?
    ): GeneratedAudio

    private external fun getSampleRate(ptr: Long): Int
    private external fun getNumSpeakers(ptr: Long): Int

    companion object {
        init {
            System.loadLibrary("sherpa-onnx-jni")
        }
    }
}

fun getOfflineTtsConfig(
    modelDir: String,
    modelName: String, // for VITS
    acousticModelName: String, // for Matcha
    vocoder: String, // for Matcha
    voices: String, // for Kokoro or kitten
    lexicon: String,
    tokens: String = "",
    dataDir: String,
    dictDir: String, // unused
    ruleFsts: String,
    ruleFars: String,
    numThreads: Int? = null,
    isKitten: Boolean = false,
    isStyleBertVits2: Boolean = false,
): OfflineTtsConfig {
    val numberOfThreads = if (numThreads != null) {
        numThreads
    } else if (voices.isNotEmpty()) {
        4
    } else {
        2
    }

    val vits = if (modelName.isNotEmpty() && voices.isEmpty()) {
        OfflineTtsVitsModelConfig(
            model = if (modelName.contains("/")) modelName else "$modelDir/$modelName",
            lexicon = if (lexicon == "" || lexicon.contains("/")) {
                if (lexicon == "") (if (tokens != "" && tokens.contains("/")) tokens else "$modelDir/tokens.txt") else lexicon
            } else "$modelDir/$lexicon",
            tokens = when {
                tokens != "" -> if (tokens.contains("/")) tokens else "$modelDir/$tokens"
                else -> "$modelDir/tokens.txt"
            },
            dataDir = dataDir,
        )
    } else {
        OfflineTtsVitsModelConfig()
    }

    val matcha = if (acousticModelName.isNotEmpty()) {
        OfflineTtsMatchaModelConfig(
            acousticModel = if (acousticModelName.contains("/")) acousticModelName else "$modelDir/$acousticModelName",
            vocoder = vocoder,
            lexicon = if (lexicon == "" || lexicon.contains("/")) lexicon else "$modelDir/$lexicon",
            tokens = when {
                tokens != "" -> if (tokens.contains("/")) tokens else "$modelDir/$tokens"
                else -> "$modelDir/tokens.txt"
            },
            dataDir = dataDir,
        )
    } else {
        OfflineTtsMatchaModelConfig()
    }

    val kokoro = if (voices.isNotEmpty() && !isKitten) {
        OfflineTtsKokoroModelConfig(
            model = if (modelName.contains("/")) modelName else "$modelDir/$modelName",
            voices = if (voices.contains("/")) voices else "$modelDir/$voices",
            tokens = when {
                tokens != "" -> if (tokens.contains("/")) tokens else "$modelDir/$tokens"
                else -> "$modelDir/tokens.txt"
            },
            dataDir = dataDir,
            lexicon = when {
                lexicon == "" -> lexicon
                "," in lexicon -> lexicon
                lexicon.contains("/") -> lexicon
                else -> "$modelDir/$lexicon"
            },
        )
    } else {
        OfflineTtsKokoroModelConfig()
    }

    val kitten = if (isKitten) {
        OfflineTtsKittenModelConfig(
            model = if (modelName.contains("/")) modelName else "$modelDir/$modelName",
            voices = if (voices.contains("/")) voices else "$modelDir/$voices",
            tokens = when {
                tokens != "" -> if (tokens.contains("/")) tokens else "$modelDir/$tokens"
                else -> "$modelDir/tokens.txt"
            },
            dataDir = dataDir,
        )
    } else {
        OfflineTtsKittenModelConfig()
    }

    return OfflineTtsConfig(
        model = OfflineTtsModelConfig(
            vits = vits,
            matcha = matcha,
            kokoro = kokoro,
            kitten = kitten,
            numThreads = numberOfThreads,
            debug = true,
            provider = "cpu",
        ),
        ruleFsts = ruleFsts,
        ruleFars = ruleFars,
    )
}
