package com.example.data.model

data class CountryItem(
    val code: String,
    val name: String,
    val englishName: String,
    val flag: String
)

data class GenreItem(
    val tag: String,
    val title: String,
    val icon: String
)

data class CountryGenreCombo(
    val id: String,
    val title: String,
    val subtitle: String,
    val country: CountryItem,
    val genre: GenreItem,
    val icon: String
)

object FilterPresets {

    val GERMANY = CountryItem(code = "DE", name = "Deutschland", englishName = "Germany", flag = "🇩🇪")
    val AUSTRIA = CountryItem(code = "AT", name = "Österreich", englishName = "Austria", flag = "🇦🇹")
    val SWITZERLAND = CountryItem(code = "CH", name = "Schweiz", englishName = "Switzerland", flag = "🇨🇭")
    val UK = CountryItem(code = "GB", name = "Großbritannien", englishName = "The United Kingdom", flag = "🇬🇧")
    val USA = CountryItem(code = "US", name = "USA", englishName = "The United States Of America", flag = "🇺🇸")
    val FRANCE = CountryItem(code = "FR", name = "Frankreich", englishName = "France", flag = "🇫🇷")
    val ITALY = CountryItem(code = "IT", name = "Italien", englishName = "Italy", flag = "🇮🇹")
    val SPAIN = CountryItem(code = "ES", name = "Spanien", englishName = "Spain", flag = "🇪🇸")
    val NETHERLANDS = CountryItem(code = "NL", name = "Niederlande", englishName = "Netherlands", flag = "🇳🇱")
    val POLAND = CountryItem(code = "PL", name = "Polen", englishName = "Poland", flag = "🇵🇱")
    val SWEDEN = CountryItem(code = "SE", name = "Schweden", englishName = "Sweden", flag = "🇸🇪")
    val IRELAND = CountryItem(code = "IE", name = "Irland", englishName = "Ireland", flag = "🇮🇪")
    val BELGIUM = CountryItem(code = "BE", name = "Belgien", englishName = "Belgium", flag = "🇧🇪")
    val NORWAY = CountryItem(code = "NO", name = "Norwegen", englishName = "Norway", flag = "🇳🇴")
    val DENMARK = CountryItem(code = "DK", name = "Dänemark", englishName = "Denmark", flag = "🇩🇰")
    val CANADA = CountryItem(code = "CA", name = "Kanada", englishName = "Canada", flag = "🇨🇦")
    val AUSTRALIA = CountryItem(code = "AU", name = "Australien", englishName = "Australia", flag = "🇦🇺")
    val PORTUGAL = CountryItem(code = "PT", name = "Portugal", englishName = "Portugal", flag = "🇵🇹")
    val GREECE = CountryItem(code = "GR", name = "Griechenland", englishName = "Greece", flag = "🇬🇷")
    val TURKEY = CountryItem(code = "TR", name = "Türkei", englishName = "Turkey", flag = "🇹🇷")
    val BRAZIL = CountryItem(code = "BR", name = "Brasilien", englishName = "Brazil", flag = "🇧🇷")
    val JAPAN = CountryItem(code = "JP", name = "Japan", englishName = "Japan", flag = "🇯🇵")
    val CROATIA = CountryItem(code = "HR", name = "Kroatien", englishName = "Croatia", flag = "🇭🇷")
    val CZECHIA = CountryItem(code = "CZ", name = "Tschechien", englishName = "Czech Republic", flag = "🇨🇿")
    val FINLAND = CountryItem(code = "FI", name = "Finnland", englishName = "Finland", flag = "🇫🇮")

    val POPULAR_COUNTRIES = listOf(
        GERMANY,
        AUSTRIA,
        SWITZERLAND,
        UK,
        USA,
        FRANCE,
        ITALY,
        SPAIN,
        NETHERLANDS,
        IRELAND,
        SWEDEN,
        POLAND,
        BELGIUM,
        NORWAY,
        DENMARK,
        CANADA,
        AUSTRALIA,
        PORTUGAL,
        GREECE,
        TURKEY,
        BRAZIL,
        JAPAN,
        CROATIA,
        CZECHIA,
        FINLAND
    )

