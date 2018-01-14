package me.paixao.videoplayer.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import me.paixao.videoplayer.App;
import me.paixao.videoplayer.R;
import me.paixao.videoplayer.db.models.Playlist;
import me.paixao.videoplayer.events.DeletePlaylistEvent;
import me.paixao.videoplayer.events.EditPlaylistEvent;
import me.paixao.videoplayer.events.OpenPlaylistEvent;

public class PlaylistAdapter extends BaseAdapter {

    private Context context;
    private List<Playlist> data;
    private static LayoutInflater inflater = null;

    public PlaylistAdapter(Context context, List<Playlist> data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.playlist_row, null);
        TextView text = vi.findViewById(R.id.text);
        text.setText(data.get(position).getName());

        // UI Events when clicking Row, Edit or Delete buttons
        vi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getInstance().bus.post(new OpenPlaylistEvent(data.get(position)));
            }
        });

        ImageButton edit = vi.findViewById(R.id.edit_playlist);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getInstance().bus.post(new EditPlaylistEvent(data.get(position)));
            }
        });

        ImageButton del = vi.findViewById(R.id.delete_playlist);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getInstance().bus.post(new DeletePlaylistEvent(data.get(position)));
            }
        });
        return vi;
    }
}
