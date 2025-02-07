package mb.dnm.access.db;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.TransactionContext;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * {@code QueryExecutor}는 데이터베이스 쿼리 작업을 수행하는 객체이다. MyBatis 라이브러리를 기반으로 작동한다.<br><br>
 *
 * @see ExecutorTemplate
 * @see DataSourceProvider
 */
@Slf4j
public class QueryExecutor implements Serializable {
    private static final long serialVersionUID = -447383890877348209L;
    @Getter
    private SqlSessionFactory sqlSessionFactory;
    private Map<ExecutorType, SqlSessionTemplate> sqlSessionTemplateMap;
    @Setter @Getter
    private int defaultFetchSize = 0;
    @Setter @Getter
    private ExecutorType defaultExecutorType = ExecutorType.BATCH;

    /**
     * Instantiates a new Query executor.
     */
    public QueryExecutor() {
    }

    /**
     * Initialize.
     *
     * @param sqlSessionFactory the sql session factory
     */
    public void initialize(SqlSessionFactory sqlSessionFactory) {
        if (sqlSessionFactory == null) {
            throw new NullPointerException("sqlSessionFactory is null");
        }
        this.sqlSessionFactory = sqlSessionFactory;
        sqlSessionTemplateMap = new HashMap<>();
        log.debug("QueryExecutor initialized with SqlSessionFactory: {}", sqlSessionFactory);
    }

    /**
     * Gets default executor.
     *
     * @return the default executor
     */
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

    /**
     * Select 쿼리를 실행하여 결과를 {@code List<Map<String, Object>>} 로 반환한다.
     *
     * @param txCtx TransactionContext 객체
     * @param sqlId 실행될 Select 쿼리의 ID
     * @return 실행된 Select 쿼리의 결과 List. 결과가 없는 경우 빈 List가 반환된다.
     */
    public List<Map<String, Object>> doSelect(TransactionContext txCtx, String sqlId) {
        return getDefaultExecutor().selectList(sqlId);
    }

    /**
     * Select 쿼리를 실행하여 결과를 {@code List<Map<String, Object>>} 로 반환한다.
     * 쿼리를 실행할 때 파라미터로 전달된 Map 을 사용할 수 있다.
     *
     * @param txCtx       TransactionContext 객체
     * @param sqlId       실행될 Select 쿼리의 ID
     * @param selectParam Select 쿼리를 실행할 때 사용할 파라미터
     * @return 실행된 Select 쿼리의 결과 {@code List<Map<String, Object>>}. 결과가 없는 경우 빈 {@code List<Map<String, Object>>}가 반환된다.
     */
    public List<Map<String, Object>> doSelect(TransactionContext txCtx, String sqlId, Map<String, Object> selectParam) {
        return getDefaultExecutor().selectList(sqlId, selectParam);
    }

    /**
     * Select 쿼리를 실행하여 결과를 List 로 반환한다.
     * 쿼리를 실행할 때 파라미터로 전달된 List 을 사용할 수 있다.
     * List 의 size 만큼 Select 쿼리가 반복 수행되며 결과가 합산되어 반환된다.
     *
     * @param txCtx       TransactionContext 객체
     * @param sqlId       실행될 Select 쿼리의 ID
     * @param selectParam Select 쿼리를 실행할 때 사용할 파라미터 List
     * @return 실행된 Select 쿼리의 결과 List. 결과가 없는 경우 빈 List가 반환된다.
     */
    public List<Map<String, Object>> doSelects(TransactionContext txCtx, String sqlId, List<Map<String, Object>> selectParam) {
        return doSelects(txCtx, sqlId, selectParam, null);
    }

