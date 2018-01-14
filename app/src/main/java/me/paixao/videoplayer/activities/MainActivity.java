package me.paixao.videoplayer.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.paixao.videoplayer.R;
import me.paixao.videoplayer.db.models.Playlist;
import me.paixao.videoplayer.db.models.Playlist_Table;
import me.paixao.videoplayer.db.models.Video;
import me.paixao.videoplayer.events.DeletePlaylistEvent;
import me.paixao.videoplayer.events.EditPlaylistEvent;
import me.paixao.videoplayer.events.OpenPlaylistEvent;
import me.paixao.videoplayer.events.SetNewTitleEvent;
import me.paixao.videoplayer.events.StartCreatePlaylistEvent;
import me.paixao.videoplayer.ui.adapters.ImageAdapter;
import me.paixao.videoplayer.ui.adapters.PlaylistAdapter;

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

        updatePlaylistList();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_cancel:
                                cancelCreatePlaylist();
                                break;
                            case R.id.action_save:
                                askForPlaylistName();
                                break;
                        }
                        return true;
                    }
                });
        super.onCreate(savedInstanceState);
    }

    public void updatePlaylistList() {
        ListView listView = findViewById(R.id.lst_menu_items);

        List<Playlist> playlists = new Select().from(Playlist.class).orderBy(Playlist_Table.name, true).queryList();

        listView.setAdapter(new PlaylistAdapter(this, playlists));
    }

    public void cancelCreatePlaylist() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        imageAdapter.setSelectMode(false);
        imageAdapter.reset();
        slideDown(bottomNavigationView);
        setTitle(R.string.main_activity_title);
    }

    public void askForPlaylistName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please write a name for your playlist");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if (name == null || name.equals("")) {
                    toast("Please write a name for your playlist");
                    askForPlaylistName();
                } else {
                    Playlist pl = new Playlist();
                    pl.setName(name);
                    pl.save();
                    List<String> selectedVideos = imageAdapter.getCheckedItems();
                    List<Video> myVideos = new ArrayList();
                    for (String vid : selectedVideos) {
                        Video video = new Video();
                        video.setUri(vid);
                        video.setPlaylist(pl);
                        myVideos.add(video);
                    }
                    Video.saveAll(myVideos);
                    toast("Playlist " + name + " saved successfully!");
                    updatePlaylistList();
                    cancelCreatePlaylist();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void refresh() {
        if (imageAdapter == null) {
            imageAdapter = new ImageAdapter(MainActivity.this, getAllMedia(), new ArrayList<String>());

            final GridView grid = findViewById(R.id.gridview);
            grid.setAdapter(imageAdapter);
        }
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

    @Subscribe
    public void onOpenPlaylistEvent(final OpenPlaylistEvent event) {
        // doSomething with the event...
        toast("ABRI PL: " + ((Playlist) event.getModel()).getName());
    }

    @Subscribe
    public void onEditPlaylistEvent(final EditPlaylistEvent event) {
        // doSomething with the event...
        toast("Edit PL: " + ((Playlist) event.getModel()).getName());
    }

    @Subscribe
    public void onDeletePlaylistEvent(final DeletePlaylistEvent event) {
        // doSomething with the event...
        toast("DELETE PL: " + ((Playlist) event.getModel()).getName());
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
