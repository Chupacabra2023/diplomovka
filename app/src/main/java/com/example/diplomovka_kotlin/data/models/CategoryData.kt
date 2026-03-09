package com.example.diplomovka_kotlin.data.models

val CATEGORY_MAP: Map<String, List<String>> = linkedMapOf(
    "Sports" to listOf(
        "Ball sports", "Athletics", "Combat sports", "Aquatic sports",
        "Gymnastics", "Cycling sports", "Winter sports", "Strength & Conditioning",
        "Outdoor sports", "Motor sports", "E-sports (competitive gaming)",
        "Racquet sports", "Watercraft sports", "Animal sports", "Extreme sports",
        "Team sports", "Individual sports", "Fitness & Wellness",
        "Precision sports", "Recreational sports", "Iné"
    ),
    "Music & Entertainment" to listOf(
        "Koncert", "Festival", "Party / Clubbing", "Karaoke", "DJ Set", "Iné"
    ),
    "Art & Culture" to listOf(
        "Film", "Divadlo", "Výstava", "Múzeum", "Literatúra / čítanie",
        "Maľovanie / kreatívne umenie", "Verejné zhromaždenie", "Protest", "Iné"
    ),
    "Food & Drinks" to listOf(
        "Food festival", "Street food", "Degustácia vín", "Degustácia piva",
        "Reštauračné eventy", "Grilovačka", "Opekačka", "Iné"
    ),
    "Business & Tech" to listOf(
        "Startup meetup", "Networking event", "Konferencia", "Prednáška",
        "Pitch Night", "Veľtrh práce", "Workshop (IT)", "Hackathon",
        "Gaming turnaj", "E-sports", "Odborné prednášky", "Iné"
    ),
    "Education" to listOf(
        "Vzdelávací kurz", "Workshop (ručné práce)", "Jazykový kurz",
        "Osobný rozvoj", "Tvorivé dielne", "Iné"
    ),
    "Nature & Outdoor" to listOf(
        "Turistika", "Výlet", "Kempovanie", "Návšteva parku", "Botanická záhrada", "Iné"
    ),
    "Family & Kids" to listOf(
        "Rodinná akcia", "Tvorivé dielne pre deti", "Detské divadlo", "Iné"
    ),
    "Community & Social" to listOf(
        "Speed dating", "Komunitné akcie", "Street meetup", "Iné"
    ),
    "Iné" to listOf("Iné")
)
