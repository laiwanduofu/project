package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

public class TestGson {
    static class Test {
        private int aaa=0;
        private int bbb=0;
    }
    public static void main(String[] args) {
        //1, 先创建一个Gson对象
        Gson gson=new GsonBuilder().create();

        // 2, 把键值对数据转化为Json格式的字符串
        //表示一个英雄相关信息
        HashMap<String,String>map=new HashMap<>();
        map.put("name","李白");
        map.put("skill1","突击");
        map.put("skill2","旋转");
        map.put("skill3","斩杀");
        map.put("skill4","加攻速");
        String result=gson.toJson(map);
        System.out.println(result);

        //3,json格式对象转化成字符串
        String jsonString="{\"aaa\":1,\"222\":2}";
        //Test.class 取出当前这个类的类对象
        //formJson依赖反射机制
         Test t= gson.fromJson(jsonString,Test.class);
        System.out.println(t.aaa);
        System.out.println(t.bbb);
    }
}
