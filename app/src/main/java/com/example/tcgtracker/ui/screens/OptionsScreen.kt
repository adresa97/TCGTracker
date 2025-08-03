package com.example.tcgtracker.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tcgtracker.OwnedCardsImporterExporter
import com.example.tcgtracker.R
import com.example.tcgtracker.ui.theme.PocketBlack
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
            icon = R.drawable.download,
            text = "Importar datos"
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .alpha(0.5f),
            thickness = 2.dp,
            color = PocketBlack
        )
        OptionButton (
            onTap = {},
            icon = R.drawable.upload,
            text = "Exportar datos"
        )
    }
}

@Composable
fun OptionButton(
    onTap: () -> Unit = {},
    icon: Int,
    text: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clickable(onClick = onTap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 5.dp),
            painter = painterResource(icon),
            contentDescription = null
        )
        Text(
            text = text,
            fontSize = 24.sp
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