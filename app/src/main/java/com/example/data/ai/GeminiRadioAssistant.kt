package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.CuratedStations
import com.example.data.model.ActiveProfile
import com.example.data.model.ProfileConfig
import com.example.data.model.RadioStation
import com.example.data.repository.RadioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiMoodPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val prompt: String
)

data class AiStationSuggestion(
    val stationName: String,
    val description: String,
    val whyItFits: String,
    val streamUrl: String,
    val tags: String,
    val country: String,
    val matchedStation: RadioStation
)

data class AiAssistantResponse(
    val message: String,
    val suggestions: List<AiStationSuggestion>,
    val isAiGenerated: Boolean = true,
    val isKeyMissing: Boolean = false
)

class GeminiRadioAssistant(
    private val repository: RadioRepository
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    companion object {
        const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        val MOOD_PRESETS = listOf(
            AiMoodPreset(
                id = "romantic",
                title = "Romantischer Abend",
                subtitle = "Sanfter Jazz, Chanson & warme Melodien für zwei",
                icon = "🍷",
                prompt = "Wir machen uns einen gemütlichen, romantischen Abend zu zweit bei einem Glas Wein. Welche werbefreien oder entspannten Radio-Sender passen perfekt?"
            ),
            AiMoodPreset(
                id = "cooking",
                title = "Kochen & Genießen",
                subtitle = "Beschwingt, italienisch, Pop & gute Laune in der Küche",
                icon = "🍳",
                prompt = "Wir kochen am Wochenende zusammen etwas Schönes und wollen angenehme, beschwingte Hintergrundmusik ohne nervige Unterbrechungen."
            ),
            AiMoodPreset(
                id = "sunday_morning",
                title = "Sonntagsfrühstück",
                subtitle = "Kaffee, Croissant, feine Akustik & Kultur",
                icon = "☕",
                prompt = "Sonntagmorgen, ausgiebiges Frühstück mit Kaffee und Zeitung. Wir suchen ruhige, elegante Klänge, sanften Pop oder Kultur."
            ),
            AiMoodPreset(
                id = "roadtrip",
                title = "Roadtrip & Fahrt",
                subtitle = "Echte Meilensteine, Rock & 80er zum Mitsingen",
                icon = "🚗",
                prompt = "Wir fahren mit dem Auto los und wollen lebendige Klassiker, 80er Hits und Rock, bei dem man mitsingen kann."
            ),
            AiMoodPreset(
                id = "chillout",
                title = "Entspannung & Ruhe",
                subtitle = "Ambient, Lounge & stressfreie Klangwelten",
                icon = "🛋️",
                prompt = "Wir wollen einfach abschalten, lesen und den Tag ausklingen lassen. Ruhige Chillout- und Ambient-Sender."
            ),
            AiMoodPreset(
                id = "party_80s90s",
                title = "80er & 90er Kult",
                subtitle = "Kult-Hits, Synthpop & Eurodance für Feierlaune",
                icon = "🕺",
                prompt = "Wir haben Lust auf die besten Hits der 80er und 90er Jahre mit echten Ohrwürmern."
            ),
            AiMoodPreset(
                id = "schlager",
                title = "Schlager & Gemütlichkeit",
                subtitle = "Die schönsten deutschen Hits & Melodien",
                icon = "🍻",
                prompt = "Gute Laune mit den schönsten deutschen Schlagern und Klassikern."
            ),
            AiMoodPreset(
                id = "focus",
                title = "Konzentration & Lesen",
                subtitle = "Instrumentale Meisterwerke & Klaviermusik",
                icon = "📖",
                prompt = "Ruhige instrumentale Musik, Klassik oder Jazz ohne viel Gesang für Fokus und Entspannung."
            ),
            AiMoodPreset(
                id = "swiss_austria",
                title = "Alpenklänge & Schweiz",
                subtitle = "Werbefreier Pop & Alpenrock aus CH & AT",
                icon = "🏔️",
                prompt = "Empfiehl uns die besten Qualitätssender aus der Schweiz und Österreich wie Radio Swiss Pop oder Hitradio Ö3."
            ),
            AiMoodPreset(
                id = "discover_world",
                title = "Weltradios & Exotik",
                subtitle = "Geheimtipps & Sounds aus aller Welt",
                icon = "🌍",
                prompt = "Überrasche uns mit besonderen Weltradios und Geheimtipps, die man nicht an jeder Ecke hört."
            )
        )
    }

    suspend fun getRecommendations(
        userQuery: String,
        profileConfig: ProfileConfig
    ): AiAssistantResponse = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getCuratedFallbackResponse(userQuery, profileConfig, isKeyMissing = true)
        }

        try {
            val curatedList = CuratedStations.INITIAL_STATIONS
            val stationNames = curatedList.joinToString(", ") { "${it.name} (${it.tags}, ${it.country})" }

            val profileContext = when (profileConfig.activeProfile) {
                ActiveProfile.MINE -> "Der Hörer hört gerade im Profil '${profileConfig.myName}' (Er)."
                ActiveProfile.HERS -> "Die Hörerin hört gerade im Profil '${profileConfig.herName}' (Sie)."
                ActiveProfile.BOTH -> "Das Ehepaar hört gerade gemeinsam im Profil '${profileConfig.sharedName}'."
            }

            val systemInstruction = """
                Du bist der persönliche Hi-Fi-Radio-Concierge für ein deutsches Ehepaar (Er und seine Frau).
                Sie möchten hochwertige, werbearme Webradios hören.
                $profileContext
                
                Verfügbare geprüfte Live-Sender in der App:
                $stationNames
                
                Aufgabe:
                Beantworte die Anfrage des Paars auf Deutsch, charmant und sympathisch in 1-2 Sätzen.
                Empfehle 2 bis maximal 4 Sender aus der Liste (oder bekannte europäische Sender wie SWR3, 1LIVE, Bayern 3, Rock Antenne, Radio Swiss Pop, Radio Swiss Jazz, 80s80s, Klassik Radio, Deutschlandfunk, BR Schlager etc.), die am besten zur Stimmung passen.
                
                Antworte AUSSCHLIESSLICH im folgenden JSON-Format ohne Markdown-Codeblöcke:
                {
                  "message": "Liebe Grüße! Für euren Abend habe ich folgende Sender ausgesucht...",
                  "recommendations": [
                    {
                      "stationName": "SWR3",
                      "description": "Pop, Hits & sympathische Moderation",
                      "whyItFits": "Bringt schwungvolle Hits und tolle Laune."
                    }
                  ]
                }
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemInstruction\n\nAnfrage des Ehepaars: $userQuery"))
                        })
                    })
                })
            }

            val endpoint = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w("GeminiRadioAssistant", "Gemini API error ${response.code}: $responseBody")
                return@withContext getCuratedFallbackResponse(userQuery, profileConfig, isKeyMissing = false)
            }

            parseGeminiResponse(responseBody, userQuery)
        } catch (e: Exception) {
            Log.e("GeminiRadioAssistant", "Failed to query Gemini", e)
            getCuratedFallbackResponse(userQuery, profileConfig, isKeyMissing = false)
        }
    }

    private fun parseGeminiResponse(responseBody: String, originalQuery: String): AiAssistantResponse {
        return try {
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            val cleanJson = text
                .substringAfter("{")
                .substringBeforeLast("}")
                .let { "{$it}" }

            val parsedJson = JSONObject(cleanJson)
            val message = parsedJson.optString("message", "Hier sind meine Empfehlungen für euch:")
            val recArray = parsedJson.optJSONArray("recommendations") ?: JSONArray()

            val suggestions = mutableListOf<AiStationSuggestion>()
            for (i in 0 until recArray.length()) {
                val rec = recArray.getJSONObject(i)
                val name = rec.optString("stationName", "")
                val desc = rec.optString("description", "")
                val why = rec.optString("whyItFits", "")

                val matched = findBestMatchingStation(name)
                suggestions.add(
                    AiStationSuggestion(
                        stationName = matched.name,
                        description = desc.ifBlank { matched.tags },
                        whyItFits = why.ifBlank { "Passt ideal zu eurer musikalischen Stimmung." },
                        streamUrl = matched.streamUrl,
                        tags = matched.tags,
                        country = matched.country,
                        matchedStation = matched
                    )
                )
            }

            if (suggestions.isEmpty()) {
                fallbackSuggestionsForQuery(originalQuery)
            } else {
                AiAssistantResponse(
                    message = message,
                    suggestions = suggestions,
                    isAiGenerated = true,
                    isKeyMissing = false
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiRadioAssistant", "Failed to parse JSON response: $responseBody", e)
            fallbackSuggestionsForQuery(originalQuery)
        }
    }

    private fun findBestMatchingStation(nameQuery: String): RadioStation {
        val q = nameQuery.trim().lowercase()
        val all = CuratedStations.INITIAL_STATIONS
        return all.firstOrNull { it.name.lowercase().contains(q) || q.contains(it.name.lowercase()) }
            ?: all.firstOrNull { it.tags.lowercase().contains(q) }
            ?: all.first()
    }

    fun getCuratedFallbackResponse(
        query: String,
        profileConfig: ProfileConfig,
        isKeyMissing: Boolean
    ): AiAssistantResponse {
        val q = query.lowercase()
        val profileName = when (profileConfig.activeProfile) {
            ActiveProfile.MINE -> profileConfig.myName
            ActiveProfile.HERS -> profileConfig.herName
            ActiveProfile.BOTH -> profileConfig.sharedName
        }

        val all = CuratedStations.INITIAL_STATIONS
        val selected = when {
            q.contains("romant") || q.contains("wein") || q.contains("abend") -> {
                listOf(
                    all.first { it.id == "curated_jazz_radio" } to "Warmer Jazz & Blues ohne Unterbrechungen für einen stimmungsvollen Zweisamkeits-Abend.",
                    all.first { it.id == "curated_swiss_pop" } to "Feiner, melodischer Pop – unaufdringlich und komplett werbefrei.",
                    all.first { it.id == "curated_somafm_groove" } to "Chillout & Downtempo mit entspanntem Rhythmus."
                )
            }
            q.contains("koch") || q.contains("küche") || q.contains("essen") -> {
                listOf(
                    all.first { it.id == "curated_swr3" } to "Aktuelle Hits, erstklassiger Sound und beste Unterhaltung beim Kochen.",
                    all.first { it.id == "curated_swiss_pop" } to "Werbefreie Wohlfühl-Hits, ideal für entspanntes Schnippeln und Brutzeln.",
                    all.first { it.id == "curated_bayern3" } to "Beliebte Hits und bayerischer Charme."
                )
            }
            q.contains("frühstück") || q.contains("kaffee") || q.contains("sonntag") -> {
                listOf(
                    all.first { it.id == "curated_swiss_classic" } to "Erhabene Klassikmeisterwerke für einen ruhigen, genussvollen Morgen.",
                    all.first { it.id == "curated_dlf" } to "Erstklassiger Journalismus und Hintergrundberichte zum Kaffee.",
                    all.first { it.id == "curated_wdr2" } to "Angenehmer Pop-Mix und regionale News."
                )
            }
            q.contains("auto") || q.contains("fahrt") || q.contains("roadtrip") -> {
                listOf(
                    all.first { it.id == "curated_rock_antenne" } to "Echter Rock & Classic Rock – treibt die Fahrt energiegeladen voran!",
                    all.first { it.id == "curated_80s80s" } to "Unsterbliche 80er Meilensteine, bei denen man laut mitsingt.",
                    all.first { it.id == "curated_swr1" } to "Große Meilensteine der Musikgeschichte für die Landstraße."
                )
            }
            q.contains("schweiz") || q.contains("swiss") || q.contains("alpen") || q.contains("österreich") -> {
                listOf(
                    all.first { it.id == "curated_swiss_pop" } to "100% werbefreier Schweizer Genuss – harmonischer Pop für jede Lebenslage.",
                    all.first { it.id == "curated_jazz_radio" } to "Radio Swiss Jazz mit feinstem Sound direkt aus Basel.",
                    all.first { it.id == "curated_swiss_classic" } to "Radio Swiss Classic für erstklassige Kammermusik und Orchester."
                )
            }
            q.contains("rock") || q.contains("gitarre") || q.contains("metal") -> {
                listOf(
                    all.first { it.id == "curated_rock_antenne" } to "Der beste Rock non-stop von AC/DC bis Foo Fighters.",
                    all.first { it.id == "curated_radio_bob" } to "Echter Rock'n'Roll & handgemachter Sound.",
                    all.first { it.id == "curated_1live" } to "Moderne Rock- und Alternative-Tracks."
                )
            }
            q.contains("80") || q.contains("90") || q.contains("kult") || q.contains("retro") -> {
                listOf(
                    all.first { it.id == "curated_80s80s" } to "Synthesizer, New Wave und die größten Legenden der 80er.",
                    all.first { it.id == "curated_90s90s" } to "Eurodance, Boybands und die besten 90er Erinnerungen.",
                    all.first { it.id == "curated_wdr4" } to "Lieblingshits aus vier Jahrzehnten."
                )
            }
            q.contains("schlager") || q.contains("volksmusik") || q.contains("deutsch") -> {
                listOf(
                    all.first { it.id == "curated_br_schlager" } to "100% deutsche Schlager, Heimatmelodien und gute Stimmung.",
                    all.first { it.id == "curated_swr4" } to "Die schönsten Schlager & Oldies der Region mit Herz.",
                    all.first { it.id == "curated_wdr4" } to "Deutsche Hits und unvergessene Klassiker."
                )
            }
            q.contains("jazz") || q.contains("blues") || q.contains("soul") -> {
                listOf(
                    all.first { it.id == "curated_jazz_radio" } to "Souveräner Schweizer Jazz ohne Unterbrechungen.",
                    all.first { it.id == "curated_somafm_groove" } to "Groove Salad: Feiner Downtempo-Jazz und Ambient.",
                    all.first { it.id == "curated_swiss_pop" } to "Melodischer Akustik-Pop mit Seele."
                )
            }
            q.contains("klassik") || q.contains("orchester") || q.contains("piano") || q.contains("klavier") -> {
                listOf(
                    all.first { it.id == "curated_klassik_radio" } to "Die schönsten Klassik-Hits, Filmmusik und Neoklassik.",
                    all.first { it.id == "curated_swiss_classic" } to "Exzellente Schweizer Orchestermusik, glasklar übertragen.",
                    all.first { it.id == "curated_dlf" } to "Kultursender mit Tiefgang und feinsten Klängen."
                )
            }
            q.contains("chill") || q.contains("relax") || q.contains("lounge") || q.contains("ambient") || q.contains("ruhe") -> {
                listOf(
                    all.first { it.id == "curated_somafm_groove" } to "SomaFM Groove Salad – der weltweite Maßstab für Entspannung & Ambient.",
                    all.first { it.id == "curated_sunshine_live" } to "Elektronische Klangwelten und entspannte Deep House Beats.",
                    all.first { it.id == "curated_swiss_pop" } to "Stressfreier Pop-Balsam für die Seele."
                )
            }
            q.contains("party") || q.contains("tanz") || q.contains("feiern") || q.contains("dance") -> {
                listOf(
                    all.first { it.id == "curated_bigfm" } to "Heiße Beats, Urban & Club-Sounds.",
                    all.first { it.id == "curated_sunshine_live" } to "Electronic Music Radio für echte Club-Vibes.",
                    all.first { it.id == "curated_90s90s" } to "90er Eurodance-Klassiker zum Abtanzen."
                )
            }
            else -> {
                listOf(
                    all.first { it.id == "curated_swr3" } to "Deutschlands beliebtester Pop- und Rock-Mix mit sympathischen Moderatoren.",
                    all.first { it.id == "curated_swiss_pop" } to "Komplett werbefreier Genuss aus der Schweiz mit wunderschöner Musik.",
                    all.first { it.id == "curated_rock_antenne" } to "Für Liebhaber von handgemachtem Rock und starken Riffs."
                )
            }
        }

        val suggestions = selected.map { (station, why) ->
            AiStationSuggestion(
                stationName = station.name,
                description = station.tags,
                whyItFits = why,
                streamUrl = station.streamUrl,
                tags = station.tags,
                country = station.country,
                matchedStation = station
            )
        }

        val message = "Hier sind persönliche Musik-Empfehlungen für $profileName, abgestimmt auf euren Moment:"

        return AiAssistantResponse(
            message = message,
            suggestions = suggestions,
            isAiGenerated = !isKeyMissing,
            isKeyMissing = isKeyMissing
        )
    }

    private fun fallbackSuggestionsForQuery(query: String): AiAssistantResponse {
        val all = CuratedStations.INITIAL_STATIONS
        val suggestions = all.take(3).map { station ->
            AiStationSuggestion(
                stationName = station.name,
                description = station.tags,
                whyItFits = "Beliebter Qualitätssender für jeden Moment.",
                streamUrl = station.streamUrl,
                tags = station.tags,
                country = station.country,
                matchedStation = station
            )
        }
        return AiAssistantResponse(
            message = "Hier sind passende Stationen für eure Auswahl:",
            suggestions = suggestions,
            isAiGenerated = true,
            isKeyMissing = false
        )
    }
}
