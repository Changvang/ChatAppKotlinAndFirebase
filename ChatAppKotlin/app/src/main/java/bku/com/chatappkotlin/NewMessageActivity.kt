package bku.com.chatappkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

//        val adapter = GroupAdapter<ViewHolder>()
//
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//
//        recycler_view_new_message.adapter = adapter

        fetchUserData()
    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUserData(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach{
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if(user != null){
                        adapter.add(UserItem(user))
                    }
                }


                adapter.setOnItemClickListener{item, view ->

                    val useritem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY,useritem.user)
                    startActivity(intent)

                    finish()
                }

                recycler_view_new_message.adapter = adapter
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.user_textview_new_message.text = user.userName
        Picasso.get().load(user.url_image).into(viewHolder.itemView.user_imageview_new_message)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}
