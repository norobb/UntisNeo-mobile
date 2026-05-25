import androidx.compose.runtime.Composable
import com.example.data.UntisRepository
import com.example.data.room.InMemoryUntisDao
import com.russhwolf.settings.SharedPreferencesSettings
import android.content.Context

actual fun getPlatformName(): String = "Android"

// For Android, this is overridden in the Activity/Application.
// We provide an in-memory fallback here for compilation.
actual fun createRepository(): UntisRepository {
    // On Android, the real repository is created with Context in the ViewModel factory
    // This fallback is provided just to satisfy the expect/actual contract
    val dao = InMemoryUntisDao()
    // Settings requires context - we use empty settings as fallback
    val settings = com.russhwolf.settings.MapSettings()
    return UntisRepository(settings, dao)
}

@Composable fun MainView() = App()
