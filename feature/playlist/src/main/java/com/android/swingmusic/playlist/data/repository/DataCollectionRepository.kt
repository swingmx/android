package com.android.swingmusic.playlist.data.repository

import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.network.data.api.service.NetworkApiService
import com.android.swingmusic.network.data.dto.CollectionItemDto
import com.android.swingmusic.network.data.dto.CollectionItemRequestDto
import com.android.swingmusic.network.data.dto.CreateCollectionRequestDto
import com.android.swingmusic.playlist.domain.Collection
import com.android.swingmusic.playlist.domain.CollectionRepository
import com.android.swingmusic.playlist.domain.CollectionTarget
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class DataCollectionRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : CollectionRepository {

    override suspend fun getCollections(): Resource<List<Collection>> = request("Couldn't load collections") {
        networkApiService.getCollections("${baseUrl()}collections", bearerToken()).mapNotNull { dto ->
            val id = dto.id ?: return@mapNotNull null
            Collection(
                id = id,
                name = dto.name.orEmpty(),
                description = dto.extra?.description.orEmpty(),
                itemCount = dto.items.orEmpty().size
            )
        }
    }

    override suspend fun createCollection(name: String, item: CollectionTarget): Resource<Unit> =
        request("Couldn't create collection") {
            networkApiService.createCollection(
                url = "${baseUrl()}collections",
                request = CreateCollectionRequestDto(
                    name = name,
                    description = "",
                    items = listOf(CollectionItemDto(type = item.type.value, hash = item.hash))
                ),
                bearerToken = bearerToken()
            )
            Unit
        }

    override suspend fun addToCollection(collectionId: Int, item: CollectionTarget): Resource<Unit> =
        request("Couldn't add to collection") {
            networkApiService.addCollectionItem(
                url = "${baseUrl()}collections/$collectionId/items",
                request = CollectionItemRequestDto(CollectionItemDto(type = item.type.value, hash = item.hash)),
                bearerToken = bearerToken()
            )
            Unit
        }

    private suspend fun baseUrl() = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl() ?: ""

    private suspend fun bearerToken() =
        "Bearer ${AuthTokenHolder.accessToken ?: authRepository.getAccessToken()}"

    private suspend fun <T> request(errorMessage: String, block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            // The server answers 400 with {"error": "items already in collection"} for duplicates
            val serverError = runCatching {
                JSONObject(e.response()?.errorBody()?.string().orEmpty()).optString("error")
            }.getOrNull().orEmpty()
            val message = if (serverError.contains("already", ignoreCase = true)) {
                "Already in this collection"
            } else {
                errorMessage
            }
            Resource.Error(message = message)
        } catch (e: Exception) {
            Timber.tag("CollectionRepository").e(e)
            Resource.Error(message = errorMessage)
        }
}
