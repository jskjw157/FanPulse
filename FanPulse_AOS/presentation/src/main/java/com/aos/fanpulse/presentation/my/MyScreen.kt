package com.aos.fanpulse.presentation.my


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.presentation.R
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import com.aos.fanpulse.presentation.common.formatIsoTimeToEnglish
import com.aos.fanpulse.presentation.community.CommunityItem
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MyScreen (
    viewModel: MyViewModel = hiltViewModel(),
    goSettingScreen: () -> Unit = {},
    goPostDetailScreen: (String) -> Unit,
){
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MyContract.SideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
            is MyContract.SideEffect.NavigateSetting -> {
                goSettingScreen()
            }
            is MyContract.SideEffect.NavigateBack -> {
                showDialog = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .paint(
                painter = painterResource(id = R.drawable.loginscreen_bg),
                contentScale = ContentScale.Crop
            )
    ) {
        CommonTopAppBar(
            isActiveLeftTextTitle = true,
            leftTextTitle = "My Profile",
            isActiveRightSetting = true,
            onRightSetting = { viewModel.goSettingScreen() }
        )
        Row(
            modifier = Modifier
                .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = state.userPhotoUrl,
                contentDescription = null,
                placeholder = painterResource(id = R.drawable.default_user),
                error = painterResource(id = R.drawable.default_user),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.userNickname.toString(),
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    modifier = Modifier
                        .wrapContentHeight(Alignment.CenterVertically)
                        .clickable{
                            showDialog = true
                        }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.userEmail.toString(),
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.userCreatedAt?.formatIsoTimeToEnglish() ?: "",
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }
        }
        Column (
            Modifier
                .background(color = colorResource(R.color.white))
                .fillMaxSize()
                .padding(
                    start = 4.dp,
                    end = 4.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
        ) {
            LazyColumn(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.posts) { item ->
                    CommunityItem(
                        item,
                        goPostDetailScreen = {
                            goPostDetailScreen(item.id)
                        },
                        onLikeClick = {
                            viewModel.toggleLike(item.id)
                        },
                        onBookmarkClick = {
                            viewModel.toggleBookmark(item.id)
                        }
                    )
                }
            }
        }
        if (showDialog) {
            Dialog(
                onDismissRequest = {
                    showDialog = false
                }
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    EditNicknameScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun EditNicknameScreen(
    viewModel: MyViewModel
) {
    val state by viewModel.collectAsState()

    var nicknameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = nicknameInput,
            onValueChange = { nicknameInput = it },
            label = { Text("새 닉네임") },
            placeholder = { Text("변경할 닉네임을 입력하세요") },
            singleLine = true,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.updateProfile(nicknameInput)
            },
            enabled = nicknameInput.isNotBlank() && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "닉네임 변경")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyScreen(){}
}