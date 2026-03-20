package com.example.diplomovka_kotlin.ui

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Register : Screen("register")
    object ResetPassword : Screen("reset_password")
    object Map : Screen("map")
    object EventCreation : Screen("event_creation")
    object EventDetail : Screen("event_detail")
    object MyCreatedEvents : Screen("my_created_events")
    object MyVisitedEvents : Screen("my_visited_events")
    object MyUpcomingEvents : Screen("my_upcoming_events")
    object RecommendedEvents : Screen("recommended_events")
    object MyInvitations : Screen("my_invitations")
    object ProfileSettings : Screen("profile_settings")
    object Settings : Screen("settings")
    object Help : Screen("help")
    object Contact : Screen("contact")
    object Logout : Screen("logout")
    object JoinPrivateEvent : Screen("join_private_event")
    object FavoriteEvents : Screen("favorite_events")
    object PublicProfile : Screen("public_profile")
}
