package org.xendan.logmonitor.idea.model;

/**
 * User: id967161
 * Date: 19/09/13
 */
public interface BeanChangeListener<T> {

    void onBeanSet(T newBean);
}
