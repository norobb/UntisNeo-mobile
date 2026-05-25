import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.data.UntisRepository
import com.example.ui.UntisViewModel
import com.example.ui.theme.MyApplicationTheme
import com.russhwolf.settings.Settings

expect fun getPlatformName(): String

/**
 * Creates the AppDatabase (platform-specific driver needed).
 * For now uses an in-memory/stub DAO.
 */
expect fun createRepository(): UntisRepository

@Composable
fun App() {
    val repository = remember { createRepository() }
    val viewModel = remember { UntisViewModel(repository) }

    MyApplicationTheme {
        UntisAppRoot(viewModel)
    }
}

/**
 * Main composable that routes between screens based on viewModel state.
 */
@Composable
fun UntisAppRoot(viewModel: UntisViewModel) {
    com.example.ui.MainAppContent(viewModel)
}