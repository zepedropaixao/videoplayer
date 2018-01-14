package me.paixao.videoplayer.events;

public class SetNewTitleEvent extends BaseEvent {
    public SetNewTitleEvent(String title) {
        super(title);
    }
}
