package cn.mcf.myapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class KNN {
    public final int AP = 3;
    public final int BLOCK = 16;
    public Knn_Data[] data = new Knn_Data[BLOCK];

    private String[] names;


    public void init(InputStream is) throws IOException {

        for (int i = 0; i < BLOCK; i++) {
            data[i] = new Knn_Data();
        }

        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader readTxt = new BufferedReader(reader);
        String textLine = "";
        String str = "";
        textLine = readTxt.readLine();  //读第一行
        names = textLine.split(" "); //第一行的名字存于names数组

        while ((textLine = readTxt.readLine()) != null) {
            str += textLine + " ";
        }

        String[] numbersArray = str.split(" ");//括号里还可以改成空格，即读取用空格隔开的数据
        int j = 0;
        //录入指纹
        for (int i = 0; i < numbersArray.length-1; i += 8) {  //一次循环是一个block
            //设置坐标
            data[j].setCoordinate(numbersArray[i] + "," + numbersArray[i + 1]);
            //设置信号
            data[j].setRssi(new double[]{Integer.parseInt(numbersArray[i + 2]), Integer.parseInt(numbersArray[i + 3]), Integer.parseInt(numbersArray[i + 4]), Integer.parseInt(numbersArray[i + 5]), Integer.parseInt(numbersArray[i + 6]), Integer.parseInt(numbersArray[i + 7])});
            j++;
        }

    }

    public int[] minDistance()//找到最近的距离
    {
        double min=data[0].getDistance();
        int index = 0;
        for (int i = 1; i < BLOCK; i++) {
            if(data[i].getDistance()<min){
                min = data[i].getDistance();
                index = i;
            }
        }
        String[] coord = data[index].getCoordinate().split(",");
        int[] coordXY = new int[2];
        for(int i = 0;i<2;i++){
            coordXY[i]=Integer.parseInt(coord[i]);
        }
        return coordXY;
    }

    public void Euclid(String[] givenNames, double[] rssi_li) {
        int[] index = new int[AP];
        int k=0;
        for(int i=0;i<3;i++){
            for(int j=0;j<names.length;j++){
                if (givenNames[i].equals(names[j])){
                    index[k++]=j; //保存被搜寻到的蓝牙设备的序号
                    break;
                }
            }
        }

        for (int i = 0; i < BLOCK; i++) {
            //套公式
            double a = Math.abs((data[i].getRssi())[index[0]] - rssi_li[0]);
            double b = Math.abs((data[i].getRssi())[index[1]] - rssi_li[1]);
            double c = Math.abs((data[i].getRssi())[index[2]] - rssi_li[2]);
            double distance = a*a + b*b + c*c;
            data[i].setDistance(distance);
//            Log.e("Euclid","NO."+i+", distance: "+distance);
        }
    }
}