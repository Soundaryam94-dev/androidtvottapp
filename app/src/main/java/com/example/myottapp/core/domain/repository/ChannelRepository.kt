package com.example.myottapp.core.domain.repository

import com.example.myottapp.core.domain.model.Channel
import com.example.myottapp.core.network.NetworkResult
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    fun getChannel(id: String): Flow<NetworkResult<Channel>>
}