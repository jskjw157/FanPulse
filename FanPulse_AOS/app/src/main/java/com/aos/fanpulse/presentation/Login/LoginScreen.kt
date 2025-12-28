package com.aos.fanpulse.presentation.Login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R

@Composable
fun LoginScreen () {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .paint(
                painter = painterResource(id = R.drawable.loginscreen_bg),
                contentScale = ContentScale.Crop
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                text = "Welcome to FanPulse",
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                text = "글로벌 K-POP 팬들의 인터렉티브 플랫폼",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
            )

            Column(
                modifier = Modifier
                    .background(
                        color = colorResource(R.color.white),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(24.dp),
            ) {
                Row (
                    modifier = Modifier
                        .background(
                            color = colorResource(R.color.color_2),
                            shape = RoundedCornerShape(100.dp)
                        )
                        .padding(4.dp)
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.color_1),
                            contentColor = Color.White
                        ),
                        onClick = { }
                    ) {
                        Text(text ="로그인",
                            fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.color_1),
                            contentColor = Color.White
                        ),
                        onClick = { }
                    ) {
                        Text("회원가입")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                LoginComponent()
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "둘러보기",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun LoginComponent(){

    var text by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(100.dp)
                ),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_google),
                    contentDescription = "Google 로그인",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text("Google로 로그인")
            }
        }   //
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            painter = painterResource(id = R.drawable.login_divider),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("이메일") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,

                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,

                focusedLabelColor = colorResource(id = R.color.black),
                unfocusedLabelColor = colorResource(id = R.color.black),

                focusedContainerColor = colorResource(id = R.color.color_text_2),
                unfocusedContainerColor = colorResource(id = R.color.color_text_2),

                cursorColor = Color.Black
            )
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,

                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,

                focusedLabelColor = colorResource(id = R.color.black),
                unfocusedLabelColor = colorResource(id = R.color.black),

                focusedContainerColor = colorResource(id = R.color.color_text_2),
                unfocusedContainerColor = colorResource(id = R.color.color_text_2),

                cursorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .paint(
                    painter = painterResource(id = R.drawable.login_button),
                    contentScale = ContentScale.FillBounds
                )
                .clickable { }
                .wrapContentSize(Alignment.Center)
        ) {
            Text(
                text = "로그인",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier.clickable {},
            text = "비밀번호를 잊으셨나요?",
            color = colorResource(id = R.color.color_text_3),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
        )
    }
}
    //  Box는 정렬이나 겹침을 사용할때 한다
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LoginScreen()
}