package com.bsp.fsccis.entity.tag;

import java.util.Date;

public interface BSPTimeStampable { 

    public Date getCdate();

    public void setCdate(Date cdate);

    public Date getCtime();

    public void setCtime(Date ctime);

    public String getCuser();

    public void setCuser(String cuser);
}
