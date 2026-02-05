/* src/main/webapp/resources/js/app-config.js */
(function() {
    const config = window.APP_CONFIG || {};
    const contextMeta = document.querySelector('meta[name="app-context-path"]');
    const csrfNameMeta = document.querySelector('meta[name="csrf-name"]');
    const csrfTokenMeta = document.querySelector('meta[name="csrf-token"]');

    if (contextMeta) {
        config.contextPath = contextMeta.content || "";
    }
    if (csrfNameMeta) {
        config.csrfName = csrfNameMeta.content || "";
    }
    if (csrfTokenMeta) {
        config.csrfToken = csrfTokenMeta.content || "";
    }

    window.APP_CONFIG = config;
})();
