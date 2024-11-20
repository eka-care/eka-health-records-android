package eka.care.documents.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.moczul.ok2curl.CurlInterceptor
import eka.care.documents.BuildConfig
import com.moczul.ok2curl.logger.Logger
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.reflect.Type
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.Collections
import java.util.concurrent.TimeUnit

class Networking private constructor() {
    private lateinit var okHttpSetup: IOkHttpSetup
    private lateinit var baseUrl: String
    private lateinit var retrofit: Retrofit
    private val servicesCache = Collections.synchronizedMap(LRUCache<String, Any>(10))

    companion object {
        private val instance by lazy { Networking() }

        fun init(
            baseUrl: String,
            okHttpSetup: IOkHttpSetup,
            converterFactoryType: ConverterFactoryType = ConverterFactoryType.GSON
        ) {
            instance.init(baseUrl, okHttpSetup, converterFactoryType)
        }

        fun <T> create(
            clazz: Class<T>,
            baseUrl: String? = null,
            converterFactoryType: ConverterFactoryType = ConverterFactoryType.GSON
        ): T = instance.create(clazz, baseUrl, converterFactoryType)
    }

    fun init(baseUrl: String, okHttpSetup: IOkHttpSetup, converterFactoryType: ConverterFactoryType) {
        if (this::baseUrl.isInitialized) {
            throw Exception("Networking is already initialised. Check if you are calling from multiple places")
        }
        this.baseUrl = baseUrl
        this.okHttpSetup = okHttpSetup
        retrofit = createClient(this.baseUrl, converterFactoryType)
    }

    @Synchronized
    fun <T> create(
        clazz: Class<T>,
        baseUrl: String? = null,
        converterFactoryType: ConverterFactoryType
    ): T {
        val key = clazz.canonicalName ?: throw IllegalArgumentException("Class must have a canonical name")

        @Suppress("UNCHECKED_CAST")
        return servicesCache.getOrPut(key) {
            if (!baseUrl.isNullOrEmpty() && !this.baseUrl.equals(baseUrl, true)) {
                createClient(baseUrl, converterFactoryType).create(clazz)
            } else {
                retrofit.create(clazz)
            }
        } as T
    }

    private fun createClient(baseUrl: String, converterFactoryType: ConverterFactoryType): Retrofit {
        val builder = Retrofit.Builder().apply {
            baseUrl(baseUrl)
            addCallAdapterFactory(NetworkResponseAdapterFactory())

            // Add converter factories based on type
            when (converterFactoryType) {
                ConverterFactoryType.GSON -> {
                    val gson = GsonBuilder()
                        .setLenient()
                        .create()
                    addConverterFactory(GsonConverterFactory.create(gson))
                }
                ConverterFactoryType.PROTO -> {
                    // Custom Protobuf converter factory
                    addConverterFactory(object : Converter.Factory() {
                        override fun responseBodyConverter(
                            type: Type,
                            annotations: Array<out Annotation>,
                            retrofit: Retrofit
                        ): Converter<ResponseBody, *>? {
                            return if (type == ByteArray::class.java) {
                                Converter<ResponseBody, ByteArray> { body -> body.bytes() }
                            } else null
                        }
                    })
                }
            }
        }

        val clientBuilder = OkHttpClient.Builder().apply {
            addInterceptor(DefaultNetworkInterceptor(okHttpSetup))
            addInterceptor(AuthInterceptor(okHttpSetup))

            // Add content type interceptor for Protobuf
            addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Accept", "application/x-protobuf")
                    .build()
                chain.proceed(request)
            }

            callTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            connectTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            readTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            writeTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            authenticator(AccessTokenAuthenticator(okHttpSetup))

            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                })
            }
        }

        return builder.client(clientBuilder.build()).build()
    }
}

enum class ConverterFactoryType {
    GSON,
    PROTO
}