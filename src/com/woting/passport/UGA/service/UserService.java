package com.woting.passport.UGA.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.UGA.UgaUserService;
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.StringUtils;
import com.woting.passport.UGA.persistence.pojo.UserPo;
import com.woting.passport.thirdlogin.ThirdLoginUtils;
import com.woting.passport.thirdlogin.persis.po.ThirdUserPo;

public class UserService implements UgaUserService {
    @Resource(name="defaultDAO")
    private MybatisDAO<UserPo> userDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<ThirdUserPo> thirdUserDao;

    @PostConstruct
    public void initParam() {
        userDao.setNamespace("WT_USER");
        thirdUserDao.setNamespace("THIRDUSRE");
    }

    @Override
    @SuppressWarnings("unchecked")
    public UserPo getUserByLoginName(String loginName) {
        try {
            return userDao.getInfoObject("getUserByLoginName", loginName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 根据选手
     * @param username
     * @return
     */
    public UserPo getUserByUserName(String username) {
    	try {
    		Map<String, Object> m = new HashMap<>();
    		m.put("whereByClause", "userName='"+username+"' and team is not null limit 1");
			return userDao.getInfoObject("getListByWhere",m);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
     * 根据绑定手机号，获得用户信息
     * @param userNum 用户号码
     * @return 用户信息
     */
    public UserPo getUserByPhoneNum(String phoneNum) {
        try {
            return userDao.getInfoObject("getUserByPhoneNum", phoneNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public UserPo getUserById(String userId) {
        try {
            return userDao.getInfoObject("getUserById", userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<UserPo> getUserByIds(List<String> userIds) {
        try {
            String whereStr="";
            if (userIds!=null&&userIds.size()>0) {
                for (String id: userIds) {
                    whereStr+=" or id='"+id+"'";
                }
            }
            Map<String, String> param=new HashMap<String, String>();
            if (!StringUtils.isNullOrEmptyOrSpace(whereStr)) param.put("whereByClause", whereStr.substring(4));
            param.put("orderByClause", "cTime desc");
            return userDao.queryForList("getListByWhere", param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建用户成功返回1，否则返回0
     */
    public int insertUser(UserPo user) {
        int i=0;
        try {
            if (StringUtils.isNullOrEmptyOrSpace(user.getUserId())) {
                user.setUserId(SequenceUUID.getUUIDSubSegment(4));
            }
            userDao.insert("insertUser", user);
            i=1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    /**
     * 更新用户
     * @param user 用户信息
     * @return 更新用户成功返回1，否则返回0
     */
    public int updateUser(UserPo user) {
        int i=0;
        try {
            userDao.update(user);
            i=1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }
    
    /**
     * 第三方登录
     * @param thirdType 第三方登录类型
     * @param tuserId 第三方中的用户唯一标识
     * @param tuserName 第三方中的用户名称
     * @param tuserImg 第三方用户的头像
     * @param tuserData 第三方用户的其他信息
     * @return Map 当成功则有如下内容：
     *   count——登录的次数
     *   userInfo——用户信息
     */
    public Map<String, Object> thirdLogin(String thirdType, String thirdUserId, String tuserName, String tuserImg, Map<String, Object> tuserData) {
        Map<String, Object> r=new HashMap<String, Object>();
        //查看是否已经存在了
        ThirdUserPo tuPo=thirdUserDao.getInfoObject("getInfoByThirdUserId", thirdUserId);
        UserPo uPo=null;

        if (tuPo==null) {
            //若没有存在：1-加入新的用户
            uPo=new UserPo();
            uPo.setUserId(SequenceUUID.getUUIDSubSegment(4));
            uPo.setLoginName(tuserName);
            uPo.setUserState(1);
            uPo.setUserType(1);
            uPo.setPortraitBig(tuserImg);
            uPo.setPortraitMini(tuserImg);
            uPo.setUserName(tuserName);
            uPo.setPassword("--"+uPo.getUserId());
            ThirdLoginUtils.fillUserInfo(uPo, thirdType, tuserData);
            //若没有存在：2-建立关联关系
            tuPo=new ThirdUserPo();
            tuPo.setId(SequenceUUID.getUUIDSubSegment(4));
            tuPo.setUserId(uPo.getUserId());
            tuPo.setThirdUserId(thirdUserId);
            tuPo.setThirdLoginType(thirdType);
            tuPo.setThirdLoginCount(1);
            tuPo.setThirdUserInfo(JsonUtils.objToJson(tuserData));
            //保存到数据库
            thirdUserDao.insert(tuPo);
            this.insertUser(uPo);
        } else {
            //若存在：加一次登录数
            Map<String, Object> paramM=new HashMap<String, Object>();
            paramM.put("id", tuPo.getId());
            tuPo.setThirdLoginCount(tuPo.getThirdLoginCount()+1);
            paramM.put("thirdLoginCount", tuPo.getThirdLoginCount());
            thirdUserDao.update(paramM);
            uPo=userDao.getInfoObject("getUserById", tuPo.getUserId());
        }
        if (uPo!=null) r.put("userInfo", uPo);
        if (tuPo!=null) r.put("count", tuPo.getThirdLoginCount());
       
        return r;
    }
}