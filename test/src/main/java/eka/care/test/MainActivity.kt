package eka.care.test

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eka.network.IOkHttpSetup
import eka.care.documents.Document
import eka.care.documents.DocumentConfiguration
import eka.care.documents.network.OkHttpSetup
import eka.care.documents.ui.presentation.activity.DocumentActivity
import eka.care.test.ui.theme.DocumentsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Document.init(
            chatInitConfiguration = DocumentConfiguration(
                okHttpSetup = OkHttpSetup()
            )
        )
        setContent {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { startActivity(Intent(this@MainActivity, DocumentActivity::class.java)) },
                    content = { Text(text = "Records") }
                )
            }
        }
    }
}