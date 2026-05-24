package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UntisViewModel
import com.example.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ArcadeScreen(viewModel: UntisViewModel) {
    var activeTab by remember { mutableStateOf("MENU") } // MENU, SNAKE, FLAPPY, RICK

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Back Button if inside a game
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NothingHeader(text = "NEO ARCADE", fontSize = 28.sp)
                if (activeTab != "MENU") {
                    IconButton(
                        onClick = { activeTab = "MENU" },
                        modifier = Modifier.background(androidx.compose.ui.graphics.Color.DarkGray, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Game", tint = androidx.compose.ui.graphics.Color.White)
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.currentScreen = "INFO" },
                        modifier = Modifier.background(NothingCardGray, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Info", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }

            when (activeTab) {
                "MENU" -> ArcadeMenu(onSelect = { activeTab = it })
                "SNAKE" -> SnakeGameScreen()
                "FLAPPY" -> FlappyBirdGameScreen()
                "RICK" -> RickCheckScreen()
            }
        }
    }
}

@Composable
fun ArcadeMenu(onSelect: (String) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NothingCardGray),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect("SNAKE") }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(androidx.compose.ui.graphics.Color.Red, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Black, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SNAKE CLASSIC",  fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White, fontSize = 16.sp)
                        Text("Bewege die Schlange, iss den roten Punkt und werde unendlich lang!", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NothingCardGray),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect("FLAPPY") }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(NothingWhite, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("NEO FLAPPER",  fontWeight = FontWeight.Bold, color = NothingWhite, fontSize = 16.sp)
                        Text("Fliege durch die Säulen, meide Hindernisse und schlage den Highscore!", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NothingCardGray),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect("RICK") }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFEAB308), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = NothingBlack, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("URGENTE SCHULMITTEILUNG", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite, fontSize = 16.sp)
                        Text("Eine offizielle dringende Mitteilung der Schulleitung bezüglich des Stundenplans.", fontFamily = FontFamily.Monospace, color = androidx.compose.ui.graphics.Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ------------------- SNAKE GAME IMPLEMENTATION -------------------
enum class SnakeDirection { UP, DOWN, LEFT, RIGHT }

@Composable
fun SnakeGameScreen() {
    val gridSize = 16
    var snake by remember { mutableStateOf(listOf(Offset(8f, 10f), Offset(8f, 11f), Offset(8f, 12f))) }
    var food by remember { mutableStateOf(Offset(5f, 5f)) }
    var direction by remember { mutableStateOf(SnakeDirection.UP) }
    var isRunning by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(0) }
    var speedMs by remember { mutableStateOf(200L) }

    val scope = rememberCoroutineScope()

    fun resetGame() {
        snake = listOf(Offset(8f, 8f), Offset(8f, 9f), Offset(8f, 10f))
        direction = SnakeDirection.UP
        food = Offset(Random.nextInt(gridSize).toFloat(), Random.nextInt(gridSize).toFloat())
        score = 0
        isGameOver = false
        isRunning = true
    }

    // Snake movement loop
    LaunchedEffect(isRunning, direction, speedMs) {
        if (!isRunning) return@LaunchedEffect
        while (isActive) {
            delay(speedMs)
            
            val head = snake.first()
            val newHead = when (direction) {
                SnakeDirection.UP -> Offset(head.x, head.y - 1)
                SnakeDirection.DOWN -> Offset(head.x, head.y + 1)
                SnakeDirection.LEFT -> Offset(head.x - 1, head.y)
                SnakeDirection.RIGHT -> Offset(head.x + 1, head.y)
            }

            // Wall collision or self collision
            val isCollision = newHead.x < 0 || newHead.x >= gridSize ||
                              newHead.y < 0 || newHead.y >= gridSize ||
                              snake.contains(newHead)

            if (isCollision) {
                isGameOver = true
                isRunning = false
                if (score > highScore) {
                    highScore = score
                }
                break
            }

            val newSnake = mutableListOf(newHead)
            if (newHead == food) {
                // Growth & Reposition Food
                newSnake.addAll(snake)
                score++
                var nextFood = Offset(Random.nextInt(gridSize).toFloat(), Random.nextInt(gridSize).toFloat())
                while (snake.contains(nextFood)) {
                    nextFood = Offset(Random.nextInt(gridSize).toFloat(), Random.nextInt(gridSize).toFloat())
                }
                food = nextFood
            } else {
                newSnake.addAll(snake.take(snake.size - 1))
            }
            snake = newSnake
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SCORE: $score", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite, fontSize = 14.sp)
            Text("BEST: $highScore", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingMutedGray, fontSize = 14.sp)
        }

        // Speed Selection Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TEMPO:", fontFamily = FontFamily.Monospace, color = NothingMutedGray, fontSize = 11.sp)
            listOf(250L to "EASY", 180L to "NORMAL", 100L to "TURBO").forEach { (ms, name) ->
                val selected = speedMs == ms
                Text(
                    text = name,
                    fontFamily = FontFamily.Monospace,
                    color = if (selected) androidx.compose.ui.graphics.Color.Red else NothingMutedGray,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable { speedMs = ms }
                        .background(if (selected) Color(0x33FF3131) else Color.Transparent, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp
                )
            }
        }

        // Game canvas box with detectTapGestures to support simple touch direction controls
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .border(2.dp, Color(0xFF222222), RoundedCornerShape(16.dp))
                .background(Color(0xFF070707))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val localWidth = size.width
                        val localHeight = size.height

                        // Split into quadrant diagonals
                        val clickX = offset.x
                        val clickY = offset.y

                        // Compute if it's UP, DOWN, LEFT, RIGHT depending on quadrant
                        val isLeftOfSlash = clickY > (localHeight / localWidth) * clickX
                        val isLeftOfBackslash = clickY < localHeight - (localHeight / localWidth) * clickX

                        if (isLeftOfSlash && isLeftOfBackslash) {
                            if (direction != SnakeDirection.RIGHT) direction = SnakeDirection.LEFT
                        } else if (!isLeftOfSlash && !isLeftOfBackslash) {
                            if (direction != SnakeDirection.LEFT) direction = SnakeDirection.RIGHT
                        } else if (isLeftOfSlash && !isLeftOfBackslash) {
                            if (direction != SnakeDirection.UP) direction = SnakeDirection.DOWN
                        } else {
                            if (direction != SnakeDirection.DOWN) direction = SnakeDirection.UP
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val blockW = size.width / gridSize
                val blockH = size.height / gridSize

                // Draw Food
                drawRect(
                    color = androidx.compose.ui.graphics.Color.Red,
                    topLeft = Offset(food.x * blockW + 2, food.y * blockH + 2),
                    size = Size(blockW - 4, blockH - 4)
                )

                // Draw Snake
                snake.forEachIndexed { idx, segment ->
                    val color = if (idx == 0) Color.White else Color.Gray
                    drawRect(
                        color = color,
                        topLeft = Offset(segment.x * blockW + 1, segment.y * blockH + 1),
                        size = Size(blockW - 2, blockH - 2)
                    )
                }
            }

            if (!isRunning && !isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xE0000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("SNAKE NEO", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite, fontSize = 20.sp)
                        Text("Tippe auf den Rand um zu steuern", fontFamily = FontFamily.Monospace, color = NothingMutedGray, fontSize = 12.sp)
                        NothingButton(text = "SPIEL STARTEN", onClick = { resetGame() })
                    }
                }
            }

            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xF0000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("SPIEL VORBEI", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingRed, fontSize = 24.sp)
                        Text("Dein Score: $score", fontFamily = FontFamily.Monospace, color = NothingWhite, fontSize = 16.sp)
                        NothingButton(text = "NEUSTARTS", onClick = { resetGame() })
                    }
                }
            }
        }

        // Control buttons helpers
        Row(
            modifier = Modifier.fillMaxWidth(0.6f),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { if (direction != SnakeDirection.DOWN) direction = SnakeDirection.UP },
                    modifier = Modifier.background(NothingCardGray, CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "UP", tint = NothingWhite)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    IconButton(
                        onClick = { if (direction != SnakeDirection.RIGHT) direction = SnakeDirection.LEFT },
                        modifier = Modifier.background(NothingCardGray, CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "LEFT", tint = NothingWhite)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    IconButton(
                        onClick = { if (direction != SnakeDirection.LEFT) direction = SnakeDirection.RIGHT },
                        modifier = Modifier.background(NothingCardGray, CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "RIGHT", tint = NothingWhite)
                    }
                }
                IconButton(
                    onClick = { if (direction != SnakeDirection.UP) direction = SnakeDirection.DOWN },
                    modifier = Modifier.background(NothingCardGray, CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "DOWN", tint = NothingWhite)
                }
            }
        }
    }
}


