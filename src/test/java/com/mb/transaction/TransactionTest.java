package com.mb.transaction;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionTest {

    @Test
    public void cast_test() {
        SqlSessionFactory sqlSF = (SqlSessionFactory) new SqlSessionFactoryBean();
        //TransactionSynchronizationManager
        //TransactionSynchronization
    }

}
