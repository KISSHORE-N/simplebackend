import React, { useState, useMemo, useEffect, useCallback } from 'react';
import './OpsPage.css'; 
import { useNavigate } from 'react-router-dom';
import fetchAPI from '../../utils/api'; // Assuming correct path

// --- Static User Info for the Header ---
const opsUser = {
    name: 'Welcome Ops',
    email: 'admin@standardchartered.com',
};

// --- Icon Components (Locally defined) ---
const SearchIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
);

const TransferIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="17 1 21 5 17 9"/><line x1="3" y1="5" x2="21" y2="5"/><polyline points="17 15 21 19 17 23"/><line x1="3" y1="19" x2="21" y2="19"/></svg>
);

const CompleteIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
);

const NotificationIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
);

const CloseIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
);

const AddIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
);

const LogOutIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
);


function OpsPage() {
    const navigate = useNavigate();
    
    // We start files and notifications empty, relying entirely on the API calls
    const [files, setFiles] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [notifications, setNotifications] = useState([]); 
    const [showNotifications, setShowNotifications] = useState(false);

    // Helper to map statuses from JAVA_CASE (e.g., READY_TO_TRANSFER) to css-case (ready-to-transfer)
    const formatStatus = (status) => {
        return status ? status.toLowerCase().replace(/_/g, '-') : 'unknown';
    };

    // --- API FETCH FUNCTIONS ---
    
    const fetchTransferQueue = useCallback(async () => {
        // GET /api/ops/queue
        try {
            const fetchedFiles = await fetchAPI('/queue', 'GET'); 
            setFiles(fetchedFiles);
        } catch (error) {
            console.error("Failed to fetch transfer queue:", error);
            // setFiles([]); // Keep files empty on error
        }
    }, []);

    const fetchNotifications = useCallback(async () => {
        // GET /api/ops/notifications
        try {
            const fetchedNotifications = await fetchAPI('/notifications', 'GET');
            setNotifications(fetchedNotifications); 
        } catch (error) {
            console.error("Failed to fetch notifications:", error);
            // setNotifications([]);
        }
    }, []);

    // Initial data load effect
    useEffect(() => {
        fetchTransferQueue();
        fetchNotifications();
    }, [fetchTransferQueue, fetchNotifications]);


    // --- ACTION HANDLERS ---
    
    const toggleNotifications = () => {
        setShowNotifications(prev => !prev);
    };

    const handleLogout = () => {
        console.log("Operator Logged out.");
        navigate('/');
    };

    // ACTION 1: Initiate Transfer (Moves status to Processing/Transferred)
    const handleTransfer = async (fileId) => {
        // OPTIMISTIC UPDATE: Set status to processing instantly for UI feedback
        setFiles(prevFiles => prevFiles.map(file => 
            file.id === fileId ? { ...file, status: 'PROCESSING' } : file
        ));
        
        // POST /api/ops/transfer/{fileId}
        try {
            const response = await fetchAPI(`/transfer/${fileId}`, 'POST');

            if (response.success) {
                // Refetch the queue to get the final 'Transferred' status from the server
                fetchTransferQueue();
            } else {
                console.error("Transfer failed on server.");
                fetchTransferQueue(); // Refresh to get accurate status (likely back to READY)
            }
        } catch (error) {
            console.error("API call failed during transfer:", error);
            fetchTransferQueue(); // Refresh the table
        }
    };
    
    // ACTION 2: Get File (Acknowledges notification and adds file to queue)
    const handleGetFile = async (notification) => {
        // POST /api/ops/acknowledge/{fileId}
        try {
            const response = await fetchAPI(`/acknowledge/${notification.id}`, 'POST');

            if (response.success) {
                // 1. Remove from local notification list immediately
                setNotifications(prev => prev.filter(n => n.id !== notification.id));

                // 2. Refetch the main queue list to show the new file added by the server
                fetchTransferQueue();
            }
        } catch (error) {
            console.error("API call failed during acknowledgment:", error);
        }
    };


    // --- Filtering Logic (Remains the same) ---
    const filteredFiles = useMemo(() => {
        return files.filter(file => 
            file.fileName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            file.id.toLowerCase().includes(searchTerm.toLowerCase())
        );
    }, [files, searchTerm]);


    return (
        <div className="app-container">

            {/* --- NAVBAR / HEADER --- */}
            <header className="main-header" style={{ justifyContent: 'space-between' }}>
                
                <div className="header-left-content">
                    <div className="logo-section" onClick={() => navigate('/')}>
                        <img src="/path/to/standard-chartered-logo.svg" alt="Standard Chartered Logo" className="sc-logo" />
                    </div>
                    <div className="user-profile">
                        <h1 className="user-name">{opsUser.name}</h1>
                        <p className="user-email">{opsUser.email}</p>
                    </div>
                </div>
                
                <div className="header-actions">
                    <button 
                        className={`notification-toggle-button ${showNotifications ? 'active' : ''}`}
                        onClick={toggleNotifications}
                        title={showNotifications ? "Hide Notifications" : "Show Notifications"}
                    >
                        {showNotifications ? <CloseIcon /> : <NotificationIcon />}
                        {notifications.length > 0 && (
                            <span className="notification-badge">{notifications.length}</span>
                        )}
                    </button>

                    <button 
                        className="action-button logout-button"
                        onClick={handleLogout}
                        title="Logout Operator"
                    >
                        <LogOutIcon />
                        Logout
                    </button>
                </div>
            </header>
            
            {/* --- MAIN DASHBOARD CONTENT AREA --- */}
            <div className="dashboard-content ops-page-container">

                <h1 className="main-page-title report-title">Remote File Transfer Operations</h1>

                {/* --- Content Grid (Dynamic Layout) --- */}
                <div 
                    className="ops-main-grid"
                    style={{ gridTemplateColumns: showNotifications ? '3fr 1fr' : '1fr' }}
                >
                    
                    <div className="ops-main-content">
                        
                        {/* Search Bar */}
                        <div className="search-bar-container">
                            <div className="search-input-group">
                                <SearchIcon /> 
                                <input 
                                    type="text"
                                    placeholder="Search File ID or Name..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    className="sc-search-input"
                                />
                            </div>
                        </div>

                        {/* Transfer Table */}
                        <div className="reports-area transfer-table-card">
                            <h2 className="table-subtitle">Files Ready for Action ({filteredFiles.length} found)</h2>
                            
                            <div className="table-responsive">
                                <table className="data-table">
                                    <thead>
                                        <tr>
                                            <th style={{ width: '15%' }}>File ID</th>
                                            <th style={{ width: '35%' }}>File Name</th>
                                            <th style={{ width: '25%' }}>Destination Folder</th>
                                            <th style={{ width: '15%' }}>Status</th>
                                            <th style={{ width: '10%' }}>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {filteredFiles.length > 0 ? (
                                            filteredFiles.map((file) => (
                                                <tr key={file.id}>
                                                    <td>{file.id}</td>
                                                    <td><p className="report-description">{file.fileName}</p></td>
                                                    <td>
                                                        <span className="destination-tag">{file.destinationGroup}</span>
                                                    </td>
                                                    <td>
                                                        {/* Use formatStatus helper for CSS class mapping */}
                                                        <span className={`status-tag status-${formatStatus(file.status)}`}>
                                                            {file.status.replace(/_/g, ' ')}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <button 
                                                            className="action-button transfer-button"
                                                            onClick={() => handleTransfer(file.id)}
                                                            disabled={file.status === 'TRANSFERRED' || file.status === 'PROCESSING'}
                                                        >
                                                            {file.status === 'TRANSFERRED' ? <CompleteIcon /> : <TransferIcon />}
                                                            {file.status === 'PROCESSING' ? 'Moving...' : 
                                                             file.status === 'TRANSFERRED' ? 'Completed' : 'Transfer'}
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan="5" className="no-reports">No files found matching the criteria.</td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            {/* --- RIGHT SIDEBAR: Notification Panel (Fixed Position) --- */}
            <div className={`ops-notification-sidebar ${showNotifications ? 'visible' : ''}`}>
                <div className="ops-notification-panel">
                    
                    <div className="notification-panel-header">
                        <h3 className="notification-title">New File Arrivals ({notifications.length})</h3>
                        <button className="action-button close-sidebar-button" onClick={toggleNotifications}>
                            <CloseIcon />
                        </button>
                    </div>

                    <div className="notification-list-scroll">
                        {notifications.length > 0 ? (
                            notifications.map(n => (
                                <div key={n.id} className="ops-notification-item">
                                    <div className="item-details">
                                        <strong className="item-file-name">{n.fileName}</strong>
                                        <span className="item-file-id">ID: {n.id}</span>
                                        <span className="item-destination">To: {n.destinationGroup}</span>
                                    </div>
                                    <button 
                                        className="action-button get-file-button"
                                        onClick={() => handleGetFile(n)}
                                    >
                                        <AddIcon /> Get File
                                    </button>
                                </div>
                            ))
                        ) : (
                            <p className="no-reports">No new files waiting.</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default OpsPage;
