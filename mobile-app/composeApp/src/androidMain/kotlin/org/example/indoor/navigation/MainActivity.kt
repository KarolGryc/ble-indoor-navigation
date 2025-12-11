package org.example.indoor.navigation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import data.filesystemProviders.readFileFromUri
import data.service.FileImportHandler

class MainActivity : ComponentActivity() {
    private val importHandler = FileImportHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            App(fileImportHandler = importHandler)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                val file = readFileFromUri(this, uri)
                if (file != null ) {
                    importHandler.handleNewFile(file)
                }
            }
        }
    }

}