    /**
     * Select 쿼리를 실행하여 결과를 List 로 반환한다.
     * 쿼리를 실행할 때 파라미터로 전달된 List 을 사용할 수 있다.
     * List 의 size 만큼 Select 쿼리가 반복 수행되며 결과가 합산되어 반환된다.
     *
     * @param txCtx       TransactionContext 객체
     * @param sqlId       실행될 Select 쿼리의 ID
     * @param selectParam Select 쿼리를 실행할 때 사용할 파라미터 List
     * @param commonParam Select 쿼리를 실행할 때 사용할 공통 파라미터. <code>selectParam</code> 의 size 만큼 쿼리가 실행될 때 <code>commonParam</code> 이 공통 파라미터로서 계속 사용된다.
     * @return 실행된 Select 쿼리의 결과 List. 결과가 없는 경우 빈 List가 반환된다.
     */
    public List<Map<String, Object>> doSelects(TransactionContext txCtx, String sqlId, List<Map<String, Object>> selectParam, Map<String, Object> commonParam) {
        List<Map<String, Object>> result = new ArrayList<>();
        SqlSessionTemplate executor = getDefaultExecutor();

        if (selectParam == null || selectParam.isEmpty()) {
            List<Map<String, Object>> subResult = executor.selectList(sqlId, commonParam);
            result.addAll(subResult);
        } else {
            for (Map<String, Object> param : selectParam) {
                Map<String, Object> addParam = new HashMap<>();
                if (commonParam != null) {
                    addParam.putAll(commonParam);
                }
                addParam.putAll(param);
                List<Map<String, Object>> subResult = executor.selectList(sqlId, addParam);
                result.addAll(subResult);
            }
        }
        return result;
    }

    /**
     * Select 쿼리의 결과를 반환하지 않고 파라미터로 전달될 ResultHandlingSupport 를 통해 결과를 핸들링한다.
     * Select 쿼리의 결과가 어댑터 어플리케이션의 메모리 리소스 부족 문제를 줄 수 있을것으로 예상되는 경우에 사용이 적합하다.
     *
     * @param txCtx                 TransactionContext 객체
     * @param sqlId                 실행될 Select 쿼리의 ID
     * @param resultHandlingSupport 실행된 Select 쿼리의 결과를 핸들링할 ResultHandlingSupport 객체
     * @return int int
     */
    public int doHandleSelect(TransactionContext txCtx, String sqlId, ResultHandlingSupport resultHandlingSupport) {
        return doHandleSelect(txCtx, sqlId, null, null, resultHandlingSupport);
    }


    /**
     * Do handle select int.
     *
     * @param txCtx                 the tx ctx
     * @param sqlId                 the sql id
     * @param selectParam           the select param
     * @param resultHandlingSupport the result handling support
     * @return the int
     */
    public int doHandleSelect(TransactionContext txCtx, String sqlId, Map<String, Object> selectParam, ResultHandlingSupport resultHandlingSupport) {
        List<Map<String, Object>> paramList = null;
        if (selectParam != null && selectParam.isEmpty()) {
            paramList = new ArrayList<>();
            paramList.add(selectParam);
        }
        return doHandleSelect(txCtx, sqlId, paramList, null, resultHandlingSupport);
    }

    /**
     * Do handle select int.
     *
     * @param txCtx                 the tx ctx
     * @param sqlId                 the sql id
     * @param selectParam           the select param
     * @param resultHandlingSupport the result handling support
     * @return the int
     */
    public int doHandleSelect(TransactionContext txCtx, String sqlId, List<Map<String, Object>> selectParam, ResultHandlingSupport resultHandlingSupport) {
        return doHandleSelect(txCtx, sqlId, selectParam, null, resultHandlingSupport);
    }

    /**
     * Do handle select int.
     *
     * @param txCtx                 the tx ctx
     * @param sqlId                 the sql id
     * @param selectParam           the select param
     * @param commonParam           the common param
     * @param resultHandlingSupport the result handling support
     * @return the int
     */
    public int doHandleSelect(TransactionContext txCtx, String sqlId, List<Map<String, Object>> selectParam, Map<String, Object> commonParam, ResultHandlingSupport resultHandlingSupport) {
        int fetchedCnt = 0;
        SqlSessionTemplate executor = getDefaultExecutor();
        txCtx.setConstant(true); /*ResultHandling 이 진행되는 동안 내부 프로세스에서 doHandleSelect(...) 에서 사용되는 트랜잭션을 종료시키지 않기 위한 flag 추가. 2024-12-11 오유현*/
        if (selectParam == null || selectParam.isEmpty()) {
            executor.select(sqlId, commonParam, resultHandlingSupport.getHandler());
            resultHandlingSupport.flushBuffer();
            fetchedCnt = resultHandlingSupport.getTotalFetchedCount();
        } else {
            for (Map<String, Object> param : selectParam) {
                Map<String, Object> addParam = new HashMap<>();
                if (commonParam != null) {
                    addParam.putAll(commonParam);
                }
                addParam.putAll(param);
                executor.select(sqlId, addParam, resultHandlingSupport.getHandler());
                resultHandlingSupport.flushBuffer();
                fetchedCnt += resultHandlingSupport.getTotalFetchedCount();
            }
        }
        return fetchedCnt;
    }

