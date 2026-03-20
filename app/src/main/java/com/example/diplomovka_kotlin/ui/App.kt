package com.example.diplomovka_kotlin.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diplomovka_kotlin.data.services.AuthService
import com.example.diplomovka_kotlin.ui.auth.*
import com.example.diplomovka_kotlin.ui.events.*
import com.example.diplomovka_kotlin.ui.map.MapScreen
import com.example.diplomovka_kotlin.ui.settings.*
import com.example.diplomovka_kotlin.viewmodel.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun App() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val appViewModel: AppViewModel = viewModel()

    val startDestination = if (FirebaseAuth.getInstance().currentUser != null)
        Screen.Map.route else Screen.Landing.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color(0xFF1A1C1E)) {
                AppDrawer(
                    navController = navController,
                    onClose = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        NavHost(navController = navController, startDestination = startDestination) {

            composable(Screen.Landing.route) {
                LandingHost(navController, appViewModel)
            }

            composable(Screen.Login.route) {
                val vm: LoginViewModel = viewModel(factory = LoginViewModelFactory(LocalContext.current.applicationContext))
                LoginScreen(
                    vm = vm,
                    onLoginSuccess = {
                        FirebaseAuth.getInstance().currentUser?.uid?.let {
                            appViewModel.loadUserProfile(it)
                            appViewModel.loadInvitations(it)
                        }
                        navController.navigate(Screen.Map.route) { popUpTo(0) { inclusive = true } }
                    },
                    onForgotPasswordClick = { navController.navigate(Screen.ResetPassword.route) },
                    onBack = { navController.navigateUp() }
                )
            }

            composable(Screen.Register.route) {
                val vm: RegisterViewModel = viewModel()
                RegisterScreen(
                    vm = vm,
                    onRegisterSuccess = { navController.navigate(Screen.Login.route) },
                    onBack = { navController.navigateUp() }
                )
            }

            composable(Screen.ResetPassword.route) {
                val vm: ResetPasswordViewModel = viewModel()
                ResetPasswordScreen(vm = vm, onSuccess = { navController.navigateUp() })
            }

            composable(Screen.Map.route) {
                MapScreen(
                    appViewModel = appViewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    navController = navController
                )
            }

            composable(Screen.EventDetail.route) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                appViewModel.selectedEvent?.let { event ->
                    LaunchedEffect(event.id) {
                        appViewModel.loadAttendeeProfiles(listOf(event.creatorId) + event.attendees)
                        appViewModel.startChatListener(event.id)
                    }
                    DisposableEffect(event.id) {
                        onDispose { appViewModel.stopChatListener() }
                    }
                    EventDetailScreen(
                        event = event,
                        currentUserId = currentUserId,
                        isFavorite = appViewModel.isFavorite(event.id),
                        chatMessages = appViewModel.chatMessages,
                        attendeeProfiles = appViewModel.attendeeProfiles,
                        onBack = { navController.navigateUp() },
                        onEditClick = {
                            appViewModel.prepareCreation(event, fromMap = false)
                            navController.navigate(Screen.EventCreation.route)
                        },
                        onJoin = { appViewModel.joinEvent(event.id, currentUserId) },
                        onLeave = { appViewModel.leaveEvent(event.id, currentUserId) },
                        onLeaveWaitlist = { appViewModel.leaveWaitlist(event.id, currentUserId) },
                        onDelete = {
                            appViewModel.deleteEvent(event.id)
                            navController.popBackStack(Screen.Map.route, false)
                        },
                        onToggleFavorite = { appViewModel.toggleFavorite(event.id) },
                        onInvite = { email ->
                            val fromName = appViewModel.currentUserProfile?.displayName
                                ?: FirebaseAuth.getInstance().currentUser?.displayName ?: ""
                            appViewModel.sendInvitation(event, currentUserId, fromName, email)
                        },
                        onCreatorClick = {
                            appViewModel.loadPublicProfile(event.creatorId)
                            navController.navigate(Screen.PublicProfile.route)
                        },
                        onRate = { stars -> appViewModel.rateEvent(event.id, currentUserId, stars) },
                        onSendMessage = { text -> appViewModel.sendChatMessage(event.id, currentUserId, text) },
                        onAttendeeClick = { uid ->
                            appViewModel.loadPublicProfile(uid)
                            navController.navigate(Screen.PublicProfile.route)
                        },
                        onRemoveAttendee = { uid -> appViewModel.removeAttendee(event.id, uid) }
                    )
                }
            }

            composable(Screen.EventCreation.route) {
                appViewModel.selectedEvent?.let { event ->
                    EventCreationScreen(
                        event = event,
                        onBack = { navController.navigateUp() },
                        onSave = { updatedEvent ->
                            if (appViewModel.creatingFromMap) {
                                appViewModel.saveEventToMap(updatedEvent)
                                navController.popBackStack(Screen.Map.route, false)
                            } else {
                                appViewModel.updateEvent(updatedEvent)
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }

            composable(Screen.PublicProfile.route) {
                val profile = appViewModel.viewedProfile
                if (profile != null) {
                    val publicEvents = appViewModel.events.values
                        .filter { it.creatorId == profile.uid && it.visibility == "public" }
                        .sortedByDescending { it.createdAt }
                    com.example.diplomovka_kotlin.ui.settings.PublicProfileScreen(
                        profile = profile,
                        events = publicEvents,
                        onBack = { navController.navigateUp() },
                        onEventClick = { event ->
                            appViewModel.selectEvent(event)
                            navController.navigate(Screen.EventDetail.route)
                        }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFFB300))
                    }
                }
            }

            composable(Screen.FavoriteEvents.route) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val favoriteIds = appViewModel.currentUserProfile?.favoriteEventIds ?: emptyList()
                val favoriteEvents = appViewModel.events.values
                    .filter { it.id in favoriteIds }
                    .sortedByDescending { it.createdAt }
                EventListScreen(
                    title = "Obľúbené udalosti",
                    events = favoriteEvents,
                    onBack = { navController.navigateUp() },
                    onEventClick = { event ->
                        appViewModel.selectEvent(event)
                        navController.navigate(Screen.EventDetail.route)
                    }
                )
            }

            composable(Screen.MyCreatedEvents.route) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val myEvents = appViewModel.events.values
                    .filter { it.creatorId == currentUserId }
                    .sortedByDescending { it.createdAt }
                EventListScreen(
                    title = "Moje udalosti",
                    events = myEvents,
                    onBack = { navController.navigateUp() },
                    onEventClick = { event ->
                        appViewModel.selectEvent(event)
                        navController.navigate(Screen.EventDetail.route)
                    }
                )
            }

            composable(Screen.MyVisitedEvents.route) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val now = java.util.Date()
                val visitedEvents = appViewModel.events.values
                    .filter { event ->
                        currentUserId in event.attendees
                            && event.creatorId != currentUserId
                            && (event.dateTo ?: event.dateFrom)?.before(now) == true
                    }
                    .sortedByDescending { it.dateFrom }
                EventListScreen(
                    title = "História udalostí",
                    events = visitedEvents,
                    onBack = { navController.navigateUp() },
                    onEventClick = { event ->
                        appViewModel.selectEvent(event)
                        navController.navigate(Screen.EventDetail.route)
                    }
                )
            }

            composable(Screen.MyUpcomingEvents.route) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val now = java.util.Date()
                val upcomingEvents = appViewModel.events.values
                    .filter { event ->
                        currentUserId in event.attendees
                            && event.creatorId != currentUserId
                            && (event.dateFrom?.after(now) == true || event.dateFrom == null)
                    }
                    .sortedBy { it.dateFrom }
                EventListScreen(
                    title = "Nadchádzajúce udalosti",
                    events = upcomingEvents,
                    onBack = { navController.navigateUp() },
                    onEventClick = { event ->
                        appViewModel.selectEvent(event)
                        navController.navigate(Screen.EventDetail.route)
                    }
                )
            }

            composable(Screen.RecommendedEvents.route) {
                appViewModel.refreshRecommendations()
                val recommendedEvents = appViewModel.recommendedEvents.map { it.event }
                EventListScreen(
                    title = "Odporúčané udalosti",
                    events = recommendedEvents,
                    onBack = { navController.navigateUp() },
                    onEventClick = { event ->
                        appViewModel.selectEvent(event)
                        navController.navigate(Screen.EventDetail.route)
                    }
                )
            }

            composable(Screen.MyInvitations.route) {
                MyInvitationsScreen(
                    appViewModel = appViewModel,
                    onBack = { navController.navigateUp() }
                )
            }

            composable(Screen.JoinPrivateEvent.route) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                JoinPrivateEventScreen(
                    appViewModel = appViewModel,
                    currentUserId = currentUserId,
                    onBack = { navController.navigateUp() },
                    onJoined = { navController.navigate(Screen.EventDetail.route) }
                )
            }

            composable(Screen.ProfileSettings.route) {
                ProfileSettingsScreen(
                    appViewModel = appViewModel,
                    onBack = { navController.navigateUp() },
                    onEventClick = { event ->
                        appViewModel.selectEvent(event)
                        navController.navigate(Screen.EventDetail.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    appViewModel = appViewModel,
                    onLogoutClick = { navController.navigate(Screen.Logout.route) }
                )
            }

            composable(Screen.Help.route) { HelpScreen() }

            composable(Screen.Contact.route) {
                ContactScreen(onBack = { navController.navigateUp() })
            }

            composable(Screen.Logout.route) {
                LogoutScreen(onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Landing.route) { popUpTo(0) { inclusive = true } }
                })
            }
        }
    }
}

