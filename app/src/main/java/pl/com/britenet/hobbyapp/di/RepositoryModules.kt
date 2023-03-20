package pl.com.britenet.hobbyapp.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.com.britenet.hobbyapp.BuildConfig
import pl.com.britenet.hobbyapp.data.FirestoreHobbyDatabase
import pl.com.britenet.hobbyapp.data.HobbiesRepository
import pl.com.britenet.hobbyapp.data.IHobbiesRepository
import pl.com.britenet.hobbyapp.data.IHobbyDatabase
import pl.com.britenet.hobbyapp.data.admin.AdminRepository
import pl.com.britenet.hobbyapp.data.admin.IAdminRepository
import pl.com.britenet.hobbyapp.data.chat.ChatRepository
import pl.com.britenet.hobbyapp.data.chat.IChatRepository
import pl.com.britenet.hobbyapp.data.images.IImagesRepository
import pl.com.britenet.hobbyapp.data.images.ImagesRepository
import pl.com.britenet.hobbyapp.data.location.ILocationRepository
import pl.com.britenet.hobbyapp.data.location.LocationRepository
import pl.com.britenet.hobbyapp.data.user.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImagesModule {
    @Singleton
    @Provides
    fun provideImageRepository(): IImagesRepository = ImagesRepository(ImagesRepository.ImageFolder.IMAGES_FOLDER)
}

@Module
@InstallIn(SingletonComponent::class)
// a module for database dependent components that need to be mocked in tests
abstract class DatabaseDependentModule {
    @Singleton
    @Binds
    abstract fun bindUserRepository(userRepository: UserRepository): IUserRepository

    @Singleton
    @Binds
    abstract fun bindUserAuthRepository(userAuthRepository: UserAuthRepository): IUserAuthRepository

    @Singleton
    @Binds
    abstract fun bindAdminRepository(adminRepository: AdminRepository): IAdminRepository

    @Singleton
    @Binds
    abstract fun bindLoginRepository(loginRepository: LoginRepository): ILoginRepository

    @Singleton
    @Binds
    abstract fun bindHobbyDatabase(hobbyDatabase: FirestoreHobbyDatabase): IHobbyDatabase

    @Singleton
    @Binds
    abstract fun bindLocationRepository(locationRepository: LocationRepository): ILocationRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule() {
    @Singleton
    @Binds
    abstract fun bindHobbiesRepository(hobbiesRepository: HobbiesRepository): IHobbiesRepository
}

@Module
@InstallIn(SingletonComponent::class)
class RealtimeDbModule() {
    @Singleton
    @Provides
    fun provideChatRepository(): IChatRepository = ChatRepository(BuildConfig.REALTIME_DB_URL)
}
