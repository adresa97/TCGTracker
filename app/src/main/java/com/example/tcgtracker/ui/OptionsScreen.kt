package com.example.tcgtracker.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tcgtracker.OwnedCardsImporterExporter
import com.example.tcgtracker.utils.GetCustomContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    context: Context,
    scope: CoroutineScope = rememberCoroutineScope(),
    onCardsImported: (List<String>) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val jsonPicker = rememberLauncherForActivityResult(
        contract = GetCustomContents(isMultiple = false),
        onResult = { uris ->
            val message = OwnedCardsImporterExporter.importFromJSON(context, uris[0])
            if (!message.second.isNullOrEmpty()) onCardsImported(message.second!!)
            scope.launch {
                snackbarHostState.showSnackbar(message.first)
            }
        }
    )

    /*
    val storePicker = rememberLauncherForActivityResult(

    )
    */

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OptionButton (
            onTap = { jsonPicker.launch("application/json") },
            text = "Importar JSON"
        )
        OptionButton (
            onTap = {},
            text = "Exportar JSON"
        )
    }
}

@Composable
fun OptionButton(
    onTap: () -> Unit = {},
    text: String = ""
) {
    Button(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RectangleShape,
        colors = ButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.secondary,
            disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
            disabledContentColor = MaterialTheme.colorScheme.error
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = text
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewButton() {
    OptionsScreen(
        context = LocalContext.current,
    )
}