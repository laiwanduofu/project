package api;

import com.google.gson.Gson;
import dao.Project;
import dao.ProjectDao;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
@WebServlet("/login")
//基于多态
public class AllRankServlet extends HttpServlet {
    private Gson gson=new Gson().newBuilder().create();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
        //1,准备工作
        resp.setContentType("application/json; charset=utf-8");
        //2,解析请求，获取其中的日期参数
        String date=req.getParameter("date");
        if(date==null||date.equals("")){
            resp.setStatus(404);
            resp.getWriter().write("date:参数错误");
            return;
        }
        //3,从数据库中查找数据
        ProjectDao projectDao=new ProjectDao();
        List<Project>projects=projectDao.selectProjectByDate(date);
        //4，把数据整理成json格式并返回给客户端
        String respString=gson.toJson(projects);
        resp.getWriter().write(respString);
    }
}
