package com.nexusdev.nexushomes.ui.screens

import android.graphics.fonts.Font
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.rememberNavController
import com.nexusdev.nexushomes.R
import com.nexusdev.nexushomes.navigation.AppNavigation

@Composable
fun HomeScreen(
    modifier: Modifier,
    navController: NavHostController
) {
    // variables of system
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // header section
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(80.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nexus Homes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Image(
                    painter = if (isDarkTheme) {
                        painterResource(id = R.drawable.outline_settings_ligth)
                    } else {
                        painterResource(id = R.drawable.outline_settings_dark)
                    },
                    contentDescription = "settings icon",
                    modifier = Modifier
                        .height(24.dp)
                        .clickable(
                            onClick = {
                                // navigate to settings screen
                                Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                )
            }
        }

        // end of header section

        // begin profile info section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row {
                        Text(
                            text = "Bienvenido,",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lamine Yamal",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Text(
                        text = "ejemplo@email.com",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        // begin of upload section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = if (isDarkTheme) {
                        painterResource(id = R.drawable.outline_upload_ligth)
                    } else {
                        painterResource(id = R.drawable.outline_upload_dark)
                    },
                    contentDescription = "upload new icon",
                    modifier = Modifier
                        .height(24.dp)
                        .clickable(
                            onClick = {
                                // to upload a new post
                                navController.navigate("addNew")
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publicar")

                Spacer(modifier = Modifier.width(16.dp))

                Image(
                    painter = if (isDarkTheme) {
                        painterResource(id = R.drawable.my_uploads_ligth)
                    } else {
                        painterResource(id = R.drawable.outline_upload_dark)
                    },
                    contentDescription = "my uploads icon",
                    modifier = Modifier
                        .height(24.dp)
                        .clickable(
                            onClick = {
                                // to my uploads
                                Toast.makeText(context, "My uploads clicked", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mis publicaciones")
            }
        }

        // end of upload section

        // begin of body section
        // Search Bar
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Buscar un inmueble...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(16.dp),
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default
        )

    }
}