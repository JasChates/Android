package com.example.jaschates.data

data class ChatRoomModel(
    val user: HashMap<String, Any> = HashMap(),
    val title: String? = null,
    val titleImage: String? = null,
    val comments : HashMap<String, Comment> = HashMap()
) {
    class Comment(val uid: String? = null, val message: String? = null, val time: String? = null)
}
