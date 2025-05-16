package com.marklogic.flux.rest.api;

import com.marklogic.flux.api.JdbcImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jdbc-importer")
public class JdbcImporterController {

    private final JdbcImporter jdbcImporter;

    @Autowired
    public JdbcImporterController(JdbcImporter jdbcImporter) {
        this.jdbcImporter = jdbcImporter;
    }

    @PostMapping("/import")
    public String importData(@RequestBody JdbcImportRequest request) {
        // Example usage; actual implementation will depend on your JdbcImporter implementation
        jdbcImporter.from(options -> {
            options.query(request.getQuery());
            if (request.getGroupBy() != null) {
                options.groupBy(request.getGroupBy());
            }
            if (request.getAggregateColumns() != null && request.getAggregateColumns().length > 0) {
                options.aggregateColumns(request.getAggregateColumnName(), request.getAggregateColumns());
            }
        });
        // ...invoke .to() as needed...
        return "Import started";
    }
}
