package com.db.viewer.dbviewer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynamicTableService {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getTableData(String table, List<String> columns, int page, int size) {
        String cols = columns.isEmpty() ? "*" : String.join(",", columns);
        String sql = String.format("SELECT %s FROM %s ORDER BY id LIMIT ? OFFSET ?", cols, table);
        return jdbcTemplate.query(sql, new Object[]{size, page * size}, new ColumnMapRowMapper());
    }


    public List<String> getTableColumns(String tableName) {
        return jdbcTemplate.query(
                String.format("SELECT * FROM %s LIMIT 1", tableName),
                rs -> {
                    ResultSetMetaData meta = rs.getMetaData();
                    List<String> cols = new ArrayList<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        cols.add(meta.getColumnName(i));
                    }
                    return cols;
                }
        );
    }

    public List<String> getTableColumns(String tableName, Long id) {
        return jdbcTemplate.query(
                String.format("SELECT * FROM %s WHERE id=%d LIMIT 1", tableName, id),
                rs -> {
                    ResultSetMetaData meta = rs.getMetaData();
                    List<String> cols = new ArrayList<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        cols.add(meta.getColumnName(i));
                    }
                    return cols;
                }
        );
    }

    public int countRows(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }

    public List<String> getAllTableNames() {
        return jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                String.class
        );
    }

    public Map<String, Object> getRowById(String table, Long id) {
        String sql = "SELECT * FROM " + table + " WHERE id = ?";
        return jdbcTemplate.queryForMap(sql, id);
    }

    public void updateRow(String tableName, Object id, Map<String, Object> data) {
        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        List<Object> values = new ArrayList<>();

        Map<String, String> columnTypes = getColumnTypes(tableName);

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sql.append(entry.getKey()).append(" = ?, ");
            values.add(convertValue(entry.getValue(), columnTypes.get(entry.getKey())));
        }

        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");
        values.add(id);

        jdbcTemplate.update(sql.toString(), values.toArray());
    }

    private Object convertValue(Object value, String dbType) {
        if (value == null) return null;
        if(value.equals("false") || value.equals("true")) {
            try {
                return Boolean.parseBoolean(value.toString());
            } catch (NumberFormatException e) {
            }
        }

        if(value.toString().contains(".") || value.toString().contains(",")) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
            }
        }

        return switch (dbType) {
            case "boolean" -> Boolean.valueOf(value.toString());
            case "integer", "int4" -> Integer.valueOf(!value.toString().isBlank() ? value.toString() : "0");
            case "bigint", "int8" -> Long.valueOf(!value.toString().isBlank() ? value.toString() : "0");
            case "real", "float4" -> Float.valueOf(!value.toString().isBlank() ? value.toString() : "0.0");
            case "double precision", "float8" -> Double.valueOf(!value.toString().isBlank() ? value.toString() : "0.0");
            case "date" -> LocalDate.parse(value.toString());
            case "timestamp without time zone", "timestamp with time zone" -> Timestamp.valueOf(value.toString());
            default -> value.toString();
        };
    }


    public void insertRow(String table, Map<String, Object> values) {
        Map<String, Integer> columnTypes = getColumnTypesToInsert(table);

        String columns = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";

        Object[] params = values.entrySet().stream()
                .map(entry -> convertToColumnType(entry.getValue().toString(), columnTypes.get(entry.getKey())))
                .toArray();

        jdbcTemplate.update(sql, params);
    }

    public Map<String, String> getColumnTypes(String tableName) {
        String sql = """
        SELECT column_name, data_type
        FROM information_schema.columns
        WHERE table_name = ?
    """;

        return jdbcTemplate.query(sql, new Object[]{tableName}, rs -> {
            Map<String, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("column_name"), rs.getString("data_type"));
            }
            return map;
        });
    }

    private Map<String, Integer> getColumnTypesToInsert(String tableName) {
        Map<String, Integer> columnTypes = new HashMap<>();
        jdbcTemplate.query("SELECT * FROM " + tableName + " LIMIT 1", rs -> {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                columnTypes.put(metaData.getColumnName(i), metaData.getColumnType(i));
            }
        });
        return columnTypes;
    }

    private Object convertToColumnType(String value, int sqlType) {
        System.out.println("sqlType: " + sqlType + "value: " + value);
        if (value == null) return null;
        if(sqlType == Types.BIT && (value.equals("false") || value.equals("true")))
            return Boolean.parseBoolean(value);

        if(sqlType == Types.NUMERIC && (value.contains(".") || value.contains(","))) {
            return Double.parseDouble(value);
        }
        switch (sqlType) {
            case Types.INTEGER: return Integer.parseInt(value);
            case Types.BIGINT: return Long.parseLong(value);
            case Types.BOOLEAN: return Boolean.parseBoolean(value);
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE: return Double.parseDouble(value);
            case Types.DATE: return java.sql.Date.valueOf(value);
            case Types.TIMESTAMP: return LocalDateTime.now();
            default: return value;
        }
    }


    public void deleteRow(String table, Long id) {
        jdbcTemplate.update("DELETE FROM " + table + " WHERE id = ?", id);
    }

}
