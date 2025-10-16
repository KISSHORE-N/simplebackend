package com.scb.rwtoolbackend.dao;

import com.scb.rwtoolbackend.model.ReportFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportFileRepository extends JpaRepository<ReportFile, String> {

    // 1. Used for NEW FILE NOTIFICATIONS (Status: NEW, Location: REMOTE)
    List<ReportFile> findByCurrentLocationAndTransferStatus(
        ReportFile.StorageLocation location, ReportFile.TransferStatus status);
    
    // 2. Used for the OPS TRANSFER QUEUE (Status: READY_TO_TRANSFER or PROCESSING, Location: REMOTE)
    List<ReportFile> findByCurrentLocationAndTransferStatusIn(
        ReportFile.StorageLocation location, List<ReportFile.TransferStatus> statuses);
}
