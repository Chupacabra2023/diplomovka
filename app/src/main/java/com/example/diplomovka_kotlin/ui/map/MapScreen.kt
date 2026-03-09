package com.example.diplomovka_kotlin.ui.map

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.data.models.Event
import com.example.diplomovka_kotlin.data.models.CATEGORY_MAP
import com.example.diplomovka_kotlin.data.services.EventFilterService
import com.example.diplomovka_kotlin.data.services.FilterCriteria
import com.example.diplomovka_kotlin.ui.AppViewModel
import com.example.diplomovka_kotlin.ui.Screen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import java.text.SimpleDateFormat
import java.util.*

// ── Farby ────────────────────────────────────────────────────────────────────
private val DarkMapBackground = Color(0xFF1A1C1E)
private val DarkAccent        = Color(0xFF2D2F31)
private val BottomPanelWhite  = Color(0xCCFFFFFF)
private val PrimaryTextDark   = Color(0xFFE2E2E6)
private val AccentColor       = Color(0xFFD0BCFF)

// ── Čierny marker ────────────────────────────────────────────────────────────
private fun blackMarkerIcon(context: android.content.Context): BitmapDescriptor {
    val drawable = ContextCompat.getDrawable(context, com.example.diplomovka_kotlin.R.drawable.ic_location_pin)!!
        .mutate()
    drawable.setTint(android.graphics.Color.BLACK)
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

// ── Štýly mapy ───────────────────────────────────────────────────────────────
private enum class MapStyle(val label: String) {
    DARK("Tmavá"),
    NORMAL("Štandardná"),
    SATELLITE("Satelit"),
    HYBRID("Hybrid"),
    TERRAIN("Terén")
}

private val BRATISLAVA = LatLng(48.1486, 17.1077)

// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MapScreen(
    appViewModel: AppViewModel,
    onOpenDrawer: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()

    // ── Poloha používateľa ────────────────────────────────────────────────────
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationPermissionGranted = granted
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation
                .addOnSuccessListener { loc ->
                    loc?.let { userLocation = LatLng(it.latitude, it.longitude) }
                }
        }
    }

    var googleMap      by remember { mutableStateOf<GoogleMap?>(null) }
    val markers        = remember { mutableMapOf<String, Marker>() }
    var isPicking      by remember { mutableStateOf(false) }
    var cameraCenter   by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var showFilter     by remember { mutableStateOf(false) }
    var currentStyle   by remember { mutableStateOf(MapStyle.NORMAL) }
    var showStyleMenu  by remember { mutableStateOf(false) }

    val mapView = remember { MapView(context).also { it.onCreate(null) } }

    // Lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START   -> mapView.onStart()
                Lifecycle.Event.ON_RESUME  -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE   -> mapView.onPause()
                Lifecycle.Event.ON_STOP    -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    // Sledujeme zmeny v events (aj async načítanie z Firestore) a sync-ujeme markery
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val blackIcon = remember { blackMarkerIcon(context) }
    LaunchedEffect(googleMap) {
        val map = googleMap ?: return@LaunchedEffect
        snapshotFlow { appViewModel.events.toMap() }.collect { events ->
            // Pridaj markery — súkromné eventy len pre ich tvorcu
            events.values
                .filter { it.visibility == "public" || it.creatorId == currentUserId }
                .forEach { event ->
                if (!markers.containsKey(event.id)) {
                    val marker = map.addMarker(event.toMarkerOptions(blackIcon))
                    marker?.tag = event.id
                    if (marker != null) markers[event.id] = marker
                }
            }
            // Odstráň markery pre vymazané eventy
            markers.keys.toList().filter { !events.containsKey(it) }.forEach { id ->
                markers[id]?.remove()
                markers.remove(id)
            }
            appViewModel.consumeNewEvent()
        }
    }

    // Zmena štýlu mapy
    LaunchedEffect(currentStyle) {
        val map = googleMap ?: return@LaunchedEffect
        when (currentStyle) {
            MapStyle.DARK      -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark))
            }
            MapStyle.NORMAL    -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                map.setMapStyle(null)
            }
            MapStyle.SATELLITE -> { map.mapType = GoogleMap.MAP_TYPE_SATELLITE; map.setMapStyle(null) }
            MapStyle.HYBRID    -> { map.mapType = GoogleMap.MAP_TYPE_HYBRID;    map.setMapStyle(null) }
            MapStyle.TERRAIN   -> { map.mapType = GoogleMap.MAP_TYPE_TERRAIN;   map.setMapStyle(null) }
        }
    }

    if (!Places.isInitialized()) {
        Places.initialize(context.applicationContext, stringResource(R.string.google_maps_key))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkMapBackground)
    ) {
        // ── Mapa ─────────────────────────────────────────────────────────────
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { mv ->
            mv.getMapAsync { map ->
                if (googleMap == null) {
                    googleMap = map
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(BRATISLAVA, 13f))
                    map.setOnCameraMoveListener { cameraCenter = map.cameraPosition.target }
                    map.setOnMarkerClickListener { marker ->
                        appViewModel.events[marker.tag as? String ?: ""]?.let { event ->
                            appViewModel.selectEvent(event)
                            navController.navigate(Screen.EventDetail.route)
                        }
                        true
                    }
                }
            }
        }

        // ── Stredový pin ─────────────────────────────────────────────────────
        if (isPicking) {
            CenterPin(modifier = Modifier.align(Alignment.Center))

            AndroidView(
                factory = { PlaceAutocompleteView(it).apply {
                    onPredictionSelected = { prediction ->
                        val placesClient = Places.createClient(context)
                        val request = FetchPlaceRequest.newInstance(
                            prediction.placeId,
                            listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
                        )
                        placesClient.fetchPlace(request).addOnSuccessListener { response ->
                            selectedAddress = response.place.address
                            response.place.latLng?.let { latLng ->
                                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                                cameraCenter = latLng
                            }
                        }
                    }
                }},
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 72.dp)
            )
        }

        // ── Tlačidlá vpravo hore ─────────────────────────────────────────────
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profil
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkAccent),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Profil",
                        tint = PrimaryTextDark, modifier = Modifier.size(30.dp))
                }
            }

            // Prepínač štýlu
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkAccent),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { showStyleMenu = true }) {
                    Icon(Icons.Filled.Layers, contentDescription = "Štýl mapy",
                        tint = AccentColor, modifier = Modifier.size(24.dp))
                }
                DropdownMenu(
                    expanded = showStyleMenu,
                    onDismissRequest = { showStyleMenu = false }
                ) {
                    MapStyle.entries.forEach { style ->
                        DropdownMenuItem(
                            text = { Text(style.label, color = if (style == currentStyle) AccentColor else Color.Unspecified) },
                            onClick = { currentStyle = style; showStyleMenu = false }
                        )
                    }
                }
            }
        }

        // ── Spodný panel s FAB ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(BottomPanelWhite)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExtendedFloatingActionButton(
                    text = { Text(if (isPicking) "OK" else "Vytvoriť udalosť", color = PrimaryTextDark) },
                    icon = {},
                    containerColor = DarkAccent,
                    onClick = {
                        if (isPicking) {
                            val center = cameraCenter ?: return@ExtendedFloatingActionButton
                            val id = appViewModel.nextEventId()
                            val creatorId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            isPicking = false
                            scope.launch {
                                val place = selectedAddress ?: withContext(Dispatchers.IO) {
                                    try {
                                        Geocoder(context, Locale.getDefault())
                                            .getFromLocation(center.latitude, center.longitude, 1)
                                            ?.firstOrNull()?.getAddressLine(0) ?: ""
                                    } catch (e: Exception) { "" }
                                }
                                val tempEvent = Event(
                                    id = id, creatorId = creatorId,
                                    title = "Udalosť $id",
                                    latitude = center.latitude, longitude = center.longitude,
                                    createdAt = Date(), place = place
                                )
                                appViewModel.prepareCreation(tempEvent, fromMap = true)
                                navController.navigate(Screen.EventCreation.route)
                            }
                        } else {
                            isPicking = true
                        }
                    }
                )

                ExtendedFloatingActionButton(
                    text = { Text(if (isPicking) "Zrušiť" else "Nájsť udalosť", color = PrimaryTextDark) },
                    icon = {},
                    containerColor = if (isPicking) Color(0xFF8B1A1A) else DarkAccent,
                    onClick = {
                        if (isPicking) { isPicking = false; cameraCenter = null }
                        else showFilter = true
                    }
                )
            }
        }
    }

    // ── Filter bottom sheet ───────────────────────────────────────────────────
    if (showFilter) {
        FilterBottomSheet(
            events = appViewModel.events.values.toList(),
            markers = markers,
            userLocation = userLocation,
            onDismiss = { showFilter = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CenterPin(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_location_pin),
        contentDescription = null,
        tint = Color.Black,
        modifier = modifier.size(48.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    events: List<Event>,
    markers: Map<String, Marker>,
    userLocation: LatLng?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formatter = SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault())

    var name                by remember { mutableStateOf("") }
    var selectedCategory    by remember { mutableStateOf("") }
    var selectedSubcategory by remember { mutableStateOf("") }
    var participants        by remember { mutableStateOf(0f) }
    var maxPrice            by remember { mutableStateOf(500f) }
    var maxDistance         by remember { mutableStateOf(50f) }
    var dateFrom            by remember { mutableStateOf<Date?>(null) }
    var expandedCategory    by remember { mutableStateOf(false) }
    var expandedSubcategory by remember { mutableStateOf(false) }

    val categories = listOf("") + CATEGORY_MAP.keys.toList()
    val subcategories = CATEGORY_MAP[selectedCategory] ?: emptyList()

    fun showDateTimePicker(onSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            TimePickerDialog(context, { _, h, min ->
                cal.set(y, m, d, h, min); onSelected(cal.time)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor        = PrimaryTextDark,
        unfocusedTextColor      = PrimaryTextDark,
        focusedBorderColor      = AccentColor,
        unfocusedBorderColor    = Color(0xFF4A4C4F),
        focusedLabelColor       = AccentColor,
        unfocusedLabelColor     = PrimaryTextDark.copy(alpha = 0.6f),
        cursorColor             = AccentColor,
        focusedContainerColor   = DarkAccent,
        unfocusedContainerColor = DarkAccent
    )

    val sliderColors = SliderDefaults.colors(
        thumbColor          = AccentColor,
        activeTrackColor    = AccentColor,
        inactiveTrackColor  = Color(0xFF4A4C4F)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkMapBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AccentColor) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Filter udalostí", style = MaterialTheme.typography.titleLarge, color = PrimaryTextDark)

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Názov") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, colors = fieldColors
            )

            ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = it }) {
                OutlinedTextField(
                    value = selectedCategory.ifEmpty { "Všetky kategórie" },
                    onValueChange = {}, readOnly = true,
                    label = { Text("Kategória") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = fieldColors
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false },
                    containerColor = DarkAccent
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.ifEmpty { "Všetky kategórie" }, color = if (cat == selectedCategory) AccentColor else PrimaryTextDark) },
                            onClick = {
                                selectedCategory = cat
                                selectedSubcategory = ""
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            // Podkategória — zobrazí sa len ak je vybraná hlavná kategória
            if (selectedCategory.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expandedSubcategory,
                    onExpandedChange = { expandedSubcategory = it }
                ) {
                    OutlinedTextField(
                        value = selectedSubcategory.ifEmpty { "Všetky podkategórie" },
                        onValueChange = {}, readOnly = true,
                        label = { Text("Podkategória") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubcategory) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = fieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSubcategory,
                        onDismissRequest = { expandedSubcategory = false },
                        containerColor = DarkAccent
                    ) {
                        DropdownMenuItem(
                            text = { Text("Všetky podkategórie", color = if (selectedSubcategory.isEmpty()) AccentColor else PrimaryTextDark) },
                            onClick = { selectedSubcategory = ""; expandedSubcategory = false }
                        )
                        subcategories.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub, color = if (sub == selectedSubcategory) AccentColor else PrimaryTextDark) },
                                onClick = { selectedSubcategory = sub; expandedSubcategory = false }
                            )
                        }
                    }
                }
            }

            Text("Počet osôb: ${participants.toInt()}", color = AccentColor)
            Slider(value = participants, onValueChange = { participants = it },
                valueRange = 0f..200f, colors = sliderColors)

            Text(
                if (maxPrice >= 500f) "Max cena: bez obmedzenia" else "Max cena: ${"%.0f".format(maxPrice)} €",
                color = AccentColor
            )
            Slider(value = maxPrice, onValueChange = { maxPrice = it },
                valueRange = 0f..500f, colors = sliderColors)

            Text(
                if (userLocation == null) "Vzdialenosť: poloha nedostupná"
                else if (maxDistance >= 50f) "Vzdialenosť: kdekoľvek"
                else "Vzdialenosť: do ${"%.0f".format(maxDistance)} km",
                color = if (userLocation != null) AccentColor else PrimaryTextDark.copy(alpha = 0.4f)
            )
            Slider(
                value = maxDistance,
                onValueChange = { maxDistance = it },
                valueRange = 1f..50f,
                enabled = userLocation != null,
                colors = sliderColors
            )

            OutlinedButton(
                onClick = { showDateTimePicker { dateFrom = it } },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentColor.copy(alpha = 0.5f))
            ) {
                Text(dateFrom?.let { "Začína od: ${formatter.format(it)}" } ?: "Dátum začiatku (voliteľné)")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTextDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A4C4F))
                ) { Text("Zrušiť") }

                Button(
                    onClick = {
                        val criteria = FilterCriteria(
                            name = name.trim().ifEmpty { null },
                            category = selectedCategory.ifEmpty { null },
                            subcategory = selectedSubcategory.ifEmpty { null },
                            participants = participants.toInt().takeIf { it > 0 },
                            maxPrice = maxPrice.toDouble(),
                            dateFrom = dateFrom,
                            maxDistanceKm = if (userLocation != null && maxDistance < 50f) maxDistance.toDouble() else null,
                            refLat = userLocation?.latitude ?: 0.0,
                            refLng = userLocation?.longitude ?: 0.0
                        )
                        val filtered = EventFilterService(criteria).filter(events)
                        markers.values.forEach { it.isVisible = false }
                        filtered.forEach { event -> markers[event.id]?.isVisible = true }
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = DarkMapBackground
                    )
                ) { Text("Použiť", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
