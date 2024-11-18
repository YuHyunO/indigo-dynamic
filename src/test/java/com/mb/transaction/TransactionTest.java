package com.mb.transaction;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class TransactionTest {

    @Test
    public void tx_test() {
        //TransactionSynchronizationManager
        //TransactionSynchronization
        //DataSourceTransactionManager
        //AbstractPlatformTransactionManager
        //JdbcTransactionObjectSupport
        //ConnectionHolder
        //SqlSessionUtils
        //SqlSessionTemplate
        //DefaultSqlSessionFactory
        log.info("{}", TransactionSynchronizationManager.getResourceMap());
        log.info("{}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("{}", TransactionSynchronizationManager.getCurrentTransactionIsolationLevel());
        log.info("{}", TransactionSynchronizationManager.isSynchronizationActive());
        log.info("{}", TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        log.info("{}", TransactionSynchronizationManager.isActualTransactionActive());
        //log.info("{}", TransactionSynchronizationManager.getSynchronizations());

    }

}
