package com.example.lobbyin.model

data class ClassRoom(
    var id: String = "",
    var name: String = "",
    var floor: Int = 1,
    var status: String = "Kosong",
    var time: String = "",
    var capacity: String = "100 Orang",
    var facilities: String = "LCD / Proyektor"
) {
    // Firebase needs a no-arg constructor
    constructor() : this("", "", 1, "Kosong", "", "100 Orang", "LCD / Proyektor")
}