package com.spiritdata.dataanal.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 算法类
 * 此类中都是基础算法的静态方法
 * @author wh
 */
public abstract class Arithmetic {

    /**
     * 全组合：给定一个数组(个数为n)，得到这个数组的全组合。即C(1,n)+C(2,n)+...+C(3,n)。
     * 如求["A", "B","C"]的全组合，结果为：
     * {
     *   1:[["A"], ["C"], ["B"]],
     *   2:[["A","B"], ["C", "A"], ["B", "C"]]
     *   3:[["A","C","B"]]
     * }
     * 由于结果书无排序的，因此下面的结果也对：
     * {
     *   1:[["A"], ["B"], ["C"]],
     *   2:[["A","B"], ["B", "C"], ["A", "C"]]
     *   3:[["B","A","C"]]
     * }
     * @param array 数组
     * @return 一个Map，其中key是组合的个数，从1到n，List是具体的组合（这个组合是无序的）
     */
    public static Map<Integer, List<Object[]>> AllCompages(Object[] array) {
        Map<Integer, List<Object[]>> ret =  new HashMap<Integer, List<Object[]>>();
        int _allCount = 1<<(array.length);
        List<Integer> suffix = new ArrayList<Integer>();
        for (int i=1; i<=_allCount-1; i++) {
            int j=0, a=i, b=0;
            suffix.clear();
            do {
                b=a%2;
                a=a/2;
                if (b==1) suffix.add(new Integer(j));
                j++;
            } while (a!=0);
            Object[] _this = new Object[suffix.size()];
            for (int n=0; n<suffix.size(); n++) {
                _this[n] = array[suffix.get(n)];
            }
            Integer c = new Integer(suffix.size());
            if (ret.get(c)!=null) {
                ret.get(c).add(_this);
            } else {
                List<Object[]> l = new ArrayList<Object[]>();
                l.add(_this);
                ret.put(c, l);
            }
        }
        return ret;
    }
}