// ------------------- FLAPPY BIRD GAME IMPLEMENTATION -------------------
class ObstaclePipe(
    var x: Float,
    val topHeight: Float,
    val bottomHeight: Float,
    var passed: Boolean = false
)

@Composable
fun FlappyBirdGameScreen() {
    var birdY by remember { mutableStateOf(300f) }
    var velocity by remember { mutableStateOf(0f) }
    val gravity = 0.8f
    val jumpForce = -12f

    var pipes = remember { mutableStateListOf<ObstaclePipe>() }
    var isRunning by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(0) }

    fun resetGame() {
        birdY = 250f
        velocity = 0f
        pipes.clear()
        // Pre-add first pipe
        pipes.add(ObstaclePipe(800f, 150f, 150f))
        score = 0
        isGameOver = false
        isRunning = true
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        while (isActive) {
            delay(20) // ~50fps logic

            // Apply gravity
            velocity += gravity
            birdY += velocity

            // Move pipes
            pipes.forEach { pipe ->
                pipe.x -= 4.2f
            }

            // Remove off-screen pipes
            if (pipes.isNotEmpty() && pipes.first().x < -100f) {
                pipes.removeAt(0)
            }

            // Standard Spawn pipe
            if (pipes.isEmpty() || pipes.last().x < 450f) {
                val gap = 160f
                val totalHeight = 500f
                val topH = Random.nextInt(80, 260).toFloat()
                val bottomH = totalHeight - topH - gap
                pipes.add(ObstaclePipe(800f, topH, bottomH))
            }

            // Bird collisions
            val birdRadius = 15f
            val groundY = 500f
            if (birdY - birdRadius < 0 || birdY + birdRadius >= groundY) {
                // Ground or Ceiling Hit
                isGameOver = true
                isRunning = false
                if (score > highScore) {
                    highScore = score
                }
                break
            }

            // Pipe collision logic
            var collisionDetected = false
            pipes.forEach { pipe ->
                // Bird x is centered at 150f on canvas
                val birdX = 150f
                val pipeWidth = 60f
                
                if (birdX + birdRadius > pipe.x && birdX - birdRadius < pipe.x + pipeWidth) {
                    // Inside pipe x span, check y span boundaries
                    if (birdY - birdRadius < pipe.topHeight || birdY + birdRadius > groundY - pipe.bottomHeight) {
                        collisionDetected = true
                    }
                }

                // Check active point passing
                if (!pipe.passed && pipe.x + pipeWidth < birdX) {
                    pipe.passed = true
                    score++
                }
            }

            if (collisionDetected) {
                isGameOver = true
                isRunning = false
                if (score > highScore) {
                    highScore = score
                }
                break
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SCORE: $score", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite, fontSize = 14.sp)
            Text("BEST: $highScore", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingMutedGray, fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .border(2.dp, Color(0xFF222222), RoundedCornerShape(16.dp))
                .background(Color(0xFF070707))
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (isRunning) {
                            velocity = jumpForce
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val groundY = 500f // Local math relative coordinates mapping standard heights

                // Draw Pipes
                pipes.forEach { pipe ->
                    // Top pipe
                    drawRect(
                        color = Color.Gray,
                        topLeft = Offset(pipe.x, 0f),
                        size = Size(60.dp.toPx(), pipe.topHeight)
                    )
                    // Top pipe lip accent
                    drawRect(
                        color = Color(0xFF444444),
                        topLeft = Offset(pipe.x - 4f, pipe.topHeight - 15.dp.toPx()),
                        size = Size(60.dp.toPx() + 8f, 15.dp.toPx())
                    )

                    // Bottom pipe
                    drawRect(
                        color = Color.Gray,
                        topLeft = Offset(pipe.x, groundY - pipe.bottomHeight),
                        size = Size(60.dp.toPx(), pipe.bottomHeight)
                    )
                    // Bottom pipe lip accent
                    drawRect(
                        color = Color(0xFF444444),
                        topLeft = Offset(pipe.x - 4f, groundY - pipe.bottomHeight),
                        size = Size(60.dp.toPx() + 8f, 15.dp.toPx())
                    )
                }

                // Draw Bird (Futuristic Minimalist dot shape reflecting Nothing UI)
                drawCircle(
                        color = Color.Red,
                        radius = 12.dp.toPx(),
                    center = Offset(150f, birdY)
                )

                // White eye dot to look like flappy dot
                drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                    center = Offset(154f, birdY - 4f)
                )

                // Draw ground boundary line
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(0f, groundY),
                    end = Offset(size.width, groundY),
                    strokeWidth = 3f
                )
            }

            if (!isRunning && !isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xE0000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("NEO FLAPPER", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite, fontSize = 20.sp)
                        Text("Tippe auf das Display zum Fliegen", fontFamily = FontFamily.Monospace, color = NothingMutedGray, fontSize = 12.sp)
                        NothingButton(text = "SPIEL STARTEN", onClick = { resetGame() })
                    }
                }
            }

            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xF0000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("COLLISION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingRed, fontSize = 24.sp)
                        Text("Dein Score: $score", fontFamily = FontFamily.Monospace, color = NothingWhite, fontSize = 16.sp)
                        NothingButton(text = "NEUSTART", onClick = { resetGame() })
                    }
                }
            }
        }

        // Action tap trigger help
        if (isRunning) {
            Button(
                onClick = { velocity = jumpForce },
                colors = ButtonDefaults.buttonColors(containerColor = NothingCardGray),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("SPRINGEN", fontFamily = FontFamily.Monospace, color = NothingWhite)
            }
        }
    }
}


