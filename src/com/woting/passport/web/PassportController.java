package com.woting.passport.web;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.SpiritRandom;
import com.spiritdata.framework.util.StringUtils;
import com.spiritdata.framework.util.RequestUtils;
import com.woting.content.manage.utils.RedisUtils;
import com.woting.passport.UGA.persistence.pojo.UserPo;
import com.woting.passport.UGA.service.UserService;
import com.woting.plugins.sms.SendSMS;


@Controller
@RequestMapping(value="/passport/")
public class PassportController {
	@Resource
	private UserService userService;
	
	/**
	 * 用户登录
	 * @param request
	 * @return
	 */
	@RequestMapping(value="user/login.do")
	@ResponseBody
	public Map<String, Object> loginin(HttpServletRequest request){
		Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
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
//            if (u!=null) {
//                sk.setUserId(u.getUserId());
//                map.put("SessionId", sk.getSessionId());
//                //3.1-处理Session
//                smm.expireAllSessionByIMEI(sk.getMobileId()); //作废所有imei对应的Session
//                MobileSession ms=new MobileSession(sk);
//                ms.addAttribute("user", u);
//                smm.addOneSession(ms);
//                //3.2-保存使用情况
//                MobileUsedPo mu=new MobileUsedPo();
//                mu.setImei(sk.getMobileId());
//                mu.setStatus(1);
//                mu.setPCDType(sk.getPCDType());
//                mu.setUserId(u.getUserId());
//                muService.saveMobileUsed(mu);
//            }
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
	@RequestMapping(value="user/logout.do")
	@ResponseBody
	public Map<String, Object> logout(HttpServletRequest request){
		
		return null;
	}
	
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
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
                return map;
            }

