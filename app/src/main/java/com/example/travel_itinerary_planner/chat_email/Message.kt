package com.example.travel_itinerary_planner.chat_email

data class Message(
    var MessageID: String="",
    var UserID: String="",
    var RecipientID: String = "",
    var MessageText: String="",
    var MessageImage:String="",
    var MessageDate:String="",
    var MessageChannel: String = ""

)

