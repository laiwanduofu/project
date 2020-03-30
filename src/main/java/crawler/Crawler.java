package crawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dao.Project;
import dao.ProjectDao;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Crawler {
    private HashSet<String> urlBlackList = new HashSet<>();

    private OkHttpClient okHttpClient = new OkHttpClient();

    private Gson gson = new GsonBuilder().create();

    {
        urlBlackList.add("https://github.com/events");
        urlBlackList.add("https://github.community");
        urlBlackList.add("https://github.com/about");
    }

    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler();
        //1,获取入口页面
        String html = crawler.getPage("https://github.com/akullpp/awesome-java/blob/master/README.md");
        //   System.out.println(html);
        //2，解析入口页面，获取项目列表
        List<Project> projects = crawler.ParseProjectList(html);
   //     System.out.println(projects);
   //     System.out.println("==============================");
        ProjectDao projectDao   =new ProjectDao();
        //3遍历项目列表，调用github API 获取项目信息获取star数,fork数
        for (int i = 0; i < projects.size() && i < 20; i++) {
            Project project = projects.get(i);
            System.out.println("crawing" + project.getName()+"....");
            String repoName = crawler.getRepoName(project.getUrl());
            String jsonString = crawler.getRepoInfo(repoName);
            //           System.out.println(jsonString);
            //           System.out.println("============================");
            //4 ,解析每个仓库获取到的JSon数据，得到需要的信息
            crawler.parseRepoInfo(jsonString, project);
            System.out.println(project);
            System.out.println("===============================");
//            //5,在这个位置，把project保存到数据库中
               projectDao.save(project);
            System.out.println("crawing"+project.getName()+"down!");

             }
        }
        public String getPage (String url) throws IOException {
            //1,先创建一个OKHttpClient 对象,只要一个程序中包含一个实例对象就行

            OkHttpClient okHttpClient = new OkHttpClient();
            //2,创建一个Request对象
            //java中实例化有很多方法，可以直接new ，也可以使用某个静态工厂方法
            //此处的Builder这个类是一个辅助构造Request对象的类
            // Builder中提供的URL方法能够设定当前请求的url;
            Request request = new Request.Builder().url(url).build();
            //3,创建一个call对象（这个对象负责一次网络访问操作）
            Call call = okHttpClient.newCall(request);
            //4,发送请求给服务器,获取到response对象

            Response response = call.execute();
            if (!response.isSuccessful()) {
                System.out.println("请求失败");
                return null;
            }
            return response.body().string();

        }
        public List<Project> ParseProjectList (String html){
            ArrayList<Project> result = new ArrayList<>();
            //使用jSoup分析一下页面结构，把其中的li标签都获取到
            //1，先创建一个Document对象（文档对象，对应一个html)
            // 相当于把一个html字符串转换成Document（相当于一个描述页面的树形结构）对象
            Document document = Jsoup.parse(html);
            //2,使用getElementByTag方法来获取到所有的li标签
            //elements对象相当于是一个集合类，包含了很多Element对象，每个Element就对应一个li标签
            //但是有些li 标签并不是项目
            Elements elements = document.getElementsByTag("li");
            for (Element li : elements) {
                //再获取里面的a标签
                Elements allLink = li.getElementsByTag("a");
                if (allLink.size() == 0) {
                    //说明当前的这个li标签中没有a标签，直接忽略掉这个li
                    continue;
                }
                //一个项目的li标签中，只有一个a 标签
                Element link = allLink.get(0);
                //输出a标签中的内容
//            System.out.println(link.text());
//            System.out.println(link.attr("href"));
//            System.out.println(li.text());
//            System.out.println("=========================");
                //如果当前这个项目的URL不是以https://github.com开头的就，就丢去掉
                String url = link.attr("href");
                if (!url.startsWith("https://github.com")) {
                    continue;
                }
                if (urlBlackList.contains(url)) {
                    continue;
                }
                Project project = new Project();
                project.setName(link.text());
                project.setUrl(link.attr("href"));
                project.setDescription(li.text());
                result.add(project);
            }
            return result;
        }
        //调用 Github API 获取指定仓库的信息
        //仓库名的形式：doov-io/doov
        public String getRepoInfo (String repoName) throws IOException {
            String userName = "laiwanduofu";
            String password = "haha529986";
            //进行身份验证,把用户名密码加密后，得到一个字符串，把这个字符串放到Http Header中
            String credential = Credentials.basic(userName, password);

            String url = "https://api.github.com/repos/" + repoName;
            //OkHttpClient对象前面已经创建了，不需要重复创建
            //请求对象，Call对象，响应对象，还是需要创建的
            Request request = new Request.Builder().url(url).header("Authorization", credential).build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            if (!response.isSuccessful()) {
                System.out.println("访问Github API 失败！url=" + url);
                return null;
            }
            return response.body().string();
        }

        //这个方法的功能，就是把项目的URL提取出其中的仓库名和作者的名字
        //  https://github.com/doov-io/doov =>     doov-io/doov
        public String getRepoName (String url){
            int lastOne = url.lastIndexOf("/");
            int lastTwo = url.lastIndexOf("/", lastOne - 1);
            if (lastOne == -1 || lastTwo == -1) {
                System.out.println("当前url不是一个标准的项目 url! url:" + url);
                return null;
            }
            return url.substring(lastTwo + 1);
        }

        //通过这个方法，获取到该仓库的相关信息
        //第一个参数表示Github API 获取到的结果
        //第二个参数表示解析出的star数，fork数，opened_issue数保存到project对象中
        //使用Gson这个库来进行解析

        public void parseRepoInfo (String jsonString, Project project){
        //
            Type type = new TypeToken<HashMap<String, Object>>() {
            }.getType();

            HashMap<String, Object> map = gson.fromJson(jsonString, type);
            Double starCount = (double) map.get("stargazers_count");
            project.setStarCount(starCount.intValue());
            Double forkCount = (double) map.get("forks_count");
            project.setForkCount(forkCount.intValue());
            Double openedIssueCount = (double) map.get("open_issues_count");
            project.setOpenedIssueCount(openedIssueCount.intValue());
        }

    }