    /**
     * Do call list.
     *
     * @param txCtx     the tx ctx
     * @param sqlId     the sql id
     * @param callParam the call param
     * @return the list
     */
    public List<Map<String, Object>> doCall(TransactionContext txCtx, String sqlId, List<Map<String, Object>> callParam) {
        /*List<Map<String, Object>> result = new ArrayList<>();
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
        }*/

        return doCall(txCtx, sqlId, callParam, null);
    }

    /**
     * Do call list.
     *
     * @param txCtx       the tx ctx
     * @param sqlId       the sql id
     * @param callParam   the call param
     * @param commonParam the common param
     * @return the list
     */
    public List<Map<String, Object>> doCall(TransactionContext txCtx, String sqlId, List<Map<String, Object>> callParam, Map<String, Object> commonParam) {
        List<Map<String, Object>> result = new ArrayList<>();
        SqlSessionTemplate executor = getDefaultExecutor();
        if (callParam == null || callParam.isEmpty()) {
            Object obj = executor.selectList(sqlId, commonParam);
            if (obj != null)
                result = (List<Map<String, Object>>) obj;
            result.add(commonParam);
        } else {
            for (Map<String, Object> param : callParam) {
                Map<String, Object> addParam = new HashMap<>();
                if (commonParam != null) {
                    addParam.putAll(commonParam);
                }
                addParam.putAll(param);
                Object obj = executor.selectList(sqlId, addParam);
                if (obj != null)
                    result.addAll((List<Map<String, Object>>) obj);
                result.add(addParam);
            }
        }

        return result;
    }

    /**
     * Not always available in all databases
     *
     * @param txCtx the tx ctx
     * @param sqlId the sql id
     */
    public void doOpenCursor(TransactionContext txCtx, String sqlId) {
        getDefaultExecutor().selectOne(sqlId);
    }

    /**
     * Not always available in all databases
     *
     * @param txCtx the tx ctx
     * @param sqlId the sql id
     * @return the list
     */
    public List<Map<String, Object>> doFetch(TransactionContext txCtx, String sqlId) {
        List<Map<String, Object>> result = getDefaultExecutor().selectList(sqlId);
        return result;
    }

    /**
     * Do insert int.
     *
     * @param txCtx     the tx ctx
     * @param sqlId     the sql id
     * @param insertRow the insert row
     * @return the int
     */
    public int doInsert(TransactionContext txCtx, String sqlId, Map<String, Object> insertRow) {
        if (defaultExecutorType == ExecutorType.BATCH) {
            SqlSessionTemplate session = getDefaultExecutor();
            session.insert(sqlId, insertRow);
            return getBatchResultCount(session.flushStatements());
        } else {
            return getDefaultExecutor().insert(sqlId, insertRow);
        }
    }


    /**
     * Do update int.
     *
     * @param txCtx       the tx ctx
     * @param sqlId       the sql id
     * @param updateParam the update param
     * @return the int
     */
    public int doUpdate(TransactionContext txCtx, String sqlId, Map<String, Object> updateParam) {
        if (defaultExecutorType == ExecutorType.BATCH) {
            SqlSessionTemplate session = getDefaultExecutor();
            session.update(sqlId, updateParam);
            return getBatchResultCount(session.flushStatements());
        } else {
            return getDefaultExecutor().update(sqlId, updateParam);
        }
    }


