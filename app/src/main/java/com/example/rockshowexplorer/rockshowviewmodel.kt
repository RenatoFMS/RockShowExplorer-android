package com.example.rockshowexplorer

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RockShowViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("rock_shows")

    private val _shows = MutableStateFlow<List<RockShow>>(emptyList())
    val shows: StateFlow<List<RockShow>> = _shows

    init {
        fetchShows()
    }

    private fun fetchShows() {
        collectionRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val showList = snapshot.documents.mapNotNull { it.toObject(RockShow::class.java)?.copy(id = it.id) }
                _shows.value = showList
            }
        }
    }

    fun addShow(show: RockShow) {
        val newDoc = collectionRef.document()
        newDoc.set(show.copy(id = newDoc.id))
    }

    fun deleteShow(showId: String) {
        collectionRef.document(showId).delete()
    }

    fun updateShow(show: RockShow) {
        collectionRef.document(show.id).set(show)
    }
}
