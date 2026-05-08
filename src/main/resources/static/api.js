// static/api.js

function getApiBaseUrl() {
    const { protocol, hostname, port, origin } = window.location;
    const localHost = hostname === "localhost" || hostname === "127.0.0.1";
    const servedByBackend = localHost && port === "9090";

    if (servedByBackend) return origin;
    if (protocol === "file:") return "http://localhost:9090";
    if (localHost) return "http://localhost:9090";
    return origin;
}

const BASE_URL = getApiBaseUrl();

/**
 * Universal fetch wrapper that handles:
 * 1. Base URL prepending for relative paths
 * 2. Automatic JWT attachment (checks both token and tnp_admin_token)
 * 3. JSON body stringifying
 * 4. Error parsing
 */
async function apiFetch(url, options = {}) {
    const finalUrl = url.startsWith("http") ? url : `${BASE_URL}${url.startsWith("/") ? "" : "/"}${url}`;
    
    // Determine which token to use based on the path
    const isAdminPath = url.startsWith("/api/admin/") || url.startsWith("/api/company/") || url.startsWith("/tnp/");
    const token = isAdminPath ? localStorage.getItem('tnp_admin_token') : localStorage.getItem('token');
    
    const headers = options.headers || {};
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    if (options.body && !(options.body instanceof FormData) && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
        if (typeof options.body === 'object') {
            options.body = JSON.stringify(options.body);
        }
    }

    try {
        const res = await fetch(finalUrl, { ...options, headers });
        let data = null;
        
        if (!res.ok) {
            let errorMsg = res.statusText;
            try {
                const text = await res.text();
                try {
                    const json = JSON.parse(text);
                    errorMsg = json.message || json.error || text || errorMsg;
                } catch(e) {
                    errorMsg = text || errorMsg;
                }
            } catch(e) {}
            return { ok: false, error: errorMsg, status: res.status, data: null };
        }

        const text = await res.text();
        try {
            data = text ? JSON.parse(text) : null;
        } catch (e) {
            data = text;
        }

        return { 
            ok: res.ok, 
            status: res.status, 
            data, 
            error: null 
        };
    } catch (err) {
        console.error(`Fetch error for ${finalUrl}:`, err);
        return { ok: false, status: 0, data: null, error: "Network error: Failed to connect to server" };
    }
}

// ---- HELPERS ----

async function loginUser(data) {
    return apiFetch('/auth/login', {
        method: "POST",
        body: data
    });
}

async function getStudentProfile(id) {
    return apiFetch(`/student/dashboard/${id}`);
}

async function updateStudentProfile(id, data) {
    return apiFetch(`/student/update/${id}`, {
        method: "PUT",
        body: data
    });
}

async function uploadStudentFile(endpoint, id, file) {
    let formData = new FormData();
    formData.append("file", file);
    return apiFetch(`/student/${endpoint}/${id}`, {
        method: "POST",
        body: formData
    });
}

async function getCompanies() {
    const r = await apiFetch('/api/company/all');
    return r.ok ? (Array.isArray(r.data) ? r.data : []) : [];
}

async function addPlacementDrive(companyData) {
    return apiFetch('/api/company/add', {
        method: 'POST',
        body: companyData
    });
}

async function updatePlacementDrive(id, companyData) {
    return apiFetch(`/api/company/update/${id}`, {
        method: 'PUT',
        body: companyData
    });
}

async function deletePlacementDrive(id) {
    return apiFetch(`/api/company/delete/${id}`, {
        method: 'DELETE'
    });
}

async function getApplicationsSummary() {
    return apiFetch('/tnp/applications/summary');
}
