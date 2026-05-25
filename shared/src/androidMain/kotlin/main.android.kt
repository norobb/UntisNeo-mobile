import androidx.compose.runtime.Composable
import com.example.data.UntisRepository
import com.example.data.room.InMemoryUntisDao
import com.russhwolf.settings.SharedPreferencesSettings
import android.content.Context

actual fun getPlatformName(): String = "Android"

// For Android, this is overridden in the Activity/Application.
// We provide an in-memory fallback here for compilation.
actual fun createRepository(): UntisRepository {
    val dao = InMemoryUntisDao()
    // In KMP, we provide a dummy fallback. In the actual Android App, we inject the context
    val settings = object : com.russhwolf.settings.Settings {
        override val keys: Set<String> get() = emptySet()
        override val size: Int get() = 0
        override fun clear() {}
        override fun getBoolean(key: String, defaultValue: Boolean): Boolean = defaultValue
        override fun getBooleanOrNull(key: String): Boolean? = null
        override fun getDouble(key: String, defaultValue: Double): Double = defaultValue
        override fun getDoubleOrNull(key: String): Double? = null
        override fun getFloat(key: String, defaultValue: Float): Float = defaultValue
        override fun getFloatOrNull(key: String): Float? = null
        override fun getInt(key: String, defaultValue: Int): Int = defaultValue
        override fun getIntOrNull(key: String): Int? = null
        override fun getLong(key: String, defaultValue: Long): Long = defaultValue
        override fun getLongOrNull(key: String): Long? = null
        override fun getString(key: String, defaultValue: String): String = defaultValue
        override fun getStringOrNull(key: String): String? = null
        override fun hasKey(key: String): Boolean = false
        override fun putBoolean(key: String, value: Boolean) {}
        override fun putDouble(key: String, value: Double) {}
        override fun putFloat(key: String, value: Float) {}
        override fun putInt(key: String, value: Int) {}
        override fun putLong(key: String, value: Long) {}
        override fun putString(key: String, value: String) {}
        override fun remove(key: String) {}
    }
    return UntisRepository(settings, dao)
}

@Composable fun MainView() = App()
