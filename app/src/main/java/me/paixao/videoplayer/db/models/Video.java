
package me.paixao.videoplayer.db.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;

import me.paixao.videoplayer.db.migrations.VPDatabase;

@Table(name = "video",
        database = VPDatabase.class,
        primaryKeyConflict = ConflictAction.REPLACE,
        insertConflict = ConflictAction.REPLACE,
        updateConflict = ConflictAction.REPLACE)
public class Video extends BaseModel {
    @Column(name = "uri")
    private String uri;

    @Column(name = "order")
    private Integer order;

    @ForeignKey(references = {@ForeignKeyReference(columnName = "playlist",
            foreignKeyColumnName = "uuid")},
            saveForeignKeyModel = false)
    @Column
    private Playlist playlist;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }
}