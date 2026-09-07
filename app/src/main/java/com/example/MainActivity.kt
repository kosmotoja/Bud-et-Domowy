package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.BudgetRepository
import com.example.ui.BudgetScreen
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppSurfaceGlass
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.TextPrimary
import com.example.widget.BudgetWidgetProvider

class MainActivity : FragmentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val repository = BudgetRepository.getInstance(applicationContext)

    setContent {
      MyApplicationTheme {
        val isBiometricEnabled by repository.isBiometricEnabled().collectAsState(initial = false)
        var isUnlocked by remember { mutableStateOf(!isBiometricEnabled) }

        // Whenever biometric setting changes to false, auto unlock
        LaunchedEffect(isBiometricEnabled) {
          if (!isBiometricEnabled) {
            isUnlocked = true
          }
        }

        // Trigger prompt on initial startup if biometric is enabled
        LaunchedEffect(isBiometricEnabled, isUnlocked) {
          if (isBiometricEnabled && !isUnlocked) {
            authenticateWithBiometrics {
              isUnlocked = true
            }
          }
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = AppBackground
        ) {
          if (isBiometricEnabled && !isUnlocked) {
            BiometricLockScreen(
              onUnlockClick = {
                authenticateWithBiometrics {
                  isUnlocked = true
                }
              }
            )
          } else {
            BudgetScreen()
          }
        }
      }
    }
  }

  private fun authenticateWithBiometrics(onSuccess: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(this)
    val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onSuccess()
      }

      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        // User can tap "Odblokuj" to retry or use device credential
      }
    })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle("Budżet Domowy")
      .setSubtitle("Potwierdź tożsamość, aby uzyskać dostęp")
      .setAllowedAuthenticators(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
      )
      .build()

    try {
      prompt.authenticate(promptInfo)
    } catch (e: Exception) {
      // In case device has no security or error occurs, allow access
      onSuccess()
    }
  }

  override fun onPause() {
    super.onPause()
    // Update home screen widget whenever user leaves the app
    BudgetWidgetProvider.updateAll(this)
  }
}

@Composable
fun BiometricLockScreen(onUnlockClick: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppBackground)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(ColorIncome.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Zablokowano",
          tint = ColorIncome,
          modifier = Modifier.size(40.dp)
        )
      }

      Text(
        text = "Aplikacja Zablokowana",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary
      )

      Text(
        text = "Włączono ochronę biometryczną. Użyj odcisku palca, rozpoznawania twarzy lub kodu PIN, aby odblokować finanse.",
        style = MaterialTheme.typography.bodyMedium,
        color = Slate400,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      Button(
        onClick = onUnlockClick,
        colors = ButtonDefaults.buttonColors(containerColor = ColorIncome),
        shape = RoundedCornerShape(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Fingerprint,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
          text = "Odblokuj",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}
