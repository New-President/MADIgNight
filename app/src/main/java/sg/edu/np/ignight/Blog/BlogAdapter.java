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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import sg.edu.np.ignight.R;

public class BlogAdapter extends RecyclerView.Adapter<BlogViewHolder> {

    private ArrayList<BlogObject> data;
    private Context c;

    public BlogAdapter(Context c, ArrayList<BlogObject> data){
        this.c = c;
        this.data = data;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_layout, parent, false);
        View item2= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_layout, parent, false);
        return new BlogViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String uid = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        FirebaseStorage storage = FirebaseStorage.getInstance();

        BlogObject blog = data.get(position);

        String blogID = blog.blogID;
        String description = blog.description;
        String location = blog.location;

        holder.desc.setText(description);
        holder.location.setText("@" + location);

        ImageView blogImage = holder.blogImg; //add fullscreen function

        blogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fullscreen
            }
        });
        ImageView likebutton = holder.likesButton; //add fullscreen function
        ImageView commentButton = holder.commentButton; //view profile

        int likes = blog.likes;
        int comments = blog.comments;
        holder.likes.setText(String.valueOf(likes));
        holder.comments.setText(String.valueOf(comments));


        likebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = database.getReference("user").child(uid).child("blog").child(blogID);
                if(blog.liked){
                    databaseReference.child("likes").setValue(blog.likes -= 1);
                    if (likes - 1 == -1){
                        holder.likes.setText(String.valueOf(0));
                    }
                    blog.liked = false;
                    databaseReference.child("liked").setValue(false);
                }
                else {
                    databaseReference.child("likes").setValue(blog.likes += 1);
                    blog.liked = true;
                    databaseReference.child("liked").setValue(true);
                    holder.likes.setText(String.valueOf(likes + 1));
                }
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go to comment activity
            }
        });

        StorageReference storageReference = storage.getReference("blog").child(uid).child(blog.imgID);
        try{
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

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}
