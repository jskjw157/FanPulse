package com.aos.fanpulse.presentation.artist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(

): ContainerHost<ArtistContract.ArtistState, ArtistContract.SideEffect>, ViewModel() {
    override val container: Container<ArtistContract.ArtistState, ArtistContract.SideEffect> =
    container(initialState = ArtistContract.ArtistState(Artist()))
}