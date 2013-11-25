package org.xendan.logmonitor.dao;

/**
 * User: id967161
 * Date: 22/11/13
 */
public interface Callback<T> {

    void onAnswer(T answer);

    void onFail(Throwable error);
}
