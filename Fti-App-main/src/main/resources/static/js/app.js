/**
 * app.js – Centralizimi i Auth per FTI App
 *
 * Rrjedha:
 *   Login → token ne localStorage → çdo faqe e tjeter e ngarkone app.js
 *   → app.js mer token nga localStorage → e dergon si 'Authorization: Bearer <token>'
 *   ne çdo kerkese HTTP (fetch + XMLHttpRequest/jQuery)
 */

// ── Funksion ndihmës global për headers ──────────────────────────────────────
window.getAuthHeaders = function () {
    var token = localStorage.getItem('jwtToken');
    return token ? { 'Authorization': 'Bearer ' + token } : {};
};

// ── authFetch – zevendeson fetch() me header automatik ──────────────────────
window.authFetch = function (url, options) {
    return window.fetch(url, options);
};

// ── Route & Role Guard ──────────────────────────────────────────────────────
(function () {
    var path = window.location.pathname.toLowerCase();
    var isPublic = ['/', '/login', '/register', '/verify-code'].indexOf(path) !== -1 ||
        path.startsWith('/register') ||
        path.startsWith('/verify') ||
        path.startsWith('/css') ||
        path.startsWith('/js') ||
        path.startsWith('/images') ||
        path.startsWith('/favicon');

    if (isPublic) return;

    var token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.replace('/login');
        return;
    }

    function parseJwt(t) {
        try {
            return JSON.parse(atob(t.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
        } catch (_) { return null; }
    }

    var payload = parseJwt(token);
    var rawRole = (payload && payload.role) ? payload.role : localStorage.getItem('userRole');
    var role = String(rawRole || '').toUpperCase().replace('ROLE_', '');
    if (role === 'TEACHER' || role === 'PETAGOG' || role === 'LEKTOR') role = 'PROFESSOR';

    var adminPaths = ['/administrator', '/dashboard_reports', '/course_schedule', '/adm', '/std', '/lende', '/tc'];
    var profPaths = ['/petagog', '/rregjistri', '/lektor'];
    var studentPaths = ['/student', '/nota', '/mungesat', '/sidebar', '/orari'];

    var isAdminPath = adminPaths.some(function (ap) { return path === ap || path.startsWith(ap + '/'); });
    var isProfPath = profPaths.some(function (pp) { return path === pp || path.startsWith(pp + '/'); });
    var isStudentPath = studentPaths.some(function (sp) { return path === sp || path.startsWith(sp + '/'); });

    if (isAdminPath && role !== 'ADMIN') {
        window.location.replace(role === 'PROFESSOR' ? '/rregjistri' : (role === 'STUDENT' ? '/student' : '/login'));
    } else if (isProfPath && role !== 'PROFESSOR') {
        window.location.replace(role === 'ADMIN' ? '/dashboard_reports' : (role === 'STUDENT' ? '/student' : '/login'));
    } else if (isStudentPath && role !== 'STUDENT') {
        window.location.replace(role === 'ADMIN' ? '/dashboard_reports' : (role === 'PROFESSOR' ? '/rregjistri' : '/login'));
    }
})();

// ── Logout handler ──────────────────────────────────────────────────────────
function handleLogout(e) {
    if (e && e.preventDefault) {
        e.preventDefault();
        e.stopPropagation();
    }
    try {
        localStorage.clear();
        sessionStorage.clear();
    } catch (_) {}
    window.location.replace('/login');
}
window.logout = window.logout || handleLogout;

document.addEventListener('click', function (e) {
    var btn = e.target.closest('.btn-logout, [data-logout], a[href="/logout"]');
    if (btn) {
        handleLogout(e);
    }
});

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.btn-logout, [data-logout], a[href="/logout"]').forEach(function (el) {
        el.addEventListener('click', handleLogout);
    });
});

console.log('[app.js] ngarkuar – token:', localStorage.getItem('jwtToken') ? 'PRESENT' : 'MISSING');