    /**
     * Do delete int.
     *
     * @param txCtx       the tx ctx
     * @param sqlId       the sql id
     * @param deleteParam the delete param
     * @return the int
     */
    public int doDelete(TransactionContext txCtx, String sqlId, Map<String, Object> deleteParam) {
        if (defaultExecutorType == ExecutorType.BATCH) {
            SqlSessionTemplate session = getDefaultExecutor();
            session.delete(sqlId, deleteParam);
            return getBatchResultCount(session.flushStatements());
        } else {
            return getDefaultExecutor().delete(sqlId, deleteParam);
        }
    }

    /**
     * Do batch insert int.
     *
     * @param txCtx      the tx ctx
     * @param sqlId      the sql id
     * @param insertRows the insert rows
     * @return the int
     */
    public int doBatchInsert(TransactionContext txCtx, String sqlId, List<Map<String, Object>> insertRows) {
        return doBatchInsert(txCtx, sqlId, insertRows, null, defaultFetchSize);
    }

    /**
     * Do batch insert int.
     *
     * @param txCtx       the tx ctx
     * @param sqlId       the sql id
     * @param insertRows  the insert rows
     * @param commonParam the common param
     * @return the int
     */
    public int doBatchInsert(TransactionContext txCtx, String sqlId, List<Map<String, Object>> insertRows, Map<String, Object> commonParam) {
        return doBatchInsert(txCtx, sqlId, insertRows, commonParam, defaultFetchSize);
    }


