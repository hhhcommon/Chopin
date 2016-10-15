package com.spiritdata.dataanal.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * 随机数生成器。
 * 他能实现在一个整数范围内[n~m]（闭区间），随机挑选k个不重复的有用随机数的过程。
 * 注意：
 * 1-这个过程不是一次生成的，是随用随生成。
 * 2-k个有用的随机数是指如下场景：简单情况，list[1..100]中，存有0到30个自然数，从中随机取出10个非0的自然数。
 *   复杂场景可能是，100万行记录的数据库表，其中有些行不符合要求，随机从中取出100行符合要求的数据。
 * 
 * 若k>=m-n则范围内的所有数都会被取到
 * @author wh
 */
public class SpiritRandom {
    protected Random _r1 = null; //生成随机数用到的
    protected Random _r2 = null; //从辅助列表中得到数据时用到的

    //随机数范围开始值
    private int beginNum = -1;
    public int getBeginNum() {
        return beginNum;
    }
    //随机数范围结束值
    private int endNum = -1;
    public int getEndNum() {
        return endNum;
    }
    //获取随机数个数
    private int randomSize = -1;
    public int getRandomSize() {
        return randomSize;
    }
    //随机缓存大小，默认值
    private int _defaultRandomCachSize = 100;
    //随机数缓存大小
    private int randomCachSize = -1;

    //以下为标记数据
    private int _rangeSize; //范围内数据的个数
    private int _remainSize = 0; //范围内未被获取的数据的个数
    private boolean _isUsed = false; //是否正在被使用
    private Integer _currentRandom = null; //当前的随机数

    /*
     * 随机数缓存数组，获取随机数就是从这个数组取数据
     */
    private List<Integer> _randomCache = null;
    /*
     * 被使用的随机数列表
     */
    private List<Integer> _usedRandom = null;
    /*
     * 辅助生成随机数的列表
     */
    private List<int[]> _assistList = null;

    //===============以下为公共方法
    public SpiritRandom() {
        this.randomCachSize = -1;
    }

