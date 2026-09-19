package com.myapplication

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import compose3d.Mesh
import loadTeapotMesh

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // The teapot is a Compose resource, so it is read off the main
            // thread; the cube stands in until it arrives.
            var mesh by remember { mutableStateOf(Mesh.cube()) }
            LaunchedEffect(Unit) {
                runCatching { loadTeapotMesh() }.getOrNull()?.let { mesh = it }
            }
            App(mesh = mesh)
        }
    }
}
