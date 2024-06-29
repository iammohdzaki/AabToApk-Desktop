package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.Styles

@Composable
fun ChooseFileTextField(value: String, label: String, onSelect: () -> Unit) {
    val density = LocalDensity.current // to calculate the intrinsic size of vector images (SVG, XML)
    Row(
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 0.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(0.4f).height(50.dp)
                .border(
                    BorderStroke(width = 2.dp, color = MaterialTheme.colors.primary),
                    shape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp, topStart = 10.dp, bottomStart = 10.dp)
                ),
            value = value,
            singleLine = true,
            readOnly = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            label = {
                Text(
                    text = label,
                    style = Styles.TextStyleMedium(16.sp),
                    fontWeight = FontWeight.Medium
                )
            },
            textStyle = Styles.TextStyleMedium(16.sp),
            onValueChange = {}
        )
        Button(
            onClick = {
                onSelect.invoke()
            },
            modifier = Modifier
                .height(50.dp)
                .wrapContentWidth()
        ) {
            Icon(
                painter = useResource("open_folder.svg") { loadSvgPainter(it, density) },
                contentDescription = ""
            )
        }
    }
}