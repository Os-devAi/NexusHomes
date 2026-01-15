package com.nexusdev.nexushomes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.nexusdev.nexushomes.ui.components.UpdatesCard
import com.nexusdev.nexushomes.ui.viewmodel.HomeDataViewModel

@Composable
fun MuUpdates(
    modifier: Modifier,
    navController: NavController
) {

    val user = FirebaseAuth.getInstance()
    val viewModel: HomeDataViewModel = viewModel()
    val houses by viewModel.houses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMyPublish(user.currentUser?.uid.toString())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    user.currentUser?.displayName.toString(),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "Mis Publicaciones",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(35.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (houses.isEmpty()) {
                    Text("No hay publicaciones")
                } else {
                    houses.forEach { house ->
                        UpdatesCard(house)
                    }
                }
            }
        }
    }
}