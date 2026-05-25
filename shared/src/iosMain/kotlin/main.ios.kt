import com.example.data.UntisRepository
import com.example.data.room.InMemoryUntisDao
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

actual fun getPlatformName(): String = "iOS"

actual fun createRepository(): UntisRepository {
    val settings = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    val dao = InMemoryUntisDao()
    return UntisRepository(settings, dao)
}