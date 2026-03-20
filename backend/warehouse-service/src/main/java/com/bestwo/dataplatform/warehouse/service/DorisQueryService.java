package com.bestwo.dataplatform.warehouse.service;

import com.bestwo.dataplatform.warehouse.config.DorisProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DorisQueryService {

    private final JdbcTemplate jdbcTemplate;
    private final DorisProperties dorisProperties;

    public DorisQueryService(JdbcTemplate dorisJdbcTemplate, DorisProperties dorisProperties) {
        this.jdbcTemplate = dorisJdbcTemplate;
        this.dorisProperties = dorisProperties;
    }

    public Map<String, Object> ping() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", result);
        response.put("database", dorisProperties.getDatabase());
        return response;
    }

    public List<Map<String, Object>> queryTestOrders() {
        String sql = "SELECT * FROM " + dorisProperties.getDatabase() + ".ods_wx_order LIMIT 10";
        return jdbcTemplate.queryForList(sql);
    }
}
