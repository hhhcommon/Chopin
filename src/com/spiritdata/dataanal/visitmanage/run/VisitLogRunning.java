package com.spiritdata.dataanal.visitmanage.run;

import com.spiritdata.dataanal.visitmanage.run.mem.VisitMemoryService;
import com.spiritdata.dataanal.visitmanage.run.monitor.Save2DB;

public class VisitLogRunning extends Thread {
    /**
     * 若在另一个JVM中执行任务，这部分功能可以放在main中
     */
    public static void Beginning() {
        VisitLogRunning vlr = new VisitLogRunning();
        vlr.setName("visitServiceMain");
        vlr.start();
    }

    /*
     * 写入数据库县城
     */
    private void save2DB() {
        Save2DB _2DB = new Save2DB();
        _2DB.setDaemon(true);
        _2DB.start();
    }

    /**
     * 启动任务服务的处理主进程
     */
    @Override
    public void run() {
        try {
            sleep(5000);//多少毫秒后启动任务处理，先让系统的其他启动任务完成，这里设置死为10秒钟
            //延迟加载，以便让Spring处理好自己的容器
            System.out.println("===============================");
            System.out.println("访问日志处理服务启动");
            System.out.println("===============================");

            System.out.println("1-初始化访问队列");
            VisitMemoryService vms = VisitMemoryService.getInstance();
            vms.init();
            
            System.out.println("2-启动——[访问日志写入数据库]线程");
            save2DB();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}