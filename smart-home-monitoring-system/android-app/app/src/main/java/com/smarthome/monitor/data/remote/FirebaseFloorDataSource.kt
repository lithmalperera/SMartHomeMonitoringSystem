package com.smarthome.monitor.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smarthome.monitor.core.util.Constants
import com.smarthome.monitor.data.model.Floor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFloorDataSource @Inject constructor(
    private val db: FirebaseDatabase
) {
    fun observeFloors(): Flow<List<Floor>> = callbackFlow {
        val ref = db.getReference(Constants.DbPaths.floors())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val floors = mutableListOf<Floor>()
                for (child in snapshot.children) {
                    val floor = child.getValue(Floor::class.java)
                    if (floor != null) {
                        val remoteImageUrl = floor.imageUrl?.takeIf { it.startsWith("https://") }
                        floors.add(floor.copy(id = child.key ?: "", imageUrl = remoteImageUrl))
                    }
                }
                floors.sortBy { it.order }
                trySend(floors)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addFloor(name: String, order: Int, gridColumns: Int, gridRows: Int, imageUrl: String?) {
        val ref = db.getReference(Constants.DbPaths.floors()).push()
        val floorId = ref.key ?: throw IllegalStateException("Could not generate floor ID")
        val floor = Floor(
            id = floorId,
            name = name,
            order = order,
            gridColumns = gridColumns,
            gridRows = gridRows,
            imageUrl = imageUrl
        )
        ref.setValue(floor).await()
    }
}
