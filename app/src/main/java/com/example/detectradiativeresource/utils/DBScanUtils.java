package com.example.detectradiativeresource.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @description: DBSCAN的Java实现
 * @author: lyj
 * @create: 2019/09/23
 **/
public class DBScanUtils {
    private static int minpts = 44;
    private static double radius = 0.00001;
    private static List<double[]> cores;
    private static int interval = 800;

    /**
     * @description: 计算距离
     * @author: lyj
     * @create: 2019/09/23
     **/
    private static double countEurDistance(double[] point1, double[] point2) {
        double eurDistance = 0.0;
        for (int i = 0; i < point1.length; i++) {
            eurDistance += Math.abs(point1[i] - point2[i]) * Math.abs(point1[i] - point2[i]);
        }
        return Math.sqrt(eurDistance);
    }

    /**
     * @description: 寻找核心点
     * @author: lyj
     * @create: 2019/09/23
     **/
    private static List<double[]> findCores(ArrayList<double[]> points, int minpts, double radius) {
        List<double[]> cores = new ArrayList<double[]>();
        for (int i = 0; i < points.size(); i++) {
            int pts = 0;
            for (int j = 0; j < points.size(); j++) {
                for (int k = 0; k < points.get(i).length; k++) {
                    if (countEurDistance(points.get(i), points.get(j)) < radius) {
                        pts++;
                    }
                }
            }
            if (pts >= minpts) {
                cores.add(points.get(i));
            }
        }
        return cores;
    }

    /**
     * @description: 确定圆心坐标
     * @author: lyj
     * @create: 2019/09/23
     **/
    private static double[] getCircle(double[] p1, double[] p2, double[] p3) {
        double[] circle = { 0.0, 0.0 };
        double x21 = p2[0] - p1[0];
        double y21 = p2[1] - p1[1];
        double x32 = p3[0] - p2[0];
        double y32 = p3[1] - p2[1];
        if (x21 * y32 - x32 * y21 == 0 || x21 == 0) {
            return circle;
        }
        double xy21 = p2[0] * p2[0] - p1[0] * p1[0] + p2[1] * p2[1] - p1[1] * p1[1];
        double xy32 = p3[0] * p3[0] - p2[0] * p2[0] + p3[1] * p3[1] - p2[1] * p2[1];
        double y0 = (x32 * xy21 - x21 * xy32) / (2 * (y21 * x32 - y32 * x21));
        double x0 = (xy21 - 2 * y0 * y21) / (2.0 * x21);
        circle[0] = x0;
        circle[1] = y0;
        return circle;
    }

    /**
     * @description: 等辐射值三点划分
     * @author: lyj
     * @create: 2019/09/23
     **/
    private static double[][] MySort(ArrayList<ArrayList<Double>> list) {
        double[][] data = new double[list.size()][3];
        for (int i = 0; i < list.size(); i++) {
            data[i][0] = list.get(i).get(0);
            data[i][1] = list.get(i).get(1);
            data[i][2] = list.get(i).get(2);
        }
        Arrays.sort(data, new MyComparator());
        return data;
    }

    private static class MyComparator implements Comparator<double[]> {
        @Override
        public int compare(double[] n1, double[] n2) {
            // TODO Auto-generated method stub
            return n1[2] - n2[2] > 0 ? 1 : -1;
        }

    }

    /**
     * @description: DBSCAN计算过程
     * @author: lyj
     * @create: 2019/09/23
     **/
    public static double[] DBSCAN(ArrayList<ArrayList<Double>> list) {
        double[][] data = MySort(list);
        minpts=list.size()/5;
        ArrayList<double[]> circle = new ArrayList<double[]>();
        for (int i = 0; i < data.length - 2; i++) {
            for (int j = i + 1; j < data.length - 1; j++) {
                if (data[j][2] - data[i][2] > interval) {
                    break;
                }
                for (int n = j + 1; n < data.length; n++) {
                    if (data[n][2] - data[i][2] > interval) {
                        break;
                    }
                    double[] res = getCircle(data[i], data[j], data[n]);
                    if (res[0] != 0.0 || res[1] != 0.0) {
                        circle.add(res);
                    }
                }
            }
        }
        cores = findCores(circle, minpts, radius);
        double[] ans = { 0.0, 0.0 };
        //double[] ans = { 116.364734, 39.968584 };
        for (double[] core : cores) {
            ans[0] += core[0];
            ans[1] += core[1];
        }
        if(cores.size()==0){
            return ans;
        }
        ans[0] /= cores.size();
        ans[1] /= cores.size();
        ans[0] =Double.parseDouble(String.format("%.6f", ans[0]));
        ans[1] =Double.parseDouble(String.format("%.6f", ans[1]));
        return ans;
    }

