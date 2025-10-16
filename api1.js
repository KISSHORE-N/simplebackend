// src/utils/api.js

// --- LIVE IMPLEMENTATION: Connects to Java/Spring Boot Backend ---

// NOTE: Ensure your Java backend is running on port 8080. 
// If your backend runs on a different port (e.g., 8090), change the port here.
const BASE_URL = 'http://localhost:8080/api/ops'; 

const fetchAPI = async (endpoint, method = 'GET', data = null) => {
    const url = `${BASE_URL}${endpoint}`;
    
    const config = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // If method is POST or PUT, include the request body
    if (data && method !== 'GET') {
        config.body = JSON.stringify(data);
    }
    
    try {
        console.log(`[LIVE API] ${method} ${url}`);
        
        const response = await fetch(url, config);

        if (!response.ok) {
            // Log the error and throw an exception for the UI to catch
            const errorText = await response.text();
            console.error(`API Error Response: ${errorText}`);
            throw new Error(`API call to ${endpoint} failed with status: ${response.status}`);
        }
        
        // Return JSON response data from the Java controller
        // Note: Java returns an empty body for some successful POSTs (e.g., /transfer)
        const text = await response.text();
        return text ? JSON.parse(text) : { success: true }; 

    } catch (error) {
        console.error("Network or API execution error:", error);
        // Return empty array for GETs to prevent UI crash, or throw
        if (method === 'GET') return []; 
        throw error;
    }
};

export default fetchAPI;
