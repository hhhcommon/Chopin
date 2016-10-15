package com.spiritdata.dataanal.visitmanage.run.monitor;

import com.spiritdata.dataanal.visitmanage.run.mem.VisitMemoryService;

public class Save2DB extends Thread {
    private VisitMemoryService vms = null;

    public Save2DB() {
        super();
        this.setName("SaveVisitLog2DB");
        this.vms = VisitMemoryService.getInstance();
    }

    @Override
    /**
     * 把访问队列中的信息写入数据库
     */
    public void run() {
        while (true) {
            try {
                this.vms.Save2DB();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}