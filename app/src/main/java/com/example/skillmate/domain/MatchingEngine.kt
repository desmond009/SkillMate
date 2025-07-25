package com.example.skillmate.domain

import com.example.skillmate.data.UserProfile
import kotlin.math.min

object MatchingEngine {
    // Compute Levenshtein distance between two strings
    fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                else 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
            }
        }
        return dp[a.length][b.length]
    }

    // Compute a score for a potential match using Jaccard similarity for skills and set intersection for availability
    fun scoreMatch(current: UserProfile, candidate: UserProfile): Double {
        val currentSkills = current.skillsOffered.map { it.trim().lowercase() }.toSet()
        val candidateSkills = candidate.skillsOffered.map { it.trim().lowercase() }.toSet()
        val intersection = currentSkills.intersect(candidateSkills)
        val union = currentSkills.union(candidateSkills)
        val skillScore = if (union.isNotEmpty()) intersection.size.toDouble() / union.size else 0.0

        val currentAvail = current.availability.map { it.trim().lowercase() }.toSet()
        val candidateAvail = candidate.availability.map { it.trim().lowercase() }.toSet()
        val availIntersection = currentAvail.intersect(candidateAvail)
        val availScore = if (currentAvail.isNotEmpty() || candidateAvail.isNotEmpty()) availIntersection.size.toDouble() / (currentAvail.union(candidateAvail).size) else 0.0

        // Weighted: skills 70%, availability 30%
        return skillScore * 0.7 + availScore * 0.3
    }

    // Get top N matches for a user from a list of candidates
    fun getTopMatches(current: UserProfile, candidates: List<UserProfile>, topN: Int = 5): List<Pair<UserProfile, Double>> {
        return candidates
            .filter { it.id != current.id }
            .map { it to scoreMatch(current, it) }
            .sortedByDescending { it.second }
            .take(topN)
    }
} 