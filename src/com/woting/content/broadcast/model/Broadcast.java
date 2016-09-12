package com.woting.content.broadcast.model;

import java.io.Serializable;
import java.util.List;

import com.spiritdata.framework.core.model.ModelSwapPo;
import com.woting.content.broadcast.persis.pojo.BroadcastPo;
import com.woting.content.broadcast.persis.pojo.FrequncePo;
import com.woting.content.broadcast.persis.pojo.LiveFlowPo;

public class Broadcast extends BroadcastPo implements Serializable, ModelSwapPo {
    private static final long serialVersionUID = 5200888821223517280L;

    private List<FrequncePo> frequnces;
    private List<LiveFlowPo> liveflows;

    private String areaName; //所属地区名称
    private String areaAllName; //所属地区全名

    @Override
    public Object convert2Po() {
        return null;
    }

    @Override
    public void buildFromPo(Object po) {
    }
}