package me.paixao.videoplayer.events;

import me.paixao.videoplayer.db.models.Playlist;

public class DeletePlaylistEvent extends BaseEvent {
    public DeletePlaylistEvent(Playlist model) {
        super(model);
    }
}
