package com.example.travel_itinerary_planner.smart_budget

data class SmartBudget(
    var BudgetID: String ="",
    var UserID: String ="",
    var TripName: String ="",
    var BudgetCurrency: String ="",
    var Budget: String ="",
    var TripStartDate: String ="",
    var TripEndDate: String ="",
    var TripDuration: String ="",
    var TotalExpensesAmountCurrency:String="",
    var TotalExpensesAmount:String=""

)
