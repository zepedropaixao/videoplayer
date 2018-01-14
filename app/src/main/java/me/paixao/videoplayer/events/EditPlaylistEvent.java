package me.paixao.videoplayer.events;

import me.paixao.videoplayer.db.models.Playlist;

public class EditPlaylistEvent extends BaseEvent {
    public EditPlaylistEvent(Playlist model) {
        super(model);
    }
}
