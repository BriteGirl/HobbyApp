package pl.com.britenet.hobbyapp.data.user

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class UserAuthRepository @Inject constructor() : IUserAuthRepository {
    override fun getCurrentUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }
}
