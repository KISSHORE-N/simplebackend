// src/utils/api.js

// --- 1. MOCK DATABASE STORE (Simulates PostgreSQL tables) ---
// Note: This data structure must match the expectations of your Java ReportFile model.
const mockFileStore = [
    // Initial NEW files (ready for notification fetch)
    { id: 'F101', fileName: 'Medical_Report_1.pdf', destinationGroup: 'HR_Metrics', status: 'NEW', remotePath: '/remote/src/F101.pdf' },
    { id: 'F102', fileName: 'Ops_Log_Q3.pdf', destinationGroup: 'Ops_Reports', status: 'NEW', remotePath: '/remote/src/F102.pdf' },
    
    // Initial READY_TO_TRANSFER file (ready to appear in the main table queue on load)
    { id: 'F103', fileName: 'Compliance_Audit.pdf', destinationGroup: 'Compliance_Data', status: 'READY_TO_TRANSFER', remotePath: '/remote/src/F103.pdf' },
];

const API_BASE_URL = '/api/ops';

const fetchAPI = async (endpoint, method = 'GET', data = null) => {
    console.log(`[API MOCK] ${method} ${API_BASE_URL}${endpoint}`);
    await new Promise(resolve => setTimeout(resolve, 500)); // Simulate network delay

    // --- Determine Target ---
    let fileId = endpoint.split('/').pop();
    let file = mockFileStore.find(f => f.id === fileId);

    // 1. GET /api/ops/notifications
    if (method === 'GET' && endpoint === '/notifications') {
        return mockFileStore.filter(f => f.status === 'NEW');
    }

    // 2. GET /api/ops/queue
    if (method === 'GET' && endpoint === '/queue') {
        return mockFileStore.filter(f => 
            f.status === 'READY_TO_TRANSFER' || f.status === 'PROCESSING'
        );
    }
    
    // 3. POST /api/ops/acknowledge/{fileId} (Get File button clicked)
    if (method === 'POST' && endpoint.startsWith('/acknowledge')) {
        if (file && file.status === 'NEW') {
            file.status = 'READY_TO_TRANSFER'; // Transition: NEW -> READY_TO_TRANSFER
            return { success: true, file };
        }
        return { success: false, error: 'File not found or already acknowledged' };
    }
    
    // 4. POST /api/ops/transfer/{fileId} (Transfer button clicked)
    if (method === 'POST' && endpoint.startsWith('/transfer')) {
        if (file && file.status === 'READY_TO_TRANSFER') {
            // Step A: Set to Processing (Optimistic update on server-side)
            file.status = 'PROCESSING';
            
            // Step B: Simulate transfer delay and final completion update
            setTimeout(() => {
                file.status = 'TRANSFERRED';
                console.log(`[MOCK DB] File ${file.id} transferred to LOCAL storage.`);
            }, 1000);

            // Return success immediately; frontend will refetch for final status
            return { success: true, file: { id: file.id, status: 'PROCESSING' } };
        }
        return { success: false, error: 'File not ready for transfer' };
    }
    
    // Placeholder for other endpoints (Admin/Subscriber)
    return []; 
};

export default fetchAPI;
