package me.paixao.videoplayer.events;

import java.util.List;

import me.paixao.videoplayer.db.models.BaseModel;

public class BaseEvent<M extends BaseModel> {
    private List<M> modelList;
    private M model;
    protected String message;

    public BaseEvent(List<M> modelList) {

        this.modelList = modelList;
    }

    public BaseEvent(M model) {
        this.model = model;
    }

    public BaseEvent(String message) {
        this.message = message;
    }

    public BaseEvent() {
    }

    public List<M> getModelList() {
        return modelList;
    }

    public M getModel() {
        return model;
    }

    public String getMessage() {
        return message;
    }
}
