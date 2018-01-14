package me.paixao.videoplayer.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.util.ArrayList;

import me.paixao.videoplayer.R;
import me.paixao.videoplayer.activities.VideoPlayer;
import me.paixao.videoplayer.ui.helpers.GlideApp;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ImageAdapter extends BaseAdapter {

    boolean selectMode = false;
    boolean deleteMode = false;
    ArrayList<String> mList;
    ArrayList<String> mSelectedList;
    LayoutInflater mInflater;
    Context mContext;


    CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

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
                } else {
                    buttonView.toggle();
                }
        }
    };

    public ImageAdapter(Context context, ArrayList<String> imageList, ArrayList<String> selectedImageList) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        if (selectedImageList != null) {
            mSelectedList = selectedImageList;
        } else {
            mSelectedList = new ArrayList<String>();
        }
        mList = new ArrayList<String>();
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int mPosition = position;

        if (convertView == null) {
            if (deleteMode) {
                convertView = mInflater.inflate(R.layout.row_multiphoto_item_remove, null);
            } else {
                convertView = mInflater.inflate(R.layout.row_multiphoto_item, null);
            }
        }

        final CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
        final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);

        final String file_location = mList.get(position);
        String url;
        if (file_location.startsWith("//web")) {
            url = file_location;
        } else if (file_location.startsWith("file://") || file_location.startsWith("http://") || file_location.startsWith("https://")) {
            url = file_location;
        } else {
            url = "file://" + file_location;
        }

        /*parent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                App.getInstance().toast("I LONG PRESSED");
                if (!selectMode)
                    selectMode = true;
                return true;
            }
        });*/

        GlideApp.with(mContext)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                .transition(withCrossFade())
                .into(imageView);

        mCheckBox.setTag(position);
        mCheckBox.setChecked((mSelectedList.contains(file_location) || mSelectedList.contains(url)));
        mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);

        mCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (deleteMode) {
                    removeItem(mPosition);
                } else if (!deleteMode && !selectMode) {

                    Log.e("ERROR", "IM HERE");


                    String uri = (String) getItem(mPosition);
                    Intent intent = new Intent(mContext, VideoPlayer.class);
                    intent.putExtra("uri", uri);
                    mContext.startActivity(intent);

                    Log.e("ERROR", "IM HERE2");
                }
            }
        });

        return convertView;
    }
}
