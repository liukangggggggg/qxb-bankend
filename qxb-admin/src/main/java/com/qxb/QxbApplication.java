package com.qxb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class QxbApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(QxbApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  服务启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "    /\\_____/\\    \n" +
                "   /  o   o  \\   \n" +
                "  ( ==  ^  == )  \n" +
                "   )         (   \n" +
                "  (           )  \n" +
                " ( (  )   (  ) )  \n" +
                "(__(__)___(__)__)");
    }
}
