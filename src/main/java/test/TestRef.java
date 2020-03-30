package test;

import java.lang.reflect.Field;

public class TestRef {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        String str="hello";
        // 1,先创建一个Filed对象
        Field field=String.class.getDeclaredField("value");
        //2,把访问权限设置为可以访问
        field.setAccessible(true);
        //3,根据刚才的field中包含的信息，把str中的value这个数组获取到
       char[] value = (char[]) field.get(str);
    }
}