    /**
     * Do batch insert int.
     *
     * @param txCtx       the tx ctx
     * @param sqlId       the sql id
     * @param insertRows  the insert rows
     * @param commonParam the common param
     * @param fetchSize   the fetch size
     * @return the int
     */
    public int doBatchInsert(TransactionContext txCtx, String sqlId, List<Map<String, Object>> insertRows, Map<String, Object> commonParam, int fetchSize) {
        SqlSessionTemplate session = getBatchExecutor();
        int insertCount = 0;

        if (insertRows == null || insertRows.isEmpty()) {
            return doInsert(txCtx, sqlId, null);
        }

        if (fetchSize <= 0) {
            for (Map<String, Object> row : insertRows) {
                Map<String, Object> addParam = new HashMap<>();
                if (commonParam != null) {
                    addParam.putAll(commonParam);
                }
                addParam.putAll(row);
                session.insert(sqlId, addParam);
            }
            insertCount = getBatchResultCount(session.flushStatements());
        } else {
            int count = 0;
            for (Map<String, Object> row : insertRows) {
                Map<String, Object> addParam = new HashMap<>();
                if (commonParam != null) {
                    addParam.putAll(commonParam);
                }
                addParam.putAll(row);
                session.insert(sqlId, addParam);
                ++count;
                if (count % fetchSize == 0) {
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

    /**
     * Do batch update int.
     *
     * @param txCtx        the tx ctx
     * @param sqlId        the sql id
     * @param updateParams the update params
     * @return the int
     */
    public int doBatchUpdate(TransactionContext txCtx, String sqlId, List<Map<String, Object>> updateParams) {
        return doBatchUpdate(txCtx, sqlId, updateParams, null, defaultFetchSize);
    }

    /**
     * Do batch update int.
     *
     * @param txCtx        the tx ctx
     * @param sqlId        the sql id
     * @param updateParams the update params
     * @param commonParam  the common param
     * @return the int
     */
    public int doBatchUpdate(TransactionContext txCtx, String sqlId, List<Map<String, Object>> updateParams, Map<String, Object> commonParam) {
        return doBatchUpdate(txCtx, sqlId, updateParams, commonParam, defaultFetchSize);
    }

    /**
     * Do batch update int.
     *
     * @param txCtx        the tx ctx
     * @param sqlId        the sql id
     * @param updateParams the update params
     * @param commonParam  the common param
     * @param fetchSize    the fetch size
     * @return the int
     */
    public int doBatchUpdate(TransactionContext txCtx, String sqlId, List<Map<String, Object>> updateParams, Map<String, Object> commonParam, int fetchSize) {
        SqlSessionTemplate session = getBatchExecutor();
        int updateCount = 0;

        if (updateParams == null || updateParams.isEmpty()) {
            return doUpdate(txCtx, sqlId, commonParam);
        }

        if (fetchSize <= 0) {
            for (Map<String, Object> param : updateParams) {
                Map<String, Object> addParam = new HashMap<>();
                if (commonParam != null) {
                    addParam.putAll(commonParam);
                }
                addParam.putAll(param);
                session.update(sqlId, addParam);
            }
            updateCount = getBatchResultCount(session.flushStatements());
        } else {
            int count = 0;
            for (Map<String, Object> param : updateParams) {
                Map<String, Object> addParam = new HashMap<>();
                if (commonParam != null) {
                    addParam.putAll(commonParam);
                }
                addParam.putAll(param);
                session.update(sqlId, addParam);
                ++count;
                if (count % fetchSize == 0) {
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

    /**
     * Delete 쿼리를 수행한다.
     * 파라미터로 전달된 deleteParams의 수만큼 쿼리가 반복실행되며 실행에 영향을 받은 row 수가 합산되어 반환된다.
     *
     * @param txCtx        TransactionContext 객체
     * @param sqlId        실행될 Delete 쿼리의 ID
     * @param deleteParams Delete 쿼리 실행 시 사용할 파라미터.
     * @return 쿼리 실행 후 영향을 받은 row 수
     */
    public int doBatchDelete(TransactionContext txCtx, String sqlId, List<Map<String, Object>> deleteParams) {
        return doBatchDelete(txCtx, sqlId, deleteParams, defaultFetchSize);
    }

    /**
     * Delete 쿼리를 Batch 로 수행한다.
     * 파라미터로 전달된 deleteParams의 수만큼 쿼리가 반복실행되며 실행에 영향을 받은 row 수가 합산되어 반환된다.
     *
     * @param txCtx        TransactionContext 객체
     * @param sqlId        실행될 Delete 쿼리의 ID
     * @param deleteParams Delete 쿼리 실행 시 사용할 파라미터
     * @param fetchSize    Batch 작업 수행 시의 fetch size
     * @return 쿼리 실행 후 영향을 받은 row 수
     */
    public int doBatchDelete(TransactionContext txCtx, String sqlId, List<Map<String, Object>> deleteParams, int fetchSize) {
        SqlSessionTemplate session = getBatchExecutor();
        int deleteCount = 0;

        if (deleteParams == null || deleteParams.isEmpty()) {
            return doDelete(txCtx, sqlId, null);
        }

        if (fetchSize <= 0) {
            for (Map<String, Object> param : deleteParams) {
                session.delete(sqlId, param);
            }
            deleteCount = getBatchResultCount(session.flushStatements());
        } else {
            int count = 0;
            for (Map<String, Object> param : deleteParams) {
                session.delete(sqlId, param);
                ++count;
                if (count % fetchSize == 0) {
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

    /**
     * Batch 작업을 할 때의 기본 Fetch size 를 설정한다.
     *
     * @param fetchSize the fetch size
     */
    public void setDefaultFetchSize(int fetchSize) {
        if (fetchSize < 0) {
            fetchSize = 0;
        }
        defaultFetchSize = fetchSize;
    }

    /**
     * 이 QueryExecutor 가 사용하는 SqlSessionFactory 를 지정한다.
     *
     * @param sqlSessionFactory the sql session factory
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * Batch 작업에 영향을 받은 row count 를 가져온다.
     *
     * @param batchResults the batch results
     * @return the batch result count
     */
    int getBatchResultCount(List<BatchResult> batchResults) {
        if (batchResults.size() == 0)
            return 0;
        int count = 0;
        int[] updateCounts = batchResults.get(0).getUpdateCounts();

        for (int i : updateCounts) {
            if (i == -2) {//-2 = Statement.SUCCESS_NO_INFO (쿼리가 정상 수행됐으나 결과 정보를 알 수 없는 상태)
                ++count;
            } else {
                count += i;
            }
        }
        return count;
    }

}
