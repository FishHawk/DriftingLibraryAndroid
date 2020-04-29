package com.fishhawk.driftinglibraryandroid.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.fishhawk.driftinglibraryandroid.repository.service.RemoteLibraryService
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail

class Repository(private val url: String) {
    private val remoteLibraryService: RemoteLibraryService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        remoteLibraryService = retrofit.create(RemoteLibraryService::class.java)
    }

    suspend fun getMangaList(lastId: String, filter: String): Result<List<MangaSummary>> {
        return try {
            remoteLibraryService.getMangaList(lastId, filter).let {
                for (s in it) {
                    s.thumb = "${url}image/${s.id}/${s.thumb}"
                }
                Result.Success(it)
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun getMangaDetail(id: String): Result<MangaDetail> {
        return try {
            remoteLibraryService.getMangaDetail(id).let {
                it.thumb = "${url}image/${it.id}/${it.thumb}"
                Result.Success(it)
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }

    suspend fun getChapterContent(
        id: String,
        collection: String,
        chapter: String
    ): Result<List<String>> {
        return try {
            remoteLibraryService.getChapterContent(id, collection, chapter).let {
                Result.Success(it.map { "${url}image/$id/$collection/$chapter/$it" })
            }
        } catch (he: Throwable) {
            Result.Error(he)
        }
    }
}
