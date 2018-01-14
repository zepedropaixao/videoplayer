package me.paixao.videoplayer.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import me.paixao.videoplayer.R;
import me.paixao.videoplayer.events.SetNewTitleEvent;
import me.paixao.videoplayer.events.StartCreatePlaylistEvent;
import me.paixao.videoplayer.ui.adapters.ImageAdapter;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ImageAdapter imageAdapter;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    boolean isCreatePlaylistMenuShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.main_activity_title);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        if (imageAdapter == null) {
            imageAdapter = new ImageAdapter(MainActivity.this, getAllMedia(), new ArrayList<String>());

            final GridView grid = findViewById(R.id.gridview);
            grid.setAdapter(imageAdapter);
        }

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_cancel:
                                imageAdapter.setSelectMode(false);
                                imageAdapter.reset();
                                slideDown(bottomNavigationView);
                                setTitle(R.string.main_activity_title);
                                break;
                            case R.id.action_save:
                                toast("GOING TO SAVE");

                                break;

                        }
                        return true;
                    }
                });
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void refresh() {


        /*final ListView videoList = findViewById(R.id.video_list);

        videoList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getAllMedia()));
        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String uri = (String) videoList.getAdapter().getItem(position);
                Intent intent = new Intent(_this, VideoPlayer.class);
                intent.putExtra("uri", uri);
                startActivity(intent);
            }
        });*/

    }

    @Override
    public void onStart() {
        super.onStart();
        app.bus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        app.bus.unregister(this);
    }

    @Subscribe
    public void onStartCreatePlaylistEvent(StartCreatePlaylistEvent event) {
        // doSomething with the event...
        if (!event.isWithError()) {
            imageAdapter.setSelectMode(true);
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            slideUp(bottomNavigationView);
        }
    }

    @Subscribe
    public void onSetNewTitleEvent(final SetNewTitleEvent event) {
        // doSomething with the event...
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(event.getMessage());
            }
        });
    }

    // slide the view from below itself to the current position
    public void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }
}
