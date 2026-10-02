package com.hocheol.chattingapp.chatdetail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hocheol.chattingapp.Key
import com.hocheol.chattingapp.R
import com.hocheol.chattingapp.databinding.ActivityChatDetailBinding
import com.hocheol.chattingapp.userlist.UserItem

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatDetailBinding
    private lateinit var chatDetailAdapter: ChatDetailAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""

    private val chatDetailItemList = mutableListOf<ChatDetailItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatDetailAdapter = ChatDetailAdapter()
        linearLayoutManager = LinearLayoutManager(applicationContext)

        chatRoomId = intent.getStringExtra(EXTRA_CHAT_ROOM_ID) ?: return
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: return
        myUserId = Firebase.auth.currentUser?.uid ?: ""

        Firebase.database.reference.child(Key.DB_USERS).child(myUserId).get().addOnSuccessListener {
            val myUserItem = it.getValue(UserItem::class.java)
            myUserName = myUserItem?.username ?: ""

            getOtherUserData()
        }

        binding.chatRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = chatDetailAdapter
        }

        chatDetailAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                linearLayoutManager.smoothScrollToPosition(
                    binding.chatRecyclerView,
                    null,
                    chatDetailAdapter.itemCount - 1
                )
            }
        })

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text?.toString()

            if (message.isNullOrEmpty()) {
                Toast.makeText(applicationContext, "빈 메세지를 전송할 수는 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newChatItem = ChatDetailItem(
                message = message,
                userId = myUserId
            )

            Firebase.database.reference.child(Key.DB_CHATS).child(chatRoomId).push().apply {
                newChatItem.chatId = key
                setValue(newChatItem)
            }

            val updates: MutableMap<String, Any> = hashMapOf(
                "${Key.DB_CHAT_ROOMS}/$myUserId/$otherUserId/lastMessage" to message,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/lastMessage" to message,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/chatRoomId" to chatRoomId,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/otherUserId" to myUserId,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/otherUserName" to myUserName,
            )

            Firebase.database.reference.updateChildren(updates)

            // A trusted backend sends notifications after authorizing this database event.
            // FCM server credentials must never be distributed with the application.

            binding.messageEditText.text?.clear()
        }
    }

    private fun getOtherUserData() {
        Firebase.database.reference.child(Key.DB_USERS).child(otherUserId).get().addOnSuccessListener {
            val otherUserItem = it.getValue(UserItem::class.java)
            chatDetailAdapter.otherUserItem = otherUserItem

            binding.sendButton.isEnabled = true
            getChatData()
        }
    }

    private fun getChatData() {
        Firebase.database.reference.child(Key.DB_CHATS).child(chatRoomId).addChildEventListener(
            object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatDetailItem = snapshot.getValue(ChatDetailItem::class.java) ?: return

                    chatDetailItemList.add(chatDetailItem)
                    chatDetailAdapter.submitList(chatDetailItemList.toMutableList())
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = Unit

                override fun onChildRemoved(snapshot: DataSnapshot) = Unit

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }
            }
        )
    }

    companion object {
        private const val TAG = "ChatDetailActivity"

        const val EXTRA_CHAT_ROOM_ID = "chatRoomId"
        const val EXTRA_OTHER_USER_ID = "otherUserId"
    }
}