            String ln=(m.get("UserName")==null?null:m.get("UserName")+"");
            String pwd=(m.get("Password")==null?null:m.get("Password")+"");
            String phonenum = m.get("MainPhoneNum")+"";
            String checknum = m.get("CheckNum")+"";
            String errMsg="";
            if (StringUtils.isNullOrEmptyOrSpace(ln)) errMsg+=",用户名为空";
            char[] c=ln.toCharArray();
            if (c[0]>='0' && c[0]<='9') errMsg+=",登录名第一个字符不能是数字";
            if (StringUtils.isNullOrEmptyOrSpace(pwd)) errMsg+=",密码为空";
            if(phonenum.toLowerCase().equals("null")) errMsg+=",手机号为空";
            if(checknum.toLowerCase().equals("null")) errMsg+=",验证码为空";
            if (!StringUtils.isNullOrEmptyOrSpace(errMsg)) {
                errMsg=errMsg.substring(1);
                map.put("ReturnType", "1002");
                map.put("Message", errMsg+",无法注册");
                return map;
            }
            UserPo nu=new UserPo();
            nu.setLoginName(ln);
            nu.setPassword(pwd);
            //1-判断是否有重复的用户
            UserPo oldUser=userService.getUserByLoginName(ln);
            if (oldUser!=null) { //重复
                map.put("ReturnType", "1003");
                map.put("Message", "登录名重复,无法注册.");
                return map;
            }
            //1.5-手机号码注册
            errMsg="";
            String checkinfo = RedisUtils.getPhoneCheckInfo(phonenum);
            if(StringUtils.isNullOrEmptyOrSpace(checkinfo)) {
            	map.put("ReturnType", "1004");
                map.put("Message", "验证信息为空");
                return map;
            }
            String[] check = checkinfo.split("::");
            if((System.currentTimeMillis()-Long.valueOf(check[0]))>6000000) {
            	map.put("ReturnType", "1004");
                map.put("Message", "验证信息超时");
                return map;
            }
            if(check[1].equals(checknum)) {
            	nu.setMainPhoneNum(phonenum);
            }
            //2-保存用户
            nu.setCTime(new Timestamp(System.currentTimeMillis()));
            nu.setUserType(1);
            nu.setUserId(SequenceUUID.getUUIDSubSegment(4));
            int rflag=userService.insertUser(nu);
            if (rflag!=1) {
                map.put("ReturnType", "1005");
                map.put("Message", "注册失败，新增用户存储失败");
                return map;
            }
            //3-处理redis里验证信息
            RedisUtils.multiPhoneCheckInfo(phonenum);
           
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
	 * 第三方登录
	 * @param request
	 * @return
	 */
	@RequestMapping(value="user/afterThirdAuth.do")
	@ResponseBody
	public Map<String, Object> afterThirdAuth(HttpServletRequest request){
		Map<String,Object> map=new HashMap<String, Object>();
		try {
			//0-获取参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
                return map;
            }
            String thirdType = m.get("ThirdType")+"";
            String thirdUserId = m.get("ThirdUserId")+"";
            String thirdUserName = m.get("ThirdUserName")+"";
            String thirdUserImg = m.get("ThirdUserImg")+"";
            Map<String, Object> thirdUserInfo = (Map<String, Object>) m.get("ThirdUserInfo");
            String errMsg="";
            //1-获得用户信息
            if(thirdType.toLowerCase().equals("null")) errMsg+=",无法获得第三方登录类型";
            if(thirdUserId.toLowerCase().equals("null")) errMsg+=",无法获取第三方用户Id";
//            if(thirdUserName.toLowerCase().equals("null")) errMsg+=",无法获取第三方用户名称";
            if (!StringUtils.isNullOrEmptyOrSpace(errMsg)) {
                errMsg=errMsg.substring(1);
                map.put("ReturnType", "1002");
                map.put("Message", errMsg);
                return map;
            }
            //2第三方登录
            Map<String, Object> rm = userService.thirdLogin(thirdType, thirdUserId, thirdUserName, thirdUserImg, thirdUserInfo);
            //3设置返回值
            map.put("IsNew", "True");
            if ((Integer)rm.get("count")>1) map.put("IsNew", "False");
            map.put("UserInfo", ((UserPo)rm.get("userInfo")).toHashMap4Mobile());
            map.put("ReturnType", "1001");
            return map;
		} catch (Exception e) {
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
		Map<String, Object> map = new HashMap<String,Object>();
		try {
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
                return map;
            }
            String  userid = m.get("UserId")+"";
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
            UserPo upo = new UserPo();
            upo.setUserId(userid);
            String userimg = m.get("UserImg")+"";
            if(!userimg.toLowerCase().equals("null")) upo.setPortraitBig(userimg);
            String userage = m.get("Age")+"";
            if(!userage.toLowerCase().equals("null")) upo.setAge(userage);
            String userbirgth = m.get("Birthday")+"";
            if(!userbirgth.toLowerCase().equals("null")) upo.setBirthday(userbirgth);
            String usersex = m.get("Sex")+"";
            if(!usersex.toLowerCase().equals("null")) upo.setSex(usersex);
            String descn = m.get("Descn")+"";
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
	
	/**
     * 用手机号注册
     */
    @RequestMapping(value="user/registerByPhoneNum.do")
    @ResponseBody
    public Map<String,Object> registerByPhoneNum(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
                return map;
            }

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
                //向Redis中加入验证信息
                RedisUtils.addPhoneCheckInfo(phoneNum, checkNum);
                map.put("SmsRetNum", smsRetNum);
            } else {
                map.put("ReturnType", "1002");
                map.put("Message","手机号已绑定");
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
            String userid = m.get("UserId")+"";
            if(userid.toLowerCase().equals("null")) {
            	map.put("ReturnType", "1002");
                map.put("Message", "无法获取用户id");
                return map;
            }
            String errMsg = "";
            String oldpassword = m.get("OldPassword")+"";
            if(oldpassword.toLowerCase().equals("null")) errMsg+=",无旧密码";
            String newpassword = m.get("NewPassword")+"";
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
            UserPo u = userService.getUserById(userid);
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
            UserPo newuser = new UserPo();
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
}
