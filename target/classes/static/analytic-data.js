// static/analytic-data.js

async function initDashboard() {
    try {
        // Use the global apiFetch from api.js if included, or relative path
        const r = await apiFetch('/tnp/applications/summary');
        if (!r.ok) {
            console.error("Failed to load analytics data", r.error);
            return;
        }

        const data = r.data;

        // Update stats
        document.getElementById('totalStudents').textContent = data.totalStudents;
        document.getElementById('totalPlaced').textContent = data.totalPlaced;
        document.getElementById('totalInternships').textContent = data.totalInternships;
        document.getElementById('placementRate').textContent = data.placementRate.toFixed(1) + '%';

        renderTrendsChart(data);
        renderTopCompaniesChart(data);
        renderSummaryTable(data);

    } catch (e) {
        console.error("Error initializing dashboard", e);
    }
}

function renderTrendsChart(data) {
    const ctx = document.getElementById('trendsChart').getContext('2d');
    
    const labels = data.placementTrends.map(t => t.year);
    const placementData = data.placementTrends.map(t => t.value);
    const internshipData = data.internshipTrends.map(t => t.value);

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Placements',
                    data: placementData,
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.1)',
                    tension: 0.4,
                    fill: true
                },
                {
                    label: 'Internships',
                    data: internshipData,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    tension: 0.4,
                    fill: true
                }
            ]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'top' }
            },
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
}

function renderTopCompaniesChart(data) {
    const ctx = document.getElementById('companiesChart').getContext('2d');
    
    const labels = data.topCompanies.map(c => c.companyName);
    const counts = data.topCompanies.map(c => c.placedCount);

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Students Placed',
                data: counts,
                backgroundColor: '#3b82f6'
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            plugins: {
                legend: { display: false }
            }
        }
    });
}

function renderSummaryTable(data) {
    const tbody = document.getElementById('summaryBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    data.topCompanies.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${c.companyName}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${c.placedCount}</td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">Active</span>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

document.addEventListener('DOMContentLoaded', initDashboard);