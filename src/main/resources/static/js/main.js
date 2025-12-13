// transFINESy Main JavaScript

// Loading Animation - Only show if page load is slow
(function() {
    const loadingOverlay = document.getElementById('loadingOverlay') || document.querySelector('.loading-overlay');
    let loadingTimeout;
    
    // Only show loading if page takes more than 300ms
    loadingTimeout = setTimeout(() => {
        if (loadingOverlay) {
            loadingOverlay.style.display = 'flex';
        }
    }, 300);
    
    // Hide loading as soon as DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', hideLoading);
    } else {
        hideLoading();
    }
    
    // Also hide on window load (for images, etc.)
    window.addEventListener('load', hideLoading);
    
    function hideLoading() {
        clearTimeout(loadingTimeout);
        if (loadingOverlay) {
            loadingOverlay.style.display = 'none';
            loadingOverlay.classList.add('hidden');
        }
    }
})();

// Initialize components
document.addEventListener('DOMContentLoaded', function() {
    initComponents();
});

function initComponents() {
    // Auto-focus RFID input
    const rfidInput = document.querySelector('#rfidInput');
    if (rfidInput) {
        rfidInput.focus();
        
        // Auto-submit on value change (for RFID scanners)
        let lastValue = '';
        setInterval(() => {
            if (rfidInput.value !== lastValue && rfidInput.value.length > 0) {
                lastValue = rfidInput.value;
                setTimeout(() => {
                    if (typeof scanRFID === 'function') {
                        scanRFID();
                    }
                }, 100);
            }
        }, 200);
    }
    
    // Form validation
    const forms = document.querySelectorAll('form[data-validate]');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });
    
    // Smooth scroll
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });
}

// Utility function to show toast notifications
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `alert alert-${type}`;
    toast.textContent = message;
    toast.style.position = 'fixed';
    toast.style.top = '20px';
    toast.style.right = '20px';
    toast.style.zIndex = '10000';
    toast.style.minWidth = '300px';
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Format currency
function formatCurrency(amount) {
    return '₱' + parseFloat(amount).toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