// ------------------- RICK ROLL SCREEN IMPLEMENTATION -------------------
@Composable
fun RickCheckScreen() {
    val context = LocalContext.current
    var isRevealed by remember { mutableStateOf(false) }

    val lyrics = listOf(
        "Never gonna give you up 🎶",
        "Never gonna let you down 🎶",
        "Never gonna run around and desert you 🎶",
        "Never gonna make you cry 🎶",
        "Never gonna say goodbye 🎶",
        "Never gonna tell a lie and hurt you! 🎶"
    )

    if (!isRevealed) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF444444)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEAB308),
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "WICHTIGER SICHERHEITS-PATCH",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Die Schulleitung hat eine Sicherheitskomponente für Untis Neo vorgeschrieben. Bitte verifiziere deine Benutzerdaten im WebPortal des Hamburger Bildungsservers.",
                        fontFamily = FontFamily.Monospace,
                        color = NothingMutedGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    NothingButton(
                        text = "Jetzt Verifizieren",
                        onClick = {
                            isRevealed = true
                            // Direct Webtrigger Intent Rick-Roll!
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Mitteilung geladen!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    } else {
        // Revealed scrolling lyrics / animated page!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F000F)), // Vaporwave retro purple hues
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NothingHeader(text = "GET RICK'D!", fontSize = 32.sp, showRedDot = true)

                Text(
                    text = "REINGREINGELEGT! 🥳",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NothingRed,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, NothingRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        lyrics.forEach { line ->
                            Text(
                                text = line,
                                fontFamily = FontFamily.Monospace,
                                color = NothingWhite,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                NothingButton(
                    text = "RICHTIGES VIDEO ÖFFNEN",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                NothingButton(
                    text = "Zurück zur Auswahl",
                    onClick = { isRevealed = false },
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false
                )
            }
        }
    }
}
