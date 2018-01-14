package me.paixao.videoplayer.db.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import me.paixao.videoplayer.db.migrations.VPDatabase;

@Table(name = "playlist",
        database = VPDatabase.class,
        primaryKeyConflict = ConflictAction.REPLACE,
        insertConflict = ConflictAction.REPLACE,
        updateConflict = ConflictAction.REPLACE)
public class Playlist extends BaseModel {

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Video> getVideos() {
        return new Select().from(Video.class)
                .where(Video_Table.playlist.eq(getUuid()))
                .orderBy(Video_Table.order, true)
                .queryList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean delete() {
        Video.deleteAll(getVideos());
        return super.delete();
    }
}