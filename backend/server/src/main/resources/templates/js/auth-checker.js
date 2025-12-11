// Authentication Checker - Protects user pages
const API_BASE_URL = 'http://localhost:8080';

/**
 * Get JWT token from cookies
 */
function getJWTToken() {
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'jwt') {
            return value;
        }
    }
    return null;
}

/**
 * Check if user is authenticated by calling the backend
 */
async function checkAuthentication() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/user/check-auth`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            return { authenticated: true, user: data };
        } else {
            return { authenticated: false, error: 'Not authenticated' };
        }
    } catch (error) {
        console.error('Auth check error:', error);
        return { authenticated: false, error: error.message };
    }
}

/**
 * Protect a page - redirects to login if not authenticated
 * @param {boolean} requireAuth - If true, redirects to login if not authenticated
 */
async function protectPage(requireAuth = true) {
    // Skip protection for auth pages (login, signup)
    const currentPath = window.location.pathname;
    if (currentPath.includes('/auth/') || currentPath === '/' || currentPath.includes('LandingPage')) {
        return; // Allow access to auth pages
    }

    const authResult = await checkAuthentication();
    
    if (!authResult.authenticated && requireAuth) {
        // Redirect to login page
        window.location.href = `${API_BASE_URL}/pages/auth/LoginPage.html`;
        return false;
    }
    
    return authResult.authenticated;
}

/**
 * Initialize authentication check when page loads
 */
document.addEventListener('DOMContentLoaded', async () => {
    // Check if we're on a protected page (user pages)
    const currentPath = window.location.pathname;
    const isUserPage = currentPath.includes('/user/');
    
    if (isUserPage) {
        const isAuthenticated = await protectPage(true);
        if (!isAuthenticated) {
            // Redirect already happened, stop execution
            return;
        }
        
        // User is authenticated, you can load user-specific data here
        console.log('User is authenticated');
    }
});

// Export for use in other scripts
window.authChecker = {
    checkAuthentication,
    protectPage,
    getJWTToken
};

