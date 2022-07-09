package sg.edu.np.ignight.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class mapViewPagerAdapter extends FragmentStateAdapter {

    public mapViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case (0):
                return new UserPreferredFragment();
            case (1):
                return new HotSpotsFragment();
            case (2):
                return new AllLocFragment();
            default:
                return new UserPreferredFragment();

        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
