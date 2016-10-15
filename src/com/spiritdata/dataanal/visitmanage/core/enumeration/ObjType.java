package com.spiritdata.dataanal.visitmanage.core.enumeration;

/**
 * 访问对象类别
 * @author wh
 */
public abstract class ObjType {
    private int value;
    public int getValue() {
        return this.value;
    }
    protected String name;
    public String getName() {
        return this.name;
    }

    protected ObjType(int v, String n) {
        this.value=v;
        this.name=n;
    }
}