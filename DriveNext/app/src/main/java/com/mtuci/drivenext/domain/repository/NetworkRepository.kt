package com.mtuci.drivenext.domain.repository

interface NetworkRepository {
    fun isInternetAvailable(): Boolean
}
