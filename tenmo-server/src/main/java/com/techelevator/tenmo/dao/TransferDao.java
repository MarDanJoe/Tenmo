package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.TransferDTO;

import java.util.List;

public interface TransferDao {
    TransferDTO get(int transferId);

    TransferDTO createTransfer(TransferDTO transfer);
    List<TransferDTO> getTransfersByUserId(int userId);
    TransferDTO getTransferById(int transferId);

    List<TransferDTO> findPendingTransfersByUsername(String username);

    Integer getTransferTypeId(String transferTypeDesc);
    Integer getTransferStatusId(String transferStatusDesc);
}
