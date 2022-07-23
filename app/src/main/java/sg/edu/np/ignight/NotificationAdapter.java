package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import android.content.Context;
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
    Context c;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    // Get the current user
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    //get the current user's UID
    String Uid = user.getUid();

    public NotificationAdapter(Context c, ArrayList<String> likedUser, ArrayList<UserObject> userList, ArrayList<BlogObject> blogList){
        this.c = c;
        this.likedUser = likedUser;
        this.userList = userList;
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = null;
        item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_layout, null, false);

        return new NotificationViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        String liked = likedUser.get(position);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseUser.getUid();

        for (int i = 0; i < blogList.size(); i++){
            BlogObject blog = blogList.get(i);
            String blogID = blog.blogID;
/*
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("user").child(uid).child("blog").child(blogID);
*/
            ImageView blogImage = holder.blogImage;
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

        for (int j = 0; j < userList.size(); j++){
            UserObject user = userList.get(j);
            Log.d("TAG","Hello1: "+ liked);
            if (user.getUid().equals(liked)){
                Log.d("TAG","Hello1: "+ user.getUsername());
                holder.description.setText(user.getUsername() + " liked your blog.");

                ImageView profile = holder.profile;

                Glide.with(c).load(user.getProfilePicUrl()).placeholder(R.drawable.ic_baseline_image_24).into(profile);

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
                /*try{
                    StorageReference storageReference = storage.getReference("profilePicture").child(uid).child(user.getProfilePicUrl());
                    File localfile = File.createTempFile("tempfile", ".png");
                    storageReference.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                            profile.setImageBitmap(bitmap);
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
                }*/

            }
        }



        String username = "";
        /*Log.e(TAG, "Hello: " + data.size());*/
        /*for (int i = 0; i< userList.size(); i++){
            UserObject tempUser = userList.get(i);
            Log.e(TAG, "Hello: " + tempUser.getUid());
            for (int j = 0; j<context.likedUsersList.size(); j++){
                Log.e(TAG, "Hello1: " + context.likedUsersList.get(j));
                if(tempUser.getUid().equals(context.likedUsersList.get(j))){
                    username = tempUser.getUsername();
                    BlogCommentObject blo = new BlogCommentObject(username, "comments", "hi");
                    bloList.add(blo);
                }
            }
        }*/







        /*ArrayList<String> userLikedList = context.likedUsersList;
        for (int i = 0; i<=userLikedList.size(); i++){
            String userUid = userLikedList.get(i);
            for (int j = 0; j <= userList.size(); i++){
                UserObject tempUser = userList.get(j);
                if(userUid.equals(tempUser.getUid())){
                    holder.description.setText(tempUser.getUsername());
                }
            }

        }*/

    }

    @Override
    public int getItemCount() {
        return likedUser.size();
    }
}
