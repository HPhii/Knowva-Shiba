package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void addFullTextIndexes() {
        String[] tablesAndIndexes = {
                "flashcard_sets,ft_title_description,description,title",
                "accounts,ft_username_email,username,email",
                "quiz_sets,ft_title_description,title,description"
        };

        for (String tableAndIndex : tablesAndIndexes) {
            String[] parts = tableAndIndex.split(",");
            String table = parts[0];
            String indexName = parts[1];
            String columns = parts[2] + "," + parts[3];

            try {
                boolean indexExists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = 'exe' AND table_name = ? AND index_name = ?",
                        Integer.class, table, indexName) > 0;

                if (!indexExists) {
                    jdbcTemplate.execute("ALTER TABLE " + table + " ADD FULLTEXT INDEX " + indexName + " (" + columns + ")");
                    System.out.println("Created full-text index " + indexName + " on table " + table);
                }
            } catch (Exception e) {
                System.err.println("Error creating index " + indexName + " on table " + table + ": " + e.getMessage());
            }
        }
    }
}
