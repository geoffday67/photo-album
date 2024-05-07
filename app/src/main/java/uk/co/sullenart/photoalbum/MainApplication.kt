package uk.co.sullenart.photoalbum

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.util.Logger
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
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
import uk.co.sullenart.photoalbum.auth.RealmUser
import uk.co.sullenart.photoalbum.auth.UserRepository
import uk.co.sullenart.photoalbum.background.BackgroundFetcher
import uk.co.sullenart.photoalbum.google.GooglePhotos
import uk.co.sullenart.photoalbum.photos.PhotosRepository
import uk.co.sullenart.photoalbum.photos.PhotosViewmodel
import uk.co.sullenart.photoalbum.photos.RealmPhoto
import uk.co.sullenart.photoalbum.settings.SettingsViewmodel

class MainApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        startKoin {
            androidContext(this@MainApplication)
            modules(
                module {
                    viewModelOf(::AlbumsViewmodel)
                    viewModelOf(::PhotosViewmodel)
                    viewModelOf(::SettingsViewmodel)
                    singleOf(::GooglePhotos)
                    singleOf(::Auth)
                    singleOf(::AuthInterceptor)
                    factoryOf(::UserRepository)
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
                            schema = setOf(RealmAlbum::class, RealmUser::class, RealmPhoto::class),
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
        override var minLevel = Logger.Level.Verbose

        override fun log(tag: String, level: Logger.Level, message: String?, throwable: Throwable?) {
            Timber.i(message)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
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
            .build()
    }
}
