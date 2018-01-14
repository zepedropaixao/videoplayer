package me.paixao.videoplayer.db.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.AsyncModel;
import com.raizlabs.android.dbflow.structure.InvalidDBConfiguration;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import me.paixao.videoplayer.App;
import me.paixao.videoplayer.db.migrations.VPDatabase;

public abstract class BaseModel implements Model {

    public App app = App.getInstance();

    @PrimaryKey
    @Unique(onUniqueConflict = ConflictAction.REPLACE)
    @Column(name = "uuid")
    public String uuid;

    @Column(name = "date_created")
    private Date dateCreated;

    @Column(name = "date_updated")
    private Date dateUpdated;

    @Column(name = "date_played")
    private Date datePlayed;

    public BaseModel() {
        super();
    }

    public String getUuid() {
        if (uuid == null)
            setUuid();
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setUuid() {
        if (this.uuid == null)
            this.uuid = UUID.randomUUID().toString();
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Date getDatePlayed() {
        return datePlayed;
    }

    public void setDatePlayed(Date datePlayed) {
        this.datePlayed = datePlayed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean save() {
        getUuid();
        FlowManager.getDatabase(VPDatabase.class)
                .beginTransactionAsync(FastStoreModelTransaction
                        .saveBuilder(getModelAdapter())
                        .addAll(Collections.singletonList(this)).build()).execute();
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean save(DatabaseWrapper databaseWrapper) {
        return getModelAdapter().save(this, databaseWrapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean delete() {
        FlowManager.getDatabase(VPDatabase.class)
                .beginTransactionAsync(FastStoreModelTransaction
                        .deleteBuilder(getModelAdapter())
                        .addAll(Collections.singletonList(this)).build()).execute();
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean delete(DatabaseWrapper databaseWrapper) {
        return getModelAdapter().delete(this, databaseWrapper);
    }

    /**
     * Save multiple records in a single transaction
     *
     * @param models Models to save
     */
    public static <T extends BaseModel> void saveAll(final List<T> models) {
        if (models != null && !models.isEmpty())
            FlowManager.getDatabase(VPDatabase.class)
                    .beginTransactionAsync(FastStoreModelTransaction
                            .saveBuilder(models.get(0).getModelAdapter())
                            .addAll(models).build()).execute();
    }

    /**
     * Delete multiple records in a single transaction
     *
     * @param models Models to save
     */
    public static <T extends BaseModel> void deleteAll(final List<T> models) {
        if (models != null && !models.isEmpty())
            FlowManager.getDatabase(VPDatabase.class)
                    .beginTransactionAsync(FastStoreModelTransaction
                            .deleteBuilder(models.get(0).getModelAdapter())
                            .addAll(models).build()).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean update() {
        return getModelAdapter().update(this);
    }

    @SuppressWarnings("unchecked")
    public boolean update(DatabaseWrapper databaseWrapper) {
        return getModelAdapter().update(this, databaseWrapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public long insert() {
        return getModelAdapter().insert(this);
    }

    @SuppressWarnings("unchecked")
    public long insert(DatabaseWrapper databaseWrapper) {
        return getModelAdapter().insert(this, databaseWrapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean exists() {
        return getModelAdapter().exists(this);
    }

    @SuppressWarnings("unchecked")
    public boolean exists(DatabaseWrapper databaseWrapper) {
        return getModelAdapter().exists(this, databaseWrapper);
    }

    /**
     * @return An async instance of this model where all transactions are on the {@link DefaultTransactionQueue}
     */
    public AsyncModel<BaseModel> async() {
        return new AsyncModel<>(this);
    }

    /**
     * @return The associated {@link ModelAdapter}. The {@link FlowManager}
     * may throw a {@link InvalidDBConfiguration} for this call if this class
     * is not associated with a table, so be careful when using this method.
     */
    public ModelAdapter getModelAdapter() {
        return FlowManager.getModelAdapter(getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load() {
        getModelAdapter().load(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load(DatabaseWrapper wrapper) {
        getModelAdapter().load(this, wrapper);
    }

    public static void l(String tag, String message) {
        App.getInstance().l(tag, message);
    }

    public static void le(String tag, String message) {
        App.getInstance().le(tag, message);
    }

    public void l(String message) {
        app.l(getClassName(), message);
    }

    public void le(String message) {
        app.le(getClassName(), message);
    }

    public String getClassName() {
        return this.getClass().getSimpleName();
    }
}
