package com.scb.rwtoolbackend.service;

import com.scb.rwtoolbackend.dao.ReportFileRepository;
import com.scb.rwtoolbackend.model.ReportFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class FileTransferService {

    @Autowired
    private ReportFileRepository fileRepository;

    /*
     * MOCK INITIALIZATION: Populates initial state for demonstration.
     */
    public void initializeRemoteFiles() {
        if (fileRepository.count() == 0) {
            // Files currently waiting for Ops acknowledgment (NEW)
            discoverNewFile("Medical_Report_1.pdf", "HR_Metrics");
            discoverNewFile("Ops_Log_Q3.pdf", "Ops_Reports");

            // Files already in the queue (Ready for transfer button click)
            createMockFile("Compliance_Audit.pdf", "Compliance_Data", 
                ReportFile.TransferStatus.READY_TO_TRANSFER, ReportFile.StorageLocation.REMOTE);
        }
    }
    
    // INTERNAL HELPER: Simulates a background worker discovering a new file.
    public ReportFile discoverNewFile(String fileName, String destinationGroup) {
        ReportFile newFile = new ReportFile();
        newFile.setFileId(UUID.randomUUID().toString());
        newFile.setFileName(fileName);
        newFile.setRemotePath("/remote/source/" + fileName);
        newFile.setDestinationGroup(destinationGroup);
        newFile.setTransferStatus(ReportFile.TransferStatus.NEW); // Status that triggers initial notification
        newFile.setCurrentLocation(ReportFile.StorageLocation.REMOTE);
        newFile.setDateCreated(LocalDateTime.now());
        
        return fileRepository.save(newFile);
    }
    
    // API 1: GET /api/ops/notifications (Returns NEW files)
    public List<ReportFile> getNewFileNotifications() {
        return fileRepository.findByCurrentLocationAndTransferStatus(
            ReportFile.StorageLocation.REMOTE, ReportFile.TransferStatus.NEW);
    }

    // API 2: POST /api/ops/acknowledge/{fileId} (Ops Clicks 'Get File')
    public ReportFile acknowledgeNotification(String fileId) throws Exception {
        ReportFile file = fileRepository.findById(fileId)
            .orElseThrow(() -> new Exception("File not found: " + fileId));

        if (file.getTransferStatus() == ReportFile.TransferStatus.NEW) {
            // Acknowledge the notification and put the file into the main queue
            file.setTransferStatus(ReportFile.TransferStatus.READY_TO_TRANSFER); 
            return fileRepository.save(file);
        }
        return file;
    }
    
    // API 3: GET /api/ops/queue (Returns files ready for action)
    public List<ReportFile> getTransferQueue() {
        List<ReportFile.TransferStatus> statuses = Arrays.asList(
            ReportFile.TransferStatus.READY_TO_TRANSFER, 
            ReportFile.TransferStatus.PROCESSING);
            
        // Returns files that Ops has pulled into the queue (READY or PROCESSING)
        return fileRepository.findByCurrentLocationAndTransferStatusIn(
            ReportFile.StorageLocation.REMOTE, statuses);
    }

    // API 4: POST /api/ops/transfer/{id} (Ops Clicks 'Transfer' button)
    public ReportFile initiateTransfer(String fileId) throws Exception {
        ReportFile file = fileRepository.findById(fileId)
            .orElseThrow(() -> new Exception("File not found"));

        if (file.getTransferStatus() != ReportFile.TransferStatus.READY_TO_TRANSFER) {
            throw new Exception("File is not ready for transfer.");
        }

        // 1. Update status to PROCESSING (Immediate UI feedback)
        file.setTransferStatus(ReportFile.TransferStatus.PROCESSING);
        fileRepository.save(file);

        // --- Simulated Transfer Logic ---
        // Simulate file move to Local Storage and success
        file.setLocalPath("/local/accessible/" + file.getFileName()); 
        file.setTransferStatus(ReportFile.TransferStatus.TRANSFERRED);
        file.setCurrentLocation(ReportFile.StorageLocation.LOCAL);
        
        // Final database update marking completion
        return fileRepository.save(file);
    }
    
    // Helper for creating mock files (needed for initialization)
    private ReportFile createMockFile(String fileName, String destinationGroup, 
                                      ReportFile.TransferStatus status, 
                                      ReportFile.StorageLocation location) {
        // Implementation remains the same as in the previous step
        ReportFile file = new ReportFile();
        file.setFileId(UUID.randomUUID().toString());
        file.setFileName(fileName);
        file.setRemotePath("/remote/source/" + fileName);
        file.setDestinationGroup(destinationGroup);
        file.setTransferStatus(status);
        file.setCurrentLocation(location);
        file.setDateCreated(LocalDateTime.now());
        return file;
    }
}
