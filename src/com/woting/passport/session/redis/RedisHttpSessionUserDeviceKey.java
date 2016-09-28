package com.woting.passport.session.redis;

import javax.servlet.http.HttpSession;

import com.spiritdata.framework.exceptionC.Plat5101CException;
import com.spiritdata.framework.util.StringUtils;
import com.woting.passport.session.key.UserDeviceKey;
import com.woting.redis.session.RedisLoginData;

public class RedisHttpSessionUserDeviceKey extends UserDeviceKey implements RedisLoginData {
    private static final long serialVersionUID = -3905663905600004190L;

    private HttpSession session;

    public RedisHttpSessionUserDeviceKey(UserDeviceKey udKey, HttpSession session) {
        if (udKey==null) throw new Plat5101CException("用户设备Key不能为空");
        if (StringUtils.isNullOrEmptyOrSpace(this.getDeviceId()))  new Plat5101CException("未设置设备Id");
        if (this.getPCDType()<=0)  new Plat5101CException("未设置PCDType");

        this.session=session;
        this.setUserId(udKey.getUserId());
        this.setPCDType(udKey.getPCDType());
        this.setDeviceId(udKey.getDeviceId());
    }
    @Override
    public String getKey_Lock() {
        String ret="Session_User_LoginLock::UserId::";
        if (!StringUtils.isNullOrEmptyOrSpace(this.getUserId())) return ret+this.getUserId();
        else
        if (!StringUtils.isNullOrEmptyOrSpace(this.getDeviceId())) return ret+this.getDeviceId();
        throw new Plat5101CException("未设置设备Id");
    }

    @Override
    public String getKey_UserLoginStatus() {
        if (StringUtils.isNullOrEmptyOrSpace(this.getDeviceId()))  new Plat5101CException("未设置设备Id");
        if (this.getPCDType()<=0)  new Plat5101CException("未设置PCDType");

        String _userId=StringUtils.isNullOrEmptyOrSpace(this.getUserId())?this.getDeviceId():this.getUserId();
        String ret="Session_User_Login::UserId_DType_DId::"+_userId+"_"+this.getPCDType()+"_"+this.getDeviceId();
        if (this.getPCDType()==3) ret="Session_User_Login::UserId_DType_DId::"+_userId+"_"+this.getPCDType()+"-"+this.session.getId()+"_"+this.getDeviceId();
        return ret;
    }

    @Override
    public String getKey_UserLoginDeviceType() {
        if (StringUtils.isNullOrEmptyOrSpace(this.getDeviceId()))  new Plat5101CException("未设置设备Id");
        if (this.getPCDType()<=0)  new Plat5101CException("未设置PCDType");

        String _userId=StringUtils.isNullOrEmptyOrSpace(this.getUserId())?this.getDeviceId():this.getUserId();
        String ret="Session_User_Login::UserId_DType::"+_userId+"_"+this.getPCDType();
        if (this.getPCDType()==3) ret="Session_User_Login::UserId_DType::"+_userId+"_"+this.getPCDType()+"-"+this.session.getId();
        return ret;
    }

    @Override
    public String getValue_DeviceId() {
        if (StringUtils.isNullOrEmptyOrSpace(this.getDeviceId()))  new Plat5101CException("未设置设备Id");
        if (this.getPCDType()<=0)  new Plat5101CException("未设置PCDType");

        return this.getDeviceId();
    }

    public String getKey_UserPhoneCheck() {
        if (StringUtils.isNullOrEmptyOrSpace(this.getDeviceId()))  new Plat5101CException("未设置设备Id");
        if (this.getPCDType()<=0)  new Plat5101CException("未设置PCDType");

        String _userId=StringUtils.isNullOrEmptyOrSpace(this.getUserId())?this.getDeviceId():this.getUserId();
        String ret="User_PhoneCheck::UserId_DType_DId::"+_userId+"_"+this.getPCDType()+"_"+this.getDeviceId();
        if (this.getPCDType()==3) ret="User_PhoneCheck::UserId_DType_DId::"+_userId+"_"+this.getPCDType()+"-"+this.session.getId()+"_"+this.getDeviceId();
        return ret;
    }
}
