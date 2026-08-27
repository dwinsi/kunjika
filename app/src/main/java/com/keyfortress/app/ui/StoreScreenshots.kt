package com.keyfortress.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keyfortress.app.ui.screens.onboarding.OnboardingScreen
import com.keyfortress.app.ui.theme.KunjikaTheme

@Preview(device = "spec:width=1080px,height=2340px,dpi=440", showBackground = true)
@Composable
fun Screenshot_Onboarding() {
    KunjikaTheme(darkTheme = true) {
        OnboardingScreen(onFinished = {})
    }
}

// Note: For other screens like Vault and Generator, 
// we would normally need mock ViewModels. 
// I will render the Onboarding first as a test.
