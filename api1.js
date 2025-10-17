// src/utils/api.js

// NOTE: Ensure your Java backend is running on port 8080. 
// Change the port if your Spring Boot app is using a different one (e.g., 8090).
const BASE_URL = 'http://localhost:8080/api/ops'; 

const fetchAPI = async (endpoint, method = 'GET', data = null) => {
    const url = `${BASE_URL}${endpoint}`;
    
    const config = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            // Note: If you face CORS issues, ensure your Spring Boot controller has @CrossOrigin
        },
    };

    if (data && method !== 'GET') {
        config.body = JSON.stringify(data);
    }
    
    try {
        console.log(`[LIVE API] ${method} ${url}`);
        
        const response = await fetch(url, config);

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`API FAILED: ${url} | Status: ${response.status}`, errorText);
            // Throwing an error here ensures the catch block in OpsPage.js is hit
            throw new Error(`Server error: ${response.status} ${response.statusText}`);
        }
        
        // Handle responses that might have empty bodies (like successful POSTs)
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            return response.json();
        }
        
        // Return a successful dummy object if no content is returned (e.g., for POST requests)
        return { success: true }; 

    } catch (error) {
        console.error(`Network/Execution Error for ${url}:`, error);
        // On error, return an empty array for GETs to prevent UI crash
        if (method === 'GET') return []; 
        throw error;
    }
};

export default fetchAPI;