package com.example.diplomovka_kotlin.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.diplomovka_kotlin.data.models.ChatMessage
import com.example.diplomovka_kotlin.data.models.Event
import com.example.diplomovka_kotlin.data.models.UserProfile
import java.text.SimpleDateFormat
import java.util.*

private val DarkMapBackground = Color(0xFF1A1C1E)
private val DarkAccent        = Color(0xFF2D2F31)
private val PrimaryTextDark   = Color(0xFFE2E2E6)
private val AccentColor       = Color(0xFFFFB300)

private val TAB_LABELS = listOf("Detail", "Účastníci", "Chat")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    currentUserId: String,
    isFavorite: Boolean,
    chatMessages: List<ChatMessage>,
    attendeeProfiles: Map<String, UserProfile>,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onLeaveWaitlist: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onInvite: (String) -> Unit,
    onCreatorClick: () -> Unit,
    onRate: (Int) -> Unit,
    onSendMessage: (String) -> Unit,
    onAttendeeClick: (String) -> Unit,
    onRemoveAttendee: (String) -> Unit
) {
    val context       = LocalContext.current
    val formatter     = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val isCreator     = event.creatorId == currentUserId
    val isAttending   = currentUserId in event.attendees
    val isOnWaitlist  = currentUserId in event.waitlist
    val waitlistPos   = event.waitlist.indexOf(currentUserId) + 1
    val isFull        = event.participants > 0 && event.attendees.size >= event.participants
    val isBanned      = currentUserId in event.bannedUsers
    val hasRated      = currentUserId in event.ratedBy

    var selectedStars    by remember { mutableStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteEmail      by remember { mutableStateOf("") }
    var selectedTab      by remember { mutableStateOf(0) }

    fun shareEvent() {
        val sb = StringBuilder()
        sb.appendLine("🎉 ${event.title}")
        if (event.place.isNotEmpty()) sb.appendLine("📍 ${event.place}")
        event.dateFrom?.let { sb.appendLine("📅 ${formatter.format(it)}") }
        if (event.price > 0) sb.appendLine("💰 ${"%.2f".format(event.price)} €")
        else sb.appendLine("💰 Zadarmo")
        if (event.category.isNotEmpty()) sb.appendLine("🏷️ ${event.category}")
        if (event.description.isNotEmpty()) sb.appendLine("\n${event.description}")
        sb.appendLine("\nKód udalosti: ${event.id}")
        sb.appendLine("Nájdi ho v appke Joinly!")

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Zdieľať udalosť"))
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false; inviteEmail = "" },
            title = { Text("Pozvať používateľa", color = PrimaryTextDark) },
            text = {
                OutlinedTextField(
                    value = inviteEmail,
                    onValueChange = { inviteEmail = it },
                    label = { Text("Email používateľa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = Color(0xFF4A4C4F),
                        focusedTextColor = PrimaryTextDark,
                        unfocusedTextColor = PrimaryTextDark,
                        cursorColor = AccentColor,
                        focusedContainerColor = DarkAccent,
                        unfocusedContainerColor = DarkAccent
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { onInvite(inviteEmail); showInviteDialog = false; inviteEmail = "" }) {
                    Text("Pozvať", color = AccentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false; inviteEmail = "" }) {
                    Text("Zrušiť", color = PrimaryTextDark.copy(alpha = 0.6f))
                }
            },
            containerColor = DarkAccent
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Vymazať udalosť?", color = PrimaryTextDark) },
            text = { Text("Táto akcia je nezvratná.", color = PrimaryTextDark.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Vymazať", color = Color(0xFFCF6679), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Zrušiť", color = AccentColor) }
            },
            containerColor = DarkAccent
        )
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event.title, color = PrimaryTextDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Späť", tint = PrimaryTextDark)
                    }
                },
                actions = {
                    IconButton(onClick = { shareEvent() }) {
                        Icon(Icons.Filled.Share, contentDescription = "Zdieľať", tint = AccentColor)
                    }
                    if (!isCreator) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Obľúbené",
                                tint = if (isFavorite) Color(0xFFCF6679) else AccentColor
                            )
                        }
                    }
                    if (isCreator) {
                        IconButton(onClick = { showInviteDialog = true }) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = "Pozvať", tint = AccentColor)
                        }
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Filled.Edit, contentDescription = "Upraviť", tint = AccentColor)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Vymazať", tint = Color(0xFFCF6679))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAccent)
            )
        },
        containerColor = DarkMapBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tab row ───────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkAccent,
                contentColor = AccentColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AccentColor
                    )
                }
            ) {
                TAB_LABELS.forEachIndexed { index, label ->
                    val tabLabel = when (index) {
                        1 -> "Účastníci (${event.attendees.size})"
                        2 -> "Chat"
                        else -> label
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                tabLabel,
                                color = if (selectedTab == index) AccentColor
                                        else PrimaryTextDark.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // ── Tab content ───────────────────────────────────────────────────
            when (selectedTab) {
                0 -> DetailTab(
                    event = event,
                    currentUserId = currentUserId,
                    isCreator = isCreator,
                    isAttending = isAttending,
                    isOnWaitlist = isOnWaitlist,
                    isFull = isFull,
                    isBanned = isBanned,
                    waitlistPos = waitlistPos,
                    isFavorite = isFavorite,
                    hasRated = hasRated,
                    selectedStars = selectedStars,
                    onSelectedStarsChange = { selectedStars = it },
                    formatter = formatter,
                    onJoin = onJoin,
                    onLeave = onLeave,
                    onLeaveWaitlist = onLeaveWaitlist,
                    onCreatorClick = onCreatorClick,
                    onRate = onRate
                )
                1 -> AttendeesTab(
                    event = event,
                    currentUserId = currentUserId,
                    isEventCreator = isCreator,
                    attendeeProfiles = attendeeProfiles,
                    onAttendeeClick = onAttendeeClick,
                    onRemoveAttendee = onRemoveAttendee
                )
                2 -> ChatTab(
                    chatMessages = chatMessages,
                    currentUserId = currentUserId,
                    canSend = (isAttending || isCreator) && !isBanned,
                    onSendMessage = onSendMessage
                )
            }
        }
    }
}

