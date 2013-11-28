package org.xendan.logmonitor.idea.model.node;

import org.xendan.logmonitor.model.BaseObject;

/**
 * User: id967161
 * Date: 27/11/13
 */
public class EntityObject<E extends BaseObject> {

    protected final E entity;

    public EntityObject(E entity) {
        this.entity = entity;
    }

    public E getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return entity.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityObject that = (EntityObject) o;

        if (entity != null ? !entity.equals(that.entity) : that.entity != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return entity != null ? entity.hashCode() : 0;
    }
}