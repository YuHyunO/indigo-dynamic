package com.mb.access.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MyBatisQueryExecutor {

    private SqlSessionFactory sqlSessionFactory;
    private Map<ExecutorType, SqlSessionTemplate> sqlSessionTemplateMap;
    private int defaultPatchSize = 0;

    public MyBatisQueryExecutor() {
        sqlSessionTemplateMap = new HashMap<>();
    }

    public MyBatisQueryExecutor(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
        sqlSessionTemplateMap = new HashMap<>();
    }

    private SqlSessionTemplate getSqlSessionTemplate(ExecutorType executorType) {
        SqlSessionTemplate sessionTemplate = sqlSessionTemplateMap.get(executorType);
        if (sessionTemplate == null) {
            sessionTemplate = new SqlSessionTemplate(sqlSessionFactory, executorType);
            sqlSessionTemplateMap.put(executorType, sessionTemplate);
        }
        return sessionTemplate;
    }

    private SqlSessionTemplate getSimpleExecutor() {
        return getSqlSessionTemplate(ExecutorType.SIMPLE);
    }

    private SqlSessionTemplate getBatchExecutor() {
        return getSqlSessionTemplate(ExecutorType.BATCH);
    }
    
    public List<Map<String, Object>> select(String sqlId, Map<String, Object> selectParam) {
        return getSimpleExecutor().selectList(sqlId, selectParam);
    }

    
    public int insert(String sqlId, Map<String, Object> insertRow) {
        return getSimpleExecutor().insert(sqlId, insertRow);
    }

    
    public int update(String sqlId, Map<String, Object> updateParam) {
        return getSimpleExecutor().update(sqlId, updateParam);
    }

    
    public int delete(String sqlId, Map<String, Object> deleteParam) {
        return getSimpleExecutor().delete(sqlId, deleteParam);
    }

    
    public int batchInsert(String sqlId, List<Map<String, Object>> insertRows) {
        return batchInsert(sqlId, insertRows, defaultPatchSize);
    }

    
    public int batchInsert(String sqlId, List<Map<String, Object>> insertRows, int patchSize) {
        SqlSessionTemplate session = getBatchExecutor();
        int insertCount = 0;

        if (insertRows == null || insertRows.isEmpty()) {
            return insert(sqlId, null);
        }

        if (patchSize <= 0) {
            for (Map<String, Object> row : insertRows) {
                session.insert(sqlId, row);
            }
            insertCount = getBatchResultCount(session.flushStatements());
        } else {
            int count = 0;
            for (Map<String, Object> row : insertRows) {
                session.insert(sqlId, row);
                ++count;
                if (count % patchSize == 0) {
                    insertCount += getBatchResultCount(session.flushStatements());
                    count = 0;
                }
            }
            if (count > 0) {
                insertCount += getBatchResultCount(session.flushStatements());
            }
        }
        return insertCount;
    }

    
    public int batchUpdate(String sqlId, List<Map<String, Object>> updateParams) {
        return batchUpdate(sqlId, updateParams, defaultPatchSize);
    }

    
    public int batchUpdate(String sqlId, List<Map<String, Object>> updateParams, int patchSize) {
        SqlSessionTemplate session = getBatchExecutor();
        int updateCount = 0;

        if (updateParams == null || updateParams.isEmpty()) {
            return update(sqlId, null);
        }

        if (patchSize <= 0) {
            for (Map<String, Object> param : updateParams) {
                session.update(sqlId, param);
            }
            updateCount = getBatchResultCount(session.flushStatements());
        } else {
            int count = 0;
            for (Map<String, Object> param : updateParams) {
                session.update(sqlId, param);
                ++count;
                if (count % patchSize == 0) {
                    updateCount += getBatchResultCount(session.flushStatements());
                    count = 0;
                }
            }
            if (count > 0) {
                updateCount += getBatchResultCount(session.flushStatements());
            }
        }
        return updateCount;
    }

    
    public int batchDelete(String sqlId, List<Map<String, Object>> deleteParams) {
        return batchDelete(sqlId, deleteParams, defaultPatchSize);
    }

    
    public int batchDelete(String sqlId, List<Map<String, Object>> deleteParams, int patchSize) {
        SqlSessionTemplate session = getBatchExecutor();
        int deleteCount = 0;

        if (deleteParams == null || deleteParams.isEmpty()) {
            return delete(sqlId, null);
        }

        if (patchSize <= 0) {
            for (Map<String, Object> param : deleteParams) {
                session.delete(sqlId, param);
            }
            deleteCount = getBatchResultCount(session.flushStatements());
        } else {
            int count = 0;
            for (Map<String, Object> param : deleteParams) {
                session.delete(sqlId, param);
                ++count;
                if (count % patchSize == 0) {
                    deleteCount += getBatchResultCount(session.flushStatements());
                    count = 0;
                }
            }
            if (count > 0) {
                deleteCount += getBatchResultCount(session.flushStatements());
            }
        }
        return deleteCount;
    }

    
    public void setDefaultPatchSize(int patchSize) {
        if (patchSize < 0) {
            patchSize = 0;
        }
        defaultPatchSize = patchSize;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    private int getBatchResultCount(List<BatchResult> batchResults) {
        if (batchResults.size() == 0)
            return 0;
        int count = 0;
        int[] updateCounts = batchResults.get(0).getUpdateCounts();
        for (int i : updateCounts) {
            count += i;
        }
        return count;
    }

}
