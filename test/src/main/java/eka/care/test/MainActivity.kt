package eka.care.test

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eka.network.IOkHttpSetup
import eka.care.documents.Document
import eka.care.documents.DocumentConfiguration
import eka.care.documents.ui.presentation.activity.DocumentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        Document.init(
//            documentConfiguration = DocumentConfiguration(
//                host = "https://vault.eka.care/",
//                okHttpSetup = object : IOkHttpSetup {
//                    override fun getDefaultHeaders(url: String): Map<String, String> {
//                        return emptyMap()
//                    }
//
//                    override fun onSessionExpire() {
//
//                    }
//
//                    override fun refreshAuthToken(url: String): Map<String, String>? {
//                        return emptyMap()
//                    }
//
//                }
//            )
//        )
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