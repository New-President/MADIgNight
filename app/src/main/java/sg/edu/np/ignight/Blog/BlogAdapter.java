package sg.edu.np.ignight.Blog;

import android.content.Context;
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
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import sg.edu.np.ignight.Objects.BlogObject;
import sg.edu.np.ignight.Objects.UserObject;
import sg.edu.np.ignight.R;

public class BlogAdapter extends RecyclerView.Adapter<BlogViewHolder> {

    private ArrayList<BlogObject> data;
    private Context c;
    private UserObject userObject;
    public BlogAdapter(Context c, ArrayList<BlogObject> data, UserObject userObject){
        this.c = c;
        this.data = data;
        this.userObject = userObject;
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
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
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
                .getReference("users").child(uid).child("blog").child(blogID);

        ImageView blogImage = holder.blogImg; //add fullscreen function

        blogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fullscreen
            }
        });
        ImageView likebutton = holder.likesButton; //add fullscreen function
        ImageView commentButton = holder.commentButton; //view profile

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


        likebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getLikedUsersList(uid, blogID, blog);

                databaseReference.child("likedUsersList").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean liked = snapshot.getValue(Boolean.class);
                        Log.d("likedstate", liked.toString());
                        if(blog.likedUsersList.contains(firebaseUser.getUid()) && liked){
                            databaseReference.child("likes").setValue(blog.likes -= 1);

                            holder.likes.setText(String.valueOf(likes - 1));

                            blog.likedUsersList.remove(firebaseUser.getUid());
                            databaseReference.child("likedUsersList").child(firebaseUser.getUid()).setValue(false);
                            likebutton.setBackgroundResource(R.drawable.heartwithhole);
                        }
                        else {
                            databaseReference.child("likes").setValue(blog.likes += 1);
                            blog.likedUsersList.add(firebaseUser.getUid());
                            holder.likes.setText(String.valueOf(likes + 1));
                            databaseReference.child("likedUsersList").child(firebaseUser.getUid()).setValue(true);
                            likebutton.setBackgroundResource(R.drawable.heart);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go to comment activity
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
                    Toast.makeText(c, "Failed to retrieve blogs", Toast.LENGTH_LONG).show();
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

    private void getLikedUsersList(String uid, String blogID, BlogObject blog) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference databaseReference = database.getReference("user").child(uid).child("blog").child(blogID).child("likedUsersList");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    for (DataSnapshot likedUserSnapshot : snapshot.getChildren()) {
                        blog.likedUsersList.add(likedUserSnapshot.getKey().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
