package sg.edu.np.ignight.Blog;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import sg.edu.np.ignight.CommentSectionActivity;
import sg.edu.np.ignight.CreateBlogActivity;
import sg.edu.np.ignight.Objects.BlogObject;
import sg.edu.np.ignight.Objects.UserObject;
import sg.edu.np.ignight.R;
import sg.edu.np.ignight.Notification.SendBlogNotification;

public class BlogAdapter extends RecyclerView.Adapter<BlogViewHolder> {

    private ArrayList<BlogObject> data;
    private Context c;
    private UserObject userObject;
    private Boolean canEdit;
    public String token;

    public BlogAdapter(Context c, ArrayList<BlogObject> data, UserObject userObject, Boolean canEdit){
        this.c = c;
        this.data = data;
        this.userObject = userObject;
        this.canEdit = canEdit;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_layout, parent, false);
        return new BlogViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        BlogObject blog = data.get(position);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        String uid;
        if (userObject != null){
            uid = userObject.getUid();
        }
        else{
            uid = firebaseUser.getUid();
        }

        String blogID = blog.blogID;
        String description = blog.description;
        String location = blog.location;

        holder.desc.setText(description);
        holder.location.setText("@" + location);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("user").child(uid).child("blog").child(blogID);


        ImageView likebutton = holder.likesButton; // like
        ImageView commentButton = holder.commentButton; //comment
        ImageView editBlogButton = holder.editBlogButton; //edit blog
        ImageView blogImage = holder.blogImg; //blog image

        ImageView editBlogBtn = holder.editBlogButton;
        if (canEdit){
            editBlogBtn.setVisibility(View.VISIBLE);
        }
        else{
            editBlogBtn.setVisibility(View.GONE);
        }

        if (blog.likedUsersList.contains(firebaseUser.getUid())){
            likebutton.setBackgroundResource(R.drawable.heart);
        }
        else{
            likebutton.setBackgroundResource(R.drawable.heartwithhole);
        }

        int likes = blog.likes;
        int comments = blog.comments;
        holder.likes.setText(String.valueOf(likes));
        holder.comments.setText(String.valueOf(comments));

        // Like a photo and store the number of likes in the firebase
        likebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (blog.likedUsersList.contains(firebaseUser.getUid())) {
                    blog.likes = ((blog.likes - 1) <= 0)?0:(blog.likes -= 1);
                    databaseReference.child("likedUsersList").child(firebaseUser.getUid()).setValue(false);
                    databaseReference.child("likes").setValue(blog.likes);
                    holder.likes.setText(String.valueOf(blog.likes));
                    likebutton.setBackgroundResource(R.drawable.heartwithhole);
                }
                else {
                    blog.likes += 1;
                    databaseReference.child("likedUsersList").child(firebaseUser.getUid()).setValue(true);
                    databaseReference.child("likes").setValue(blog.likes);
                    holder.likes.setText(String.valueOf(blog.likes));
                    likebutton.setBackgroundResource(R.drawable.heart);
                    pushNotification(uid, blogID, "liked");
                }
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentSection = new Intent(c, CommentSectionActivity.class);
                commentSection.putExtra("uid", uid);
                commentSection.putExtra("blogID", blogID);
                commentSection.putExtra("imgID", blog.imgID);
                commentSection.putExtra("numOfComments", blog.comments);
                c.startActivity(commentSection);
            }
        });

        editBlogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editBlogPage = new Intent(c, CreateBlogActivity.class);
                editBlogPage.putExtra("fromEdit", true);
                editBlogPage.putExtra("blogObject", (Serializable) blog);
                c.startActivity(editBlogPage);
            }
        });

        try{
            StorageReference storageReference = storage.getReference("blog").child(uid).child(blog.imgID);
            File localfile = File.createTempFile("tempfile", ".png");
            storageReference.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                    blogImage.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(c, "Failed to retrieve blogs", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception ex){
            Log.d("Load Image Error", "Failed to load image");
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void pushNotification(String userUID, String blogID, String message) {

        DatabaseReference myRef = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        myRef.child("user").child(userUID).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fcmToken = snapshot.getValue().toString();
                    SendBlogNotification sender = new SendBlogNotification(fcmToken, FirebaseAuth.getInstance().getUid(), "hi", "hi", c);
                    sender.sendNotification();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    /*private void sendNotification(String uid){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("user").child(uid).child("fcmToken");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                token = snapshot.getValue().toString();
                String title = "Liked message";
                String body = "John";


                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://fcm.googleapis.com/fcm/send/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                BlogApi api = retrofit.create(BlogApi.class);
                Log.d("TAG", "Hello" + token);
                Call<ResponseBody> call = api.sendNotification(token, title, body);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        *//*try{
                           Toast.makeText(c, response.body().string(), Toast.LENGTH_SHORT).show();
                        }catch(IOException e){
                            e.printStackTrace();
                        }*//*
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }*/

}
