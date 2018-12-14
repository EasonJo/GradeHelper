package com.eason.grade.utils;

import android.support.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * use rxjava to simulation event bus
 */
public class RxBus {
    private ConcurrentHashMap<Object, List<Subject>> maps = new ConcurrentHashMap<>();
    private static RxBus instance;

    private RxBus() {
    }

    public static RxBus get() {
        if (instance == null) {
            synchronized (RxBus.class) {
                if (instance == null) {
                    instance = new RxBus();
                }
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz) {
        List<Subject> subjects = maps.get(tag);
        if (subjects == null) {
            subjects = new ArrayList<>();
            maps.put(tag, subjects);
        }
        Subject<T> subject = PublishSubject.<T>create();
        subjects.add(subject);
        return subject;
    }

    /**
     * register Observer on {@link T}
     * @param tClass data class
     * @param <T> data type
     * @return Observer {@link Observable}
     */
    public <T> Observable<T> register(Class<T> tClass) {
        return register(tClass.getSimpleName(), tClass);
    }

    @SuppressWarnings("unchecked")
    public void unregister(@NonNull Object tag, @NonNull Observable observable) {
        List<Subject> subjects = maps.get(tag);
        if (subjects != null) {
            subjects.remove(observable);
            if (subjects.isEmpty()) {
                maps.remove(tag);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void post(@NonNull Object o) {
        post(o.getClass().getSimpleName(), o);
    }

    @SuppressWarnings("unchecked")
    public void post(@NonNull Object tag, @NonNull Object o) {
        List<Subject> subjects = maps.get(tag);
        if (subjects != null && !subjects.isEmpty()) {
            for (Subject s : subjects) {
                s.onNext(o);
            }
        }
    }
}