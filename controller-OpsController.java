package com.scb.rwtoolbackend.controller;

import com.scb.rwtoolbackend.model.ReportFile;
import com.scb.rwtoolbackend.service.FileTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ops")
public class OpsController {

    @Autowired
    private FileTransferService fileTransferService;

    // 1. GET /api/ops/notifications - Fetches NEW files waiting for Ops action
    @GetMapping("/notifications")
    public ResponseEntity<List<ReportFile>> getNewFileNotifications() {
        return ResponseEntity.ok(fileTransferService.getNewFileNotifications());
    }

    // 2. GET /api/ops/queue - Fetches files ready or processing in the main table
    @GetMapping("/queue")
    public ResponseEntity<List<ReportFile>> getTransferQueue() {
        return ResponseEntity.ok(fileTransferService.getTransferQueue());
    }

    // 3. POST /api/ops/acknowledge/{fileId} - Ops clicks "Get File" in the notification sidebar
    @PostMapping("/acknowledge/{fileId}")
    public ResponseEntity<ReportFile> acknowledgeNotification(@PathVariable String fileId) {
        try {
            ReportFile updatedFile = fileTransferService.acknowledgeNotification(fileId);
            return ResponseEntity.ok(updatedFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); 
        }
    }

    // 4. POST /api/ops/transfer/{fileId} - Ops clicks "Transfer" in the main table
    @PostMapping("/transfer/{fileId}")
    public ResponseEntity<ReportFile> initiateTransfer(@PathVariable String fileId) {
        try {
            ReportFile transferredFile = fileTransferService.initiateTransfer(fileId);
            return ResponseEntity.ok(transferredFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); 
        }
    }
}
