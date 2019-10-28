package bku.com.chatappkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var ToUser : User? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recycleview_chat_log.adapter = adapter

        ToUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = ToUser?.userName

       //setupDummyData()
        listenforMessage()

        send_button_chatlog.setOnClickListener{
            Log.d(TAG, "Click SEND Button")

            performSendMessage()
        }

    }


    class ChatMessage(val id : String, val text: String, val fromId: String, val toId: String, val timestamp : Long){
        constructor(): this("","","","",-1)
    }

    private fun listenforMessage(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = ToUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null){
                    Log.d(TAG, " ${chatMessage.fromId} - ${chatMessage.toId} - ${chatMessage.text}")


                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        Log.d(TAG, "From ID")
                        val currentuser = MessageActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentuser!!))
                    }
                    else{
                        Log.d(TAG, "To ID")
                        //val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                        // cho nay chac chan co van de khi hien tat ca du lieu
                        adapter.add(ChatToItem(chatMessage.text, ToUser!!))
                    }
                }

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun performSendMessage(){
        val text = text_message_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if(fromId == null) return
        // send message to firebase
        //val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val chatMessage = ChatMessage(ref.key!!,text, fromId, toId, System.currentTimeMillis()/1000)
        ref.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "Message from text box : ${ref.key}")
                    text_message_chat_log.text.clear()
                    recycleview_chat_log.scrollToPosition(adapter.itemCount - 1)
                }
        val Reref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        Reref.setValue(chatMessage)

        // lastest message

        val lastref = FirebaseDatabase.getInstance().getReference("/last-messages/$fromId/$toId")
        lastref.setValue(chatMessage)

        val lastReref = FirebaseDatabase.getInstance().getReference("/last-messages/$toId/$fromId")
        lastReref.setValue(chatMessage)
    }

//    private fun setupDummyData(){
//        val adapter = GroupAdapter<ViewHolder>()
//
//        adapter.add(ChatFromItem("From Message"))
//        adapter.add(ChatToItem("To text"))
//        adapter.add(ChatFromItem("From Message"))
//        adapter.add(ChatToItem("To text"))
//        adapter.add(ChatFromItem("From Message"))
//        adapter.add(ChatToItem("To text"))
//
//        recycleview_chat_log.adapter = adapter
//    }
}

class ChatFromItem(val text : String, val user:User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_chat_from_row.text = text
        val image = user.url_image
        val targetImageView = viewHolder.itemView.imageView_chat_from_row
        Picasso.get().load(image).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_chat_to_row.text = text
        val image = user.url_image
        val targetImageView = viewHolder.itemView.imageView_chat_to_row
        Picasso.get().load(image).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}
