package sg.edu.np.ignight;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

public class BlogAdapter extends RecyclerView.Adapter<BlogViewHolder> {


    public ArrayList<Blog> data;
    public Context c;

    public BlogAdapter(Context c, ArrayList<Blog> data){
        this.c = c;
        this.data = data;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_layout, parent, false);
        return new BlogViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        setupImageLoader();
        Blog blog = data.get(position);

        String username = blog.username;
        String description = blog.description;
        String device = blog.device;
        String location = blog.location;
        int likes = blog.likes;
        int comments = blog.comments;

        holder.username.setText(username);
        holder.desc.setText(description);
        holder.device.setText("Sent via " + device);
        holder.location.setText("@" + location);
        holder.likes.setText(String.valueOf(likes));
        holder.comments.setText(String.valueOf(comments));

        ImageView blogImage = holder.blogImg; //add fullscreen function
        ImageView profilePic = holder.profilePic; //view profile
        ImageView likebutton = holder.likesButton; //add fullscreen function
        ImageView commentButton = holder.commentButton; //view profile

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //find user

            }
        });

        blogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        likebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        int defaultImage = c.getResources().getIdentifier("@drawables/failed.jpg", null, c.getPackageName());
        int defaultImage2 = c.getResources().getIdentifier("@drawables/failed.jpg", null, c.getPackageName());

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        imageLoader.displayImage(blog.imgUrl, blogImage, options);
        imageLoader.displayImage(blog.profileUrl, profilePic, options);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void setupImageLoader(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                c)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
    }
}
