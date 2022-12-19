package com.example.jaschates.data

data class ChatRoomModel(
    var user: HashMap<String, Any> = HashMap(),
    var title: String? = null,
    var titleImage: String? = null,
    var description: String? = null,
    var comments: HashMap<String, Comment> = HashMap()
) {
    class Comment(val uid: String? = null, val message: String? = null, val time: String? = null)
}
