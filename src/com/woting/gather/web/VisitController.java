package com.woting.gather.web;

import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spiritdata.dataanal.common.model.Owner;
import com.spiritdata.dataanal.common.util.SessionUtils;
import com.spiritdata.dataanal.visitmanage.core.persistence.pojo.VisitLogPo;
import com.spiritdata.dataanal.visitmanage.run.mem.VisitMemoryService;
import com.spiritdata.framework.util.StringUtils;

/**
 * 主要用来写访问信息
 * @author wh
 */
@Controller
@RequestMapping(value="")
public class VisitController {

    @RequestMapping("/gather.do")
    public void save(HttpServletRequest req) throws InterruptedException {
        if (StringUtils.isNullOrEmptyOrSpace(req.getParameter("objType"))) return ;

        Owner o = SessionUtils.getOwner(req.getSession());
        VisitLogPo vlp = new VisitLogPo();
        vlp.setOwnerId(o.getOwnerId());
        vlp.setOwnerType(o.getOwnerType());
        String temp = req.getParameter("pointInfo");
        vlp.setPointInfo(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("clientIp");
        vlp.setClientIp(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("clientMac");
        vlp.setClientMac(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("equipName");
        vlp.setEquipName(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("equipVer");
        vlp.setEquipVer(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("exploreName");
        vlp.setExploreName(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("exploreVer");
        vlp.setExploreVer(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("objType");
        vlp.setObjType(StringUtils.isNullOrEmptyOrSpace(temp)?null:Integer.parseInt(temp));
        temp = req.getParameter("objId");
        vlp.setObjId(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("objUrl");
        vlp.setObjUrl(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("fromUrl");
        vlp.setFromUrl(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);

        vlp.setVisitTime(new Timestamp(System.currentTimeMillis()));

        VisitMemoryService vms=VisitMemoryService.getInstance();
        vms.put2Queue(vlp);
    }

    @RequestMapping("/_gather.gif")
    public void gatherWeb(HttpServletRequest req) throws InterruptedException {
        VisitLogPo vlp = new VisitLogPo();

        Owner o = SessionUtils.getOwner(req.getSession());
        vlp.setOwnerId(o.getOwnerId());
        vlp.setOwnerType(o.getOwnerType());

        String temp = req.getParameter("pointInfo");
        vlp.setPointInfo(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("clientIp");
        vlp.setClientIp(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("clientMac");
        vlp.setClientMac(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("equipName");
        vlp.setEquipName(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("equipVer");
        vlp.setEquipVer(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("exploreName");
        vlp.setExploreName(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("exploreVer");
        vlp.setExploreVer(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("objType");
        vlp.setObjType(StringUtils.isNullOrEmptyOrSpace(temp)?null:Integer.parseInt(temp));
        temp = req.getParameter("objId");
        vlp.setObjId(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("objUrl");
        vlp.setObjUrl(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        temp = req.getParameter("fromUrl");
        vlp.setFromUrl(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);

        vlp.setVisitTime(new Timestamp(System.currentTimeMillis()));

        VisitMemoryService vms = VisitMemoryService.getInstance();
        vms.put2Queue(vlp);
    }
}