    /**
     * 构造随机数生成器
     * @param beginNum 范围开始值
     * @param endNum 范围结束值
     * @param randomSize 有用随机数个数
     * @throws InstantiationException 
     * @throws Exception 
     */
    public SpiritRandom(int beginNum, int endNum, int randomSize) throws InstantiationException {
        super();
        try {
            init(beginNum, endNum, randomSize);
        } catch(Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * 构造随机数生成器
     * @param beginNum 范围开始值
     * @param endNum 范围结束值
     * @param randomSize 有用随机数个数
     * @param randomCachSize 随机数缓存大小
     * @throws InstantiationException 
     * @throws Exception 
     */
    public SpiritRandom(int beginNum, int endNum, int randomSize, int randomCachSize) throws InstantiationException {
        super();
        try {
            init(beginNum, endNum, randomSize, randomCachSize);
        } catch(Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * 初始化随机数生成器
     * @param beginNum 范围开始值
     * @param endNum 范围结束值
     * @param randomSize 有用随机数个数
     * @throws Exception
     */
    public void init(int beginNum, int endNum, int randomSize) throws Exception {
        if (isUsed()) throw new Exception("随机数生成器正在使用，不能初始化");

        this.beginNum = beginNum;
        this.endNum = endNum;
        this.randomSize = randomSize;

        this.randomCachSize = -1;
        init();
    }

    /**
     * 初始化随机数生成器
     * @param beginNum 范围开始值
     * @param endNum 范围结束值
     * @param randomSize 有用随机数个数
     * @param randomCachSize 随机数缓存大小
     * @throws Exception
     */
    public void init(int beginNum, int endNum, int randomSize, int randomCachSize) throws Exception {
        if (isUsed()) throw new Exception("随机数生成器正在使用，不能初始化");

        this.beginNum = beginNum;
        this.endNum = endNum;
        this.randomSize = randomSize;

        this.randomCachSize = -1;
        this.setRandamCachSize(randomCachSize);
        init();
    }

    /**
     * 设置缓存大小
     */
    public void setRandamCachSize(int randomCachSize) throws Exception {
        if (isUsed()) throw new Exception("随机数生成器正在使用，不能设置缓存大小");
        if (randomCachSize-1>this.endNum-this.beginNum) this.randomCachSize = this.endNum-this.beginNum+1;
        else this.randomCachSize = randomCachSize;
    }

    /**
     * 获取当前缓存大小
     * @return
     */
    public int getRandamCacheSize() {
        return _randomCache==null?0:_randomCache.size();
    }

    /**
     * 获取缓存设置大小
     * @return
     */
    public int getRandamCacheSetSize() {
        if (this.randomCachSize!=-1) return this.randomCachSize;
        else return _defaultRandomCachSize;
    }

    /**
     * 获取下一个随机数，若随机数在范围内已经被用完，则抛出异常
     * @return 随机数
     * @throws Exception
     */
    public int getNextRandom() throws Exception {
        if (isComplete()){
            if (_remainSize==0) throw new IndexOutOfBoundsException("范围内所有数已取完");
            if (_usedRandom!=null&&_usedRandom.size()==randomSize) throw new IndexOutOfBoundsException("有用随机数已经取到了上限["+randomSize+"]");
        }
        if (_randomCache==null||_randomCache.size()==0) buildRandamCache();
        _currentRandom = _randomCache.remove(0);
        _remainSize--;
        _isUsed = true; //已经使用
        return _currentRandom;
    }

    /**
     * 设置当前随机数为有用
     */
    public void setCurrentRandomUsed() {
        if (_usedRandom==null) _usedRandom = new ArrayList<Integer>();
        if (_currentRandom!=null) _usedRandom.add(_currentRandom);
        _currentRandom = null;
    }

    /**
     * 随机数是否已经用完
     * @return
     */
    public boolean isComplete() {
        if (_remainSize==0) return true;
        if (_usedRandom!=null) return _usedRandom.size()==randomSize;
        return false;
    }

    /**
     * 当范围内有用的随机数都获取完成后，得到所有有用的随机数。
     * 有用的随机数可能会小于随机数的个数
     * @return 有用随机数列表
     * @throws Exception 
     */
    public List<Integer> getAllUsedRandamListBeforeComplete() throws Exception {
        if (isUsed()) throw new Exception("随机数生成器正在使用，不能获取所有有用的随机列表");
        return _usedRandom;
    }

    /**
     * 清除当前的随机数生成器，当生成器在使用时，不能被清除
     */
    public void clean() throws Exception {
        if (isComplete()) _isUsed = false;
        else {
            if (isUsed()) throw new Exception("随机数生成器正在使用，不能进行清除操作");
        }
        init();
    }

    /**
     * 强制清除当前的随机数生成器
     */
    public void forceClean() throws Exception {
        init();
    }

    //================以下为私有方法
    /*
     * 初始化随机数构造器
     * @throws Exception
     */
    private void init() throws Exception {
        if (beginNum==-1) throw new Exception("随机数范围下限未设置");
        if (endNum==-1) throw new Exception("随机数范围上限未设置");
        if (randomSize==-1) throw new Exception("随机数获取个数未设置");
        if (isUsed()) throw new Exception("随机数生成器正在使用，不能初始化");
        if (beginNum>endNum) throw new Exception("随机数下限beginNum必须小于等于上限endNum，当前输入的值为[beginNum="+beginNum+"][endNum="+endNum+"]");

        _rangeSize = (endNum-beginNum)+1;
        _remainSize = _rangeSize;
        _isUsed = false;
        _currentRandom = null;
        _randomCache = null;
        _usedRandom = null;
        _assistList = null;
        _r1 = new Random();
        _r2 = new Random();
        if (this.randomCachSize==-1) caculateDefaultRandomCachSize();

//        logger.info("初始化成功：数据范围为["+this.beginNum+".."+this.endNum+"],随机数个数为{"+this.randomSize+"},随机数缓存大小为{"+this.getRandamCacheSetSize()+"}");
        buildRandamCache();
    }
    private void caculateDefaultRandomCachSize() {
        //计算范围的1/10
        _defaultRandomCachSize = ((this._rangeSize/10)<_defaultRandomCachSize)?(this._rangeSize/10):_defaultRandomCachSize;
        if (_defaultRandomCachSize==0) _defaultRandomCachSize=this._rangeSize;
    }

    /*
     * 随机数生成器实例是否正在使用
     * @return 正在使用true，否则false
     */
    private boolean isUsed() {
        return _isUsed;
    }

    /*
     * 生成随机数缓存列表
     */
    private void buildRandamCache() {
        if (_randomCache==null) _randomCache=new ArrayList<Integer>();
        _randomCache.clear();

        List<int[]> l;
        int[] range;
        int cacheSize = this.getRandamCacheSetSize();
        do {
            if (_assistList==null) {
                _assistList = new ArrayList<int[]>();
                range = new int[2];
                range[0]=this.beginNum;
                range[1]=this.endNum;
            } else {
                int index = SpiritRandom.getRandom(this._r2, 0, _assistList.size()-1);
                range= _assistList.remove(index);
            }
            l = getRandom(range);
            for (int i=0; i<l.size(); i++) {
                if (i==0) _randomCache.add(l.get(i)[0]); 
                else _assistList.add(l.get(i));
            }
        } while(_randomCache.size()<cacheSize);
    }

    /**
     * 把参数范围数组用生成数分割的两个范围数组， 
     * @param range 范围数组
     * @return List[0]=[r];List[1]=[b..r-1];List[2]=[r+1..e];分割后的两个范围数组
     */
    private List<int[]> getRandom(int[] range) {
        List<int[]> ret = new ArrayList<int[]>();
        int b = range[0], e=range[1];
        int newR = SpiritRandom.getRandom(this._r1, b, e);
        
        int[] random={newR};
        ret.add(random);
        if (b!=e) {
            if (newR==b) {
                int[] retRang1 = {b+1, e};
                ret.add(retRang1);
            } else if (newR==e) {
                int[] retRang2 = {b, e-1};
                ret.add(retRang2);
            } else {
                int[] retRang1 = {b, newR-1};
                ret.add(retRang1);
                int[] retRang2 = {newR+1, e};
                ret.add(retRang2);
            }
        }
        return ret;
    }

    /**
     * 得到随机数的静态方法
     * @param r 随机数生成方法类
     * @param begin 整数范围下限(包括)
     * @param end 整数范围上限(包括)
     * @return 在[begin..end]之间的整数
     */
    public static int getRandom(Random r, int begin, int end) {
        if (begin>end) throw new IllegalArgumentException("下限begin必须小于等于上限end，当前输入的值为[begin="+begin+"][end="+end+"]");

        if (begin==end) return begin;

        int _begin=begin-1, _end=end+1;
        int ret = _begin;
        while (ret==_end||ret==_begin) {
            float fr = r.nextFloat();
            fr = (_begin+fr*(_end-_begin))+0.5f;
            Float f = fr;
            ret=f.intValue();
        }
        return ret;
    }
}