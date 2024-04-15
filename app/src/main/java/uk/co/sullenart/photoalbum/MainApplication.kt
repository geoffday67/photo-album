package uk.co.sullenart.photoalbum

import android.app.Application
import android.content.Context
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
import uk.co.sullenart.photoalbum.albums.AlbumsViewModel
import uk.co.sullenart.photoalbum.config.Config
import uk.co.sullenart.photoalbum.realm.RealmAlbum
import uk.co.sullenart.photoalbum.service.Auth
import uk.co.sullenart.photoalbum.service.AuthInterceptor
import uk.co.sullenart.photoalbum.service.GooglePhotos
import uk.co.sullenart.photoalbum.service.TokensRepository
import uk.co.sullenart.photoalbum.sign_in.SignInViewModel

class MainApplication : Application() {
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
                    viewModelOf(::PhotosViewModel)
                    viewModelOf(::SignInViewModel)
                    viewModelOf(::AlbumsViewModel)
                    singleOf(::GooglePhotos)
                    singleOf(::Auth)
                    singleOf(::AuthInterceptor)
                    factoryOf(::TokensRepository)
                    factoryOf(::AlbumsRepository)

                    single<Realm> {
                        val config = RealmConfiguration.create(schema = setOf(RealmAlbum::class))
                        Realm.open(config)
                    }
                }
            )
        }

        /*GlobalScope.launch {
            /*realm.write {
                val album = RealmAlbum().apply {
                    title = "Geoff is great!"
                }
                copyToRealm(album)
                Timber.d("Data written to Realm")
            }*/

            realm.query<RealmAlbum>().asFlow().map { it.list.copyFromRealm().map { Album(it.title) } }.collect {
                Timber.d("Found ${it.joinToString { it.title }}")
            }

            realm.close()
            Timber.d("Realm closed")
        }*/
    }
}