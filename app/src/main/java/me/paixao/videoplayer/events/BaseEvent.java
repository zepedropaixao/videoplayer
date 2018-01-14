package me.paixao.videoplayer.events;

import java.util.List;

import me.paixao.videoplayer.db.models.BaseModel;

public class BaseEvent<M extends BaseModel> {
    protected boolean withError = false;
    protected List<M> modelList;
    protected M model;
    protected String message;
    protected Integer limit;
    protected Integer offset;

    public BaseEvent(boolean withError, List<M> modelList, Integer offset, Integer limit) {
        this.withError = withError;
        this.modelList = modelList;
        this.limit = limit;
        this.offset = offset;
    }

    public BaseEvent(boolean withError, String message, Integer offset, Integer limit) {
        this.withError = withError;
        this.message = message;
        this.limit = limit;
        this.offset = offset;
    }

    public BaseEvent(boolean withError, List<M> modelList) {
        this.withError = withError;
        this.modelList = modelList;
    }

    public BaseEvent(boolean withError, M model) {
        this.withError = withError;
        this.model = model;
    }

    public BaseEvent(boolean withError, String message) {
        this.withError = withError;
        this.message = message;
    }

    public BaseEvent(boolean withError) {
        this.withError = withError;
        this.message = "";
    }

    public BaseEvent(String message) {
        this.withError = false;
        this.message = message;
    }

    public BaseEvent(M model) {
        this.withError = false;
        this.model = model;
    }

    public List<M> getModelList() {
        return modelList;
    }

    public M getModel() {
        return model;
    }

    public boolean isWithError() {
        return withError;
    }

    public String getMessage() {
        return message;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
