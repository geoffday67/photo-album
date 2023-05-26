package uk.co.sullenart.photoalbum

import android.app.Application
import android.content.Context
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import uk.co.sullenart.photoalbum.albums.AlbumsViewModel
import uk.co.sullenart.photoalbum.database.Database
import uk.co.sullenart.photoalbum.service.Auth
import uk.co.sullenart.photoalbum.service.AuthInterceptor
import uk.co.sullenart.photoalbum.service.GooglePhotos
import uk.co.sullenart.photoalbum.service.Tokens
import uk.co.sullenart.photoalbum.sign_in.SignInViewModel

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(
                module {
                    single<Database> { buildDatabase(get()) }
                    viewModelOf(::PhotosViewModel)
                    viewModelOf(::SignInViewModel)
                    viewModelOf(::AlbumsViewModel)
                    singleOf(::GooglePhotos)
                    singleOf(::Auth)
                    singleOf(::AuthInterceptor)
                }
            )
        }
    }

    private fun buildDatabase(context: Context) =
        Room.databaseBuilder(
            context,
            Database::class.java, "photo-album"
        ).build()
}