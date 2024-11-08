package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.TransferDTO;
import com.techelevator.tenmo.model.TransferStatus;
import com.techelevator.tenmo.model.TransferType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

@Component
public class JdbcTransferDao implements TransferDao {
    private static final int PENDING_STATUS_ID = 1;
    private final JdbcTemplate jdbcTemplate;
    private final UserDao userDao;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate, UserDao userDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
    }


    @Override
    public TransferDTO get(int transferId) {
        TransferDTO transfer = null;
        String sql = "SELECT DISTINCT transfer.transfer_id, transfer.transfer_type_id, transfer.transfer_status_id, transfer.account_from, "+
          " transfer.account+to, transfer.amount, from_user.username AS from_user, to_user.username AS to_user " +
          " FROM transfer_history AS transfer " +
          " JOIN transfer_history AS from_user " +
            "ON transfer.account_from = from_user.account_id " +
            " JOIN transfer_history AS to_user " +
            " ON transfer.account)to = to_user.account_id " +
                " WHERE transfer.transfer_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
        if(results.next()) {
            transfer = mapRowToTransfer(results);
        }
        return transfer;
    }
    @Override
    public TransferDTO createTransfer(TransferDTO transfer) {
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, (SELECT account_id FROM account WHERE user_id = ?), (SELECT account_id FROM account WHERE user_id = ?), ?) RETURNING transfer_id";

        try {
            System.out.println("Creating transfer with values: TypeID=" + transfer.getTypeId() +
                    ", StatusID=" + transfer.getStatusId() +
                    ", AccountFrom=" + transfer.getAccountFrom() +
                    ", AccountTo=" + transfer.getAccountTo() +
                    ", Amount=" + transfer.getTransferAmount());

            Integer newTransferId = jdbcTemplate.queryForObject(sql, Integer.class,
                    transfer.getTypeId(),
                    transfer.getStatusId(),
                    transfer.getAccountFrom(),
                    transfer.getAccountTo(),
                    transfer.getTransferAmount());

            transfer.setTransferId(newTransferId);
            System.out.println("Transfer created successfully with ID: " + newTransferId);
            return transfer;

        } catch (Exception e) {
            System.err.println("Error during transfer creation: " + e.getMessage());
            throw new RuntimeException("Failed to create transfer", e);
        }
    }


    // Corrected query to use `transfer_status_id`
    public List<TransferDTO> findPendingTransfersByUsername(String username) {
        String sql = "SELECT * FROM transfer WHERE account_to = " +
                "(SELECT user_id FROM tenmo_user WHERE username = ?) AND transfer_status_id = ?";
        return jdbcTemplate.query(sql, new TransferRowMapper(), username, PENDING_STATUS_ID);
    }

    // TransferRowMapper with nullable handling for all integer columns
    private  class TransferRowMapper implements RowMapper<TransferDTO> {
        @Override
        public  TransferDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TransferDTO(
                    (Integer) rs.getObject("transfer_id"),         // Nullable transfer_id
                    (Integer) rs.getObject("transfer_type_id"),    // Nullable transfer_type_id
                    (Integer) rs.getObject("transfer_status_id"),  // Nullable transfer_status_id
                    (Integer) rs.getObject("account_from"),        // Nullable account_from
                    (Integer) rs.getObject("account_to"),          // Nullable account_to
                    rs.getBigDecimal("amount") != null ? rs.getBigDecimal("amount").doubleValue() : null
            );
        }
    }

    private TransferDTO mapRowToTransfer(SqlRowSet rowSet) {
        TransferDTO transfer = new TransferDTO();
        transfer.setTypeId(rowSet.getInt("transfer_id"));
        transfer.setTransferId(rowSet.getInt("transfer_type_id"));
        transfer.setStatusId(rowSet.getInt("transfer_status_id"));
        transfer.setAccountFrom(rowSet.getInt("account_from"));
        transfer.setAccountTo(rowSet.getInt("account_to"));
        transfer.setTransferAmount(rowSet.getDouble("amount"));
       // transfer.setTransferId(rowSet.getString("from_user"));
       // transfer.setToUsername(rowSet.getString("to_user"));

        return transfer;
    }


    @Override
    public List<TransferDTO> getTransfersByUserId(int userId) {
        int accountId = userDao.getAccountIdByUserId(userId);
        String sql = "SELECT * FROM transfer WHERE account_from = ? OR account_to = ?";
        return jdbcTemplate.query(sql, new TransferRowMapper(), accountId, accountId);
    }

    @Override
    public TransferDTO getTransferById(int transferId) {
        return null;
    }
    @Override
    public Integer getTransferTypeId(String transferTypeDesc) {
        String sql = "SELECT transfer_type_id FROM transfer_type WHERE transfer_type_desc = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, transferTypeDesc);
    }

    @Override
    public Integer getTransferStatusId(String transferStatusDesc) {
        String sql = "SELECT transfer_status_id FROM transfer_status WHERE transfer_status_desc = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, transferStatusDesc);
    }

    private class TransferTypeRowMapper implements RowMapper<TransferType> {
        @Override
        public TransferType mapRow(ResultSet rs, int rowNum) throws SQLException {
            TransferType type = new TransferType();
            type.setId(rs.getInt("transfer_type_id"));
            type.setDescription(rs.getString("transfer_type_desc"));
            return type;
        }
    }

    // RowMapper for TransferStatus
    private class TransferStatusRowMapper implements RowMapper<TransferStatus> {
        @Override
        public TransferStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
            TransferStatus status = new TransferStatus();
            status.setId(rs.getInt("transfer_status_id"));
            status.setDescription(rs.getString("transfer_status_desc"));
            return status;
        }
    }
}

