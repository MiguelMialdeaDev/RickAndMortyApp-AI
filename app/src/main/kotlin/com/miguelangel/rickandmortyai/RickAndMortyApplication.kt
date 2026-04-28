package com.miguelangel.rickandmortyai

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RickAndMortyApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(true)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .respectCacheHeaders(false)
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(100L * 1024 * 1024)
                .build()
        }
        .build()
}
