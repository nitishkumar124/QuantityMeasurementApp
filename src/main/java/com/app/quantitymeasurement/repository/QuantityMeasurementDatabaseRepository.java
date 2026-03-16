package com.app.quantitymeasurement.repository;

import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.app.quantitymeasurement.exception.DatabaseException;
import com.app.quantitymeasurement.util.ConnectionPool;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QuantityMeasurementDatabaseRepository
        implements IQuantityMeasurementRepository {

    private final ConnectionPool pool = ConnectionPool.getInstance();

    // Constructor to initialize database schema
    public QuantityMeasurementDatabaseRepository() {
        initializeSchema();
    }

    private void initializeSchema() {

        try (Connection conn = pool.getConnection();
             Statement stmt = conn.createStatement()) {

            InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream("db/schema.sql");

            if (input == null) {
                throw new RuntimeException("schema.sql not found in resources/db");
            }

            Scanner scanner = new Scanner(input);

            StringBuilder sql = new StringBuilder();

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine().trim();

                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }

                sql.append(line);

                if (line.endsWith(";")) {

                    stmt.execute(sql.toString().replace(";", ""));
                    sql.setLength(0);

                } else {
                    sql.append(" ");
                }
            }

            scanner.close();

        } catch (Exception e) {
            throw DatabaseException.queryFailed("Schema initialization", e);
        }
    }

    @Override
    public void save(QuantityMeasurementEntity entity) {

        String sql = """
                INSERT INTO quantity_measurement_entity
                (this_value,this_unit,this_measurement_type,
                 that_value,that_unit,that_measurement_type,
                 operation,result_value,result_unit,result_measurement_type,
                 result_string,is_error,error_message)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, entity.thisValue);
            ps.setString(2, entity.thisUnit);
            ps.setString(3, entity.thisMeasurementType);

            ps.setDouble(4, entity.thatValue);
            ps.setString(5, entity.thatUnit);
            ps.setString(6, entity.thatMeasurementType);

            ps.setString(7, entity.operation);

            ps.setDouble(8, entity.resultValue);
            ps.setString(9, entity.resultUnit);
            ps.setString(10, entity.resultMeasurementType);

            ps.setString(11, entity.resultString);
            ps.setBoolean(12, entity.isError);
            ps.setString(13, entity.errorMessage);

            ps.executeUpdate();

        } catch (SQLException e) {

            throw DatabaseException.queryFailed(sql, e);

        }
    }

    @Override
    public List<QuantityMeasurementEntity> getAllMeasurements() {

        List<QuantityMeasurementEntity> list = new ArrayList<>();

        String sql = "SELECT * FROM quantity_measurement_entity";

        try (Connection conn = pool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

            	QuantityMeasurementEntity entity =
            	        new QuantityMeasurementEntity();

                entity.thisValue = rs.getDouble("this_value");
                entity.thisUnit = rs.getString("this_unit");
                entity.thisMeasurementType = rs.getString("this_measurement_type");

                entity.thatValue = rs.getDouble("that_value");
                entity.thatUnit = rs.getString("that_unit");
                entity.thatMeasurementType = rs.getString("that_measurement_type");

                entity.operation = rs.getString("operation");

                entity.resultValue = rs.getDouble("result_value");
                entity.resultUnit = rs.getString("result_unit");
                entity.resultMeasurementType = rs.getString("result_measurement_type");

                entity.resultString = rs.getString("result_string");

                entity.isError = rs.getBoolean("is_error");
                entity.errorMessage = rs.getString("error_message");

                list.add(entity);

            }

        } catch (SQLException e) {

            throw DatabaseException.queryFailed(sql, e);

        }

        return list;
    }
}