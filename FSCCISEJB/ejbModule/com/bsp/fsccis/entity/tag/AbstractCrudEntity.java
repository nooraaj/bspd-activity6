package com.bsp.fsccis.entity.tag;

import javax.persistence.Transient;

public abstract class AbstractCrudEntity implements CRUDTag {
	@Transient
	private int crudStatus;

	@Override
	public int getCrudStatus() {
		return crudStatus;
	}

	@Override
	public void setCrudStatus(int crudStatus) {
		this.crudStatus = crudStatus;
	}

//    public abstract boolean equalsCompare(AbstractCrudEntity object);

}
