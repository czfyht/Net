/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

import java.util.Vector;
import java.util.Random;
import sun.security.x509.OIDMap;
import java.lang.Iterable;
import java.util.Iterator;
import java.lang.Math;
import java.util.Arrays;
import java.util.List;
/**
 *
 * @author HT
 */
class Point {

    int id;     //节点序号
    double x;
    double y;
    double s0;  //节点初始强度
    double s;   //节点强度
    double beta;
    int linkNumber;  //与节点连接的节点数量

    public Point(int id, double x, double y, double s0, double s, double beta, int linkNumber) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.s0 = s0;
        this.s = s;
        this.beta = beta;
        this.linkNumber = linkNumber;
    }
}

public class Net {

    int step;
    int pointNumber;        //网络中节点的数量
    int addNumber;          //新加节点连接的节点数
    public Vector<Point> pointListVector;   //初始所有的节点
    Vector<Point> netPointVector;           //为选出来，处理过的节点
   
    double link[][];    //点的连接矩阵表，如果为0表示着两个点没有连接，如果大于0表示着两点间边的权重
    double w0 = 1.0;  //边的初始权重， 目前统一设为1.0
    Point points[];    //点列表，保存所有的节点信息

    public Net() {
        this.pointListVector = new <Point>Vector();
        this.netPointVector = new <Point>Vector();
        this.step = 0;
        this.addNumber = 1;

    }

    public Net(int pointNumber) {
        this.pointListVector = new <Point>Vector();
        this.netPointVector = new <Point>Vector();
        this.pointNumber = pointNumber;
        this.link = new double[pointNumber][pointNumber];
        for (int i = 0; i < pointNumber; i++) {
            for (int j = 0; j < pointNumber; j++) {
                link[i][j] = 0.0;
            }
        }
        this.step = 0;
        this.addNumber = 1;
        this.points = new Point[pointNumber];

    }

//    void calculatePossibility(){
//        
//    }
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public void addToNet(Vector<Point> netPointVector, Point addPoint, int addNumber) {
        double[] possibility = new double[netPointVector.size()];
        double[] tempPossibility = new double[netPointVector.size()];

        if (netPointVector.isEmpty()) {
            netPointVector.add(addPoint);
        } else {
            for (int i = 0; i < netPointVector.size(); i++) {
                double d = distance(netPointVector.get(i).x, netPointVector.get(i).y, addPoint.x, addPoint.y);
                possibility[i] = (netPointVector.get(i).s + netPointVector.get(i).beta) * Math.pow(d, -0.6 * 3.0);
                tempPossibility = Arrays.copyOf(possibility, netPointVector.size());
                Arrays.sort(tempPossibility);
                for (int j = 0; j < Math.min(netPointVector.size(), addNumber); j++) {
                    int index = Arrays.binarySearch(possibility, tempPossibility[j]);
                    link[addPoint.id][netPointVector.get(index).id] = 1;
                    addPoint.linkNumber++;
                    netPointVector.get(index).linkNumber++;
                }
            }
        }
    }

    public void update(Vector<Point> netPointVector){
        
    }
    public void setPoints(double[][] pointlist) {
        for (int i = 0; i < this.pointNumber; i++) {
            this.points[i] = new Point((int) pointlist[i][0], pointlist[i][1], pointlist[i][2], pointlist[i][3], pointlist[i][4], pointlist[i][5], (int) pointlist[i][6]);
            this.pointListVector.add(this.points[i]);
        }
    }

    void stepAdd() {
        step++;
    }

    void printStep() {
        System.out.println(step);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("Hello World!");
        Random random = new Random();
        Net net = new Net(6);
        int pointNumber = 6;
        int randomInt;



        double[][] pointlist = {{0.0, 114.349, 30.089, 0.0178, 0.0178, 0.0, 0.0},
            {1.0, 114.215, 30.793, 0.0567, 0.0567, 0.0, 0.0},
            {2.0, 114.561, 30.651, 0.0461, 0.0461, 0.0, 0.0},
            {3.0, 114.381, 30.723, 0.0567, 0.0567, 0.0, 0.0},
            {4.0, 114.124, 30.615, 0.0276, 0.0276, 0.0, 0.0},
            {5.0, 114.255, 30.379, 0.0472, 0.0472, 0.0, 0.0}};
        //序号，x, y, s0(节点初始强度), s（节点强度）, β

        net.setPoints(pointlist);

        for (int i = 0; i < pointNumber; i++) {
            net.stepAdd();
//            net.printStep();
            randomInt = random.nextInt(net.pointListVector.size());
            Point addPoint = net.pointListVector.get(randomInt);
            net.pointListVector.remove(randomInt);
            net.addToNet(net.netPointVector, addPoint, net.addNumber);
            
//            net.netPointVector.add(net.pointListVector.get(randomInt));

            //更新beta
            if (net.netPointVector.size() == 1) {
                net.netPointVector.get(0).beta = 0.0;
            } else if (net.netPointVector.size() == 2) {
                for (Point tempPoint : net.netPointVector) {
                    tempPoint.beta = 1.0;
                    tempPoint.linkNumber++;
                }
            } else {
                int insertPointId;
                double possibility = 0;
                Point insertPoint = net.netPointVector.lastElement();
                for (Point tempPoint : net.netPointVector) {

                    tempPoint.beta = 1.0;
                    tempPoint.linkNumber++;
                }
            }
        }

        //更新s
        //更新概率
        //更新邻接表，从这里开始（2013-6-5）
        System.out.println(net.netPointVector.get(0).id);
    }
//        Double[] point;
//        Object Obj = net.netPointVector.get(0);
//        point = (Double[]) Obj;
}
