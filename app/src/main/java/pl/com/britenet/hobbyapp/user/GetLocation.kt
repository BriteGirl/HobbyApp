package pl.com.britenet.hobbyapp.user

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import pl.com.britenet.hobbyapp.data.location.GeoDataResult

class GetLocation : ActivityResultContract<Unit, GeoDataResult>() {
    companion object {
        const val RESULT_DATA_EXTRA = "RESULT_DATA"
    }

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, UserLocationActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): GeoDataResult {
        return intent?.getParcelableExtra(RESULT_DATA_EXTRA)
            ?: GeoDataResult.GeoDataResultCancelled()
    }
}