// ── Detail tab ────────────────────────────────────────────────────────────────

@Composable
private fun DetailTab(
    event: Event,
    currentUserId: String,
    isCreator: Boolean,
    isAttending: Boolean,
    isOnWaitlist: Boolean,
    isFull: Boolean,
    isBanned: Boolean,
    waitlistPos: Int,
    isFavorite: Boolean,
    hasRated: Boolean,
    selectedStars: Int,
    onSelectedStarsChange: (Int) -> Unit,
    formatter: SimpleDateFormat,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onLeaveWaitlist: () -> Unit,
    onCreatorClick: () -> Unit,
    onRate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Images pager
        if (event.imageUrls.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { event.imageUrls.size })
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                HorizontalPager(state = pagerState) { page ->
                    AsyncImage(
                        model = event.imageUrls[page],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (event.imageUrls.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        repeat(event.imageUrls.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) Color.White
                                        else Color.White.copy(alpha = 0.45f)
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Info card
        DetailCard {
            DetailRow("Miesto", event.place.ifEmpty { "—" })
            DetailRow("Popis", event.description.ifEmpty { "—" })
            DetailRow("Vytvorené", formatter.format(event.createdAt))
            event.dateFrom?.let { DetailRow("Od", formatter.format(it)) }
            event.dateTo?.let { DetailRow("Do", formatter.format(it)) }
            DetailRow("Cena", "${"%.2f".format(event.price)} €")
            DetailRow("Kategória", event.category.ifEmpty { "—" })
            if (event.subcategory.isNotEmpty()) DetailRow("Podkategória", event.subcategory)
            DetailRow("Viditeľnosť", event.visibility)
        }

        // Creator + capacity card
        DetailCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tvorca", color = AccentColor, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text(
                    text = if (isCreator) "Vy" else event.creatorId.take(8) + "...",
                    color = if (isCreator) PrimaryTextDark else AccentColor,
                    fontSize = 13.sp,
                    fontWeight = if (isCreator) FontWeight.Normal else FontWeight.Medium,
                    modifier = if (!isCreator) Modifier.clickable { onCreatorClick() } else Modifier
                )
            }
            val capacityText = if (event.participants > 0)
                "${event.attendees.size} / ${event.participants}"
            else
                event.attendees.size.toString()
            DetailRow("Účastníci", capacityText)
            if (event.waitlist.isNotEmpty()) {
                DetailRow("Poradovník", "${event.waitlist.size} čakajúcich")
            }
        }

        // Join / leave buttons
        if (!isCreator) {
            Spacer(modifier = Modifier.height(4.dp))
            when {
                isBanned -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3B1A1A))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.Block,
                        contentDescription = null,
                        tint = Color(0xFFCF6679),
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            "Bol/a si odstránený/á z tohto eventu",
                            color = Color(0xFFCF6679),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Tvorca ti zablokoval prístup.",
                            color = PrimaryTextDark.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }

                isAttending -> Button(
                    onClick = onLeave,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B1A1A), contentColor = PrimaryTextDark
                    )
                ) { Text("Odísť z eventu", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

                isOnWaitlist -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkAccent)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Si na poradovníku — pozícia #$waitlistPos z ${event.waitlist.size}",
                        color = AccentColor, fontWeight = FontWeight.Medium, fontSize = 14.sp
                    )
                    Text(
                        "Keď niekto odíde, automaticky ťa zaradíme.",
                        color = PrimaryTextDark.copy(alpha = 0.6f), fontSize = 12.sp
                    )
                    Button(
                        onClick = onLeaveWaitlist,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3A3C3F), contentColor = PrimaryTextDark
                        )
                    ) { Text("Odísť z poradovníka", fontWeight = FontWeight.Bold) }
                }

                isFull -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Event je plný (${event.attendees.size}/${event.participants})" +
                            if (event.waitlist.isNotEmpty()) " · ${event.waitlist.size} čakajúcich" else "",
                        color = PrimaryTextDark.copy(alpha = 0.6f), fontSize = 13.sp
                    )
                    Button(
                        onClick = onJoin,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkAccent, contentColor = AccentColor
                        )
                    ) { Text("Pridať sa do poradovníka", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                }

                else -> Button(
                    onClick = onJoin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor, contentColor = DarkMapBackground
                    )
                ) { Text("Pripojiť sa", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
        }

        // Rating card (non-creator attendees only)
        if (!isCreator && isAttending) {
            DetailCard {
                Text("Hodnotenie", color = AccentColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                if (event.totalRatings > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { i ->
                            Icon(
                                if (i < event.rating.toInt()) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = null, tint = AccentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "${"%.1f".format(event.rating)} (${event.totalRatings})",
                            color = PrimaryTextDark.copy(alpha = 0.7f), fontSize = 12.sp
                        )
                    }
                }

                if (hasRated) {
                    Text(
                        "Už si ohodnotil/a tento event.",
                        color = PrimaryTextDark.copy(alpha = 0.5f), fontSize = 13.sp
                    )
                } else {
                    Text("Ohodnoť event:", color = PrimaryTextDark.copy(alpha = 0.7f), fontSize = 13.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { i ->
                            IconButton(
                                onClick = { onSelectedStarsChange(i + 1) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    if (i < selectedStars) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "${i + 1} hviezdičiek",
                                    tint = AccentColor, modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    if (selectedStars > 0) {
                        Button(
                            onClick = { onRate(selectedStars) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentColor, contentColor = DarkMapBackground
                            )
                        ) { Text("Potvrdiť hodnotenie ($selectedStars ★)", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

// ── Attendees tab ─────────────────────────────────────────────────────────────

@Composable
private fun AttendeesTab(
    event: Event,
    currentUserId: String,
    isEventCreator: Boolean,
    attendeeProfiles: Map<String, UserProfile>,
    onAttendeeClick: (String) -> Unit,
    onRemoveAttendee: (String) -> Unit
) {
    var pendingRemoveUid by remember { mutableStateOf<String?>(null) }

    // Confirmation dialog
    pendingRemoveUid?.let { uid ->
        val name = attendeeProfiles[uid]?.displayName?.ifEmpty { null } ?: uid.take(8) + "..."
        AlertDialog(
            onDismissRequest = { pendingRemoveUid = null },
            title = { Text("Odstrániť účastníka?", color = PrimaryTextDark) },
            text = { Text("Naozaj chceš odstrániť $name z eventu?", color = PrimaryTextDark.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { onRemoveAttendee(uid); pendingRemoveUid = null }) {
                    Text("Odstrániť", color = Color(0xFFCF6679), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoveUid = null }) {
                    Text("Zrušiť", color = AccentColor)
                }
            },
            containerColor = DarkAccent
        )
    }

    if (event.attendees.isEmpty() && event.creatorId.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Zatiaľ žiadni účastníci.", color = PrimaryTextDark.copy(alpha = 0.4f), fontSize = 14.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Creator row
        if (event.creatorId.isNotEmpty()) {
            item {
                val profile = attendeeProfiles[event.creatorId]
                AttendeeRow(
                    uid = event.creatorId,
                    profile = profile,
                    isCreator = true,
                    isSelf = event.creatorId == currentUserId,
                    showRemoveButton = false,
                    onClick = {
                        if (event.creatorId != currentUserId) onAttendeeClick(event.creatorId)
                    },
                    onRemove = {}
                )
            }
        }

        // Attendee rows
        if (event.attendees.isNotEmpty()) {
            item {
                Text(
                    "Účastníci (${event.attendees.size})",
                    color = AccentColor, fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(event.attendees, key = { it }) { uid ->
                val profile = attendeeProfiles[uid]
                AttendeeRow(
                    uid = uid,
                    profile = profile,
                    isCreator = false,
                    isSelf = uid == currentUserId,
                    showRemoveButton = isEventCreator && uid != currentUserId,
                    onClick = { if (uid != currentUserId) onAttendeeClick(uid) },
                    onRemove = { pendingRemoveUid = uid }
                )
            }
        }
    }
}

@Composable
private fun AttendeeRow(
    uid: String,
    profile: UserProfile?,
    isCreator: Boolean,
    isSelf: Boolean,
    showRemoveButton: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkAccent)
            .then(if (!isSelf) Modifier.clickable { onClick() } else Modifier)
            .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = if (showRemoveButton) 4.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (profile?.photoUrl?.isNotEmpty() == true) {
                AsyncImage(
                    model = profile.photoUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(DarkMapBackground),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = profile?.displayName
                        ?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        ?.take(2)?.joinToString("") ?: uid.take(2).uppercase()
                    Text(initials, color = AccentColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Name + badges
        Column(Modifier.weight(1f)) {
            Text(
                profile?.displayName?.ifEmpty { null } ?: (uid.take(8) + "..."),
                color = PrimaryTextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isCreator) Badge(label = "Tvorca", color = AccentColor)
                if (isSelf)    Badge(label = "Ty", color = Color(0xFF6CB4E4))
            }
        }

        // Remove button (creator only) or chevron icon
        if (showRemoveButton) {
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.PersonRemove,
                    contentDescription = "Odstrániť",
                    tint = Color(0xFFCF6679),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else if (!isSelf) {
            Icon(
                Icons.Filled.Person, contentDescription = null,
                tint = PrimaryTextDark.copy(alpha = 0.3f), modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun Badge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Chat tab ──────────────────────────────────────────────────────────────────

@Composable
private fun ChatTab(
    chatMessages: List<ChatMessage>,
    currentUserId: String,
    canSend: Boolean,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to newest message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Messages list
        if (chatMessages.isEmpty()) {
            Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Zatiaľ žiadne správy.\nBuď prvý!",
                    color = PrimaryTextDark.copy(alpha = 0.35f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatMessages, key = { it.id.ifEmpty { it.timestamp.toString() } }) { msg ->
                    MessageBubble(message = msg, isOwn = msg.senderId == currentUserId)
                }
            }
        }

        HorizontalDivider(color = DarkAccent)

        // Input row
        if (canSend) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkAccent)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Napíš správu...", color = PrimaryTextDark.copy(alpha = 0.4f)) },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = PrimaryTextDark,
                        unfocusedTextColor = PrimaryTextDark,
                        focusedContainerColor = Color(0xFF3A3C3F),
                        unfocusedContainerColor = Color(0xFF3A3C3F),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AccentColor
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank()
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Odoslať",
                        tint = if (inputText.isNotBlank()) AccentColor else PrimaryTextDark.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkAccent)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Chatovať môžu iba účastníci eventu.",
                    color = PrimaryTextDark.copy(alpha = 0.5f), fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, isOwn: Boolean) {
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isOwn) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isOwn) 14.dp else 4.dp,
                        topEnd = if (isOwn) 4.dp else 14.dp,
                        bottomStart = 14.dp, bottomEnd = 14.dp
                    )
                )
                .background(if (isOwn) AccentColor else DarkAccent)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!isOwn && message.senderName.isNotEmpty()) {
                Text(
                    message.senderName, color = AccentColor,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold
                )
            }
            Text(
                message.text,
                color = if (isOwn) DarkMapBackground else PrimaryTextDark,
                fontSize = 14.sp
            )
            Text(
                timeFmt.format(Date(message.timestamp)),
                color = if (isOwn) DarkMapBackground.copy(alpha = 0.55f) else PrimaryTextDark.copy(alpha = 0.45f),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkAccent)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AccentColor, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        Text(value, color = PrimaryTextDark, fontSize = 13.sp)
    }
}
