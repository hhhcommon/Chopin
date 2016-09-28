package com.woting.passport.web;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.SpiritRandom;
import com.spiritdata.framework.util.StringUtils;
import com.spiritdata.framework.ext.redis.ExpirableBlockKey;
import com.spiritdata.framework.ext.redis.RedisBlockLock;
import com.spiritdata.framework.util.RequestUtils;
import com.woting.passport.UGA.persistence.pojo.UserPo;
import com.woting.passport.UGA.service.UserService;
import com.woting.passport.login.persistence.pojo.MobileUsedPo;
import com.woting.passport.login.service.MobileUsedService;
import com.woting.passport.mobile.MobileParam;
import com.woting.passport.mobile.MobileUDKey;
import com.woting.passport.session.SessionService;
import com.woting.passport.session.redis.RedisHttpSessionUserDeviceKey;
import com.woting.plugins.sms.SendSMS;

@Controller
@RequestMapping(value="/passport/")
public class PassportController {
	@Resource
	private UserService userService;
    @Resource
    private MobileUsedService muService;
    @Resource(name="redisSessionService")
    private SessionService sessionService;
    @Resource
    JedisConnectionFactory redisConn;

    /**
     * 用户注册
     * @param request
     * @return
     */
    @RequestMapping(value="user/register.do")
    @ResponseBody
    public Map<String, Object> register(HttpServletRequest request){
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                    mUdk.setDeviceId(request.getSession().getId());
                }
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) {
                    map.put("ReturnType", "0000");
                    map.put("Message", "无法获取设备Id(IMEI)");
                } else {
                    map.putAll(mUdk.toHashMapAsBean());
                }
            }
            if (map.get("ReturnType")!=null) return map;

            String ln=(m.get("UserName")==null?null:m.get("UserName")+"");
            String pwd=(m.get("Password")==null?null:m.get("Password")+"");
            String phonenum=m.get("PhoneNum")+"";
            String usePhone=(m.get("UsePhone")==null?null:m.get("UsePhone")+"");

            String errMsg="";
            if (StringUtils.isNullOrEmptyOrSpace(ln)) errMsg+=",用户名为空";
            if (ln!=null) {
                char[] c=ln.toCharArray();
                if (c[0]>='0' && c[0]<='9') errMsg+=",登录名第一个字符不能是数字";
            }
            if (StringUtils.isNullOrEmptyOrSpace(pwd)) errMsg+=",密码为空";
            if (usePhone!=null&&usePhone.equals("1")) {
                if(phonenum.toLowerCase().equals("null")) errMsg+=",手机号为空";
            }
            if (!StringUtils.isNullOrEmptyOrSpace(errMsg)) {
                errMsg=errMsg.substring(1);
                map.put("ReturnType", "1002");
                map.put("Message", errMsg+",无法注册");
                return map;
            }
            UserPo nu=new UserPo();
            nu.setLoginName(ln);
            nu.setMainPhoneNum(phonenum);
            nu.setPassword(pwd);
            //1-判断是否有重复的用户
            UserPo oldUser=userService.getUserByLoginName(ln);
            if (oldUser!=null) { //重复
                map.put("ReturnType", "1003");
                map.put("Message", "登录名重复,无法注册.");
                return map;
            }
            RedisConnection rConn=redisConn.getConnection();
            RedisHttpSessionUserDeviceKey redisUdk=new RedisHttpSessionUserDeviceKey(mUdk, request.getSession());
            if (usePhone!=null&&usePhone.equals("1")) {
                //1.5-手机号码注册
                byte[] getValue=rConn.get(redisUdk.getKey_UserPhoneCheck().getBytes());
                String info=getValue==null?"":new String(rConn.get(getValue));
                if (info.startsWith("OK")) {
                    nu.setMainPhoneNum(info.substring(4));
                }
            }
            //2-保存用户
            nu.setCTime(new Timestamp(System.currentTimeMillis()));
            nu.setUserType(1);
            nu.setUserId(SequenceUUID.getUUIDSubSegment(4));
            int rflag=userService.insertUser(nu);
            if (rflag!=1) {
                map.put("ReturnType", "1004");
                map.put("Message", "注册失败，新增用户存储失败");
                return map;
            }
            //3-注册成功后，自动登陆，及后处理
            mUdk.setUserId(nu.getUserId());
            ExpirableBlockKey rLock=RedisBlockLock.lock(redisUdk.getKey_Lock(), rConn);
            try {
                sessionService.registUser(mUdk, request.getSession());
                MobileUsedPo mu=new MobileUsedPo();
                mu.setImei(mUdk.getDeviceId());
                mu.setStatus(1);
                mu.setPCDType(mUdk.getPCDType());
                mu.setUserId(nu.getUserId());
                muService.saveMobileUsed(mu);
            } finally {
                rLock.unlock();
                rConn.close();
                rConn=null;
            }
            //4-返回成功，若没有IMEI也返回成功
            map.put("ReturnType", "1001");
            map.put("UserId", nu.getUserId());
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    /**
	 * 用户登录
	 * @param request
	 * @return
	 */
	@RequestMapping(value="user/mlogin.do")
	@ResponseBody
	public Map<String, Object> mlogin(HttpServletRequest request){
		Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            MobileUDKey mUdk=MobileParam.build(m).getUserDeviceKey();
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
                return map;
            }

            String ln=(m.get("UserName")==null?null:m.get("UserName")+"");
            String pwd=(m.get("Password")==null?null:m.get("Password")+"");
            String errMsg="";
            if (StringUtils.isNullOrEmptyOrSpace(ln)) errMsg+=",用户名为空";
            if (StringUtils.isNullOrEmptyOrSpace(pwd)) errMsg+=",密码为空";
            if (!StringUtils.isNullOrEmptyOrSpace(errMsg)) {
                errMsg=errMsg.substring(1);
                map.put("ReturnType", "0000");
                map.put("Message", errMsg+",无法登陆");
                return map;
            }

            UserPo u=userService.getUserByLoginName(ln);
            if (u==null) u=userService.getUserByPhoneNum(ln);
            //1-判断是否存在用户
            if (u==null) { //无用户
                map.put("ReturnType", "1002");
                map.put("Message", "无登录名为["+ln+"]的用户.");
                return map;
            }
            //2-判断密码是否匹配
            if (!u.getPassword().equals(pwd)) {
                map.put("ReturnType", "1003");
                map.put("Message", "密码不匹配.");
                return map;
            }
            //3-用户登录成功
            mUdk.setUserId(u.getUserId());
            if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                mUdk.setDeviceId(request.getSession().getId());
            }
            RedisHttpSessionUserDeviceKey redisUdk=new RedisHttpSessionUserDeviceKey(mUdk, request.getSession());
            RedisConnection rConn=redisConn.getConnection();
            ExpirableBlockKey rLock=RedisBlockLock.lock(redisUdk.getKey_Lock(), rConn);
            try {
                sessionService.registUser(mUdk, request.getSession());
                MobileUsedPo mu=new MobileUsedPo();
                mu.setImei(mUdk.getDeviceId());
                mu.setStatus(1);
                mu.setPCDType(mUdk.getPCDType());
                mu.setUserId(u.getUserId());
                muService.saveMobileUsed(mu);
            } finally {
                rLock.unlock();
                rConn.close();
                rConn=null;
            }
            //4-返回成功，若没有IMEI也返回成功
            map.put("ReturnType", "1001");
            map.put("UserInfo", u.toHashMap4Mobile());
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
	}
	
	/**
	 * 用户注销
	 * @param request
	 * @return
	 */
	@RequestMapping(value="user/mlogout.do")
	@ResponseBody
	public Map<String, Object> mlogot(HttpServletRequest request){
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            String userId="";
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                    mUdk.setDeviceId(request.getSession().getId());
                }
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "user/mlogout", request.getSession());
                if ((retM.get("ReturnType")+"").equals("2001")) {
                    map.put("ReturnType", "0000");
                    map.put("Message", "无法获取设备Id(IMEI)");
                } else if ((retM.get("ReturnType")+"").equals("2003")) {
                    map.put("ReturnType", "200");
                    map.put("Message", "需要登录");
                } else {
                    if (mUdk.isUser()) userId=mUdk.getUserId();
                }
                if (map.get("ReturnType")==null&&StringUtils.isNullOrEmptyOrSpace(userId)) {
                    map.put("ReturnType", "1002");
                    map.put("Message", "无法获取用户Id");
                }
            }
            if (map.get("ReturnType")!=null) return map;

            //2-注销
            RedisConnection rConn=redisConn.getConnection();
            RedisHttpSessionUserDeviceKey redisUdk=new RedisHttpSessionUserDeviceKey(mUdk, request.getSession());
            ExpirableBlockKey rLock=RedisBlockLock.lock(redisUdk.getKey_Lock(), rConn);
            try {
                sessionService.logoutSession(mUdk, request.getSession());
                //保存使用情况
                MobileUsedPo mu=new MobileUsedPo();
                mu.setImei(mUdk.getDeviceId());
                mu.setStatus(2);
                mu.setUserId(mUdk.getUserId());
                mu.setPCDType(mUdk.getPCDType());
                muService.saveMobileUsed(mu);
            } finally {
                rLock.unlock();
                rConn.close();
                rConn=null;
            }
            //3-返回成功，不管后台处理情况，总返回成功
            map.put("ReturnType", "1001");
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
	}

    /**
     * 修改登录密码
     * @param request
     * @return
     */
    @RequestMapping(value="user/updatePwd.do")
    @ResponseBody
    public Map<String, Object> updatePassword(HttpServletRequest request){
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
                return map;
            }
            String userid=m.get("UserId")+"";
            if(userid.toLowerCase().equals("null")) {
                map.put("ReturnType", "1002");
                map.put("Message", "无法获取用户id");
                return map;
            }
            String errMsg="";
            String oldpassword=m.get("OldPassword")+"";
            if(oldpassword.toLowerCase().equals("null")) errMsg+=",无旧密码";
            String newpassword=m.get("NewPassword")+"";
            if(newpassword.toLowerCase().equals("null")) errMsg+=",无新密码";
            if (!StringUtils.isNullOrEmptyOrSpace(errMsg)) {
                errMsg=errMsg.substring(1);
                map.put("ReturnType", "1003");
                map.put("Message", errMsg+",密码信息获取失败");
                return map;
            }
            if(oldpassword.equals(newpassword)) {
                map.put("ReturnType", "1004");
                map.put("Message", "新旧密码不能相同");
                return map;
            }
            UserPo u=userService.getUserById(userid);
            if(u==null) {
                map.put("ReturnType", "1005");
                map.put("Message", "用户信息不存在");
                return map;
            }
            if(!u.getPassword().equals(oldpassword)) {
                map.put("ReturnType", "1006");
                map.put("Message", "旧密码不匹配");
                return map;
            }
            UserPo newuser=new UserPo();
            newuser.setUserId(userid);
            newuser.setPassword(newpassword);
            if(userService.updateUser(newuser)!=1) {
                map.put("ReturnType", "1007");
                map.put("Message", "存储新密码失败");
                return map;
            }
            map.put("ReturnType", "1001");
            return map;
        } catch (Exception e) {
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    /**
     * 用手机号注册
     */
    @RequestMapping(value="user/registerByPhoneNum.do")
    @ResponseBody
    public Map<String,Object> registerByPhoneNum(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                    mUdk.setDeviceId(request.getSession().getId());
                }
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "user/registerByPhoneNum", request.getSession());
                if (retM==null) {
                    map.put("ReturnType", "1003");
                    map.put("Message", "无法获取会话信息");
                }
            }
            if (map.get("ReturnType")!=null) return map;

            //1-获取电话号码
            String phoneNum=(m.get("PhoneNum")==null?null:m.get("PhoneNum")+"");
            if (StringUtils.isNullOrEmptyOrSpace(phoneNum)) {
                map.put("ReturnType", "1000");
                map.put("Message", "无法获取手机号");
            }
            if (map.get("ReturnType")!=null) return map;

            //验证重复的手机号
            UserPo u=userService.getUserByPhoneNum(phoneNum);
            if (u==null) { //正确
                map.put("ReturnType", "1001");
                int random=SpiritRandom.getRandom(new Random(), 1000000, 1999999);
                String checkNum=(random+"").substring(1);
                String smsRetNum=SendSMS.sendSms(phoneNum, checkNum, "通过手机号注册用户");
                //向Session中加入验证信息
                RedisConnection rConn=redisConn.getConnection();
                RedisHttpSessionUserDeviceKey redisUdk=new RedisHttpSessionUserDeviceKey(mUdk, request.getSession());
                try {
                    rConn.pSetEx(redisUdk.getKey_UserPhoneCheck().getBytes(), 100*1000, (System.currentTimeMillis()+"::"+phoneNum+"::"+checkNum).getBytes());
                } finally {
                    rConn.close();
                    rConn=null;
                }
                map.put("SmsRetNum", smsRetNum);
            } else {
                map.put("ReturnType", "1002");
            }
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    /**
     * 通过手机号找回密码
     */
    @RequestMapping(value="user/retrieveByPhoneNum.do")
    @ResponseBody
    public Map<String,Object> retrieveByPhoneNum(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                    mUdk.setDeviceId(request.getSession().getId());
                }
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "user/retrieveByPhoneNum", request.getSession());
                if (retM==null) {
                    map.put("ReturnType", "1003");
                    map.put("Message", "无法获取会话信息");
                }
            }
            if (map.get("ReturnType")!=null) return map;

            //1-获取电话号码
            String phoneNum=(m.get("PhoneNum")==null?null:m.get("PhoneNum")+"");
            if (StringUtils.isNullOrEmptyOrSpace(phoneNum)) {
                map.put("ReturnType", "1000");
                map.put("Message", "无法获取手机号");
            }
            if (map.get("ReturnType")!=null) return map;

            //验证重复的手机号
            UserPo u=userService.getUserByPhoneNum(phoneNum);
            if (u!=null) { //正确
                map.put("ReturnType", "1001");
                int random=SpiritRandom.getRandom(new Random(), 1000000, 1999999);
                String checkNum=(random+"").substring(1);
                String smsRetNum=SendSMS.sendSms(phoneNum, checkNum, "通过绑定手机号找回密码");
                //向Session中加入验证信息
                RedisConnection rConn=redisConn.getConnection();
                RedisHttpSessionUserDeviceKey redisUdk=new RedisHttpSessionUserDeviceKey(mUdk, request.getSession());
                try {
                    rConn.pSetEx(redisUdk.getKey_UserPhoneCheck().getBytes(), 100*1000, (System.currentTimeMillis()+"::"+phoneNum+"::"+checkNum).getBytes());
                } finally {
                    rConn.close();
                    rConn=null;
                }
                map.put("SmsRetNum", smsRetNum);
            } else {
                map.put("ReturnType", "1002");//该手机未绑定任何账户
            }
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    /**
     * 根据手机号码发送验证码
     */
    @RequestMapping(value="user/reSendPhoneCheckCode.do")
    @ResponseBody
    public Map<String,Object> reSendPhoneCheckCode(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                    mUdk.setDeviceId(request.getSession().getId());
                }
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "user/reSendPhoneCheckCode", request.getSession());
                if (retM==null) {
                    map.put("ReturnType", "1003");
                    map.put("Message", "无法获取会话信息");
                }
            }
            if (map.get("ReturnType")!=null) return map;

            //1-获取电话号码
            String phoneNum=(m.get("PhoneNum")==null?null:m.get("PhoneNum")+"");
            if (StringUtils.isNullOrEmptyOrSpace(phoneNum)) {
                map.put("ReturnType", "1000");
                map.put("Message", "无法获取手机号");
            }
            if (map.get("ReturnType")!=null) return map;
            //2-获取过程码
            int operType=-1;
            try {operType=Integer.parseInt(m.get("OperType")+"");} catch(Exception e) {}
            if (operType==-1) {
                map.put("ReturnType", "1000");
                map.put("Message", "无法获取过程码");
            }
            if (map.get("ReturnType")!=null) return map;

            RedisConnection rConn=redisConn.getConnection();
            RedisHttpSessionUserDeviceKey redisUdk=new RedisHttpSessionUserDeviceKey(mUdk, request.getSession());
            byte[] getValue=redisUdk.getKey_UserPhoneCheck().getBytes();
            String info=(getValue==null?null:new String(rConn.get(getValue)));

            if (info==null||info.equals("null")||info.startsWith("OK")) {//错误
                map.put("ReturnType", "1002");
                map.put("Message", "状态错误，未有之前的发送数据，无法重发");
            } else {//正确
                String[] _info=info.split("::");
                if (_info.length!=3) {
                    map.put("ReturnType", "1002");
                    map.put("Message", "状态错误，数据格式不正确");
                } else {
                    String _phoneNum=_info[1];
                    if (!_phoneNum.equals(phoneNum)) {
                        map.put("ReturnType", "1002");
                        map.put("Message", "状态错误，手机号不匹配");
                    } else {
                        int random=SpiritRandom.getRandom(new Random(), 1000000, 1999999);
                        String checkNum=(random+"").substring(1);
                        String smsRetNum=SendSMS.sendSms(phoneNum, checkNum, operType==1?"通过手机号注册用户":"通过绑定手机号找回密码");
                        //向Session中加入验证信息
                        try {
                            rConn.pSetEx(redisUdk.getKey_UserPhoneCheck().getBytes(), 100*1000, (System.currentTimeMillis()+"::"+phoneNum+"::"+checkNum).getBytes());
                        } finally {
                            rConn.close();
                            rConn=null;
                        }
                        map.put("ReturnType", "1001");
                        map.put("SmsRetNum", smsRetNum);
                    }
                }
            }
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    /**
     * 验证验证码
     */
    @RequestMapping(value="user/checkPhoneCheckCode.do")
    @ResponseBody
    public Map<String,Object> checkPhoneCheckCode(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "user/checkPhoneCheckCode", request.getSession());
                if (retM==null) {
                    map.put("ReturnType", "1000");
                    map.put("Message", "无法获取会话信息");
                }
            }
            if (map.get("ReturnType")!=null) return map;

            //1-获取电话号码
            String phoneNum=(m.get("PhoneNum")==null?null:m.get("PhoneNum")+"");
            if (StringUtils.isNullOrEmptyOrSpace(phoneNum)) {
                map.put("ReturnType", "1000");
                map.put("Message", "无法获取手机号");
            }
            if (map.get("ReturnType")!=null) return map;
            //2-获取验证码
            String checkNum=(m.get("CheckCode")==null?null:m.get("CheckCode")+"");
            if (StringUtils.isNullOrEmptyOrSpace(checkNum)) {
                map.put("ReturnType", "1000");
                map.put("Message", "无法获取手机验证码");
            }
            if (map.get("ReturnType")!=null) return map;
            //3-获取是否需要得到用户id，只有在找回密码时才有用
            boolean needUserId=false;
            try {needUserId=Boolean.parseBoolean((m.get("NeedUserId")==null?"false":m.get("NeedUserId")+""));} catch(Exception e) {}

            //验证验证码
            RedisConnection rConn=redisConn.getConnection();
            RedisHttpSessionUserDeviceKey redisUdk=new RedisHttpSessionUserDeviceKey(mUdk, request.getSession());
            String info=new String(rConn.get(redisUdk.getKey_UserPhoneCheck().getBytes()));
            if (info==null||info.equals("null")) {
                map.put("ReturnType", "1005");
                map.put("Message", "状态错误");
            } else {
                String[] _info=info.split("::");
                if (_info.length!=3) {
                    map.put("ReturnType", "1005");
                    map.put("Message", "状态错误，数据格式不正确");
                } else {
                    long time=Long.parseLong(_info[0]);
                    String _phoneNum=_info[1];
                    String _checkNum=_info[2];
                    if (System.currentTimeMillis()-time>(100*1000)) {
                        map.put("ReturnType", "1004");
                        map.put("Message", "超时");
                    } else if (!_phoneNum.equals(phoneNum)) {
                        map.put("ReturnType", "1003");
                        map.put("Message", "手机号不匹配");
                    } else if (!_checkNum.equals(checkNum)) {
                        map.put("ReturnType", "1002");
                        map.put("Message", "验证码不匹配");
                    } else {
                        try {
                            rConn.pSetEx(redisUdk.getKey_UserPhoneCheck().getBytes(), 100*1000, ("OK::"+_phoneNum).getBytes());
                        } finally {
                            rConn.close();
                            rConn=null;
                        }
                        if (needUserId) {
                            UserPo u=userService.getUserByPhoneNum(phoneNum);
                            if (u!=null) map.put("UserId", u.getUserId());
                        }
                        map.put("ReturnType", "1001");
                    }
                }
            }
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    /**
     * 修改密码，在通过手机号码找回密码时
     */
    @RequestMapping(value="user/updatePwd_AfterCheckPhoneOK.do")
    @ResponseBody
    public Map<String,Object> updatePwd_AfterCheckPhoneOK(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                    mUdk.setDeviceId(request.getSession().getId());
                }
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "user/updatePwd_AfterCheckPhoneOK", request.getSession());
                if (retM==null) {
                    map.put("ReturnType", "1000");
                    map.put("Message", "无法获取会话信息");
                }
            }
            if (map.get("ReturnType")!=null) return map;

            //1-获取其他参数
            String newPwd=(m.get("NewPassword")==null?null:m.get("NewPassword")+"");
            String errMsg="";
            if (StringUtils.isNullOrEmptyOrSpace(newPwd)) errMsg+=",新密码为空";
            if (!StringUtils.isNullOrEmptyOrSpace(errMsg)) {
                errMsg=errMsg.substring(1);
                map.put("ReturnType", "1003");
                map.put("Message", errMsg+",无法修改密码");
                return map;
            }
            UserPo up=userService.getUserById(m.get("RetrieveUserId")==null?null:(m.get("RetrieveUserId")+""));
            String info=null;
            RedisConnection rConn=redisConn.getConnection();
            RedisHttpSessionUserDeviceKey redisUdk=new RedisHttpSessionUserDeviceKey(mUdk, request.getSession());
            try {
                byte[] getValue=redisUdk.getKey_UserPhoneCheck().getBytes();
                info=getValue==null?"":new String(rConn.get(getValue));
            } finally {
                rConn.close();
                rConn=null;
            }
            if (info.startsWith("OK")) {
                up.setPassword(newPwd);
                int retFlag=userService.updateUser(up);
                if (retFlag==1) map.put("ReturnType", "1001");
                else {
                    map.put("ReturnType", "1004");
                    map.put("Message", "存储新密码失败");
                }
            } else {
                map.put("ReturnType", "1005");
                map.put("Message", "状态错误");
            }
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    /**
     * 第三方登录注册
     * @throws IOException
     */
    @RequestMapping(value="user/afterThirdAuth.do")
    @ResponseBody
    public Map<String,Object> afterThirdAuth(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                    mUdk.setDeviceId(request.getSession().getId());
                }
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) {
                    map.put("ReturnType", "0000");
                    map.put("Message", "无法获取设备Id(IMEI)");
                }
            }
            if (map.get("ReturnType")!=null) return map;

            //1-获取业务参数
            //1.1-获取业务参数：第三方登录的分类：=1微信；=2QQ；=3微博
            String thirdType=(m.get("ThirdType")==null?null:m.get("ThirdType")+"");
            if (StringUtils.isNullOrEmptyOrSpace(thirdType)) {
                map.put("ReturnType", "1002");
                map.put("Message", "无法获得第三方登录类型");
                return map;
            }
            //1.2-获取业务参数：用户的名称和Id
            String tuserId=(m.get("ThirdUserId")==null?null:m.get("ThirdUserId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(tuserId)) {
                map.put("ReturnType", "1000");
                map.put("Message", "无法获取第三方用户Id");
                return map;
            }
            String tuserName=(m.get("ThirdUserName")==null?null:m.get("ThirdUserName")+"");
            if (StringUtils.isNullOrEmptyOrSpace(tuserName)) {
                map.put("ReturnType", "1000");
                map.put("Message", "无法获取第三方用户名称");
                return map;
            }
            //1.3-获取业务参数：头像Url
            String tuserImg=(m.get("ThirdUserImg")==null?null:m.get("ThirdUserImg")+"");
            //1.4-获取业务参数：详细数据
            Map<String, Object> tuserData=(Map<String, Object>)m.get("ThirdUserInfo");

            //2第三方登录
            Map<String, Object> rm=userService.thirdLogin(thirdType, tuserId, tuserName, tuserImg, tuserData);

            //3-成功后，自动登陆，处理Redis
            if ((UserPo)rm.get("userInfo")==null) {
                map.put("ReturnType", "1003");
                map.put("Message", "第三方登录失败！");
                return map;
            }
            String _userId=((UserPo)rm.get("userInfo")).getUserId();
            mUdk.setUserId(_userId);
            RedisConnection rConn=redisConn.getConnection();
            RedisHttpSessionUserDeviceKey redisUdk=new RedisHttpSessionUserDeviceKey(mUdk, request.getSession());
            ExpirableBlockKey rLock=RedisBlockLock.lock(redisUdk.getKey_Lock(), rConn);
            try {
                sessionService.registUser(mUdk, request.getSession());
                //3.2-保存使用情况
                MobileUsedPo mu=new MobileUsedPo();
                mu.setImei(mUdk.getDeviceId());
                mu.setStatus(1);
                mu.setUserId(_userId);
                mu.setPCDType(mUdk.getPCDType());
                muService.saveMobileUsed(mu);
            } finally {
                rLock.unlock();
                if (rConn!=null) rConn.close();
                rConn=null;
            }

            //4设置返回值
            map.put("IsNew", "True");
            if ((Integer)rm.get("count")>1) map.put("IsNew", "False");
            map.put("UserInfo", ((UserPo)rm.get("userInfo")).toHashMap4Mobile());
            map.put("ReturnType", "1001");
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

	/**
	 * 修改用户基本信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="user/updateUserInfo.do")
	@ResponseBody
	public Map<String, Object> updateUserInfo(HttpServletRequest request){
		Map<String, Object> map=new HashMap<String,Object>();
		try {
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
                return map;
            }
            String  userid=m.get("UserId")+"";
            if(userid.toLowerCase().equals("null")) {
            	map.put("ReturnType", "1002");
                map.put("Message", "无法获取用户id");
                return map;
            }
            if(userService.getUserById(userid)==null) {
            	map.put("ReturnType", "1003");
            	map.put("Message", "用户信息不存在");
            	return map;
            }
            UserPo upo=new UserPo();
            upo.setUserId(userid);
            String userimg=m.get("UserImg")+"";
            if(!userimg.toLowerCase().equals("null")) upo.setPortraitBig(userimg);
            String userage=m.get("Age")+"";
            if(!userage.toLowerCase().equals("null")) upo.setAge(userage);
            String userbirgth=m.get("Birthday")+"";
            if(!userbirgth.toLowerCase().equals("null")) upo.setBirthday(userbirgth);
            String usersex=m.get("Sex")+"";
            if(!usersex.toLowerCase().equals("null")) upo.setSex(usersex);
            String descn=m.get("Descn")+"";
            if (!descn.toLowerCase().equals("null")) upo.setDescn(descn);
            if(userService.updateUser(upo)==1) {
            	map.put("ReturnType", "1001");
            	map.put("Message", "修改成功");
            	return map;
            }
		} catch (Exception e) {
			
		}
		return null;
	}
}