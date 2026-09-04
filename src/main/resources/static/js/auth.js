/**
 * auth.js - Lógica de la pantalla de login e index.html
 */

document.addEventListener("DOMContentLoaded", () => {
    // Si ya hay sesión activa, saltamos directo a la pantalla principal
    if (getToken()) {
        window.location.href = "/pages/principal.html";
        return;
    }

    const formLogin = document.getElementById("form-login");
    const errorLogin = document.getElementById("error-login");
    const linkRecuperar = document.getElementById("link-recuperar");
    const modalRecuperar = document.getElementById("modal-recuperar");
    const formRecuperar = document.getElementById("form-recuperar");
    const errorRecuperar = document.getElementById("error-recuperar");
    const cerrarModal = document.getElementById("cerrar-modal");

    formLogin.addEventListener("submit", async (e) => {
        e.preventDefault();
        errorLogin.textContent = "";

        const username = document.getElementById("login-username").value.trim();
        const password = document.getElementById("login-password").value;

        try {
            const data = await apiFetch("/auth/login", {
                method: "POST",
                body: { username, password }
            });

            setToken(data.token);
            setUsuarioActual(data.usuario);

            if (data.usuario.cambioPasswordPendiente) {
                irA("perfil.html?cambioObligatorio=true");
            } else {
                irA("principal.html");
            }
        } catch (err) {
            errorLogin.textContent = "Usuario o contraseña incorrectos.";
        }
    });

    linkRecuperar.addEventListener("click", (e) => {
        e.preventDefault();
        modalRecuperar.classList.remove("oculto");
    });

    cerrarModal.addEventListener("click", () => {
        modalRecuperar.classList.add("oculto");
    });

    formRecuperar.addEventListener("submit", async (e) => {
        e.preventDefault();
        errorRecuperar.textContent = "";

        const username = document.getElementById("recuperar-username").value.trim();
        const email = document.getElementById("recuperar-email").value.trim();

        try {
            await apiFetch("/auth/solicitar-reset", {
                method: "POST",
                body: { username, email }
            });
            errorRecuperar.style.color = "var(--color-exito)";
            errorRecuperar.textContent = "Si los datos son correctos, recibirás un email con el enlace.";
        } catch (err) {
            errorRecuperar.style.color = "var(--color-aviso)";
            errorRecuperar.textContent = err.message;
        }
    });
});
