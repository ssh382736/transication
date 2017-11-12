package com.transication;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
public class JdbcController {
    @Autowired
    private JdbcConnectionService jdbcService;

    private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    @RequestMapping("/jdbc")
    public String jdbcTransication(){
        threadPool.execute(new MyThread2());
        Connection conn= null;
        PreparedStatement ps1=null;
        PreparedStatement ps2=null;
        PreparedStatement ps3=null;
        try {
//
//            String sql="update account set money = money+100 where account.name=?";
//            String sql1="update account set money = money-100 where account.name=?";
            String sql2="insert into account (name,money) values(?,?)";
            conn=JdbcConnectionService.getConnection();
            conn.setAutoCommit(false);
            //读未提交
            //conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            //读提交
            //conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            //可重复读
            //conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            //完全同步
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
//            ps1 = conn.prepareStatement(sql);
//            ps1.setString(1,"jason");
//            ps1.executeUpdate();
//            ps2= conn.prepareStatement(sql1);
//            ps2.setString(1,"ssh");
//            ps2.executeUpdate();
            ps3= conn.prepareStatement(sql2);
            ps3.setString(1,"lin");
            ps3.setInt(2,1000);
            ps3.executeUpdate();
           // int i = 1/0;
            conn.commit();
            return  "success";
        } catch (Exception e) {
            try {
               conn.rollback();

                return "rollback";
            } catch (Exception e1) {

            }
        }finally {
            try {
                conn.close();
            } catch (Exception e) {

            }
        }
        return "nothing";

    }

    //幻读
    class MyThread2 extends Thread{
        @Override
        public void run() {
            {
                Connection conn= null;
                PreparedStatement ps=null;
                PreparedStatement ps1=null;
                try {
                    String sql="select COUNT(*) FROM account";
                    conn=JdbcConnectionService.getConnection();
                    conn.setAutoCommit(false);
                    conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                    ps = conn.prepareStatement(sql);
                    ResultSet set=ps.executeQuery();
                    int col = set.getMetaData().getColumnCount();
                    while (set.next()) {
                        for (int i = 1; i <= col; i++) {
                            if (i == 1)
                                System.out.print("幻读第一次:"+set.getString(i) + "\t");
                        }
                    }
                    Thread.sleep(1000);
                    ps1= conn.prepareStatement(sql);
                    ResultSet set1=ps1.executeQuery();
                    int col1 = set1.getMetaData().getColumnCount();
                    while (set1.next()){
                        for (int i = 1; i <= col1; i++) {
                            if(i==1)
                                System.out.print("幻读第二次:"+set1.getString(i) + "\t");
                        }
                    }
                    conn.commit();

                } catch (Exception e) {

                }finally {
                    try {
                        conn.close();
                    } catch (Exception e) {

                    }
                }

            }
        }
    }
    //重复读
    class MyThread1 extends Thread{
        @Override
        public void run() {
            {
                Connection conn= null;
                PreparedStatement ps=null;
                PreparedStatement ps1=null;
                try {
                    String sql="select money from account WHERE name ='ssh'";
                    conn=JdbcConnectionService.getConnection();
                    conn.setAutoCommit(false);
                    conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                    ps = conn.prepareStatement(sql);
                    ResultSet set=ps.executeQuery();
                    int col = set.getMetaData().getColumnCount();
                    while (set.next()) {
                        for (int i = 1; i <= col; i++) {
                            if (i == 1)
                                System.out.print("重复读第一次:"+set.getString(i) + "\t");
                        }
                    }
                    Thread.sleep(1000);
                    ps1= conn.prepareStatement(sql);
                    ResultSet set1=ps1.executeQuery();
                    int col1 = set1.getMetaData().getColumnCount();
                    while (set1.next()){
                        for (int i = 1; i <= col1; i++) {
                            if(i==1)
                                System.out.print("重读第二次:"+set1.getString(i) + "\t");
                        }
                    }
                    conn.commit();

                } catch (Exception e) {

                }finally {
                    try {
                        conn.close();
                    } catch (Exception e) {

                    }
                }

            }
        }
    }
    //脏读
    class MyThread extends Thread{
        @Override
        public void run() {
            Connection conn= null;
            PreparedStatement ps=null;
            try {
                //脏读
                String sql="select money from account WHERE name ='ssh'";
                conn=JdbcConnectionService.getConnection();
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                ps = conn.prepareStatement(sql);
                ResultSet set=ps.executeQuery();
                int col = set.getMetaData().getColumnCount();
                while (set.next()){
                    for (int i = 1; i <= col; i++) {
                        if(i==1)
                        System.out.print(set.getString(i) + "\t");
                    }

                }
                conn.commit();

            } catch (Exception e) {

            }finally {
                try {
                    conn.close();
                } catch (Exception e) {

                }
            }

        }
    }

}
