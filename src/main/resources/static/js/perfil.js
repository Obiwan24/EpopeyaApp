/**
 * perfil.js - Ver y editar mis propios datos personales.
 * Si viene con ?cambioObligatorio=true, avisa de que debe cambiar la contraseña por defecto.
 */

document.addEventListener("DOMContentLoaded", async () => {
    exigirSesion();

    const params = new URLSearchParams(window.location.search);
    if (params.get("cambioObligatorio") === "true") {
        alert("Por seguridad, debes cambiar tu contraseña por defecto antes de continuar.");
    }

    const formPerfil = document.getElementById("form-perfil");
    const errorPerfil = document.getElementById("error-perfil");
    const okPerfil = document.getElementById("ok-perfil");

    async function cargarDatos() {
        try {
            const datos = await apiFetch("/usuarios/me");
            document.getElementById("perfil-nombre").value = datos.nombre;
            document.getElementById("perfil-apellidos").value = datos.apellidos;
            document.getElementById("perfil-dni").value = datos.dni;
            document.getElementById("perfil-telefono").value = datos.telefono || "";
            document.getElementById("perfil-email").value = datos.email || "";
            document.getElementById("perfil-fecha-nacimiento").value = datos.fechaNacimiento || "";
            document.getElementById("perfil-posicion").value = datos.posicion || "DRIVE";
            setUsuarioActual(datos);
        } catch (err) {
            errorPerfil.textContent = err.message;
        }
    }

    formPerfil.addEventListener("submit", async (e) => {
        e.preventDefault();
        errorPerfil.textContent = "";
        okPerfil.style.display = "none";

        const payload = {
            dni: document.getElementById("perfil-dni").value.trim(),
            telefono: document.getElementById("perfil-telefono").value.trim(),
            email: document.getElementById("perfil-email").value.trim(),
            fechaNacimiento: document.getElementById("perfil-fecha-nacimiento").value,
            posicion: document.getElementById("perfil-posicion").value
        };

        try {
            await apiFetch("/usuarios/me", { method: "PUT", body: payload });
            okPerfil.style.display = "block";
        } catch (err) {
            errorPerfil.textContent = err.message;
        }
    });

    const formPassword = document.getElementById("form-password");
    const errorPassword = document.getElementById("error-password");

    formPassword.addEventListener("submit", async (e) => {
        e.preventDefault();
        errorPassword.textContent = "";

        const actual = document.getElementById("password-actual").value;
        const nueva = document.getElementById("password-nueva").value;
        const nueva2 = document.getElementById("password-nueva-2").value;

        if (nueva !== nueva2) {
            errorPassword.textContent = "Las contraseñas nuevas no coinciden.";
            return;
        }

        try {
            await apiFetch("/usuarios/me/password", {
                method: "PUT",
                body: { passwordActual: actual, passwordNueva: nueva }
            });
            alert("Contraseña actualizada correctamente.");
            formPassword.reset();
        } catch (err) {
            errorPassword.textContent = err.message;
        }
    });

    cargarDatos();
});
