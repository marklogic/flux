package com.marklogic.flux.rest.api;

public class JdbcImportRequest {
    private String query;
    private String groupBy;
    private String aggregateColumnName;
    private String[] aggregateColumns;

    // Getters and setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getGroupBy() { return groupBy; }
    public void setGroupBy(String groupBy) { this.groupBy = groupBy; }

    public String getAggregateColumnName() { return aggregateColumnName; }
    public void setAggregateColumnName(String aggregateColumnName) { this.aggregateColumnName = aggregateColumnName; }

    public String[] getAggregateColumns() { return aggregateColumns; }
    public void setAggregateColumns(String[] aggregateColumns) { this.aggregateColumns = aggregateColumns; }
}