@Composable
private fun LandingHost(navController: NavController, appViewModel: AppViewModel) {
    val context = LocalContext.current
    val authService = remember { AuthService(context) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                isLoading = true
                scope.launch {
                    try {
                        val user = authService.firebaseAuthWithGoogle(account.idToken!!)
                        if (user != null) {
                            appViewModel.loadUserProfile(user.uid)
                            navController.navigate(Screen.Map.route) { popUpTo(0) { inclusive = true } }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "❌ ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "❌ Google Sign-In zlyhal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LandingScreen(
        isLoading = isLoading,
        onLoginClick = { navController.navigate(Screen.Login.route) },
        onRegisterClick = { navController.navigate(Screen.Register.route) },
        onGoogleClick = { googleLauncher.launch(authService.getGoogleSignInIntent()) }
    )
}

@Composable
private fun AppDrawer(navController: NavController, onClose: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val darkBg    = Color(0xFF1A1C1E)
    val darkAccent = Color(0xFF2D2F31)
    val textColor  = Color(0xFFE2E2E6)
    val accentColor = Color(0xFFD0BCFF)

    Column(modifier = Modifier.fillMaxHeight()) {
        // ── Hlavička s info o userovi ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkAccent)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    user?.displayName ?: "Hosť",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    user?.email ?: "",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // ── Položky menu ──────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            DrawerItem(Icons.Filled.Person,  "Profil",               accentColor, textColor) { onClose(); navController.navigate(Screen.ProfileSettings.route) }
            DrawerItem(Icons.Filled.Event,        "Moje udalosti",        accentColor, textColor) { onClose(); navController.navigate(Screen.MyCreatedEvents.route) }
            DrawerItem(Icons.Filled.Favorite,     "Obľúbené",             accentColor, textColor) { onClose(); navController.navigate(Screen.FavoriteEvents.route) }
            DrawerItem(Icons.Filled.EventAvailable, "Nadchádzajúce",     accentColor, textColor) { onClose(); navController.navigate(Screen.MyUpcomingEvents.route) }
            DrawerItem(Icons.Filled.History,      "História udalostí",   accentColor, textColor) { onClose(); navController.navigate(Screen.MyVisitedEvents.route) }
            DrawerItem(Icons.Filled.Star,         "Odporúčané",          accentColor, textColor) { onClose(); navController.navigate(Screen.RecommendedEvents.route) }
            DrawerItem(Icons.Filled.Mail,    "Pozvánky",             accentColor, textColor) { onClose(); navController.navigate(Screen.MyInvitations.route) }
            DrawerItem(Icons.Filled.Lock,    "Pripojiť sa cez heslo", accentColor, textColor) { onClose(); navController.navigate(Screen.JoinPrivateEvent.route) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = darkAccent)

            DrawerItem(Icons.Filled.Settings,     "Nastavenia", accentColor, textColor) { onClose(); navController.navigate(Screen.Settings.route) }
            DrawerItem(Icons.Filled.Help,         "Pomoc",      accentColor, textColor) { onClose(); navController.navigate(Screen.Help.route) }
            DrawerItem(Icons.Filled.ContactMail,  "Kontakt",    accentColor, textColor) { onClose(); navController.navigate(Screen.Contact.route) }
            DrawerItem(Icons.Filled.Logout,   "Odhlásiť sa", Color(0xFFCF6679), textColor) { onClose(); navController.navigate(Screen.Logout.route) }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    labelColor: Color,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null, tint = iconColor) },
        label = { Text(label, color = labelColor) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent
        )
    )
}
