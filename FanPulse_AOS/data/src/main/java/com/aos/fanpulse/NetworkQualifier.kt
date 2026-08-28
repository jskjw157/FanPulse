package com.aos.fanpulse

import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LastFmNetwork
