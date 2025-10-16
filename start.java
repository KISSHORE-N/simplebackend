package com.scb.rwtoolbackend;

import com.scb.rwtoolbackend.service.FileTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupRunner implements CommandLineRunner {

    @Autowired
    private FileTransferService fileTransferService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- Initializing Mock Remote File Storage ---");
        
        // This calls the service method to populate the database with test files.
        fileTransferService.initializeRemoteFiles();
        
        System.out.println("--- Database Initialized. Ready for API calls. ---");
    }
}
