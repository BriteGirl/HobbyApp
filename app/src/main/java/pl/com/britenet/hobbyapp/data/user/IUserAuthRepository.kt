package pl.com.britenet.hobbyapp.data.user

import com.google.firebase.auth.FirebaseUser

interface IUserAuthRepository {
    fun getCurrentUser(): FirebaseUser?
}
