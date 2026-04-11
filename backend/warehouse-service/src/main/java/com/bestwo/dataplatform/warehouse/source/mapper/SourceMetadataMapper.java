package com.bestwo.dataplatform.warehouse.source.mapper;

import com.bestwo.dataplatform.warehouse.dto.MetadataTableColumnSnapshot;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SourceMetadataMapper {

    @Select({
        "<script>",
        "SELECT",
        "  table_name AS tableName,",
        "  column_name AS columnName,",
        "  data_type AS dataType,",
        "  is_nullable AS isNullable,",
        "  ordinal_position AS ordinalPosition,",
        "  NULL AS columnComment",
        "FROM information_schema.columns",
        "WHERE table_schema = current_schema()",
        "  AND table_name IN",
        "  <foreach collection='tableNames' item='tableName' open='(' separator=',' close=')'>",
        "    #{tableName}",
        "  </foreach>",
        "ORDER BY table_name, ordinal_position",
        "</script>"
    })
    List<MetadataTableColumnSnapshot> listTableColumnDetails(@Param("tableNames") List<String> tableNames);
}
