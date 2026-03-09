package com.example.diplomovka_kotlin.ui.map

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.data.models.Event
import com.example.diplomovka_kotlin.databinding.ActivityMapBinding
import com.example.diplomovka_kotlin.events.*
import com.example.diplomovka_kotlin.ui.settings.HelpActivity
import com.example.diplomovka_kotlin.ui.events.MyInvitationsActivity
import com.example.diplomovka_kotlin.ui.settings.ProfileSettingsActivity
import com.example.diplomovka_kotlin.ui.settings.LogoutActivity
import com.example.diplomovka_kotlin.ui.settings.SettingsActivity
import com.example.diplomovka_kotlin.ui.events.EventCreationInformationActivity
import com.example.diplomovka_kotlin.ui.events.EventDetailActivity
import com.example.diplomovka_kotlin.ui.events.MyCreatedEventsActivity
import com.example.diplomovka_kotlin.ui.events.MyVisitedEventsActivity
import com.example.diplomovka_kotlin.ui.events.RecommendedEventsActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var googleMap: GoogleMap

    private val markers = mutableMapOf<String, Marker>()
    private val markerEventMap = mutableMapOf<String, Event>()
    private var nextId = 1
    private var isPicking = false
    private var cameraCenter: LatLng? = null
    private var selectedAddress: String? = null

    private val bratislava = LatLng(48.1486, 17.1077)

    private val createEventLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val updatedEvent = result.data?.getSerializableExtra("updatedEvent") as? Event
            updatedEvent?.let { addEventMarker(it) }
        }
        isPicking = false
        updateFabState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        setupToolbar()
        setupDrawer()
        setupFabs()
        setupAutocomplete()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bratislava, 13f))
        googleMap.setOnCameraMoveListener {
            cameraCenter = googleMap.cameraPosition.target
        }
        googleMap.setOnMarkerClickListener { marker ->
            markerEventMap[marker.tag as? String ?: ""]?.let { event ->
                startActivity(Intent(this, EventDetailActivity::class.java).apply {
                    putExtra("event", event)
                })
            }
            true
        }
    }

    private fun setupToolbar() {
        binding.toolbar?.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_profile) {
                binding.drawerLayout.openDrawer(GravityCompat.END)
                true
            } else false
        }
    }

    private fun setupDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { item ->
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            when (item.itemId) {
                R.id.nav_profile -> startActivity(Intent(this, ProfileSettingsActivity::class.java))
                R.id.nav_created -> startActivity(Intent(this, MyCreatedEventsActivity::class.java))
                R.id.nav_visited -> startActivity(Intent(this, MyVisitedEventsActivity::class.java))
                R.id.nav_favorites -> startActivity(Intent(this, RecommendedEventsActivity::class.java))
                R.id.nav_invitations -> startActivity(Intent(this, MyInvitationsActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_logout -> startActivity(Intent(this, LogoutActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            }
            true
        }
    }

    private fun setupFabs() {
        binding.fabCreate.setOnClickListener {
            if (isPicking) {
                confirmEvent()
            } else {
                isPicking = true
                updateFabState()
            }
        }

        binding.fabSecondary.setOnClickListener {
            if (isPicking) {
                isPicking = false
                cameraCenter = null
                updateFabState()
            } else {
                openFilterSheet()
            }
        }
    }

    private fun updateFabState() {
        if (isPicking) {
            binding.fabCreate.text = "OK"
            binding.fabSecondary.text = "Zrušiť"
            binding.fabSecondary.setBackgroundColor(getColor(R.color.red))
            binding.ivCenterPin.visibility = View.VISIBLE
            binding.flAutocomplete.visibility = View.VISIBLE
        } else {
            binding.fabCreate.text = "Vytvoriť udalosť"
            binding.fabSecondary.text = "Nájsť udalosť"
            binding.fabSecondary.setBackgroundColor(getColor(R.color.indigo_500))
            binding.ivCenterPin.visibility = View.GONE
            binding.flAutocomplete.visibility = View.GONE
        }
    }

    private fun setupAutocomplete() {
        val autocompleteView = PlaceAutocompleteView(this)
        binding.flAutocomplete.addView(autocompleteView)
        autocompleteView.onPredictionSelected = { prediction ->
            onPredictionSelected(prediction)
        }
    }

    private fun onPredictionSelected(prediction: AutocompletePrediction) {
        val placesClient = Places.createClient(this)
        val request = FetchPlaceRequest.newInstance(
            prediction.placeId,
            listOf(
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
        )
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                selectedAddress = place.address
                place.latLng?.let { latLng ->
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    cameraCenter = latLng
                }
            }
    }

    private fun confirmEvent() {
        val center = cameraCenter ?: return
        val id = "event_${nextId++}"
        val tempEvent = Event(
            id = id,
            title = "Udalosť $id",
            latitude = center.latitude,
            longitude = center.longitude,
            createdAt = Date(),
            place = selectedAddress ?: ""
        )
        createEventLauncher.launch(
            Intent(this, EventCreationInformationActivity::class.java).apply {
                putExtra("event", tempEvent)
            }
        )
    }

    private fun addEventMarker(event: Event) {
        val marker = googleMap.addMarker(event.toMarkerOptions()) ?: return
        marker.tag = event.id
        markers[event.id] = marker
        markerEventMap[event.id] = event
    }

    private fun openFilterSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        dialog.setContentView(view)

        val etName = view.findViewById<EditText>(R.id.etFilterName)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)
        val sliderParticipants = view.findViewById<SeekBar>(R.id.seekBarParticipants)
        val tvParticipants = view.findViewById<TextView>(R.id.tvParticipants)
        val sliderPrice = view.findViewById<SeekBar>(R.id.seekBarPrice)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val btnDateFrom = view.findViewById<Button>(R.id.btnDateFrom)
        val btnDateTo = view.findViewById<Button>(R.id.btnDateTo)
        val rgVisibility = view.findViewById<RadioGroup>(R.id.rgVisibility)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnApply = view.findViewById<Button>(R.id.btnApply)

        val formatter = SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault())
        var dateFrom: Date? = null
        var dateTo: Date? = null

        val categories = listOf("", "Hudba", "Šport", "Party", "Kultúrne podujatia",
            "Jedlo a pitie", "Gaming", "Príroda", "Rodina", "Umenie",
            "Fotografia", "Zdravie a fitness", "Dobrovoľníctvo", "Workshop", "Diskusia", "Iné")
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        sliderParticipants.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvParticipants.text = "Počet osôb: $progress"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        sliderPrice.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvPrice.text = "Max cena: $progress €"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        btnDateFrom.setOnClickListener {
            showDateTimePicker { date ->
                dateFrom = date
                btnDateFrom.text = "Od: ${formatter.format(date)}"
            }
        }
        btnDateTo.setOnClickListener {
            showDateTimePicker { date ->
                dateTo = date
                btnDateTo.text = "Do: ${formatter.format(date)}"
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnApply.setOnClickListener {
            dialog.dismiss()
            val visibility = when (rgVisibility.checkedRadioButtonId) {
                R.id.rbPrivate -> "private"
                else -> "public"
            }
            applyFilter(
                name = etName.text.toString().trim().ifEmpty { null },
                category = spinnerCategory.selectedItem.toString().ifEmpty { null },
                participants = sliderParticipants.progress,
                maxPrice = sliderPrice.progress.toDouble(),
                visibility = visibility,
                dateFrom = dateFrom,
                dateTo = dateTo
            )
        }

        dialog.show()
    }

    private fun showDateTimePicker(onSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            TimePickerDialog(this, { _, h, min ->
                cal.set(y, m, d, h, min)
                onSelected(cal.time)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun applyFilter(
        name: String?, category: String?, participants: Int,
        maxPrice: Double, visibility: String,
        dateFrom: Date?, dateTo: Date?
    ) {
        val criteria = FilterCriteria(
            name = name,
            category = category,
            participants = participants,
            maxPrice = maxPrice,
            visibility = visibility,
            dateFrom = dateFrom,
            dateTo = dateTo
        )
        val filtered = EventFilterService(criteria).filter(markerEventMap.values.toList())

        markers.values.forEach { it.isVisible = false }
        filtered.forEach { event ->
            markers[event.id]?.isVisible = true
        }
    }
}
