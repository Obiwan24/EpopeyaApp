/**
 * auth.js - Lógica de la pantalla de login e index.html
 *
 * BUG CORREGIDO: los ids no coincidían con los de index.html
 *
 * El archivo original buscaba elementos con ids como "form-login", "login-username", "error-login",
 * "modal-recuperar", "cerrar-modal"... pero el index.html real usa otros ids distintos:
 * "loginForm", "usuario", "password", "recoverModal", etc. Como document.getElementById(...)
 * con un id que no existe devuelve null, la primera linea intentaba usar ese elemento (formLogin.addEventListener(...))
 * lanzaba: "cannot read properties of null (reading 'addEventListener')"
 * Ese error detiene todo el resto del script, asi que ningun listener se llegaba a registrar: el formulario
 * se enviaba de la forma clasica de HTML (recargaba la pagina vaciando campos) en vez
 * de llamar a la API
 *
 * Además, el modal de "Recuperar contraseña" ya se abre y se cierra solo, de forma nativa, gracias
 * a los atributos data-bs-toggle/data.bs.dismiss de Bootstrap - no hace falta ningún
 * addEventListener de JS para eso
 *
 * También se añaden comprobaciones "if (elemento)" antes de usar los elementos de mensaje de error:
 * así, aunque en el futuro se cambie el HTML y se olvide algun id, el script no se rompe entero,
 * simplemente no muestra ese mensaje concreto (usa alert() como red de seguridad)
 */

document.addEventListener("DOMContentLoaded", () => {
    // Si ya hay sesión activa, saltamos directo a la pantalla principal
    if (getToken()) {
        window.location.href = "/pages/principal.html";
        return;
    }

    //FIX: ids reales de index.html
    const formLogin = document.getElementById("loginForm");
    const errorLogin = document.getElementById("error-login");
    // FIX: el modal se abre solo via Bootstrap (data-bs-toggle en el HTML)
    // aqui solo se necesita para poder cerrarlo por JS tas un envío correcto
    const elModalRecuperar = document.getElementById("recoverModal");
    let modalRecuperarInstance = null;
    if (elModalRecuperar) {
        modalRecuperarInstance = new bootstrap.Modal(elModalRecuperar);
    }
    // FIX: ids reales
    const formRecuperar = document.getElementById("recoverForm");
    const errorRecuperar = document.getElementById("error-recuperar");

    formLogin.addEventListener("submit", async (e) => {
        e.preventDefault();
        if (errorLogin) errorLogin.textContent = "";
        const username = document.getElementById("usuario").value.trim();
        const password = document.getElementById("password").value;

        try {
            const data = await apiFetch("/auth/login", {method: "POST",
            body: {username, password}});
            setToken(data.token);
            setUsuarioActual(data.usuario);
            if (data.usuario.cambioPasswordPendiente) {
                irA("perfil.html?cambioObligatorio=true");
            }else{
                irA("principal.html");
            }
        } catch (err) {
            if (errorLogin) {
                errorLogin.textContent = "Usuario o contraseña incorrectos"
            } else {
                alert("Usuario o contraseña incorrectos");
            }
        }
    });

    if (formRecuperar) {
        formRecuperar.addEventListener("submit", async (e) => {
            e.preventDefault();
            if (errorRecuperar) errorRecuperar.textContent = "";

            const username = document.getElementById("recoverUser").value.trim();
            const email = document.getElementById("recoverEma").value.trim();

            try {
                await apiFetch("/auth/solicitar-reset", {method: "POST", body: {username, email}
                });

                if (errorRecuperar) {
                    errorRecuperar.style.color = "green";
                    errorRecuperar.textContent = "S los datos son correctos, recibirá un email con el enlace.";
                }
                setTimeout(() => {
                    if (modalRecuperarInstance) modalRecuperarInstance.hide();
                }, 2500);
            } catch (err) {
                if (errorRecuperar) {
                    errorRecuperar.style.color = "#b02a37";
                    errorRecuperar.textContent = err.message;
                } else {
                    alert(err.message);
                }
            }
        });
    }
});
