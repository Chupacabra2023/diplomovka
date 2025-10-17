import 'dart:async';
import 'package:flutter/material.dart';
import 'package:google_place/google_place.dart';

class PlaceAutocompleteField extends StatefulWidget {
  // Callback funkcia, ktorá vráti vybrané miesto do map_page
  final Function(AutocompletePrediction) onPredictionSelected;

  const PlaceAutocompleteField({
    super.key,
    required this.onPredictionSelected,
  });

  @override
  State<PlaceAutocompleteField> createState() => _PlaceAutocompleteFieldState();
}

class _PlaceAutocompleteFieldState extends State<PlaceAutocompleteField> {
  final TextEditingController _searchCtrl = TextEditingController();
  final FocusNode _searchFocus = FocusNode();

  late GooglePlace _googlePlace;
  List<AutocompletePrediction> _predictions = [];
  Timer? _debounce;

  @override
  void initState() {
    super.initState();
    // Vložte svoj API kľúč sem
    _googlePlace = GooglePlace("YOUR_API_KEY_HERE");
    _searchCtrl.addListener(_onSearchChanged);
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _searchCtrl.dispose();
    _searchFocus.dispose();
    super.dispose();
  }

  // Metóda, ktorá sa spustí pri zmene textu vo vyhľadávacom poli
  void _onSearchChanged() {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 300), () async {
      final text = _searchCtrl.text.trim();
      if (text.isEmpty) {
        if (mounted) setState(() => _predictions = []);
        return;
      }
      final result = await _googlePlace.autocomplete.get(
        text,
        language: "sk",
        components: [Component("country", "sk")],
        types: "address",
      );
      if (mounted) {
        setState(() {
          _predictions = result?.predictions ?? [];
        });
      }
    });
  }

  // Metóda na spracovanie výberu z návrhov
  void _handlePredictionSelection(AutocompletePrediction prediction) {
    _searchCtrl.text = prediction.description ?? "";
    _searchFocus.unfocus();
    setState(() {
      _predictions = [];
    });
    // Zavolá sa callback, aby sa informácia dostala späť do `map_page`
    widget.onPredictionSelected(prediction);
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      elevation: 4,
      borderRadius: BorderRadius.circular(12),
      clipBehavior: Clip.antiAlias,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            controller: _searchCtrl,
            focusNode: _searchFocus,
            decoration: const InputDecoration(
              hintText: 'Zadaj adresu...',
              prefixIcon: Icon(Icons.search),
              border: InputBorder.none,
              contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 14),
            ),
          ),
          if (_predictions.isNotEmpty)
            ConstrainedBox(
              constraints: const BoxConstraints(maxHeight: 220),
              child: ListView.builder(
                shrinkWrap: true,
                itemCount: _predictions.length,
                itemBuilder: (context, i) {
                  final p = _predictions[i];
                  return ListTile(
                    dense: true,
                    leading: const Icon(Icons.place),
                    title: Text(p.structuredFormatting?.mainText ?? p.description ?? ''),
                    subtitle: Text(p.structuredFormatting?.secondaryText ?? ''),
                    onTap: () => _handlePredictionSelection(p),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }
}
