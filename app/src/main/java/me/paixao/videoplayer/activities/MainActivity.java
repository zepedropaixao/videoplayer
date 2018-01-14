package me.paixao.videoplayer.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

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

        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        updatePlaylistList();

        Button createNewPlaylist = findViewById(R.id.create_new_playlist);
        createNewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewPlaylist();
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });

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
                                if (imageAdapter.getCheckedItems().isEmpty()) {
                                    toast("Please select at least one video for your playlist");
                                } else {
                                    if (editUUID != null) {
                                        Playlist pl = new Select().from(Playlist.class).where(Playlist_Table.uuid.eq(editUUID)).querySingle();
                                        String name = pl.getName();
                                        pl.delete();
                                        saveNewPlayList(name);
                                        toast("Playlist " + name + " edited successfully!");
                                    } else {
                                        askForPlaylistName();
                                    }
                                }
                                break;
                        }
                        return true;
                    }
                });
        super.onCreate(savedInstanceState);
    }

    public void createNewPlaylist() {
        toast(R.string.please_select_the_videos_you_wish);
        imageAdapter.setSelectMode(true);
        app.bus.post(new StartCreatePlaylistEvent(false));
    }

    String editUUID = null;

    public void editPlaylist(ArrayList<String> selected) {
        toast(R.string.please_select_or_deselect);
        closeDrawer();

        imageAdapter.setSelectMode(true);
        imageAdapter.reset(selected);
        app.bus.post(new StartCreatePlaylistEvent(false));
    }

    public void updatePlaylistList() {
        final ListView listView = findViewById(R.id.lst_menu_items);
        new Select().from(Playlist.class)
                .orderBy(Playlist_Table.name, true)
                .async().queryListResultCallback(new QueryTransaction.QueryResultListCallback<Playlist>() {
            @Override
            public void onListQueryResult(QueryTransaction transaction, @NonNull final List<Playlist> playlists) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(new PlaylistAdapter(_this, playlists));
                    }
                });
            }
        }).execute();

    }

    public void cancelCreatePlaylist() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        imageAdapter.setSelectMode(false);
        imageAdapter.reset();
        slideDown(bottomNavigationView);
        editUUID = null;
        setTitle(R.string.main_activity_title);
    }

    public void askForPlaylistName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please write a name for your playlist");
        // Why are you setting message here when you are inflating custom view?
        // You need to add another TextView in xml if you want to set message here
        // Otherwise the message will not be shown
        // builder.setMessage("Do you want to\n"+""+"exit from app");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
        final AppCompatEditText input = (AppCompatEditText) view.findViewById(R.id.editText);
        builder.setView(view);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if (name == null || name.equals("")) {
                    toast("Please write a name for your playlist");
                    askForPlaylistName();
                } else {
                    saveNewPlayList(name);
                    toast("Playlist " + name + " saved successfully!");
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void saveNewPlayList(String name) {
        Playlist pl = new Playlist();
        pl.setName(name);
        pl.save();
        List<String> selectedVideos = imageAdapter.getCheckedItems();
        List<Video> myVideos = new ArrayList();
        int order = 0;
        for (String vid : selectedVideos) {
            Video video = new Video();
            video.setUri(vid);
            video.setPlaylist(pl);
            video.setOrder(order);
            order++;
            myVideos.add(video);
        }
        Video.saveAll(myVideos);
        updatePlaylistList();
        cancelCreatePlaylist();
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

    public void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Playlist pl = (Playlist) event.getModel();
                List<Video> vids = pl.getVideos();
                String uri = vids.get(0).getUri();
                Intent intent = new Intent(_this, VideoPlayer.class);
                intent.putExtra("uri", uri);
                intent.putExtra("playlist", pl.getUuid());
                startActivity(intent);
            }
        });

    }

    @Subscribe
    public void onEditPlaylistEvent(final EditPlaylistEvent event) {
        // doSomething with the event...
        toast("Edit PL: " + ((Playlist) event.getModel()).getName());
        Playlist pl = (Playlist) event.getModel();
        String name = pl.getName();

        editUUID = pl.getUuid();

        List<Video> vids = pl.getVideos();
        ArrayList<String> selected = new ArrayList<>();
        for (Video vid : vids)
            selected.add(vid.getUri());

        editPlaylist(selected);

        /*pl.delete();
        saveNewPlayList(name);
        toast("Playlist " + name + " edited successfully!");*/

    }

    @Subscribe
    public void onDeletePlaylistEvent(final DeletePlaylistEvent event) {
        // doSomething with the event...
        Playlist pl = (Playlist) event.getModel();
        pl.delete();
        updatePlaylistList();
        closeDrawer();
        toast(R.string.success_delete_playlist);

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
