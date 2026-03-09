package com.example.diplomovka_kotlin.ui.map

import android.R
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.diplomovka_kotlin.databinding.FragmentPlaceAutocompleteBinding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class PlaceAutocompleteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = FragmentPlaceAutocompleteBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private val placesClient: PlacesClient = Places.createClient(context)
    private val debounceHandler = Handler(Looper.getMainLooper())
    private var debounceRunnable: Runnable? = null
    private var predictions: List<AutocompletePrediction> = emptyList()

    var onPredictionSelected: ((AutocompletePrediction) -> Unit)? = null

    init {
        setupSearchField()
        setupListView()
    }

    private fun setupSearchField() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onSearchChanged(s.toString().trim())
            }
        })
    }

    private fun onSearchChanged(query: String) {
        debounceRunnable?.let { debounceHandler.removeCallbacks(it) }

        if (query.isEmpty()) {
            updatePredictions(emptyList())
            return
        }

        debounceRunnable = Runnable {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries("SK")
                .setTypesFilter(listOf("address"))
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    updatePredictions(response.autocompletePredictions)
                }
                .addOnFailureListener {
                    updatePredictions(emptyList())
                }
        }

        debounceHandler.postDelayed(debounceRunnable!!, 300)
    }

    private fun updatePredictions(newPredictions: List<AutocompletePrediction>) {
        predictions = newPredictions
        if (predictions.isEmpty()) {
            binding.lvPredictions.visibility = GONE
        } else {
            val adapter = object : ArrayAdapter<AutocompletePrediction>(
                context,
                R.layout.simple_list_item_2,
                R.id.text1,
                predictions
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val p = predictions[position]
                    view.findViewById<TextView>(R.id.text1).text =
                        p.getPrimaryText(null).toString()
                    view.findViewById<TextView>(R.id.text2).text =
                        p.getSecondaryText(null).toString()
                    return view
                }
            }
            binding.lvPredictions.adapter = adapter
            binding.lvPredictions.visibility = VISIBLE
        }
    }

    private fun setupListView() {
        binding.lvPredictions.setOnItemClickListener { _, _, position, _ ->
            val selected = predictions[position]
            binding.etSearch.setText(selected.getFullText(null).toString())
            updatePredictions(emptyList())
            onPredictionSelected?.invoke(selected)
        }
    }
}
