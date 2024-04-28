package uk.co.sullenart.photoalbum

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.imageLoader
import coil.memory.MemoryCache
import coil.util.Logger
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import timber.log.Timber
import timber.log.Timber.DebugTree
import uk.co.sullenart.photoalbum.albums.AlbumsRepository
import uk.co.sullenart.photoalbum.albums.AlbumsViewmodel
import uk.co.sullenart.photoalbum.albums.RealmAlbum
import uk.co.sullenart.photoalbum.auth.Auth
import uk.co.sullenart.photoalbum.auth.AuthInterceptor
import uk.co.sullenart.photoalbum.auth.RealmTokens
import uk.co.sullenart.photoalbum.auth.TokensRepository
import uk.co.sullenart.photoalbum.background.BackgroundFetcher
import uk.co.sullenart.photoalbum.config.Config
import uk.co.sullenart.photoalbum.google.GooglePhotos
import uk.co.sullenart.photoalbum.photos.PhotosRepository
import uk.co.sullenart.photoalbum.photos.PhotosViewmodel
import uk.co.sullenart.photoalbum.photos.RealmPhoto
import uk.co.sullenart.photoalbum.sign_in.SignInViewModel

class MainApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        startKoin {
            androidContext(this@MainApplication)
            modules(
                module {
                    single<Config> { Config(album = "Photo album") }
                    viewModelOf(::SignInViewModel)
                    viewModelOf(::AlbumsViewmodel)
                    viewModelOf(::PhotosViewmodel)
                    singleOf(::GooglePhotos)
                    singleOf(::Auth)
                    singleOf(::AuthInterceptor)
                    factoryOf(::TokensRepository)
                    factoryOf(::AlbumsRepository)
                    factoryOf(::PhotosRepository)

                    singleOf(::BackgroundFetcher)
                    /*single(createdAtStart = true) {
                        BackgroundFetcher(get(), get(), get(), get(), get()).apply {
                            GlobalScope.launch {
                                start()
                            }
                        }
                    }*/

                    single<Realm> {
                        val config = RealmConfiguration.Builder(
                            schema = setOf(RealmAlbum::class, RealmTokens::class, RealmPhoto::class),
                        )
                            .deleteRealmIfMigrationNeeded()
                            .build()
                        Realm.open(config)
                    }

                    single<ImageLoader> { androidContext().imageLoader }
                }
            )
        }
    }

    private val coilLogger = object : Logger {
        override var level: Int = 0

        override fun log(tag: String, priority: Int, message: String?, throwable: Throwable?) {
            Timber.i(message)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    // TODO Configure a large cache size.
                    .maxSizeBytes(1024 * 1024 * 1024)
                    .build()
            }
            .logger(coilLogger)
            .respectCacheHeaders(false)
            .build()
    }
}
