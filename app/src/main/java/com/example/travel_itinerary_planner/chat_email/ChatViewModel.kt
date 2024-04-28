import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_itinerary_planner.chat_email.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val messagesLiveData = MutableLiveData<List<Message>>()
    private val scrollToBottomEvent = MutableLiveData<Boolean>()

    private var allowScrollToBottom = true
    init {
        startDataRefresh()
    }

    fun getMessages(): LiveData<List<Message>> {
        return messagesLiveData
    }
    fun getScrollToBottomEvent(): LiveData<Boolean> {
        return scrollToBottomEvent
    }
    private fun startDataRefresh() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                fetchMessages()
                delay(1_000)
            }
        }
    }

    private fun fetchMessages() {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        firestore.collection("Message")

            .orderBy("MessageDate", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val messagesList = mutableListOf<Message>()
                for (document in documents) {
                    val message = document.toObject(Message::class.java)
                    messagesList.add(message)
                }
                messagesLiveData.postValue(messagesList)
                if (allowScrollToBottom) {
                    scrollToBottomEvent.postValue(true)
                }
            }
            .addOnFailureListener { exception ->

            }
    }


}
