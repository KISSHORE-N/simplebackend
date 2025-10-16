// src/utils/api.js (LIVE IMPLEMENTATION)

// Point to the root of your Java backend API
// Assuming it's running on the same host but port 8080 (common for Spring Boot)
// NOTE: If your frontend and backend run on different ports, you must specify the backend port here.
const BASE_URL = 'http://localhost:8080/api/ops'; 

const fetchAPI = async (endpoint, method = 'GET', data = null) => {
    const url = `${BASE_URL}${endpoint}`;
    
    const config = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
    };

    if (data && method !== 'GET') {
        config.body = JSON.stringify(data);
    }
    
    try {
        const response = await fetch(url, config);

        if (!response.ok) {
            // Throw error for bad HTTP statuses (4xx, 5xx)
            throw new Error(`API call failed with status: ${response.status}`);
        }
        
        // Return raw JSON response data (which is what your components expect)
        return response.json(); 

    } catch (error) {
        console.error("Network or API parsing error:", error);
        // Throw an error or return a structure that alerts the UI
        return { success: false, error: error.message };
    }
};

export default fetchAPI;
