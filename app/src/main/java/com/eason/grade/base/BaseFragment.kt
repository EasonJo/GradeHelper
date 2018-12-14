package com.eason.grade.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment : Fragment() {
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false)
    }

    abstract fun getLayout(): Int

    /**
     * Add Rxjava mission to [CompositeDisposable], prevent memory leak
     *
     * @param d [Disposable]
     */
    fun add(d: Disposable) {
        disposables.add(d)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
        disposables.clear()
    }
}