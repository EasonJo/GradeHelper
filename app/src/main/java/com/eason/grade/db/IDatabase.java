package com.eason.grade.db;

import android.support.annotation.NonNull;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public interface IDatabase<M, K> {
    //增加
    boolean insert(M m);

    boolean insertOrReplace(@NonNull M m);

    boolean insertList(List<M> mList);

    boolean insertOrReplaceList(List<M> mList);

    //删除
    boolean delete(M m);

    boolean deleteByKey(K key);

    boolean deleteList(List<M> mList);

    boolean deleteByKeyInTx(K... key);

    boolean deleteAll();

    //修改
    boolean update(M m);

    boolean updateInTx(M... m);

    boolean updateList(List<M> mList);

    //查询
    M selectByPrimaryKey(K key);

    List<M> loadAll();

    boolean refresh(M m);

    /**
     * 清理缓存
     */
    void clearDaoSession();

    /**
     * Delete all tables and content from our database
     */
    boolean dropDatabase();

    /**
     * 事务
     */
    void runInTx(Runnable runnable);

    /**
     * 自定义查询
     *
     * @return
     */
    QueryBuilder<M> getQueryBuilder();

    /**
     * @param where
     * @param selectionArg
     * @return
     */
    List<M> queryRaw(String where, String... selectionArg);
}
