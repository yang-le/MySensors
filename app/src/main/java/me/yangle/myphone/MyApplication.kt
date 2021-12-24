package me.yangle.myphone

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@HiltAndroidApp
class MyApplication : Application() {
    @Module
    @InstallIn(ViewModelComponent::class)
    object Location {
        @Provides
        fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
            return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    @Module
    @InstallIn(ViewModelComponent::class)
    object Geocoder {
        @Provides
        fun provideGeocoder(@ApplicationContext context: Context): android.location.Geocoder {
            return Geocoder(context)
        }
    }
}