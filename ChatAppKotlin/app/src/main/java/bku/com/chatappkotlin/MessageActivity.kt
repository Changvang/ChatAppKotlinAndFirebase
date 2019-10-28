package bku.com.chatappkotlin

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.last_message_row.view.*

class MessageActivity : AppCompatActivity() {

    companion object {
        var currentUser:User? = null
    }

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        //setupDummyRow()
        ListenForLastMess()

        fetchCurrentUser()

        VerifyUserLoggedIn()
    }

    class LastMess(val chatMessage: ChatLogActivity.ChatMessage) : Item<ViewHolder>(){
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.message_last_message_row.text = chatMessage.text

            val chatPartner: String
            if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                chatPartner = chatMessage.toId
            }
            else{
                chatPartner = chatMessage.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("users/$chatPartner")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(User::class.java)
                    viewHolder.itemView.user_textview_last_message_row.text = user!!.userName
                    Picasso.get().load(user.url_image).into(viewHolder.itemView.imageView_last_message_row)
                }
                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }

        override fun getLayout(): Int {
            return R.layout.last_message_row
        }
    }

    val lastMessagesMap = HashMap<String, ChatLogActivity.ChatMessage>()
    private fun refreshRecycleMessage(){
        adapter.clear()
        lastMessagesMap.values.forEach {
            adapter.add(LastMess(it))
        }
    }

    private fun ListenForLastMess(){


        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/last-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatLogActivity.ChatMessage::class.java)?: return
                lastMessagesMap[p0.key!!] = chatMessage
                refreshRecycleMessage()

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatLogActivity.ChatMessage::class.java)?: return
                lastMessagesMap[p0.key!!] = chatMessage
                refreshRecycleMessage()

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })




        recycleview_message.adapter = adapter
    }

//    private fun setupDummyRow(){
//        val adapter = GroupAdapter<ViewHolder>()
//
//        adapter.add(LastMess())
//        adapter.add(LastMess())
//        adapter.add(LastMess())
//        adapter.add(LastMess())
//
//        recycleview_message.adapter = adapter
//    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("MessageActivity", "Current User ${currentUser?.userName}" )
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private  fun VerifyUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_new_message ->{
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out->{
                // Dang xuat
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_resource, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
