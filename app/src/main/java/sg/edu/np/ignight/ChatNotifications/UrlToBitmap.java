package sg.edu.np.ignight.ChatNotifications;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.net.URL;
import java.util.concurrent.Callable;

// callable to retrieve bitmap image from url
public class UrlToBitmap implements Callable<Bitmap> {

    private URL url;

    public UrlToBitmap(URL url) {
        this.url = url;
    }

    @Override
    public Bitmap call() throws Exception {
        return BitmapFactory.decodeStream(url.openStream());
    }
}
