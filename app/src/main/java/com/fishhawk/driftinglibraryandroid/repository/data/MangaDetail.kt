package com.fishhawk.driftinglibraryandroid.repository.data

data class TagGroup(
    val key: String,
    val value: List<String>
)

data class Collection(
    val title: String,
    val chapters: List<String>
)

data class MangaDetail(
    val id: String,
    val title: String,
    var thumb: String,
    val tags: List<TagGroup>,
    val collections: List<Collection>
)