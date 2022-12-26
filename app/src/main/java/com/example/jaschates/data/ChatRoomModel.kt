package com.example.jaschates.data

import java.io.Serializable

data class ChatRoomModel(
    var user: HashMap<String, Any> = HashMap(),
    var title: String? = null,
    var titleImage: String? = null,
    var description: String? = null,
    var channelID: String? = null,
    var comments: HashMap<String, Comment> = HashMap()
): Serializable {
    class Comment(val uid: String? = null, val message: String? = null, val time: String? = null)
}
