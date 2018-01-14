package me.paixao.videoplayer.activities;

import android.os.Bundle;
import android.widget.GridView;

import java.util.ArrayList;

import me.paixao.videoplayer.R;
import me.paixao.videoplayer.ui.adapters.ImageAdapter;

public class MainActivity extends BaseActivity {


    ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Pedro's Video Player");
    }

    @Override
    public void refresh() {
        imageAdapter = new ImageAdapter(MainActivity.this, getAllMedia(), new ArrayList<String>());

        final GridView grid = findViewById(R.id.gridview);
        grid.setAdapter(imageAdapter);

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
}
