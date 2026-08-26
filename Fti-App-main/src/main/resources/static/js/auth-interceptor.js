/**
 * auth-interceptor.js
 * Vendos automatikisht 'Authorization: Bearer <token>' ne çdo fetch() dhe XMLHttpRequest.
 * Mbrojtje e route-ve private: ridrejton te /login nese nuk ka token.
 */
(function () {
    var PUBLIC_PATHS = ['/', '/login', '/register', '/verify-code'];

    function isPublic(path) {
        var p = path.toLowerCase();
        for (var i = 0; i < PUBLIC_PATHS.length; i++) {
            if (p === PUBLIC_PATHS[i]) return true;
        }
        return p.startsWith('/register') ||
            p.startsWith('/verify') ||
            p.startsWith('/css') ||
            p.startsWith('/js') ||
            p.startsWith('/images') ||
            p.startsWith('/favicon');
    }

    function getToken() {
        return localStorage.getItem('jwtToken');
    }

    function parseJwtPayload(token) {
        try {
            if (!token) return null;
            var parts = token.split('.');
            if (parts.length < 2) return null;
            return JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
        } catch (e) {
            return null;
        }
    }

    function isTokenExpired(token) {
        var payload = parseJwtPayload(token);
        if (!payload || !payload.exp) return false;
        return (payload.exp * 1000) < Date.now();
    }

    function normalizeRole(role) {
        if (!role) return '';
        var r = String(role).toUpperCase().replace('ROLE_', '');
        if (r === 'TEACHER' || r === 'PETAGOG' || r === 'LEKTOR') return 'PROFESSOR';
        return r;
    }

    function getUserRole() {
        var token = getToken();
        var payload = parseJwtPayload(token);
        if (payload && payload.role) {
            return normalizeRole(payload.role);
        }
        return normalizeRole(localStorage.getItem('userRole'));
    }

    function getHomeForRole(role) {
        if (role === 'ADMIN') return '/dashboard_reports';
        if (role === 'PROFESSOR') return '/rregjistri';
        if (role === 'STUDENT') return '/student';
        return '/login';
    }

    function isPathAllowedForRole(path, role) {
        var p = path.toLowerCase();

        var adminPaths = ['/administrator', '/dashboard_reports', '/course_schedule', '/adm', '/std', '/lende', '/tc'];
        var profPaths = ['/petagog', '/rregjistri', '/lektor'];
        var studentPaths = ['/student', '/nota', '/mungesat', '/sidebar', '/orari'];

        var isAdminPath = adminPaths.some(function (ap) { return p === ap || p.startsWith(ap + '/'); });
        var isProfPath = profPaths.some(function (pp) { return p === pp || p.startsWith(pp + '/'); });
        var isStudentPath = studentPaths.some(function (sp) { return p === sp || p.startsWith(sp + '/'); });

        if (isAdminPath) return role === 'ADMIN';
        if (isProfPath) return role === 'PROFESSOR';
        if (isStudentPath) return role === 'STUDENT';

        return true;
    }

    var currentPath = window.location.pathname;

    // ── 1. Route & Role Guard ──────────────────────────────────────────────────
    var token = getToken();

    if (token && isTokenExpired(token)) {
        console.warn('[auth-interceptor] Token ka skaduar – pastrim...');
        localStorage.clear();
        sessionStorage.clear();
        token = null;
    }

    if (!isPublic(currentPath)) {
        if (!token) {
            console.warn('[auth-interceptor] Pa token ne rruge private – ridrejtim te /login');
            window.location.replace('/login');
            return; // ndalo ekzekutimin
        }

        var userRole = getUserRole();
        if (!isPathAllowedForRole(currentPath, userRole)) {
            console.warn('[auth-interceptor] Roli (' + userRole + ') nuk ka akses te (' + currentPath + ') – ridrejtim te ' + getHomeForRole(userRole));
            window.location.replace(getHomeForRole(userRole));
            return; // ndalo ekzekutimin
        }
    }

    // ── 2. fetch() interceptor ──────────────────────────────────────────────────
    if (!window.__authFetchInstalled) {
        window.__authFetchInstalled = true;
        var _origFetch = window.fetch;

        window.fetch = function (input, init) {
            var token = getToken();

            // Nese eshte thirrur me Request object
            if (typeof Request !== 'undefined' && input instanceof Request) {
                if (token && !input.headers.has('Authorization')) {
                    input.headers.set('Authorization', 'Bearer ' + token);
                }
                return _origFetch.apply(window, arguments).then(handleResponseStatus);
            }

            // Nese eshte URL string ose URL object
            init = init ? Object.assign({}, init) : {};

            if (token) {
                if (typeof Headers !== 'undefined' && init.headers instanceof Headers) {
                    if (!init.headers.has('Authorization')) {
                        init.headers.set('Authorization', 'Bearer ' + token);
                    }
                } else if (Array.isArray(init.headers)) {
                    var found = false;
                    for (var i = 0; i < init.headers.length; i++) {
                        if (init.headers[i][0].toLowerCase() === 'authorization') {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        init.headers.push(['Authorization', 'Bearer ' + token]);
                    }
                } else {
                    var h = Object.assign({}, init.headers || {});
                    if (!h['Authorization'] && !h['authorization']) {
                        h['Authorization'] = 'Bearer ' + token;
                    }
                    init.headers = h;
                }
            }

            return _origFetch.call(window, input, init).then(handleResponseStatus);
        };

        function handleResponseStatus(res) {
            if (res && res.status === 401 && !isPublic(window.location.pathname)) {
                console.warn('[auth-interceptor] 401 Unauthorized – ridrejtim te /login...');
                try {
                    localStorage.clear();
                    sessionStorage.clear();
                } catch (_) { }
                window.location.replace('/login');
            }
            return res;
        }
    }

    // ── 3. XMLHttpRequest interceptor (per $.ajax / jQuery) ────────────────────
    if (!window.__authXHRInstalled) {
        window.__authXHRInstalled = true;
        var _XHROpen = XMLHttpRequest.prototype.open;
        var _XHRSend = XMLHttpRequest.prototype.send;

        XMLHttpRequest.prototype.open = function (method, url) {
            this._url = url;
            return _XHROpen.apply(this, arguments);
        };

        XMLHttpRequest.prototype.send = function () {
            var token = getToken();
            if (token) {
                try {
                    this.setRequestHeader('Authorization', 'Bearer ' + token);
                } catch (e) { }
            }
            return _XHRSend.apply(this, arguments);
        };
    }

    // ── 4. Logout – Pastrim i plote dhe ridrejtim i sigurt ─────────────────────
    function performLogout(e) {
        if (e && e.preventDefault) {
            e.preventDefault();
            e.stopPropagation();
        }
        try {
            localStorage.clear();
            sessionStorage.clear();
        } catch (err) {
            console.error('Gabim gjate pastrimit te storage:', err);
        }
        window.location.replace('/login');
    }

    // Eksporto logout globalisht qe te mund te thirret nga cdo vend (onclick="logout()", etj.)
    window.logout = performLogout;

    // Kap cdo klikim ne butona/linke logout ne te gjitha faqet (event delegation)
    document.addEventListener('click', function (e) {
        var btn = e.target.closest('.btn-logout, [data-logout], a[href="/logout"]');
        if (btn) {
            performLogout(e);
        }
    });

    document.addEventListener('DOMContentLoaded', function () {
        var logoutLinks = document.querySelectorAll('.btn-logout, [data-logout], a[href="/logout"]');
        logoutLinks.forEach(function (el) {
            el.addEventListener('click', performLogout);
        });
    });

    console.log('[auth-interceptor] aktiv – token:', getToken() ? 'OK' : 'MUNGON');
})();
