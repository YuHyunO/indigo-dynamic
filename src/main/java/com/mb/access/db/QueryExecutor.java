package com.mb.access.db;

import com.mb.code.QueryType;

import java.util.List;
import java.util.Map;


public interface QueryExecutor {

    public String getSqlId(String namespace, QueryType queryType);

    public List<Map<String, Object>> select(String sqlId, Map<String, Object> selectParam);

    public int insert(String sqlId, Map<String, Object> insertRow);

    public int update(String sqlId, Map<String, Object> updateParam);

    public int delete(String sqlId, Map<String, Object> deleteParam);

    public int batchInsert(String sqlId, List<Map<String, Object>> insertRows);

    public int batchInsert(String sqlId, List<Map<String, Object>> insertRows, int patchSize);

    public int batchUpdate(String sqlId, List<Map<String, Object>> updateParams);

    public int batchUpdate(String sqlId, List<Map<String, Object>> updateParams, int patchSize);

    public int batchDelete(String sqlId, List<Map<String, Object>> deleteParams);

    public int batchDelete(String sqlId, List<Map<String, Object>> deleteParams, int patchSize);

    public void setDefaultPatchSize(int patchSize);

}
