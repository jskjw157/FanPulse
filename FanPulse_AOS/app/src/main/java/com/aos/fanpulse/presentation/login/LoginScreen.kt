package com.aos.fanpulse.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aos.fanpulse.R

enum class LoginState {
    LOGIN, SIGNUP
}

@Composable
fun LoginScreen (
    viewModel: LoginViewModel = hiltViewModel()
) {

    var selectedTab by rememberSaveable {
        mutableStateOf(LoginState.LOGIN)
    }

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

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(colorResource(R.color.color_2)),
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = colorResource(R.color.color_2),
                        indicator = {},
                        divider = {}
                    ) {
                        AuthTab(
                            selected = selectedTab == LoginState.LOGIN,
                            onClick = { selectedTab = LoginState.LOGIN },
                            text = "로그인"
                        )

                        AuthTab(
                            selected = selectedTab == LoginState.SIGNUP,
                            onClick = { selectedTab = LoginState.SIGNUP },
                            text = "회원가입"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                AuthComponent(selectedTab)
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
fun AuthTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
    ){
        Box(
            modifier = Modifier
                .height(36.dp)
                .fillMaxWidth()
                .padding(4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(
                    if (selected)
                        colorResource(R.color.color_1)
                    else
                        colorResource(R.color.color_2)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = if (selected) Color.White else colorResource(R.color.color_text_3))
        }
    }
}

@Composable
fun AuthComponent(
    authState: LoginState
){
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LoginGoogleButton(authState)

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            painter = painterResource(id = R.drawable.login_divider),
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(24.dp))

        LoginTextField(
            value = email,
            onValueChange = { email = it },
            label = "이메일"
        )

        LoginTextField(
            value = password,
            onValueChange = { password = it },
            label = "비밀번호"
        )

        if (authState == LoginState.SIGNUP) {
            LoginTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "비밀번호 확인"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LoginButton(authState)

        Spacer(modifier = Modifier.height(16.dp))

        if (authState == LoginState.LOGIN) {
            Text(
                modifier = Modifier.clickable { },
                text = "비밀번호를 잊으셨나요?",
                color = colorResource(id = R.color.color_text_3),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun LoginGoogleButton(authState: LoginState) {
    Button(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(100.dp)),
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.icon_google),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            if (authState == LoginState.LOGIN)
                "Google로 로그인"
            else
                "Google로 가입하기"
        )
    }
}

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(100.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = colorResource(id = R.color.color_text_2),
            unfocusedContainerColor = colorResource(id = R.color.color_text_2),
            cursorColor = Color.Black
        )
    )
}

@Composable
fun LoginButton(loginState: LoginState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF9333EA), Color(0xFFDB2777)),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                ),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable { }
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = if (loginState == LoginState.LOGIN) "로그인" else "회원가입",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LoginScreen()
}