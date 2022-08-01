package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.Tag;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.ArrayTable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.internal.bind.ArrayTypeAdapter;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import sg.edu.np.ignight.Objects.BlogObject;
import sg.edu.np.ignight.Objects.UserObject;

public class NotificationAdapter
                extends RecyclerView.Adapter<NotificationViewHolder> {

    public static ArrayList<String> likedUser;
    public static ArrayList<UserObject> userList;
    public static ArrayList<BlogObject> blogList;
    public static ArrayList<LikedCommentObject> likedCommentList;
    Context c;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    // Get the current user
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    //get the current user's UID
    String Uid = user.getUid();

    public NotificationAdapter(Context c, ArrayList<LikedCommentObject> likedCommentList, ArrayList<UserObject> userList, ArrayList<BlogObject> blogList){
        this.c = c;
        this.likedCommentList = likedCommentList;
        this.userList = userList;
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = null;
        item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_layout, parent, false);

        return new NotificationViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        LikedCommentObject likedComment = likedCommentList.get(position);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseUser.getUid();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        try{
            // Display the user's blog
            StorageReference storageReference = storage.getReference("blog").child(uid).child(likedComment.imgID);
            File localfile = File.createTempFile("tempfile", ".png");
            storageReference.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                    holder.blogImage.setImageBitmap(bitmap);
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
        for(int i = 0; i<blogList.size(); i++){
            BlogObject blog = blogList.get(i);
            if (blog.imgID == likedComment.imgID){
                // when user clicks on the blog image, bring the user to the blog activity / comment activity
                holder.blogImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Intent blogActivity = new Intent(c, BlogActivity.class);
                            Intent commentActivity = new Intent(c, CommentSectionActivity.class);
                            //Bring user to the blog activity
                            if (likedComment.liked){
                                c.startActivity(blogActivity);
                            }
                            // Bring user to the comment activity
                            else{
                                // pass data back to commentActivity so that the page could load
                                commentActivity.putExtra("uid", uid);
                                commentActivity.putExtra("blogID", blog.blogID);
                                commentActivity.putExtra("imgID", likedComment.imgID);
                                c.startActivity(commentActivity);
                            }
                        }
                    }
                });
            }
        }



        for (int j = 0; j < userList.size(); j++){
            UserObject user = userList.get(j);
            if (user.getUid().equals(likedComment.userUID)){
                // If the user liked the blog, set the text to display "liked you blog" with the username of the person who liked the blog
                if (likedComment.liked){
                    holder.description.setText(user.getUsername() + " liked your blog.");
                }
                // If the user commented on the blog, set the text to display "commented you blog" with the username of the person who commented on the blog
                else{
                    holder.description.setText(user.getUsername() + " commented on your blog: " + likedComment.content);
                }


                ImageView profile = holder.profile;
                // Set the profile picture of the user who liked or commented on the blog
                Glide.with(c).load(user.getProfilePicUrl()).placeholder(R.drawable.ic_baseline_image_24).into(profile);

                // Enlarge the picture when clicked.
                profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GenericDraweeHierarchyBuilder hierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(c.getResources())
                                .setFailureImage(R.drawable.ic_baseline_error_outline_24)
                                .setProgressBarImage(new ProgressBarDrawable())
                                .setPlaceholderImage(R.drawable.ic_baseline_image_24);

                        new ImageViewer.Builder(view.getContext(), Collections.singletonList(user.getProfilePicUrl()))
                                .setStartPosition(0)
                                .hideStatusBar(false)
                                .allowZooming(true)
                                .allowSwipeToDismiss(true)
                                .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
                                .show();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return likedCommentList.size();
    }
}