    public static double[] getTestMsg() {
        double[] ans={0,0};
        try {
            ans = DBSCAN(ArrayToList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ans;
    }

    public static ArrayList<ArrayList<Double>> ArrayToList(){
        double[][] data={{1.00090, 1.00090, 583490}, {1.00091, 1.00090, 584970}, {1.00092, 1.00090, 586080}, {1.00093, 1.00090, 587042},
                {1.00094, 1.00090, 588004}, {1.00095, 1.00090, 588929}, {1.00096, 1.00090, 589817}, {1.00097, 1.00090, 590335},
                {1.00098, 1.00090, 590742}, {1.00099, 1.00090, 590927}, {1.00100, 1.00090, 591038}, {1.00101, 1.00090, 591593},
                {1.00102, 1.00090, 590853}, {1.00103, 1.00090, 590261}, {1.00104, 1.00090, 589669}, {1.00105, 1.00090, 588929},
                {1.00106, 1.00090, 588855}, {1.00107, 1.00090, 587153}, {1.00108, 1.00090, 586043}, {1.00109, 1.00090, 584674},
                {1.00090, 1.00089, 581381}, {1.00091, 1.00089, 582861}, {1.00092, 1.00089, 584082}, {1.00093, 1.00089, 585414},
                {1.00094, 1.00089, 586450}, {1.00095, 1.00089, 587449}, {1.00096, 1.00089, 588078}, {1.00097, 1.00089, 588855},
                {1.00098, 1.00089, 589188}, {1.00099, 1.00089, 589336}, {1.00096, 1.00088, 586413}, {1.00097, 1.00088, 587264},
                {1.00098, 1.00088, 587671}, {1.00099, 1.00088, 587486}, {1.00100, 1.00088, 587375}, {1.00101, 1.00088, 587301},
                {1.00102, 1.00088, 587227}, {1.00103, 1.00088, 586782}, {1.00104, 1.00088, 586043}, {1.00105, 1.00088, 585229},
                {1.00106, 1.00088, 584341}, {1.00107, 1.00088, 583342}, {1.00108, 1.00088, 582306}, {1.00109, 1.00088, 581270},
                {1.00090, 1.00087, 577496}, {1.00091, 1.00087, 579013}, {1.00092, 1.00087, 580382}, {1.00093, 1.00087, 581566},
                {1.00094, 1.00087, 582528}, {1.00095, 1.00087, 583527}, {1.00096, 1.00087, 584266}, {1.00097, 1.00087, 585192},
                {1.00098, 1.00087, 585488}, {1.00099, 1.00087, 585524}, {1.00100, 1.00087, 585377}, {1.00101, 1.00087, 585266},
                {1.00102, 1.00087, 585081}, {1.00103, 1.00087, 584748}, {1.00104, 1.00087, 584156}, {1.00105, 1.00087, 583268},
                {1.00106, 1.00087, 582343}, {1.00107, 1.00087, 581307}, {1.00108, 1.00087, 580382}, {1.00109, 1.00087, 579234},
                {1.00090, 1.00086, 575461}, {1.00091, 1.00086, 576978}, {1.00092, 1.00086, 578384}, {1.00093, 1.00086, 579568},
                {1.00094, 1.00086, 580567}, {1.00095, 1.00086, 581418}, {1.00096, 1.00086, 582084}, {1.00097, 1.00086, 582639},
                {1.00098, 1.00086, 583008}, {1.00099, 1.00086, 583157}, {1.00100, 1.00086, 583231}, {1.00101, 1.00086, 583120},
                {1.00102, 1.00086, 583008}, {1.00103, 1.00086, 582639}, {1.00104, 1.00086, 582084}, {1.00105, 1.00086, 581270},
                {1.00106, 1.00086, 580234}, {1.00107, 1.00086, 579198}, {1.00108, 1.00086, 578088}, {1.00109, 1.00086, 576904}};
        ArrayList<ArrayList<Double>> list=new ArrayList<>();
        for(double[] data1:data){
            ArrayList<Double> list1=new ArrayList<>();
            for(double data2:data1){
                list1.add(data2);
            }
            list.add(list1);
        }
        return list;
    }

    /**
     * @description: 读取MCNP5部分
     * @author: lyj
     * @create: 2019/09/23
     **/
    private static ArrayList<ArrayList<Double>> read() throws IOException {
        String base="/assets/test_";
        ArrayList<ArrayList<Double>> data=new ArrayList<ArrayList<Double>>();
        for(int i=1;i<12;i++) {
            String file=base+i;
            InputStream inputStream = DBScanUtils.class.getResourceAsStream(file);
            InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            for(int j=0;j<20;j++) {
                ArrayList<Double> list=new ArrayList<Double>();
                data.add(list);
            }
            String s;
            int pos=1;
            int latitude_pos=(i-1)*20;
            int value_pos=(i-1)*20;
            while ((s = br.readLine()) != null) {
                String[] str=s.trim().split("\\s+");
                if(pos>=100&&pos<=119){
                    Double longitude= Double.parseDouble(str[2]);
                    Double latitude= Double.parseDouble(str[3]);
                    ArrayList<Double> list=data.get(latitude_pos++);
                    list.add(longitude);
                    list.add(latitude);
                }
                pos++;
                if(str.length==16&&str[0].equals("10000000")){
                    ArrayList<Double> list1=data.get(value_pos++);
                    list1.add((Double.valueOf(str[1])* Math.pow(10, 10)));
                    ArrayList<Double> list2=data.get(value_pos++);
                    list2.add((Double.valueOf(str[6])* Math.pow(10, 10)));
                    ArrayList<Double> list3=data.get(value_pos++);
                    list3.add((Double.valueOf(str[11])* Math.pow(10, 10)));
                }
                if(str.length==11&&str[0].equals("10000000")){
                    ArrayList<Double> list1=data.get(value_pos++);
                    list1.add((Double.valueOf(str[1])* Math.pow(10, 10)));
                    ArrayList<Double> list2=data.get(value_pos++);
                    list2.add((Double.valueOf(str[6])* Math.pow(10, 10)));
                }
            }
        }
        return data;
    }
}

