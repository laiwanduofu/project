package crawler;

import dao.Project;
import dao.ProjectDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadCrawler extends  Crawler {
    public static void main(String[] args) throws IOException {
        //使用多线程的方式
        ThreadCrawler crawler=new ThreadCrawler();
        //1,获取到首页内容
        String html=crawler.getPage("https://github.com/akullpp/awesome-java/blob/master/README.md");
        //2,分析项目列表
        List<Project> projects=crawler.ParseProjectList(html);
        //3,遍历列表，使用多线程的方式，线程池
        //submit():关注任务的结果,传入的参数是实现Runnable接口
        List<Future<?>>taskResults=new ArrayList<>();
        ExecutorService executorService= Executors.newFixedThreadPool(10);
        for(Project project:projects){
          Future<?> taskResult = executorService.submit(new CrawlerTask(project,crawler));
          taskResults.add(taskResult);
        }
        //等待线程池中的所有任务结束，再进行下一步操作
        for(Future<?>taskResult:taskResults){
            //调用get方法就会阻塞，阻塞到该任务执行完毕，get才会运行
            try {
                taskResult.get();
            } catch (InterruptedException  | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
//保存在数据库中
        ProjectDao projectDao=new ProjectDao();
        for(Project project:projects){
            projectDao.save(project);
        }
    }
    static class CrawlerTask implements Runnable{
        private Project project;
        private ThreadCrawler threadCrawler;

        public CrawlerTask(Project project, ThreadCrawler threadCrawler) {
            this.project = project;
            this.threadCrawler = threadCrawler;
        }

        @Override
        public void run() {
            //调用API获取项目数据
            try {
                System.out.println("Crawing"+project.getName()+"...");
                String repoName=threadCrawler.getRepoName(project.getUrl());
                String jsonString=threadCrawler.getRepoInfo(repoName);
                //解析项目数据
                threadCrawler.parseRepoInfo(jsonString,project);
                System.out.println("Crawing"+project.getName()+"done!");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
