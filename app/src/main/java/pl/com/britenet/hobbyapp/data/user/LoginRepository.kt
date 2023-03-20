package pl.com.britenet.hobbyapp.data.user

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import pl.com.britenet.hobbyapp.exceptions.UnidentifiedException
import javax.inject.Inject

class LoginRepository @Inject constructor() : ILoginRepository {
    override suspend fun logInWithGoogle(intent: Intent): Task<AuthResult> {
        val googleSignInAccount = GoogleSignIn.getSignedInAccountFromIntent(intent).await()
        if (googleSignInAccount.idToken != null) {
            val idToken = googleSignInAccount.idToken
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val signInTask = Firebase.auth.signInWithCredential(authCredential)
            signInTask.await()
            return signInTask
        } else throw UnidentifiedException()
    }

    override suspend fun logInWithEmail(login: String, password: String): Task<AuthResult> {
        val task = Firebase.auth.signInWithEmailAndPassword(login, password)
        task.await()
        return task
    }

    override suspend fun registerWithEmail(login: String, password: String): Task<AuthResult> {
        val taskResult = Firebase.auth.createUserWithEmailAndPassword(login, password)
        taskResult.await()
        return taskResult
    }
}
