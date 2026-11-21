package org.example.project.ui.components.screens

import org.example.project.ui.generated.resources.Res
import org.example.project.ui.generated.resources.ic_arrow_left
import org.example.project.ui.generated.resources.action_back
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.example.project.ui.components.navigation.CompliTopAppBar
import org.example.project.ui.components.navigation.CompliTopAppBarIconButton
import org.example.project.ui.theme.CompliTheme

@Composable
fun CompliActionScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = CompliTheme.colors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CompliTopAppBar(
                title = title,
                containerColor = CompliTheme.colors.background,
                navigationIcon = {
                    CompliTopAppBarIconButton(
                        onClick = onBack,
                        contentDescription = stringResource(Res.string.action_back),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_left),
                            contentDescription = null,
                        )
                    }
                },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                content(PaddingValues())
            }
            bottomBar()
        }
    }
}
