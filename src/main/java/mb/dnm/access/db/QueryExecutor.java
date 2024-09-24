package mb.dnm.access.db;

import mb.dnm.core.context.TransactionContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class QueryExecutor {
    @Getter
    private SqlSessionFactory sqlSessionFactory;
    private Map<ExecutorType, SqlSessionTemplate> sqlSessionTemplateMap;
    @Setter @Getter
    private int defaultPatchSize = 0;
    @Setter @Getter
    private ExecutorType defaultExecutorType = ExecutorType.BATCH;

    public QueryExecutor() {
    }

    public void initialize(SqlSessionFactory sqlSessionFactory) {
        if (sqlSessionFactory == null) {
            throw new NullPointerException("sqlSessionFactory is null");
        }
        this.sqlSessionFactory = sqlSessionFactory;
        sqlSessionTemplateMap = new HashMap<>();
        log.debug("QueryExecutor initialized with SqlSessionFactory: {}", sqlSessionFactory);
    }

    public SqlSessionTemplate getDefaultExecutor() {
        return getSqlSessionTemplate(defaultExecutorType);
    }

    private SqlSessionTemplate getSimpleExecutor() {
        return getSqlSessionTemplate(ExecutorType.SIMPLE);
    }

    private SqlSessionTemplate getBatchExecutor() {
        return getSqlSessionTemplate(ExecutorType.BATCH);
    }

    private SqlSessionTemplate getSqlSessionTemplate(ExecutorType executorType) {
        SqlSessionTemplate sessionTemplate = sqlSessionTemplateMap.get(executorType);
        if (sessionTemplate == null) {
            sessionTemplate = new SqlSessionTemplate(sqlSessionFactory, executorType);
            sqlSessionTemplateMap.put(executorType, sessionTemplate);
        }
        return sessionTemplate;
    }

    public List<Map<String, Object>> doSelect(TransactionContext txCtx, String sqlId) {
        return getDefaultExecutor().selectList(sqlId);
    }

    public List<Map<String, Object>> doSelect(TransactionContext txCtx, String sqlId, Map<String, Object> selectParam) {
        return getDefaultExecutor().selectList(sqlId, selectParam);
    }

    public List<Map<String, Object>> doSelects(TransactionContext txCtx, String sqlId, List<Map<String, Object>> selectParam) {
        List<Map<String, Object>> result = new ArrayList<>();
        SqlSessionTemplate executor = getDefaultExecutor();

        if (selectParam == null || selectParam.isEmpty()) {
            List<Map<String, Object>> subResult = executor.selectList(sqlId, null);
            result.addAll(subResult);
        } else {
            for (Map<String, Object> param : selectParam) {
                List<Map<String, Object>> subResult = executor.selectList(sqlId, param);
                result.addAll(subResult);
            }
        }
        return result;
    }

    public List<Map<String, Object>> doCall(TransactionContext txCtx, String sqlId, List<Map<String, Object>> callParam) {
        List<Map<String, Object>> result = new ArrayList<>();
        SqlSessionTemplate executor = getDefaultExecutor();
        if (callParam == null || callParam.isEmpty()) {
            Object obj = executor.selectList(sqlId);
            if (obj != null)
                result = (List<Map<String, Object>>) obj;
        } else {
            for (Map<String, Object> param : callParam) {
                Object obj = executor.selectList(sqlId, param);
                if (obj != null)
                    result.addAll((List<Map<String, Object>>) obj);
            }
        }

        return result;
    }

    /**
     * Not always available in all databases
     * */
    public void doOpenCursor(TransactionContext txCtx, String sqlId) {
        getDefaultExecutor().selectOne(sqlId);
    }

    /**
     * Not always available in all databases
     * */
    public List<Map<String, Object>> doFetch(TransactionContext txCtx, String sqlId) {
        List<Map<String, Object>> result = getDefaultExecutor().selectList(sqlId);
        return result;
    }

    public int doInsert(TransactionContext txCtx, String sqlId, Map<String, Object> insertRow) {
        return getDefaultExecutor().insert(sqlId, insertRow);
    }

    
    public int doUpdate(TransactionContext txCtx, String sqlId, Map<String, Object> updateParam) {
        return getDefaultExecutor().update(sqlId, updateParam);
    }

    
    public int doDelete(TransactionContext txCtx, String sqlId, Map<String, Object> deleteParam) {
        return getDefaultExecutor().delete(sqlId, deleteParam);
    }


    public int doBatchInsert(TransactionContext txCtx, String sqlId, List<Map<String, Object>> insertRows) {
        return doBatchInsert(txCtx, sqlId, insertRows, defaultPatchSize);
    }

    
    public int doBatchInsert(TransactionContext txCtx, String sqlId, List<Map<String, Object>> insertRows, int patchSize) {
        SqlSessionTemplate session = getBatchExecutor();
        int insertCount = 0;

        if (insertRows == null || insertRows.isEmpty()) {
            return doInsert(txCtx, sqlId, null);
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

    
    public int doBatchUpdate(TransactionContext txCtx, String sqlId, List<Map<String, Object>> updateParams) {
        return doBatchUpdate(txCtx, sqlId, updateParams, defaultPatchSize);
    }


    public int doBatchUpdate(TransactionContext txCtx, String sqlId, List<Map<String, Object>> updateParams, int patchSize) {
        SqlSessionTemplate session = getBatchExecutor();
        int updateCount = 0;

        if (updateParams == null || updateParams.isEmpty()) {
            return doUpdate(txCtx, sqlId, null);
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

    
    public int doBatchDelete(TransactionContext txCtx, String sqlId, List<Map<String, Object>> deleteParams) {
        return doBatchDelete(txCtx, sqlId, deleteParams, defaultPatchSize);
    }

    
    public int doBatchDelete(TransactionContext txCtx, String sqlId, List<Map<String, Object>> deleteParams, int patchSize) {
        SqlSessionTemplate session = getBatchExecutor();
        int deleteCount = 0;

        if (deleteParams == null || deleteParams.isEmpty()) {
            return doDelete(txCtx, sqlId, null);
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

    int getBatchResultCount(List<BatchResult> batchResults) {
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
