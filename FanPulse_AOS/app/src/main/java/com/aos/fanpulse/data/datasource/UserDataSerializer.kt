package com.aos.fanpulse.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.aos.fanpulse.datastore.UserData
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

/**
 * 데이터 변환기
 * */
object UserDataSerializer : Serializer<UserData> {
    override val defaultValue: UserData
        get() = UserData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserData {
        try {
            return UserData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: UserData, output: OutputStream) {
        t.writeTo(output)
    }
}