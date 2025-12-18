package com.aos.fanpulse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen () {
    Column(

    ){
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = "Welcome to FanPulse",
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            text = "글로벌 K-POP 팬들의 인터렉티브 플랫폼",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif
        )

        Row (
            modifier = Modifier
                .background(
                    color = colorResource(R.color.color_2),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            SelectableButton(
                text = "로그인",
                selected = true,
                onClick = {  },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.color_1),
                    contentColor = Color.White
                ),
                onClick = { }
            ) {
                Text("로그인")
            }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.color_1),
                    contentColor = Color.White
                ),
                onClick = { }
            ) {
                Text("회원가입")
            }
        }
    }
}

@Composable
fun SelectableButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        colorResource(R.color.color_2)
    } else {
        colorResource(R.color.color_1)
    }

    val contentColor = if (selected) {
        Color.White
    } else {
        Color.Black
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = contentColor
        )
    }
}

    //  Box는 정렬이나 겹침을 사용할때 한다
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LoginScreen()
}