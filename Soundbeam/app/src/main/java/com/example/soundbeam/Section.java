package com.example.soundbeam;

/**
 * Created by FD on 09.07.2016.
 */
public class Section {

    private int A,R,G,B;
    static public int WIDTH,HEIGHT;

    public void setA(int A){this.A = A;}
    public void setR(int R){this.R = R;}
    public void setG(int G){this.G = G;}
    public void setB(int B){this.B = B;}

    public int getA() {
        return A;
    }

    public int getB() {
        return B;
    }

    public int getG() {
        return G;
    }

    public int getR() {
        return R;
    }

    static public int[][] sectionsToInfo(Section[] sections){
        int[][] info = new int[sections.length][4];
        for(int i = 0; i < sections.length;i++){
            info[i][0] = sections[i].getA();
            info[i][1] = sections[i].getR();
            info[i][2] = sections[i].getG();
            info[i][3] = sections[i].getB();
        }
        return info;
    }

}
