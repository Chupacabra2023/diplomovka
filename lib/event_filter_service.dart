import 'event.dart';

class FilterCriteria {
  final String? name;
  final String? category;
  final int? participants;
  final double? maxPrice;
  final String? visibility;
  final DateTime? dateFrom;
  final DateTime? dateTo;

  FilterCriteria({
    this.name,
    this.category,
    this.participants,
    this.maxPrice,
    this.visibility,
    this.dateFrom,
    this.dateTo,
  });
}

// 2. Vytvoríme

class EventFilterService {
  final FilterCriteria criteria;

  EventFilterService(this.criteria);

  // Hlavná metóda, ktorá zoberie zoznam všetkých udalostí a vráti len vyfiltrované
  List<Event> filter(List<Event> allEvents) {return allEvents.where((event) => _matches(event)).toList();
  }

  // Súkromná pomocná metóda, ktorá kontroluje jednu udalosť
  bool _matches(Event event) {
    // Podmienka pre názov
    print("true");
    print("Filter name: ${criteria.name}");
    print("Event title: ${event.title}");
    print("Contains? ${event.title?.toLowerCase().contains(criteria.name?.toLowerCase() ?? '')}");
    if (criteria.name != null &&
        criteria.name!.isNotEmpty &&
        !(event.title?.toLowerCase().contains(criteria.name!.toLowerCase()) ?? false)) {
      print("false");
      return false;
    }


    // Podmienka pre kategóriu
    if (criteria.visibility != null &&
        criteria.visibility != 'All' && // Pridajte túto kontrolu
        event.visibility != criteria.visibility) {
      return false;
    }
    // Podmienka pre účastníkov
    if (criteria.participants != null &&
        criteria.participants! > 0 &&(event.participants ?? 0) < criteria.participants!) {
      return false;
    }

    // Podmienka pre cenu
    if (criteria.maxPrice != null && (event.price ?? 0) > criteria.maxPrice!) {
      return false;
    }
    if (criteria.category != null &&
        criteria.category!.isNotEmpty &&
        event.category != criteria.category) {
      return false;
    }



    // Podmienka pre dátum OD
    // ...
// Podmienka pre dátum OD
    if (criteria.dateFrom != null &&
        event.dateFrom != null && // OPRAVENÉ
        event.dateFrom!.isBefore(criteria.dateFrom!)) {
      return false;
    }

// Podmienka pre dátum DO
    if (criteria.dateTo != null &&
        event.dateTo != null && // OPRAVENÉ
        event.dateTo!.isAfter(criteria.dateTo!)) {
      return false;
    }
//...

    return true; // Ak prešlo všetkými kontrolami, udalosť vyhovuje
  }
}





