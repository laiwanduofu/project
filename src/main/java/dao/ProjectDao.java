package dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ProjectDao {
    //这个类负责对Project对象进行数据库操作
    public  void save(Project project){
        //通过save 方法能把一个Project对象保存到数据库中
        //获取数据库连接
        Connection connection=DBUtil.getConnection();
        //构造PreparedStatement对象，拼接mysql语句
        PreparedStatement statement=null;
        String sql="insert into project_table values(?,?,?,?,?,?,?)";
        try {
            statement=connection.prepareStatement(sql);
            statement.setString(1,project.getName());
            statement.setString(2,project.getUrl());
            statement.setString(3,project.getDescription());
            statement.setInt(4,project.getStarCount());
            statement.setInt(5,project.getForkCount());
            statement.setInt(6,project.getOpenedIssueCount());
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
            statement.setString(7,simpleDateFormat.format(System.currentTimeMillis()));
            //执行sql语句，完成数据库插入
             int ret=statement.executeUpdate();//发送操作
             if(ret!=1){
                 System.out.println("数据库执行插入失败");
                 return;
             }
            System.out.println("数据库执行成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,null);
        }
    }
    public List<Project> selectProjectByDate(String date){
        ArrayList<Project>projects=new ArrayList<>();
        Connection connection=DBUtil.getConnection();
        String sql="select name,url,starCount,forkCount,"+
                "openedIssueCount from project_table where date=? order by starCount desc";
                PreparedStatement statement=null;
        try {
             statement=connection.prepareStatement(sql);
            statement.setString(1,date);
            //执行sql语句
            ResultSet resultSet=statement.executeQuery();
            //获取数据
            while (resultSet.next()){
                Project project=new Project();
                project.setName(resultSet.getString("name"));
                project.setUrl(resultSet.getString("url"));
                project.setStarCount(resultSet.getInt("starCount"));
                project.setForkCount(resultSet.getInt("forkCount"));
                project.setOpenedIssueCount(resultSet.getInt("openedIssueCount"));
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,null);
        }
        return projects;
    }

}