    val GENRE_POP = GenreItem(tag = "pop", title = "Pop & Charts", icon = "🎵")
    val GENRE_ROCK = GenreItem(tag = "rock", title = "Rock & Alternative", icon = "🎸")
    val GENRE_80S = GenreItem(tag = "80s", title = "80er Hits", icon = "🕺")
    val GENRE_90S = GenreItem(tag = "90s", title = "90er Hits", icon = "📼")
    val GENRE_SCHLAGER = GenreItem(tag = "schlager", title = "Schlager & Kult", icon = "🍻")
    val GENRE_JAZZ = GenreItem(tag = "jazz", title = "Jazz & Soul", icon = "🎷")
    val GENRE_CLASSICAL = GenreItem(tag = "classical", title = "Klassik", icon = "🎻")
    val GENRE_ELECTRONIC = GenreItem(tag = "electronic", title = "Elektronisch & EDM", icon = "🎧")
    val GENRE_AMBIENT = GenreItem(tag = "ambient", title = "Chillout & Relax", icon = "🌿")
    val GENRE_NEWS = GenreItem(tag = "news", title = "Nachrichten & Info", icon = "📰")
    val GENRE_METAL = GenreItem(tag = "metal", title = "Metal & Heavy", icon = "🤘")
    val GENRE_HIPHOP = GenreItem(tag = "hiphop", title = "Hip-Hop & Urban", icon = "🎤")
    val GENRE_OLDIES = GenreItem(tag = "oldies", title = "Oldies & 60s/70s", icon = "📻")
    val GENRE_COUNTRY = GenreItem(tag = "country", title = "Country & Folk", icon = "🤠")
    val GENRE_BLUES = GenreItem(tag = "blues", title = "Blues", icon = "🎺")
    val GENRE_REGGAE = GenreItem(tag = "reggae", title = "Reggae", icon = "🌴")
    val GENRE_CHANSON = GenreItem(tag = "chanson", title = "Chanson", icon = "🥐")
    val GENRE_LOFI = GenreItem(tag = "lofi", title = "Lo-Fi & Beats", icon = "☕")
    val GENRE_DISCO = GenreItem(tag = "disco", title = "Disco & Funk", icon = "🪩")

    val POPULAR_GENRES = listOf(
        GENRE_POP,
        GENRE_ROCK,
        GENRE_80S,
        GENRE_90S,
        GENRE_SCHLAGER,
        GENRE_JAZZ,
        GENRE_CLASSICAL,
        GENRE_ELECTRONIC,
        GENRE_AMBIENT,
        GENRE_NEWS,
        GENRE_METAL,
        GENRE_HIPHOP,
        GENRE_OLDIES,
        GENRE_COUNTRY,
        GENRE_BLUES,
        GENRE_REGGAE,
        GENRE_CHANSON,
        GENRE_LOFI,
        GENRE_DISCO
    )

