package me.paixao.videoplayer.events;

public class StartCreatePlaylistEvent extends BaseEvent {

    public StartCreatePlaylistEvent(boolean withError) {
        super(withError);
    }
}
