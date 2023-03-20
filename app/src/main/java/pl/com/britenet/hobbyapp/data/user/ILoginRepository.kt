package pl.com.britenet.hobbyapp.data.user

import android.content.Intent
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

interface ILoginRepository {
    suspend fun logInWithGoogle(intent: Intent): Task<AuthResult>
    suspend fun logInWithEmail(login: String, password: String): Task<AuthResult>
    suspend fun registerWithEmail(login: String, password: String): Task<AuthResult>
}
