package com.mb.transaction;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;

public class TransactionTest {

    @Test
    public void cast_test() {
        SqlSessionFactory sqlSF = (SqlSessionFactory) new SqlSessionFactoryBean();
    }

}
