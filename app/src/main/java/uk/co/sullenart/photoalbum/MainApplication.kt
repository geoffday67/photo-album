package uk.co.sullenart.photoalbum

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.imageLoader
import coil.memory.MemoryCache
import com.jakewharton.threetenabp.AndroidThreeTen
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
import uk.co.sullenart.photoalbum.background.RefreshWorker
import uk.co.sullenart.photoalbum.coil.ItemFetcher
import uk.co.sullenart.photoalbum.coil.ItemKeyer
import uk.co.sullenart.photoalbum.google.GooglePhotos
import uk.co.sullenart.photoalbum.items.ItemUtils
import uk.co.sullenart.photoalbum.items.ItemsViewmodel
import uk.co.sullenart.photoalbum.items.MediaItemsRepository
import uk.co.sullenart.photoalbum.items.RealmItem
import uk.co.sullenart.photoalbum.settings.SettingsViewmodel
import java.util.concurrent.TimeUnit

class MainApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        startKoin {
            androidContext(this@MainApplication)
            modules(
                module {
                    viewModelOf(::AlbumsViewmodel)
                    viewModelOf(::ItemsViewmodel)
                    viewModelOf(::SettingsViewmodel)
                    singleOf(::GooglePhotos)
                    singleOf(::Auth)
                    singleOf(::AuthInterceptor)
                    factoryOf(::UserRepository)
                    factoryOf(::AlbumsRepository)
                    factoryOf(::MediaItemsRepository)
                    singleOf(::ItemUtils)
                    factoryOf(::ItemFetcher)
                    singleOf(::BackgroundFetcher)

                    single<Realm> {
                        val config = RealmConfiguration.Builder(
                            schema = setOf(RealmAlbum::class, RealmUser::class, RealmItem::class),
                        )
                            .deleteRealmIfMigrationNeeded()
                            .build()
                        Realm.open(config)
                    }

                    single<ImageLoader> { androidContext().imageLoader }
                }
            )
        }

        startRefreshWorker()
    }

    private fun startRefreshWorker() {
        //Timber.d("WorkManager initialised: ${WorkManager.isInitialized()}")
        //return

        val request = PeriodicWorkRequestBuilder<RefreshWorker>(1, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "REFRESH_WORK",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                request,
            )
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(ItemFetcher.Factory())
                add(ItemKeyer())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(1.0)
                    .build()
            }
            .build()
    }
}
