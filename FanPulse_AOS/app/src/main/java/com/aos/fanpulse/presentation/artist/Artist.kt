package com.aos.fanpulse.presentation.artist

// Data Classes
data class Artist(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val followers: Int = 0,             //  사용 유무
    val imageRes: Int = 0,
    val ranking: Int = 0                //  사용 유무
)