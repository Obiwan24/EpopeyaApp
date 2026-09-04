/**
 * api.js
 * Wrapper central de fetch() para consumir la API REST del backend.
 * Añade automáticamente el token JWT guardado tras el login y gestiona
 * errores comunes (401 -> redirige a login).
 */

const API_BASE = "/api";

function getToken() {
    return localStorage.getItem("token");
}

function setToken(token) {
    localStorage.setItem("token", token);
}

function clearSesion() {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
}

function getUsuarioActual() {
    const raw = localStorage.getItem("usuario");
    return raw ? JSON.parse(raw) : null;
}

function setUsuarioActual(usuario) {
    localStorage.setItem("usuario", JSON.stringify(usuario));
}

/**
 * Llamada genérica autenticada a la API.
 * @param {string} path - ruta relativa, ej: "/jornadas"
 * @param {object} options - { method, body }
 */
async function apiFetch(path, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    const token = getToken();
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${path}`, {
        method: options.method || "GET",
        headers,
        body: options.body ? JSON.stringify(options.body) : undefined
    });

    if (response.status === 401) {
        clearSesion();
        window.location.href = "/index.html";
        return null;
    }

    let data = null;
    try {
        data = await response.json();
    } catch (e) {
        // respuesta sin cuerpo (ej. 204 No Content)
    }

    if (!response.ok) {
        const mensaje = (data && data.mensaje) ? data.mensaje : "Error inesperado en el servidor";
        throw new Error(mensaje);
    }

    return data;
}

function irA(pagina) {
    window.location.href = `/pages/${pagina}`;
}

function exigirSesion() {
    if (!getToken()) {
        window.location.href = "/index.html";
    }
}

function calcularEdad(fechaNacimientoStr) {
    if (!fechaNacimientoStr) return "-";
    const hoy = new Date();
    const nacimiento = new Date(fechaNacimientoStr);
    let edad = hoy.getFullYear() - nacimiento.getFullYear();
    const m = hoy.getMonth() - nacimiento.getMonth();
    if (m < 0 || (m === 0 && hoy.getDate() < nacimiento.getDate())) {
        edad--;
    }
    return edad;
}
