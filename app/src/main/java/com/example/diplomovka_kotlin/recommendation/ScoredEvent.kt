package com.example.diplomovka_kotlin.recommendation

import com.example.diplomovka_kotlin.data.models.Event

data class ScoredEvent(
    val event: Event,
    val score: Double,
    val scoreBreakdown: Map<String, Double>
)
