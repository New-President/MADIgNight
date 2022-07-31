package sg.edu.np.ignight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import sg.edu.np.ignight.ChatRequest.ChatRequestSentFragment;
import sg.edu.np.ignight.ChatRequest.ChatRequestReceivedFragment;

public class ChatRequestActivity extends AppCompatActivity {

    private String[] tabHeader = {"Received", "Sent"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_request);

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);

        ImageButton backButton = findViewById(R.id.chatRequestsBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
                finish();
            }
        });

        TabLayout tabLayout = findViewById(R.id.chatRequestsTabs);
        ViewPager2 viewPager = findViewById(R.id.chatRequestsViewPager);

        viewPager.setAdapter(new fragmentAdapter(this));
        viewPager.setCurrentItem(position);

        new TabLayoutMediator(
                tabLayout,
                viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(tabHeader[position]);
                    }
                }
        ).attach();
    }

    @Override
    public void onBackPressed() {  // override onBackPressed to go to main menu if activity is started from notification
        startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
        finish();
    }

    class fragmentAdapter extends FragmentStateAdapter {

        public fragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {  // initialize activity at respective fragment based on position received
            return (position == 0)?new ChatRequestReceivedFragment():new ChatRequestSentFragment();
        }

        @Override
        public int getItemCount() {
            return tabHeader.length;
        }
    }
}