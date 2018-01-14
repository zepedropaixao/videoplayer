package me.paixao.videoplayer.events;

import me.paixao.videoplayer.db.models.Playlist;

public class OpenPlaylistEvent extends BaseEvent {

    public OpenPlaylistEvent(Playlist model) {
        super(model);
    }
}
