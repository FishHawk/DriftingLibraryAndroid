package com.fishhawk.driftinglibraryandroid.repository.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ServerInfo(
    var name: String,
    var address: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
