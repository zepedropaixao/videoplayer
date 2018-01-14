package me.paixao.videoplayer.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.util.ArrayList;

import me.paixao.videoplayer.App;
import me.paixao.videoplayer.R;
import me.paixao.videoplayer.activities.VideoPlayer;
import me.paixao.videoplayer.events.SetNewTitleEvent;
import me.paixao.videoplayer.events.StartCreatePlaylistEvent;
import me.paixao.videoplayer.ui.helpers.GlideApp;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class VideoGridAdapter extends BaseAdapter {

    private boolean selectMode = false;
    private ArrayList<String> mList;
    private ArrayList<String> mSelectedList;
    private LayoutInflater mInflater;
    private Context mContext;

    public VideoGridAdapter(Context context, ArrayList<String> imageList) {
        this(context, imageList, new ArrayList<String>());
    }

    public VideoGridAdapter(Context context, ArrayList<String> imageList, ArrayList<String> selectedImageList) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        if (selectedImageList != null) {
            mSelectedList = selectedImageList;
        } else {
            mSelectedList = new ArrayList<>();
        }
        mList = new ArrayList<>();
        this.mList = imageList;
    }

    public void removeItem(int position) {
        if (mList.size() > position) {
            try {
                mSelectedList.remove(mList.get(position));
                mList.remove(mList.get(position));
                notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addItem(String uri) {
        mList.add(0, uri);
        if (!mSelectedList.contains(uri))
            mSelectedList.add(uri);
        notifyDataSetChanged();
    }

    public ArrayList<String> getCheckedItems() {
        return mSelectedList;
    }

    public ArrayList<String> getAllItems() {
        return mList;
    }

    public void reset() {
        Handler handler = new Handler(Looper.getMainLooper());
        mSelectedList = new ArrayList<String>();
        final Runnable r = new Runnable() {
            public void run() {
                notifyDataSetChanged();
            }
        };
        handler.post(r);
    }

    public void reset(ArrayList<String> selected) {
        Handler handler = new Handler(Looper.getMainLooper());
        mSelectedList = selected;
        final Runnable r = new Runnable() {
            public void run() {
                notifyDataSetChanged();
            }
        };
        handler.post(r);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final int mPosition = position;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_multiphoto_item, null);
        }

        final CheckBox mCheckBox = convertView.findViewById(R.id.checkBox1);
        final ImageView imageView = convertView.findViewById(R.id.imageView1);

        final String file_location = mList.get(position);
        String url;
        if (file_location.startsWith("//web")
                || file_location.startsWith("file://")
                || file_location.startsWith("http://")
                || file_location.startsWith("https://")) {
            url = file_location;
        } else {
            url = "file://" + file_location;
        }

        // Long click listener to start creating a new playlist
        mCheckBox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                App.getInstance().toast(R.string.please_select_the_videos_you_wish);
                if (!selectMode)
                    selectMode = true;
                mCheckBox.performClick();
                App.getInstance().bus.post(new StartCreatePlaylistEvent());
                return true;
            }
        });

        // Generate and load thumbnails of videos with Glide
        GlideApp.with(mContext)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                .transition(withCrossFade())
                .into(imageView);

        mCheckBox.setTag(position);
        mCheckBox.setChecked((mSelectedList.contains(file_location) || mSelectedList.contains(url)));

        // OnChecked listener, selects and deselects video only when in selectMode
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isShown())
                    if (selectMode) {
                        if (isChecked) {
                            if (!mSelectedList.contains(mList.get((Integer) buttonView.getTag()))) {
                                mSelectedList.add(mList.get((Integer) buttonView.getTag()));
                            }
                        } else {
                            mSelectedList.remove(mList.get((Integer) buttonView.getTag()));
                        }

                        // Define new title for MainActivity
                        Resources res = App.getInstance().getResources();
                        String title;
                        int sel_size = mSelectedList.size();
                        if (sel_size == 0) {
                            title = res.getString(R.string.please_select_videos);
                        } else {
                            title = res.getQuantityString(R.plurals.videos_selected, sel_size, sel_size);
                        }

                        // Fire UI Event to change title of MainActivity
                        App.getInstance().bus.post(new SetNewTitleEvent(title));
                    } else {
                        buttonView.toggle();
                    }
            }
        });

        // On click listener to open video directly when not in selectMode
        mCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!selectMode) {
                    String uri = (String) getItem(mPosition);
                    Intent intent = new Intent(mContext, VideoPlayer.class);
                    intent.putExtra("uri", uri);
                    mContext.startActivity(intent);
                }
            }
        });
        return convertView;
    }
}
