package com.bestwo.dataplatform.warehouse.service;

import com.bestwo.dataplatform.warehouse.config.DorisProperties;
import com.bestwo.dataplatform.warehouse.mapper.WarehouseDorisMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class DorisSchemaService {

    private static final List<String> ODS_TABLES = List.of(
        "ods_wx_order",
        "ods_wx_payment_order",
        "ods_wx_payment_notify"
    );

    private static final List<String> ODS_SCHEMA_RESOURCES = List.of(
        "sql/doris/ods/01_create_ods_wx_order.sql",
        "sql/doris/ods/02_create_ods_wx_payment_order.sql",
        "sql/doris/ods/03_create_ods_wx_payment_notify.sql"
    );

    private final WarehouseDorisMapper warehouseDorisMapper;
    private final DorisProperties dorisProperties;

    public DorisSchemaService(WarehouseDorisMapper warehouseDorisMapper, DorisProperties dorisProperties) {
        this.warehouseDorisMapper = warehouseDorisMapper;
        this.dorisProperties = dorisProperties;
    }

    public Map<String, Object> initOdsSchema() {
        List<String> executedResources = new ArrayList<>();
        for (String resourcePath : ODS_SCHEMA_RESOURCES) {
            executeSqlResource(resourcePath);
            executedResources.add(resourcePath);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("database", dorisProperties.getDatabase());
        response.put("executedResources", executedResources);
        response.put("tables", inspectOdsTables());
        return response;
    }

    public Map<String, Object> inspectOdsTables() {
        Map<String, Object> tables = new LinkedHashMap<>();
        for (String tableName : ODS_TABLES) {
            List<String> columns = warehouseDorisMapper.listTableColumns(dorisProperties.getDatabase(), tableName);
            Map<String, Object> tableInfo = new LinkedHashMap<>();
            tableInfo.put("exists", !columns.isEmpty());
            tableInfo.put("columnCount", columns.size());
            tableInfo.put("columns", columns);
            tables.put(tableName, tableInfo);
        }
        return tables;
    }

    private void executeSqlResource(String resourcePath) {
        String script = readClasspathResource(resourcePath);
        for (String statement : splitStatements(script)) {
            warehouseDorisMapper.executeSql(statement);
        }
    }

    private String readClasspathResource(String resourcePath) {
        try {
            return new String(new ClassPathResource(resourcePath).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read Doris schema resource: " + resourcePath, exception);
        }
    }

    private List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String line : script.split("\\r?\\n")) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                continue;
            }

            builder.append(line).append('\n');
            if (trimmedLine.endsWith(";")) {
                String statement = builder.toString().trim();
                if (statement.endsWith(";")) {
                    statement = statement.substring(0, statement.length() - 1).trim();
                }
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                builder.setLength(0);
            }
        }

        String tail = builder.toString().trim();
        if (!tail.isEmpty()) {
            statements.add(tail);
        }
        return statements;
    }
}
