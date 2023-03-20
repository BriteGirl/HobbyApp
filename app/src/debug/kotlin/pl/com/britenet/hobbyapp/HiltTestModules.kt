package pl.com.britenet.hobbyapp

import com.google.firebase.auth.FirebaseUser
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import pl.com.britenet.hobbyapp.data.Hobby
import pl.com.britenet.hobbyapp.data.IHobbyDatabase
import pl.com.britenet.hobbyapp.data.SuggestedHobby
import pl.com.britenet.hobbyapp.data.UserHobbyAssociation
import pl.com.britenet.hobbyapp.data.admin.AdminData
import pl.com.britenet.hobbyapp.data.admin.AdminRepository
import pl.com.britenet.hobbyapp.data.admin.AdminRole
import pl.com.britenet.hobbyapp.data.admin.IAdminRepository
import pl.com.britenet.hobbyapp.data.images.IImagesRepository
import pl.com.britenet.hobbyapp.data.images.ImagesRepository
import pl.com.britenet.hobbyapp.data.user.*
import pl.com.britenet.hobbyapp.di.DatabaseDependentModule
import pl.com.britenet.hobbyapp.di.ImagesModule
import javax.inject.Singleton

class HiltTestModules {
    @Module
    @TestInstallIn(
        components = [SingletonComponent::class],
        replaces = [DatabaseDependentModule::class]
    )
    object DatabaseDependentTestModule {
        var hobbiesList = mutableListOf(
            Hobby("1", "Hobby1"),
            Hobby("2", "Hobby2"), Hobby("3", "Hobby3")
        )
        var userHobbies = mutableListOf(
            Hobby("2", "Hobby2"),
            Hobby("3", "Hobby3")
        )
        val users = mutableListOf(
            UserData("1U", "User 1", "user_1", "user1@email.com"),
            UserData("2U", "User 2", "user_2", "user2@email.com")
        )
        val userHobbyAssociations  = mutableListOf(
            UserHobbyAssociation("doc1", "1U", "2"),
            UserHobbyAssociation("doc2", "1U", "3")
        )
        val hobbySuggestions = mutableListOf(
            SuggestedHobby("1S", "Hobby1S", "1U"),
            SuggestedHobby("2S", "Hobby2S", "1U")
        )
        val admins = mutableListOf<AdminData>(
            AdminData("2U", AdminRole.ADMIN, "User 2", "user_2", "user2@email.com")
        )
        var isUserLoggedIn = false
        var userLoggedIn: FirebaseUser = mockk()
        var isUserAdmin = false


        @Singleton
        @Provides
        fun provideUserTestRepository(): IUserRepository {
            val mockUserRepo = mockk<UserRepository>(relaxed = true)
            coEvery { mockUserRepo.getUserData(users[0].userId) } returns users[0]
            coEvery { mockUserRepo.getFriendRequests(any()) } returns users
            coEvery { mockUserRepo.isUserAdmin(any()) } returns isUserAdmin
            return mockUserRepo
        }

        @Singleton
        @Provides
        fun provideUserAuthTestRepository(): IUserAuthRepository {
            val mockUserAuthRepo = mockk<UserAuthRepository>()
            every { mockUserAuthRepo.getCurrentUser() } answers {
                if (isUserLoggedIn) userLoggedIn else null
            }
            every { userLoggedIn.email } returns users[0].email
            every { userLoggedIn.displayName } returns users[0].name
            every { userLoggedIn.uid } answers { if (isUserLoggedIn) users[0].userId else "" }
            return mockUserAuthRepo
        }

        @Singleton
        @Provides
        fun provideAdminTestRepository(): IAdminRepository {
            val mockAdminRepo = mockk<AdminRepository>(relaxed = true)
            coEvery { mockAdminRepo.getAdmins() } returns admins
            coEvery { mockAdminRepo.getHobbySuggestions() } returns hobbySuggestions
            val booleanSlot = slot<Boolean>()
            val listSlot = slot<MutableList<SuggestedHobby>>()
            coEvery {
                mockAdminRepo.acceptOrRejectHobbySuggestions(
                    capture(booleanSlot),
                    capture(listSlot)
                )
            } answers {
                hobbySuggestions.removeAll { suggestedHobby -> listSlot.captured.contains(suggestedHobby) }
                val areHobbiesAccepted = booleanSlot.captured
                if (areHobbiesAccepted) {
                    hobbiesList.addAll(listSlot.captured)
                }
            }
            return mockAdminRepo
        }

        @Singleton
        @Provides
        fun provideLoginRepository(): ILoginRepository {
            val mockLoginRepo = mockk<LoginRepository>(relaxed = true)
            return mockLoginRepo
        }


        @Singleton
        @Provides
        fun provideHobbyTestDatabase(): IHobbyDatabase{
            val mockHobbyDb = mockk<IHobbyDatabase>(relaxed = true)
            coEvery { mockHobbyDb.getAllHobbies() } returns hobbiesList
            coEvery { mockHobbyDb.getUserHobbiesAssociations(any()) } returns userHobbyAssociations
            coEvery { mockHobbyDb.getUserHobbies(any()) } returns userHobbies
            coEvery { mockHobbyDb.getUserIdsByHobbyId(any()) } returns users.map { userData -> userData.userId }
            coEvery { mockHobbyDb.getHobbyById(any()) } returns hobbiesList[0]
            val slot = slot<SuggestedHobby>()
            coEvery { mockHobbyDb.addHobbySuggestion(capture(slot), any(), any())  } answers {
                slot.captured.name.let { SuggestedHobby("3S", it, "1U") }
                    .let { it1 -> hobbySuggestions.add(it1) }
                true
            }
            return mockHobbyDb
        }
    }

    @Module
    @TestInstallIn(
        components = [SingletonComponent::class],
        replaces = [ImagesModule::class]
    )
    object ImagesTestModule {
        @Singleton
        @Provides
        fun provideImageRepository(): IImagesRepository = mockk<ImagesRepository>(relaxed = true)
    }
}