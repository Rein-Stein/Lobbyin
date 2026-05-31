package com.example.lobbyin.model

data class Comment(
    val role: String = "",
    val text: String = ""
)

data class Booking(
    val id: String = "",
    val room: String = "",
    val date: String = "",
    val time: String = "",
    val nama: String = "",
    val nim: String = "",
    val matkul: String = "",
    val comments: List<Comment> = emptyList()
)
