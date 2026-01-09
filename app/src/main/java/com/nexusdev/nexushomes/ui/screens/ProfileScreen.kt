package com.nexusdev.nexushomes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.nexusdev.nexushomes.ui.viewmodel.LoginViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier,
    naviController: NavController
) {
    val viewModel: LoginViewModel = viewModel()
    val firebaseAuth = FirebaseAuth.getInstance()
    var user = firebaseAuth.currentUser
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            if (user?.photoUrl != null) {
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(0xFF667eea).copy(alpha = 0.1f),
                            shape = CircleShape
                        ), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user?.displayName?.firstOrNull()?.toString()
                            ?: "U",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mensaje de bienvenida
            Text(
                "¡Bienvenido!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = user?.displayName ?: "Usuario",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(32.dp))

            // boton agregar publicacion
            Button(
                onClick = {
                    naviController.navigate("addNew")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color("#05ccee".toColorInt()), contentColor = Color.White
                )
            ) {
                Text(
                    "Agregar Publicación",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // boton agregar publicacion
            Button(
                onClick = {
                    // navegar al formulario de nueva publicacion
                    naviController.navigate("updates")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color("#05ccee".toColorInt()), contentColor = Color.White
                )
            ) {
                Text(
                    "Mis Publicaciones",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // boton de cerrar sesion
            Button(
                onClick = {
                    viewModel.signOut()
                    user = null
                    if (user == null) {
                        naviController.navigate("login") {
                            popUpTo("home") {
                                inclusive = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFef4444), contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Cerrar Sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

    }

}