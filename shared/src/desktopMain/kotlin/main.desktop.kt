import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.example.data.UntisRepository
import com.example.data.room.InMemoryUntisDao
import com.russhwolf.settings.MapSettings

actual fun getPlatformName(): String = "Desktop"

actual fun createRepository(): UntisRepository {
    val settings = MapSettings()
    val dao = InMemoryUntisDao()
    return UntisRepository(settings, dao)
}

@Composable fun MainView() = App()

@Preview
@Composable
fun AppPreview() {
    App()
}