package com.gamecontrol.app.ui.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gamecontrol.app.network.LinkStatus
import com.gamecontrol.app.network.label
import com.gamecontrol.app.ui.theme.ColorError
import com.gamecontrol.app.ui.theme.ColorOnSurface
import com.gamecontrol.app.ui.theme.ColorOnSurfaceVariant
import com.gamecontrol.app.ui.theme.ColorSurfaceBase
import com.gamecontrol.app.ui.theme.ColorSurfaceHigh

/** Enter the PC address and the 6-digit pairing code shown on the PC. */
@Composable
fun ConnectScreen(
    status: LinkStatus,
    onConnect: (host: String, port: Int, code: String) -> Unit,
    onDisconnect: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var host by remember { mutableStateOf("192.168.1.") }
    var port by remember { mutableStateOf("27015") }
    var code by remember { mutableStateOf("") }

    val portValue = port.toIntOrNull()
    val linked = status is LinkStatus.Connected
    val busy = status == LinkStatus.Connecting || status == LinkStatus.Authenticating
    val canConnect = host.isNotBlank() && portValue != null && code.length == 6 && !linked && !busy

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSurfaceBase)
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Connect to PC",
                style = MaterialTheme.typography.headlineSmall,
                color = ColorOnSurface,
            )
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(ColorSurfaceHigh)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "BACK", style = MaterialTheme.typography.labelSmall, color = ColorOnSurface)
            }
        }

        OutlinedTextField(
            value = host,
            onValueChange = { host = it.trim() },
            label = { Text("PC address") },
            singleLine = true,
            enabled = !linked && !busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = port,
            onValueChange = { port = it.filter(Char::isDigit).take(5) },
            label = { Text("Port") },
            singleLine = true,
            enabled = !linked && !busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter(Char::isDigit).take(6) },
            label = { Text("Pairing code (6 digits, shown on the PC)") },
            singleLine = true,
            enabled = !linked && !busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onConnect(host, portValue ?: 27015, code) },
                enabled = canConnect,
            ) {
                Text("CONNECT")
            }
            OutlinedButton(
                onClick = onDisconnect,
                enabled = linked || busy,
            ) {
                Text("DISCONNECT")
            }
        }

        Text(
            text = "Status: ${status.label()}",
            style = MaterialTheme.typography.labelMedium,
            color = ColorOnSurface,
        )
        (status as? LinkStatus.Failed)?.let {
            Text(text = it.reason, style = MaterialTheme.typography.bodyMedium, color = ColorError)
        }
        Text(
            text = "Phone and PC must be on the same Wi-Fi network. Allow GameControl through the Windows firewall when prompted.",
            style = MaterialTheme.typography.bodyMedium,
            color = ColorOnSurfaceVariant,
        )
    }
}