    // Curated Popular Country + Genre Combinations for fast 1-tap filtering
    val COMBINATION_SUGGESTIONS = listOf(
        CountryGenreCombo(
            id = "combo_de_rock",
            title = "Deutscher Rock",
            subtitle = "Rock Antenne, Radio BOB! & Deutschrock",
            country = GERMANY,
            genre = GENRE_ROCK,
            icon = "🇩🇪🎸"
        ),
        CountryGenreCombo(
            id = "combo_de_schlager",
            title = "Deutscher Schlager",
            subtitle = "BR Schlager, SWR4 & die besten Schlager",
            country = GERMANY,
            genre = GENRE_SCHLAGER,
            icon = "🇩🇪🍻"
        ),
        CountryGenreCombo(
            id = "combo_ch_pop",
            title = "Schweizer Pop",
            subtitle = "Radio Swiss Pop & werbefreie Hits",
            country = SWITZERLAND,
            genre = GENRE_POP,
            icon = "🇨🇭🎵"
        ),
        CountryGenreCombo(
            id = "combo_at_pop",
            title = "Austropop & Austria",
            subtitle = "Hitradio Ö3, Radio Wien & Alpen-Hits",
            country = AUSTRIA,
            genre = GENRE_POP,
            icon = "🇦🇹🏔️"
        ),
        CountryGenreCombo(
            id = "combo_gb_rock",
            title = "Britpop & UK Rock",
            subtitle = "BBC Radio, Absolute Rock & UK Indie",
            country = UK,
            genre = GENRE_ROCK,
            icon = "🇬🇧🎸"
        ),
        CountryGenreCombo(
            id = "combo_us_jazz",
            title = "US Jazz & Soul",
            subtitle = "New York Jazz, Motown & Soul-Klassiker",
            country = USA,
            genre = GENRE_JAZZ,
            icon = "🇺🇸🎷"
        ),
        CountryGenreCombo(
            id = "combo_us_country",
            title = "US Country",
            subtitle = "Nashville, Americana & Bluegrass",
            country = USA,
            genre = GENRE_COUNTRY,
            icon = "🇺🇸🤠"
        ),
        CountryGenreCombo(
            id = "combo_it_pop",
            title = "Italienische Hits",
            subtitle = "Radio Italia, RAI & Sommerlaune",
            country = ITALY,
            genre = GENRE_POP,
            icon = "🇮🇹🍝"
        ),
        CountryGenreCombo(
            id = "combo_fr_pop",
            title = "Französisches Chanson",
            subtitle = "Nostalgie Chansons & Paris Pop",
            country = FRANCE,
            genre = GENRE_CHANSON,
            icon = "🇫🇷🥐"
        ),
        CountryGenreCombo(
            id = "combo_es_pop",
            title = "Latino & Spanisch",
            subtitle = "Los 40, Flamenco & Sommerrhythmen",
            country = SPAIN,
            genre = GENRE_POP,
            icon = "🇪🇸💃"
        ),
        CountryGenreCombo(
            id = "combo_global_chill",
            title = "Global Chillout",
            subtitle = "SomaFM, Lounge & stressfreie Klänge",
            country = GERMANY, // or fallback
            genre = GENRE_AMBIENT,
            icon = "🌿🧘"
        ),
        CountryGenreCombo(
            id = "combo_de_80s",
            title = "80er Kult aus Deutschland",
            subtitle = "80s80s, Synthpop & Neue Deutsche Welle",
            country = GERMANY,
            genre = GENRE_80S,
            icon = "🇩🇪🕺"
        ),
        CountryGenreCombo(
            id = "combo_de_classic",
            title = "Klassik Meisterwerke",
            subtitle = "Klassik Radio, BR Klassik & Orchestersound",
            country = GERMANY,
            genre = GENRE_CLASSICAL,
            icon = "🇩🇪🎻"
        ),
        CountryGenreCombo(
            id = "combo_jp_lofi",
            title = "Japan Lo-Fi & City Pop",
            subtitle = "Tokio Beats, City Pop & Entspannung",
            country = JAPAN,
            genre = GENRE_LOFI,
            icon = "🇯🇵☕"
        ),
        CountryGenreCombo(
            id = "combo_br_rock",
            title = "Brasilien Rock & Hits",
            subtitle = "89 A Rádio Rock, Kiss FM & Brasil Rock",
            country = BRAZIL,
            genre = GENRE_ROCK,
            icon = "🇧🇷🎸"
        )
    )

    // Popular Search Tags / Suggestions
    val TRENDING_SEARCH_SUGGESTIONS = listOf(
        "89 FM",
        "Kiss FM",
        "Brasilien",
        "SWR3",
        "Rock Antenne",
        "80s80s",
        "Radio Swiss Pop",
        "Bayern 3",
        "1LIVE",
        "Klassik Radio",
        "Deutschlandfunk",
        "BR Schlager",
        "WDR 2",
        "RADIO BOB!",
        "sunshine live",
        "bigFM",
        "Jazz",
        "Schlager",
        "Oldies",
        "Chillout"
    )
}
