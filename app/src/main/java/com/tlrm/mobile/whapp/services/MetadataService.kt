package com.tlrm.mobile.whapp.services

import com.tlrm.mobile.whapp.api.MetadataApiService
import com.tlrm.mobile.whapp.util.exceptions.ServiceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MetadataService(
    private val metadataApiService: MetadataApiService,
    private val sessionService: SessionService
) {

    suspend fun configureMetadata() {
        withContext(Dispatchers.IO) {
            val call = metadataApiService.getMetadata()
            val response = call.execute()

            if (!response.isSuccessful) {
                throw ServiceException("Unable to get the metadata from server")
            }

            val metadata = response.body()

            val cartonLength = metadata!!.fieldDefinitions.first { it.code == "Carton" }.length
            val locationLength = metadata!!.fieldDefinitions.first { it.code == "Location" }.length

            sessionService.setStorageFieldDefinition(cartonLength, locationLength)
        }
    }

    fun getStorageFieldDefinition(): StorageFieldDefinition? {
        return sessionService.getStorageFieldDefinition();
    }

}