package com.woting.gather.enumeration;

import com.spiritdata.dataanal.visitmanage.core.enumeration.ObjType;

public class ChopinObjType extends ObjType {
    public final static ChopinObjType article=new ChopinObjType(1, "文章");
    public final static ChopinObjType column=new ChopinObjType(3, "栏目");

    protected ChopinObjType(int v, String n) {
        super(v, n);
    }
}