package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.IptvDatabase
import com.example.data.IptvRepository
import com.example.ui.IptvApp
import com.example.ui.IptvViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: IptvDatabase
    private lateinit var repository: IptvRepository
    private lateinit var viewModel: IptvViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database
        database = Room.databaseBuilder(
            applicationContext,
            IptvDatabase::class.java,
            "iptv_pro_database"
        ).fallbackToDestructiveMigration().build()

        val dao = database.iptvDao()
        repository = IptvRepository(dao)

        // Instantiate ViewModel
        viewModel = ViewModelProvider(
            this,
            IptvViewModel.Factory(repository)
        )[IptvViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IptvApp(viewModel = viewModel)
                }
            }
        }
    }
}
