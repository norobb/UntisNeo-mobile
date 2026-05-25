package com.example.utils

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): () -> Unit
